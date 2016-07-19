package com.pacoapp.paco.sensors.android.procmon;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import java.util.List;

/**
 * This class contains helper functions for the RuntimePermissions accessibilitylistener.
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
   * Get the package name of the app that was previously active. This is *not* the currently
   * visible app, but the one before that in the list of active applications.
   * This function will only return an app if it was active in the past 60 seconds.
   * @return The package name of the previously active app
   */
  public String getPreviousApp() {
    return getPreviousApp(60000);
  }

  /**
   * Get the package name of the app that was previously active. This is *not* the currently
   * visible app, but the one before that in the list of active applications.
   * @param historyMillis The number of milliseconds to go back in time to find an app
   * @return The package name of the previously active app
   */
  public String getPreviousApp(long historyMillis) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
      long now = System.currentTimeMillis();
      // We get usage stats for the last 5 seconds
      List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - historyMillis, now);
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
}
