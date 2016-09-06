package com.pacoapp.paco.sensors.android;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.sensors.android.procmon.EncounteredPermissionRequest;
import com.pacoapp.paco.sensors.android.procmon.RuntimePermissionsAppUtil;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.util.TimeUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles monitoring of runtime permission changes, by implementing an accessibility
 * service. Whenever a runtime dialog box pops up, or a permission change is made in the settings,
 * it fires an intent to the BroadcastTriggerService as any other trigger would.
 * An alternative would be to regularly poll Android's PackageManager to detect any permission
 * changes, but this would not allow us to see whether a user 'reaffirmed' a permission e.g. by
 * denying it when it was first requested.
 * This class assumes an en_us locale and won't work on other languages. It needs to be extended
 * if we want it to be used in international studies.
 * This class should only be used on devices running Android 6.0 and up, since older versions don't
 * support runtime permissions.
 * This service is enabled when Paco is granted the Accessibility permission. The
 * BroadcastTriggerService will make sure that only experiments enabling accessibility logging will
 * receive accessibility events. Furthermore, when this service is killed (i.e., because the user
 * switched off Paco's accessibility permission), the onDestroy() method will get called, which sets
 * running to false. The previously encountered permissions should not be persisted, as these are
 * used only to keep state within the runtime permission dialogs themselves. In other words, on boot
 * (or when the service is restarted by allowing accessibility permissions again), there is no
 * possibility of a runtime permissions dialog still being visible. When a new one pops up,
 * the queue of previouslyEncounteredPermissionRequests will get filled again.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
public class RuntimePermissionMonitorService extends AccessibilityService {
  public static final String PACO_ACTION_ACCESSIBILITY_PAYLOAD = "paco_action_accessibility_payload";
  public static final String PAYLOAD_PERMISSION = "paco_accessibility_payload_permission";
  public static final String PAYLOAD_PERMISSION_GRANTED = "paco_accessibility_payload_permissiongranted";
  public static final String PAYLOAD_PERMISSION_USERINITIATED = "paco_accessibility_payload_permissionuserinitiated";
  public static final String PAYLOAD_PERMISSION_PACKAGES = "paco_accessibility_payload_permissionpackages";
  public static final String PAYLOAD_PERMISSION_APPNAME = "paco_accessibility_payload_permissionappname";
  // Number of milliseconds before a permission request is considered stale
  private static final long PERMISSION_REQUEST_HISTORY_MILLIS = 60000;

  // List of the names of the PERMISSION_GROUPS as they appear in the en_us localization of the
  // PackageInstaller settings
  public static final List<String> PERMISSION_SETTINGS_STRINGS = Arrays.asList(
    "Camera",
    "Contacts",
    "Location",
    "Microphone",
    "Phone",
    "Storage",
    "Body Sensors",
    "Calendar",
    "SMS"
  );
  // List of the names of the PERMISSION_GROUPS as they appear in the en_us localization of the
  // runtime permission dialogs. These are mapped to the list of strings in the
  // PERMISSION_SETTINGS_STRINGS list
  public static final Map<String, String> PERMISSION_DIALOG_STRINGS = new HashMap<String,String>();;
  static {
    PERMISSION_DIALOG_STRINGS.put("take pictures and record video", "Camera");
    PERMISSION_DIALOG_STRINGS.put("access your contacts", "Contacts");
    PERMISSION_DIALOG_STRINGS.put("access this device's location", "Location");
    PERMISSION_DIALOG_STRINGS.put("record audio", "Microphone");
    PERMISSION_DIALOG_STRINGS.put("make and manage phone calls", "Phone");
    PERMISSION_DIALOG_STRINGS.put("access photos, media, and files on your device", "Storage");
    PERMISSION_DIALOG_STRINGS.put("access sensor data about your vital signs", "Body Sensors");
    PERMISSION_DIALOG_STRINGS.put("access your calendar", "Calendar");
    PERMISSION_DIALOG_STRINGS.put("send and view SMS messages", "SMS");
  }

  // Keeps whether the service is connected
  private static boolean running = false;
  /**
   * Used to keep track of which app we are changing settings for. Needed because
   * AccessibilityEvents will only show us what information is currently being interacted with
   */
  private static ArrayList<String> currentlyHandledAppPackageNames;
  private static String currentlyHandledAppName;
  private static String currentlyHandledPermission;

  /**
   * Only used with runtime permission dialogs. Keep the currently requested permission in memory
   * so we remember it when the user actually clicked allow/deny. This is a queue because
   * accessibility events might be interleaved (i.e., the next permission request may be triggering
   * an accessibility event even before the action of the user on the previous request triggers one)
   * The reason we are using a Deque is for being able to peek at the last element in the queue, to
   * make sure we're not adding any duplicates because we got two WINDOW_STATE_CHANGE events
   * announcing the same permission in quick succession.
   */
  private static Deque<EncounteredPermissionRequest> previouslyEncounteredPermissionRequests;

  /**
   * Called only for accessibility events coming from Android's packageinstaller.
   * {@inheritDoc}
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    // Assert that we're handling events only for the package installer
    CharSequence packageName = accessibilityEvent.getPackageName();
    if (!packageName.equals("com.google.android.packageinstaller") &&
            !packageName.equals("com.android.packageinstaller")) {
      Log.e(PacoConstants.TAG, "Not expecting to receive accessibility events for " + packageName + ". Ignoring.");
      return;
    }

    int eventType = accessibilityEvent.getEventType();
    switch (eventType) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
        // For our purposes, this means: a dialog requesting a runtime permission is shown,
        // or the user navigated to the 'App info' screen for a specific app
        Log.v(PacoConstants.TAG, "New accessibility event: window state changed (we are capturing this)");
        if (isAppPermissionsScreen(accessibilityEvent)) {
          // Find the package name in this view, and store it for future use
          Log.v(PacoConstants.TAG, "We seem to be inside the app permissions screen");
          extractInformationFromAppPermissionsScreen(accessibilityEvent.getSource());
        } else if (isPermissionAppListingScreen(accessibilityEvent)) {
          Log.v(PacoConstants.TAG, "We seem to be inside the screen showing apps for a permission");
          extractInformationFromAppListingForPermission(accessibilityEvent.getSource());
        } else if (isPermissionsDialog(accessibilityEvent.getSource())) {
          Log.v(PacoConstants.TAG, "We seem to be inside a runtime permissions dialog");
          extractInformationFromPermissionDialog(accessibilityEvent);
        } else if (isAppInfoScreen(accessibilityEvent.getSource())) {
          Log.e(PacoConstants.TAG, "We're not using tags from the app info screen anymore");
          // We can use the following call to get the package name from this screen on supported
          // platforms, but it doesn't seem to be available in stock android.
          // If you re-enable this, make sure to add com.android.settings to the packageNames in the
          // runtime_permissions_accessibility_config.xml document, and to allow this package in the
          // check at the start of this function
          // extractAppPackageNameFromAppInfoScreen(accessibilityEvent.getSource());
        } else {
          Log.v(PacoConstants.TAG, "Ignoring window state changed accessibility event, since it was not a permission settings screen or a permissions dialog.");
        }
        break;
      // We used to use the next case in a similar way to TYPE_ANNOUNCEMENT, but it seems that
      // TYPE_ANNOUNCEMENT captures all needed events in this case
      //case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
      case AccessibilityEvent.TYPE_ANNOUNCEMENT:
        // For our purposes, this means: a dialog requesting a runtime permission changed to request
        // the next required permission (e.g. during user onboarding, a few permissions are
        // requested in sequence)
        Log.v(PacoConstants.TAG, "This might be a changed permissions dialog, try to extract info");
        extractInformationFromEventText(accessibilityEvent.getText());
        break;
      case AccessibilityEvent.TYPE_VIEW_CLICKED:
        // For our purposes, this means: permission change via switch button (in settings),
        // or clicking 'allow/deny' in a runtime permission dialog
        if (isPermissionsDialogAction(accessibilityEvent.getSource())) {
          Log.v(PacoConstants.TAG, "Action taken in permissions dialog");
          processPermissionDialogAction(accessibilityEvent.getSource());
        } else if (isSettingsPermissionChange(accessibilityEvent.getSource())) {
          Log.v(PacoConstants.TAG, "Action taken in permission settings activity");
          processPermissionConfigurationChange(accessibilityEvent);
        } else {
          Log.v(PacoConstants.TAG, "Ignoring TYPE_VIEW_CLICKED, since it was not in a permission dialog or a settings screen.");
        }
        break;
    }
  }

  /**
   * Get the information from the latest permission request dialog that popped up. Makes sure that
   * other classes can peek at this element without allowing them to alter the queue.
   * @return An object containing that permission's name, app's name, and package
   */
  public static EncounteredPermissionRequest getLastEncounteredPermissionRequest() {
    return previouslyEncounteredPermissionRequests.peekLast();
  }

  /**
   * Returns whether the service is running and connected.
   * @return true if we have accessibility permissions and the service is connected
   */
  public static boolean isRunning() {return running;}

  /**
   * Checks if the system is showing a runtime permissions dialog, spawned by an app to ask for a
   * runtime permission.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user is in the permissions dialog
   */
  private boolean isPermissionsDialog(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo != null &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
  }

  /**
   * Checks if the user is in the settings menu, showing the info for a specific app. This is the
   * screen containing info on storage, data usage, etc. used by the app.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user is in the app info screen
   */
  private boolean isAppInfoScreen(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo != null &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/all_details").size() > 0 &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text2").size() > 0
    );
  }

  /**
   * Checks if the user is in the screen listing all apps that request a certain permission. This
   * activity shows switch buttons for every app the user can turn on/off the permission for.
   * @param event The accessibility event that occurred
   * @return true if the user is in the permission app listing screen
   */
  private boolean isPermissionAppListingScreen(AccessibilityEvent event) {
    return (event.getSource() != null &&
            event.getText().size() > 0 && event.getText().get(0).equals("App permissions") &&
            event.getSource().findAccessibilityNodeInfosByText("App permissions").size() == 0 &&
            event.getSource().findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/list").size() == 0
    );
  }

  /**
   * Checks if the user is in the permissions screen for an app. This activity shows switch buttons
   * for every permission the user can grant to the app.
   * @param event The accessibility event that occurred
   * @return true if the user is in the app permissions screen
   */
  private boolean isAppPermissionsScreen(AccessibilityEvent event) {
    return (event.getSource() != null &&
            (
                    event.getSource().findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name").size() > 0 ||
                    // This changed in Android N
                    event.getSource().findAccessibilityNodeInfosByViewId("android:id/title").size() > 0
            ) &&
            (
                    event.getSource().findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0 ||
                    // This changed in Android N
                    event.getSource().findAccessibilityNodeInfosByViewId("android:id/switch_widget").size() > 0
            )
    );
  }

  /**
   * Checks if the user pressed the 'Deny' or 'Allow' button in a permissions dialog.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user performed an action in the permissions dialog
   */
  private boolean isPermissionsDialogAction(AccessibilityNodeInfo nodeInfo) {
    if (nodeInfo.getText() == null) {
      return false;
    }
    // Lower case because depending on Android version, the string may be all capitals or just
    // capitalized
    String nodeTextLowercase = nodeInfo.getText().toString().toLowerCase();
    return (nodeInfo != null &&
            nodeInfo.getClassName().equals("android.widget.Button") &&
            (nodeTextLowercase.equals("deny") ||nodeTextLowercase.equals("allow")));
  }

  /**
   * Checks if the user pressed the switch on any of the permissions in the app permissions screen.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user changed a permission on the permissions screen
   */
  private boolean isSettingsPermissionChange(AccessibilityNodeInfo nodeInfo) {
    // This will most certainly be too broad, but we ignore this for now until we can get some
    // real experiment data
    return (nodeInfo != null &&
            (
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0 ||
                    // This changed in Android N
                    nodeInfo.findAccessibilityNodeInfosByViewId("android:id/switch_widget").size() > 0
            )
            &&
            nodeInfo.getClassName().equals("android.widget.LinearLayout"));
  }


  /**
   * Parses an the event text from an accessibility event in order to extract information pertaining
   * to runtime permissions. Currently only handles strings of the form
   * "Allow <app> to <permission>?".
   * @param eventText All event texts from an accessibility event, as returned by the getText()
   *                  method.
   */
  private void extractInformationFromEventText(List<CharSequence> eventText) {
    for (CharSequence eventSubText : eventText) {
      Pattern permissionRegex = Pattern.compile("Allow (.*) to (.*)\\?");
      Matcher permissionMatcher = permissionRegex.matcher(eventSubText);
      if (permissionMatcher.find()) {
        String permissionText = permissionMatcher.group(2);
        String permissionString = PERMISSION_DIALOG_STRINGS.get(permissionText);
        if (permissionString == null) {
          Log.w(PacoConstants.TAG, "Unknown permission string encountered: " + permissionText);
          permissionString = permissionText;
        }
        addEncounteredPermission(permissionString, permissionMatcher.group(1));
      } else {
        Log.v(PacoConstants.TAG, "Could not extract any information from string " + eventSubText);
      }
    }
  }

  /**
   * Extracts the name of the app package from the app info screen for that package, and stores it
   * in memory.
   * This method is not used anymore, as not every Android OEM skin displays this info. Instead,
   * we extract this information by reverse engineering the name from the app permissions screen
   * (see method extractAppPackageNamesFromAppPermissionsScreen())
   * @param rootNodeInfo The root node in the tree for the accessibility event
   */
  private void extractAppPackageNameFromAppInfoScreen(AccessibilityNodeInfo rootNodeInfo) {
    // "com.android.settings:id/widget_text2" is the id for the text string which contains the
    // package name on the "App info" screen. You'll find it right underneath the version number
    List<AccessibilityNodeInfo> matchingNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text2");
    for (AccessibilityNodeInfo nodeInfo : matchingNodeInfos) {
      if (nodeInfo.getText() != null) {
        setCurrentlyHandledAppPackageName(nodeInfo.getText());
        return;
      }
    }
  }

  /**
   * Extracts the permission string from a screen listing all apps that requested a certain
   * permission. The name of this permission is at the top of the screen.
   * @param rootNodeInfo The root node in the tree for the accessibility event
   */
  private void extractInformationFromAppListingForPermission(AccessibilityNodeInfo rootNodeInfo) {
    // The only way to get the information we need is by completely traversing the tree, since
    // the element containing the actual permission does not have an id
    // So we'll need to actually go look for every possible permission string
    // (the alternative is traversing the tree as it is in a specific version of the package
    // installer, but this might be even more subject to change)
    for (String permissionString : PERMISSION_SETTINGS_STRINGS) {
      List<AccessibilityNodeInfo> nodeInfos = rootNodeInfo.findAccessibilityNodeInfosByText(permissionString + " permissions");
      if (nodeInfos.size() > 0) {
        // We found it!
        setCurrentlyHandledPermission(permissionString);
        return;
      }
    }
    Log.w(PacoConstants.TAG, "We failed to extract the permission string from the settings screen");
  }

  /**
   * Extracts the name of the app package from the app permissions screen for that package, and
   * stores it in memory.
   * We extract this information by reverse engineering the name from the app permissions screen.
   * @param rootNodeInfo The root node in the tree for the accessibility event
   */
  private void extractInformationFromAppPermissionsScreen(AccessibilityNodeInfo rootNodeInfo) {
    // "com.android.packageinstaller:id/name" is the id for the text string which contains
    // the app *label* (in case Android is showing the permissions for the app)
    List<AccessibilityNodeInfo> matchingNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
    for (AccessibilityNodeInfo nodeInfo : matchingNodeInfos) {
      if (nodeInfo.getText() != null) {
        CharSequence appLabel = nodeInfo.getText();
        setCurrentlyHandledAppName(appLabel);
      }
    }
  }

  /**
   * Called when the user changes a runtime permission from the app permissions screen. Fires a
   * broadcast event making note of whether the user accepted or denied the permission.
   * This can happen in two situations: either we are in the screen showing all permissions for an
   * app, or we are in the screen showing all apps for a permission. We decide which one by checking
   * our hardcoded list of en_us permission strings to see if one matches.
   * @param accessibilityEvent accessibility event corresponding to the permission change
   */
  private void processPermissionConfigurationChange(AccessibilityEvent accessibilityEvent) {
    List<CharSequence> textFields = accessibilityEvent.getText();
    if (textFields.size() != 2) {
      Log.e(PacoConstants.TAG, "Unexpected length for text array on permission configuration change: " + textFields);
      return;
    }
    String switchName = textFields.get(0).toString();
    if (PERMISSION_SETTINGS_STRINGS.contains(switchName)) {
      setCurrentlyHandledPermission(switchName);
    } else {
      // Will also resolve package names
      setCurrentlyHandledAppName(switchName);
    }
    // Get the switch button, and check whether it is enabled
    AccessibilityNodeInfo source = accessibilityEvent.getSource();
    List<AccessibilityNodeInfo> switchButtons = source.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget");
    if (switchButtons.size() == 0) {
      switchButtons = source.findAccessibilityNodeInfosByViewId("android:id/switch_widget");
    }
    if (switchButtons.size() == 0) {
      Log.e(PacoConstants.TAG, "We couldn't find the switch button in the permissions activity!");
      return;
    }
    boolean isAllowed = switchButtons.get(0).isChecked();
    triggerBroadcastTriggerService(isAllowed, true);
  }

  /**
   * Adds a permission to the queue of permission requests that still need to be handled by the user
   * See {@link #previouslyEncounteredPermissionRequests} for more info.
   * The permission request will not be added if it is the same as the last request already in the
   * list.
   * @param permission The requested permission, as the string shown in the user interface
   * @param appName The application name (title), if available (null otherwise).
   * @return true if the permission was added, false if it was already at the end of the queue
   */
  private boolean addEncounteredPermission(CharSequence permission, CharSequence appName) {
    EncounteredPermissionRequest lastPermissionRequest = getLastEncounteredPermissionRequest();
    if (lastPermissionRequest != null &&
            lastPermissionRequest.getPermissionString().equals(permission) &&
            lastPermissionRequest.getAppName().equals(appName)) {
      Log.w(PacoConstants.TAG, "Not adding the same permisison request twice!");
      return false;
    }
    EncounteredPermissionRequest newPermissionRequest = new EncounteredPermissionRequest(permission,
            System.currentTimeMillis(), appName);
    previouslyEncounteredPermissionRequests.addLast(newPermissionRequest);
    Log.v(PacoConstants.TAG, "Added previously handled permission " + permission.toString());
    return true;
  }

  /**
   * Set the name of the package for which the user is currently changing permissions.
   * @param packageNames a list of possible package names. Not just one string, because an app name
   *                     may correspond to multiple package names
   */
  private void setCurrentlyHandledAppPackageNames(ArrayList<String> packageNames) {
    currentlyHandledAppPackageNames = packageNames;
    Log.v(PacoConstants.TAG, "Set 'currently handled package names' to " + currentlyHandledAppPackageNames.toString());
  }

  /**
   * Set the name of the package for which the user is currently changing permissions. Contrary to
   * setCurrentlyHandledAppPackageNames(), this method will also resolve the package name to the app
   * name.
   * @param packageName package name for the app
   */
  private void setCurrentlyHandledAppPackageName(CharSequence packageName) {
    ArrayList packageList = new ArrayList();
    packageList.add(packageName);
    setCurrentlyHandledAppPackageNames(packageList);
    // Resolve app name
    try {
      PackageManager packageManager = getPackageManager();
      ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName.toString(), 0);
      currentlyHandledAppName = packageManager.getApplicationLabel(appInfo).toString();
      Log.v(PacoConstants.TAG, "Also set app name to " + currentlyHandledAppName);
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(PacoConstants.TAG, "Could not find app info for package " + packageName);
    }
  }

  /**
   * Set the name of the app for which the user is currently changing permissions. Will also resolve
   * the possible packages this app belongs to.
   * @param appName The name of the app, as it is shown to the user
   */
  private void setCurrentlyHandledAppName(CharSequence appName) {
    currentlyHandledAppName = appName.toString();
    Log.v(PacoConstants.TAG, "Set 'currently handled app name' to " + currentlyHandledAppName);
    // Resolve the possible package names for this app
    AndroidInstalledApplications installedApps = new AndroidInstalledApplications(getApplicationContext());
    ArrayList<String> packageNames = installedApps.getPackageNameFromAppLabel(appName);
    if (packageNames.size() > 0) {
      setCurrentlyHandledAppPackageNames(packageNames);
      return;
    }
  }

  /**
   * Set the name of the permission that is currently being handled.
   * @param permission The name of the handled permission
   */
  private void setCurrentlyHandledPermission(String permission) {
    currentlyHandledPermission = permission;
    Log.v(PacoConstants.TAG, "Set 'currently handled permission' to " + currentlyHandledPermission);
  }

  /**
   * Called when the user accepts or denies a runtime permission request. Fires a broadcast event
   * making note of whether the user accepted or denied the permission.
   * @param nodeInfo The source of the accessibility event
   */
  private void processPermissionDialogAction(AccessibilityNodeInfo nodeInfo) {
    if (previouslyEncounteredPermissionRequests.size() < 1) {
      Log.w(PacoConstants.TAG, "We got a dialog action on a permission request, but we never saw" +
            "the permission request in the first place");
      return;
    }
    EncounteredPermissionRequest encounteredPermission = previouslyEncounteredPermissionRequests.pollFirst();
    // Drop stale permission requests older than PERMISSION_REQUEST_HISTORY_MILLIS, *unless* it was
    // the last one still in the queue
    while (encounteredPermission.getTimestamp() < System.currentTimeMillis() - PERMISSION_REQUEST_HISTORY_MILLIS &&
            previouslyEncounteredPermissionRequests.size() > 0) {
      Log.w(PacoConstants.TAG, "Not considering permission request " +
              encounteredPermission.getPermissionString() + " for app " +
              encounteredPermission.getAppName() + " because it was too old.");
      encounteredPermission = previouslyEncounteredPermissionRequests.pollFirst();
    }
    setCurrentlyHandledPermission(encounteredPermission.getPermissionString().toString());
    setCurrentlyHandledAppName(encounteredPermission.getAppName());
    // Lower case because depending on Android version, the string may be all capitals or just
    // capitalized
    String actionTextLower = nodeInfo.getText().toString().toLowerCase();
    if (actionTextLower.equals("allow")) {
      triggerBroadcastTriggerService(true, false);
    } else if (actionTextLower.equals("deny")) {
      triggerBroadcastTriggerService(false, false);
    } else {
      Log.e(PacoConstants.TAG, "Dialog action in runtime permissions dialog was not 'allow' nor 'deny'. This should never happen");
      Log.e(PacoConstants.TAG, "Dialog action in runtime permissions dialog was not 'allow' nor 'deny'. This should never happen");
    }
  }

  /**
   * Extracts information (app package name, requested permission) from the dialog requesting a
   * runtime permission, and stores it in memory.
   * @param accessibilityEvent accessibility event corresponding to the permission request
   */
  private void extractInformationFromPermissionDialog(AccessibilityEvent accessibilityEvent) {
    // The app for which the permission is requested will be the one which was last in the
    // foreground. Since background services are not able to call requestPermissions(), the last
    // visible activity should always belong to the requesting app.
    RuntimePermissionsAppUtil runtimeUtil = new RuntimePermissionsAppUtil(getApplicationContext());
    String previousAppPackage = runtimeUtil.getAppSpawningRuntimepermissionsDialog();
    if (previousAppPackage != null) {
      setCurrentlyHandledAppPackageName(previousAppPackage);
    } else {
      Log.d(PacoConstants.TAG, "Keeping previous app package at " + currentlyHandledAppPackageNames + " because it would be null otherwise.");
    }

    // Extract the requested permission from the text in the dialog.
    extractInformationFromEventText(accessibilityEvent.getText());
    String displayText = accessibilityEvent.getText().get(0).toString();
  }

  /**
   * Calls the BroadcastTriggerService with an intent containing all information of the permission
   * change.
   * @param isGranted Whether the permission was granted (true) or denied (false)
   * @param initiatedByUser Whether the user actively initiated the permission change. False if the
   *                        permission change was the result of a permission request by the system.
   */
  private void triggerBroadcastTriggerService(boolean isGranted, boolean initiatedByUser) {
    Context context = getApplicationContext();
    Log.d(PacoConstants.TAG, "Broadcasting permission change for " + currentlyHandledAppPackageNames + ": " + currentlyHandledPermission + " set to " + isGranted);

    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, InterruptCue.PERMISSION_CHANGED);
    Bundle accessibilityPayload = new Bundle();
    accessibilityPayload.putCharSequence(PAYLOAD_PERMISSION, currentlyHandledPermission);
    accessibilityPayload.putBoolean(PAYLOAD_PERMISSION_GRANTED, isGranted);
    accessibilityPayload.putBoolean(PAYLOAD_PERMISSION_USERINITIATED, initiatedByUser);
    accessibilityPayload.putStringArrayList(PAYLOAD_PERMISSION_PACKAGES, currentlyHandledAppPackageNames);
    accessibilityPayload.putString(PAYLOAD_PERMISSION_APPNAME, currentlyHandledAppName);
    // Cue event names are off by one.
    accessibilityPayload.putString(BroadcastTriggerReceiver.TRIGGER_TYPE, InterruptCue.CUE_EVENT_NAMES[InterruptCue.PERMISSION_CHANGED-1]);
    broadcastTriggerServiceIntent.putExtra(PACO_ACTION_ACCESSIBILITY_PAYLOAD, accessibilityPayload);
    context.startService(broadcastTriggerServiceIntent);
  }

  /**
   * Called by the Android system when it connects the accessibility service. We use this to keep
   * track of whether we have the accessibility permission.
   */
  @Override
  protected void onServiceConnected() {
    previouslyEncounteredPermissionRequests = new LinkedBlockingDeque();
    running = true;
    Log.d(PacoConstants.TAG, "Connected to the accessibility service");
    if (!Locale.getDefault().getISO3Language().equals(Locale.ENGLISH.getISO3Language())) {
      // We don't really need to signal this to the user, as it is the experiment provider who
      // is responsible for checking this should not be a problem for the experiment.
      Log.w(PacoConstants.TAG, "Detected locale is " + Locale.getDefault().toString() +
              ". RuntimePermissions triggering does not support non-English languages; " +
              "permissions might not always be interpreted correctly");
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // TODO: update to Marshmallow when project SDK changes
      Log.e(PacoConstants.TAG, "RuntimePermissions triggering should not be used on pre-Marshmallow devices. Stopping service.");
      stopSelf();
    }
  }

  /**
   * Called by the Android system when the accessibility service is stopped (e.g. because the user
   * disables accessibility permissions for the app)
   */
  @Override
  public void onDestroy() {
    Log.d(PacoConstants.TAG, "Accessibility service destroyed");
    running = false;
  }

  /**
   * Called by the Android system when it wants to interrupt feedback
   */
  @Override
  public void onInterrupt() {
    // Ignore, since we are not actually a screen reader.
  }
}
