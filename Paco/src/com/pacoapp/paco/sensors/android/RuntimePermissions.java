package com.pacoapp.paco.sensors.android;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.sensors.android.procmon.RuntimePermissionsAppUtil;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.util.TimeUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
 * TODO: implement a check for the locale somewhere
 * TODO: only enable service if it is checked by the experiment organiser
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
public class RuntimePermissions extends AccessibilityService {
  public static final String PACO_ACTION_ACCESSIBILITY_PAYLOAD = "paco_action_accessibility_payload";
  public static final String PAYLOAD_PERMISSION = "paco_accessibility_payload_permission";
  public static final String PAYLOAD_PERMISSION_GRANTED = "paco_accessibility_payload_permissiongranted";
  public static final String PAYLOAD_PERMISSION_USERINITIATED = "paco_accessibility_payload_permissionuserinitiated";
  public static final String PAYLOAD_PERMISSION_PACKAGES = "paco_accessibility_payload_permissionpackages";
  public static final String PAYLOAD_PERMISSION_APPNAME = "paco_accessibility_payload_permissionappname";

  // Keeps whether the service is connected
  public static boolean running = false;
  // Used to keep track of which app we are changing settings for. Needed because
  // AccessibilityEvents will only show us what information is currently being interacted with
  private static ArrayList<String> currentlyHandledAppPackageNames;
  // Only used with runtime permission dialogs. Keep the currently requested permission in memory
  // so we remember it when the user actually clicked allow/deny
  private static CharSequence currentlyHandledPermission;

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
        if (isAppPermissionsScreen(accessibilityEvent.getSource())) {
          // Find the package name in this view, and store it for future use
          Log.v(PacoConstants.TAG, "We seem to be inside the app permissions screen");
          extractAppPackageNamesFromAppPermissionsScreen(accessibilityEvent.getSource());
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
          Log.v(PacoConstants.TAG, "Ignoring window state changed accessibility event, since it was not an app info screen or a permissions dialog.");
        }
        break;
      case AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE:
        // For our purposes, this means: permission change via switch button (in settings),
        // or clicking 'allow/deny' in a runtime permission dialog
        if (isPermissionsDialogAction(accessibilityEvent.getSource())) {
          Log.v(PacoConstants.TAG, "Action taken in permissions dialog");
          processPermissionDialogAction(accessibilityEvent.getSource());
        } else if (isSettingsPermissionChange(accessibilityEvent.getSource())) {
          Log.v(PacoConstants.TAG, "Action taken in permission settings activity");
          processPermissionConfigurationChange(accessibilityEvent);
        } else {
          Log.v(PacoConstants.TAG, "Ignoring content change type subtree, since it was not in a permission dialog or a settings screen.");
        }
        break;
    }
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
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
  }

  /**
   * Checks if the user is in the settings menu, showing the info for a specific app. This is the
   * screen containing info on storage, data usage, etc. used by the app.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user is in the app info screen
   */
  private boolean isAppInfoScreen(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/all_details").size() > 0 &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text2").size() > 0
    );
  }

  /**
   * Checks if the user is in the permissions screen for an app. This activity shows switch buttons
   * for every permission the user can grant to the app.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user is in the app permissions screen
   */
  private boolean isAppPermissionsScreen(AccessibilityNodeInfo nodeInfo) {
    // TODO: check if this is sufficient, and whether these operations are not too costly
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name").size() > 0 &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0
    );
  }

  /**
   * Checks if the user pressed the 'Deny' or 'Allow' button in a permissions dialog.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user performed an action in the permissions dialog
   */
  private boolean isPermissionsDialogAction(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo.getClassName().equals("android.widget.Button") &&
            (nodeInfo.getText().equals("Deny") || nodeInfo.getText().equals("Allow")));
  }

  /**
   * Checks if the user pressed the switch on any of the permissions in the app permissions screen.
   * @param nodeInfo The source of the accessibility event
   * @return true if the user changed a permission on the permissions screen
   */
  private boolean isSettingsPermissionChange(AccessibilityNodeInfo nodeInfo) {
    // This will most certainly be too broad, but we ignore this for now until we can get some
    // real experiment data
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0 &&
            nodeInfo.getClassName().equals("android.widget.LinearLayout"));
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
   * Extracts the name of the app package from the app permissions screen for that package, and
   * stores it in memory.
   * We extract this information by reverse engineering the name from the app permissions screen.
   * @param rootNodeInfo The root node in the tree for the accessibility event
   */
  private void extractAppPackageNamesFromAppPermissionsScreen(AccessibilityNodeInfo rootNodeInfo) {
    AndroidInstalledApplications installedApps = new AndroidInstalledApplications(getApplicationContext());
    // "com.android.settings:id/widget_text2" is the id for the text string which contains the
    // app *label*. You'll find it on top of the screen.
    List<AccessibilityNodeInfo> matchingNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
    for (AccessibilityNodeInfo nodeInfo : matchingNodeInfos) {
      if (nodeInfo.getText() != null) {
        CharSequence appLabel = nodeInfo.getText();
        ArrayList<String> packageNames = installedApps.getPackageNameFromAppLabel(appLabel);
        if (packageNames.size() > 0) {
          setCurrentlyHandledAppPackageNames(packageNames);
          return;
        }
      }
    }
  }

  /**
   * Set the name of the package for which the user is currently changing permissions.
   * @param packageNames a list of possible package names. Not just one string, because an app name
   *                     may correspond to multiple package names
   */
  private void setCurrentlyHandledAppPackageNames(ArrayList<String> packageNames) {
    currentlyHandledAppPackageNames = packageNames;
    Log.v(PacoConstants.TAG, "Set 'currently handled package names' name to " + currentlyHandledAppPackageNames.toString());
  }

  /**
   * Set the name of the package for which the user is currently changing permissions.
   * @param packageName package name for the app
   */
  private void setCurrentlyHandledAppPackageName(CharSequence packageName) {
    ArrayList packageList = new ArrayList();
    packageList.add(packageName);
    setCurrentlyHandledAppPackageNames(packageList);
  }

  /**
   * Called when the user accepts or denies a runtime permission request. Fires a broadcast event
   * making note of whether the user accepted or denied the permission.
   * @param nodeInfo The source of the accessibility event
   */
  private void processPermissionDialogAction(AccessibilityNodeInfo nodeInfo) {
    if (nodeInfo.getText().equals("Allow")) {
      triggerBroadcastTriggerService(currentlyHandledPermission, true, false);
    } else if (nodeInfo.getText().equals("Deny")) {
      triggerBroadcastTriggerService(currentlyHandledPermission, false, false);
    } else {
      Log.e(PacoConstants.TAG, "Dialog action in runtime permissions dialog was not 'Allow' nor 'Deny'. This should never happen");
    }
  }

  /**
   * Called when the user changes a runtime permission from the app permissions screen. Fires a
   * broadcast event making note of whether the user accepted or denied the permission.
   * @param accessibilityEvent accessibility event corresponding to the permission change
   */
  private void processPermissionConfigurationChange(AccessibilityEvent accessibilityEvent) {
    List<CharSequence> textFields = accessibilityEvent.getText();
    if (textFields.size() != 2) {
      Log.e(PacoConstants.TAG, "Unexpected length for text array on permission configuration change: " + textFields);
      return;
    }
    CharSequence permission = textFields.get(0);
    boolean isAllowed = textFields.get(1).equals("ON");
    triggerBroadcastTriggerService(permission, isAllowed, true);
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
    String previousAppPackage = runtimeUtil.getPreviousApp();
    if (previousAppPackage != null) {
      setCurrentlyHandledAppPackageName(previousAppPackage);
    } else {
      Log.d(PacoConstants.TAG, "Keeping previous app package at " + currentlyHandledAppPackageNames + " because it would be null otherwise.");
    }

    // Extract the requested permission from the text in the dialog. This should always be the
    // last word in the dialog. TODO: check if this is actually the case
    String displayText = accessibilityEvent.getText().get(0).toString();
    // Get the latest word and trip off the '?' at the end
    currentlyHandledPermission = displayText.subSequence(displayText.lastIndexOf(' ') + 1, displayText.length() - 1);
    Log.v(PacoConstants.TAG, "Set 'currently handled permission' to " + currentlyHandledPermission);
  }

  /**
   * Calls the BroadcastTriggerService with an intent containing all information of the permission
   * change.
   * @param permission Name of the permission that changed
   * @param isGranted Whether the permission was granted (true) or denied (false)
   * @param initiatedByUser Whether the user actively initiated the permission change. False if the
   *                        permission change was the result of a permission request by the system.
   */
  private void triggerBroadcastTriggerService(CharSequence permission, boolean isGranted, boolean initiatedByUser) {
    Context context = getApplicationContext();
    Log.d(PacoConstants.TAG, "Broadcasting permission change for " + currentlyHandledAppPackageNames + ": " + permission + " set to " + isGranted);

    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, InterruptCue.PERMISSION_CHANGED);
    Bundle accessibilityPayload = new Bundle();
    accessibilityPayload.putCharSequence(PAYLOAD_PERMISSION, permission);
    accessibilityPayload.putBoolean(PAYLOAD_PERMISSION_GRANTED, isGranted);
    accessibilityPayload.putBoolean(PAYLOAD_PERMISSION_USERINITIATED, initiatedByUser);
    accessibilityPayload.putStringArrayList(PAYLOAD_PERMISSION_PACKAGES, currentlyHandledAppPackageNames);
    broadcastTriggerServiceIntent.putExtra(PACO_ACTION_ACCESSIBILITY_PAYLOAD, accessibilityPayload);
    context.startService(broadcastTriggerServiceIntent);
  }

  /**
   * Called by the Android system when it connects the accessibility service. We use this to keep
   * track of whether we have the accessibility permission.
   */
  @Override
  protected void onServiceConnected() {
    running = true;
    Log.d(PacoConstants.TAG, "Connected to the accessibility service");
    if (!Locale.getDefault().getISO3Language().equals(Locale.ENGLISH.getISO3Language())) {
      // We don't really need to signal this to the user, as it is the experiment provider who
      // is responsible for checking this should not be a problem for the experiment.
      // TODO: add a disclaimer in the web interface when enabling this?
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
