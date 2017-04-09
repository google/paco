package com.pacoapp.paco.sensors.android.procmon;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains helper functions for the AccessibilityEventMonitorService accessibilitylistener.
 */
public class RuntimePermissionsAppUtil {
  // The application context
  private final Context context;

  /**
   * Constructor
   * @param context The application context
   */
  public RuntimePermissionsAppUtil(Context context) {
    this.context = context;
  }
  
  /**
   * Get the package name of the app that was last active, excluding the package manager.
   * This function will only return an app if it was active in the past 60 seconds.
   * @return The package name of the previously active app
   */
  public String getAppSpawningRuntimepermissionsDialog() {
    List<CharSequence> packagesToExclude = new ArrayList();
    packagesToExclude.add("com.google.android.packageinstaller");
    packagesToExclude.add("com.android.packageinstaller");
    return getLastActiveApp(packagesToExclude, 60000);
  }

  /**
   * Get the package name of the last app that was active. This is the currently
   * visible app, unless it was excluded in the excludedPackages list.
   * @param excludedPackages List of package names to exclude
   * @param historyMillis The number of milliseconds to go back in time to find an app
   * @return The package name of the last active app that does not have a package name in the
   *          excludedPackages list
   */
  public String getLastActiveApp(List<CharSequence> excludedPackages, long historyMillis) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
      long now = System.currentTimeMillis();
      // We get usage stats for the last 5 seconds
      List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - historyMillis, now);
      // Get the next-to-last app from this list.
      String lastUsedApp = null;
      long lastUsedTime = 0;
      for (UsageStats appStats : stats) {
        if (appStats.getLastTimeUsed() > lastUsedTime &&
                !excludedPackages.contains(appStats.getPackageName())) {
            lastUsedTime = appStats.getLastTimeUsed();
            lastUsedApp = appStats.getPackageName();
        }
      }
      return lastUsedApp;
    }
    return null;
  }
}
