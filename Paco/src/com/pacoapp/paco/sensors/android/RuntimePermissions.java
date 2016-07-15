package com.pacoapp.paco.sensors.android;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Experiment;
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
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
public class RuntimePermissions extends AccessibilityService {
  // Used to keep track of which app we are changing settings for. Needed because
  // AccessibilityEvents will only show us what information is currently being interacted with
  private static List<String> currentlyHandledAppPackageNames;
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
   * Checks if the system is showing a runtime permissions dialog, spawned by an app to ask for a
   * runtime permission.
   * @param nodeInfo The source of the accessibility event
   * @return
   */
  private boolean isPermissionsDialog(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
  }

  /**
   * Checks if the user is in the settings menu, showing the info for a specific app. This is the
   * screen containing info on storage, data usage, etc. used by the app.
   * @param nodeInfo The source of the accessibility event
   * @return
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
   * @return
   */
  private boolean isAppPermissionsScreen(AccessibilityNodeInfo nodeInfo) {
    // TODO: check if this is sufficient, and whether these operations are not too costly
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name").size() > 0 &&
            nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0
    );
  }

  private boolean isPermissionsDialogAction(AccessibilityNodeInfo nodeInfo) {
    return (nodeInfo.getClassName().equals("android.widget.Button") &&
            (nodeInfo.getText().equals("Deny") || nodeInfo.getText().equals("Allow")));
  }

  private boolean isSettingsPermissionChange(AccessibilityNodeInfo nodeInfo) {
    // This will most certainly be too broad, but we ignore this for now until we can get some
    // real experiment data
    return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/switchWidget").size() > 0 &&
            nodeInfo.getClassName().equals("android.widget.LinearLayout"));
  }

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

  private void extractAppPackageNamesFromAppPermissionsScreen(AccessibilityNodeInfo rootNodeInfo) {
    AndroidInstalledApplications installedApps = new AndroidInstalledApplications(getApplicationContext());
    // "com.android.settings:id/widget_text2" is the id for the text string which contains the
    // app *label*. You'll find it on top of the screen.
    List<AccessibilityNodeInfo> matchingNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/name");
    for (AccessibilityNodeInfo nodeInfo : matchingNodeInfos) {
      if (nodeInfo.getText() != null) {
        CharSequence appLabel = nodeInfo.getText();
        List<String> packageNames = installedApps.getPackageNameFromAppLabel(appLabel);
        if (packageNames.size() > 0) {
          setCurrentlyHandledAppPackageNames(packageNames);
          return;
        }
      }
    }
  }

  private void setCurrentlyHandledAppPackageNames(List<String> packageNames) {
    currentlyHandledAppPackageNames = packageNames;
    Log.v(PacoConstants.TAG, "Set 'currently handled package names' name to " + currentlyHandledAppPackageNames.toString());
  }

  private void setCurrentlyHandledAppPackageName(CharSequence packageName) {
    ArrayList packageList = new ArrayList();
    packageList.add(packageName);
    setCurrentlyHandledAppPackageNames(packageList);
  }

  private void processPermissionDialogAction(AccessibilityNodeInfo nodeInfo) {
    if (nodeInfo.getText().equals("Allow")) {
      triggerBroadcastTriggerService(currentlyHandledPermission, true, false);
    } else if (nodeInfo.getText().equals("Deny")) {
      triggerBroadcastTriggerService(currentlyHandledPermission, false, false);
    } else {
      Log.e(PacoConstants.TAG, "Dialog action in runtime permissions dialog was not 'Allow' nor 'Deny'. This should never happen");
    }
  }

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

  private void extractInformationFromPermissionDialog(AccessibilityEvent accessibilityEvent) {
    // The app for which the permission is requested will be the one which was last in the
    // foreground. Since background services are not able to call requestPermissions(), the last
    // visible activity should always belong to the requesting app.
    setCurrentlyHandledAppPackageName(getPreviousApp());

    // Extract the requested permission from the text in the dialog. This should always be the
    // last word in the dialog. TODO: check if this is actually the case
    String displayText = accessibilityEvent.getText().get(0).toString();
    // Get the latest word and trip off the '?' at the end
    currentlyHandledPermission = displayText.subSequence(displayText.lastIndexOf(' ') + 1, displayText.length() - 1);
    Log.v(PacoConstants.TAG, "Set 'currently handled permission' to " + currentlyHandledPermission);
  }

  private void triggerBroadcastTriggerService(CharSequence permission, boolean isAllowed, boolean initiatedByUser) {
    Context context = getApplicationContext();
    Log.d(PacoConstants.TAG, "Broadcasting permission change for " + currentlyHandledAppPackageNames + ": " + permission + " set to " + isAllowed);

    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, InterruptCue.PERMISSION_CHANGED);
        /* TODO: Add all parameters here
        Bundle payload = new Bundle();
        broadcastTriggerServiceIntent.putExtra(PACO_ACTION_PAYLOAD, payload);
        if (sourceIdentifier != null) {

            broadcastTriggerServiceIntent.putExtra(extraKey, sourceIdentifier);
        }
        */
    context.startService(broadcastTriggerServiceIntent);
  }

  // TODO: maybe move this code to the pacoapp.paco.asensors.android.procmon package. Ask Bob what he would like the place for this code to be
  private String getPreviousApp() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
      long now = System.currentTimeMillis();
      // We get usage stats for the last 5 seconds
      List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 5, now);
      // Get the next-to-last app from this list.
      String lastUsedApp = null;
      long lastUsedTime = 0;
      String nextToLastUsedApp = null;
      long nextToLastUsedTime = 0;
      for (UsageStats appStats : stats) {
        if (appStats.getLastTimeUsed() > nextToLastUsedTime) {
          if (appStats.getLastTimeUsed() > lastUsedTime) {
            nextToLastUsedTime = lastUsedTime;
            nextToLastUsedApp = lastUsedApp;
            lastUsedTime = appStats.getLastTimeUsed();
            lastUsedApp = appStats.getPackageName();
          } else {
            nextToLastUsedTime = appStats.getLastTimeUsed();
            nextToLastUsedApp = appStats.getPackageName();
          }
        }
      }
      return nextToLastUsedApp;
    }
    return null;
  }

  @Override
  protected void onServiceConnected() {
    Log.d(PacoConstants.TAG, "Connected to the accessibility service");
    if (!Locale.getDefault().equals(Locale.ENGLISH)) {
      // We don't really need to signal this to the user, as it is the experiment provider who
      // is responsible for checking this should not be a problem for the experiment.
      // TODO: add a disclaimer in the web interface when enabling this?
      Log.w(PacoConstants.TAG, "Detected locale is " + Locale.getDefault().toString() +
              ". RuntimePermissions triggering does not support non-English languages; " +
              "permissions might not always be interpreted correctly");
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // TODO: update to Marshmallow when project SDK changes
      // TODO: think about how we should inform the user about this
      Log.e(PacoConstants.TAG, "RuntimePermissions triggering should not be used on pre-Marshmallow. Stopping service.");
      stopSelf();
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    // TODO: open accessibility settings if we don't have accessibility permission. In API level 22 it seems like the only way to check is whether onServiceConnected() got called
  }

  @Override
  public void onInterrupt() {

  }
}
