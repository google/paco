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

import java.util.List;

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
public class RuntimePermissions extends AccessibilityService {
    // Used to keep track of which app we are changing settings for. Needed because
    // AccessibilityEvents will only show us what information is currently being interacted with
    private static CharSequence currentlyHandledAppPackage;
    // Only used with runtime permission dialogs. Keep the currently requested permission in memory
    // so we remember it when the user actually clicked allow/deny
    private static CharSequence currentlyHandledPermission;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
    // TODO: check if the previous annotation is sufficient to make sure this won't be called on pre-lollipop devices
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        // Assert that we're handling events only for the package installer
        CharSequence packageName = accessibilityEvent.getPackageName();
        if (!packageName.equals("com.google.android.packageinstaller") &&
                !packageName.equals("com.android.packageinstaller") &&
                !packageName.equals("com.android.settings")) {
            Log.e(PacoConstants.TAG, "Not expecting to receive accessibility events for " + packageName + ". Ignoring.");
            return;
        }

        int eventType = accessibilityEvent.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // TODO: check what other kinds of windows this can be by logging
                // For our purposes, this means: a dialog requesting a runtime permission is shown,
                // or the user navigated to the 'App info' screen for a specific app
                Log.v(PacoConstants.TAG, "New accessibility event: window state changed (we are capturing this)");
                if (isAppInfoScreen(accessibilityEvent.getSource())) {
                    Log.v(PacoConstants.TAG, "We seem to be inside the app info screen");
                    // Find the package name in this view, and store it for future use
                    extractAppPackageNameFromAppInfoScreen(accessibilityEvent.getSource());
                } else if (isPermissionsDialog(accessibilityEvent.getSource())) {
                    Log.v(PacoConstants.TAG, "We seem to be inside a runtime permissions dialog");
                    extractInformationFromPermissionDialog(accessibilityEvent);
                }
                break;
            case AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE:
                // For our purposes, this means: permission change via switch button (in settings),
                // or clicking 'allow/deny' in a runtime permission dialog
                // TODO: do the same checks as in the previous case: check whether this is an actual configuration change, or clicking allow/deny
                Log.v(PacoConstants.TAG, "New accessibility event: content change type subtree");
                processPermissionConfigurationChange(accessibilityEvent);
                break;
        }
    }

    private boolean isPermissionsDialog(AccessibilityNodeInfo nodeInfo) {
        return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.packageinstaller:id/permission_deny_button").size() > 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
    private boolean isAppInfoScreen(AccessibilityNodeInfo nodeInfo) {
        // TODO: check if this is sufficient, and whether these operations are not too costly
        return (nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/all_details").size() > 0 &&
                nodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text2").size() > 0
        );
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
    private void extractAppPackageNameFromAppInfoScreen(AccessibilityNodeInfo rootNodeInfo) {
        // "com.android.settings:id/widget_text2" is the id for the text string which contains the
        // package name on the "App info" screen. You'll find it right underneath the version number
        // TODO: check that this works on all Android versions >= 6.0
        List<AccessibilityNodeInfo> matchingNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.android.settings:id/widget_text2");
        for (AccessibilityNodeInfo nodeInfo : matchingNodeInfos) {
            if (nodeInfo.getText() != null) {
                setCurrentlyHandledAppPackage(nodeInfo.getText());
            }
        }
    }

    private void setCurrentlyHandledAppPackage(CharSequence packageName) {
        currentlyHandledAppPackage = packageName;
        Log.v(PacoConstants.TAG, "Set 'currently handled package' name to " + currentlyHandledAppPackage);
    }

    private void processPermissionConfigurationChange(AccessibilityEvent accessibilityEvent) {
        List<CharSequence> textFields = accessibilityEvent.getText();
        if (textFields.size() != 2) {
            Log.e(PacoConstants.TAG, "Unexpected length for text array on permission configuration change: " + textFields);
            return;
        }
        CharSequence permission = textFields.get(0);
        boolean isAllowed = textFields.get(1).equals("ON");
        triggerBroadcastTriggerService(permission, isAllowed);
    }

    private void extractInformationFromPermissionDialog(AccessibilityEvent accessibilityEvent) {
        // The app for which the permission is requested will be the one which was last in the
        // foreground. Since background services are not able to call requestPermissions(), the last
        // visible activity should always belong to the requesting app.
        setCurrentlyHandledAppPackage(getPreviousApp());

        // Extract the requested permission from the text in the dialog. This should always be the
        // last word in the dialog. TODO: check if this is actually the case
        // TODO: safety checks for array size etc.
        String displayText = accessibilityEvent.getText().get(0).toString();
        // Get the latest word and trip off the '?' at the end
        currentlyHandledPermission = displayText.subSequence(displayText.lastIndexOf(' ') + 1, displayText.length() - 1);
        Log.v(PacoConstants.TAG, "Set 'currently handled permission' to " + currentlyHandledPermission);
    }

    private void triggerBroadcastTriggerService(CharSequence permission, boolean isAllowed) {
        Context context = getApplicationContext();
        Log.d(PacoConstants.TAG, "Broadcasting permission change for " + currentlyHandledAppPackage + ": " + permission + " set to " + isAllowed);

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            // We get usage stats for the last 5 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*5, time);
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
    }

    @Override
    public void onInterrupt() {

    }
}
