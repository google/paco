package com.pacoapp.paco.sensors.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.google.common.collect.Lists;
import com.pacoapp.paco.PacoConstants;

public class AndroidInstalledApplications {

  public static final String PACKAGE_NAME = "packageName";
  public static final String APP_NAME = "appName";

  private static final List<String> whitelist =
          Lists.newArrayList("Chrome", "Gmail", "Phone", "Camera", "Messaging", "Google App",
                             "Maps", "Drive", "Google Play Movies & TV", "Google+", "Google Play Store",
                             "Google Play Newsstand", "Contacts", "Calendar", "Google Play Music",
                             "Google Play Books", "Hangouts", "Gallery", "Google Play Games", "YouTube",
                             "Keep", "Wallet", "Earth", "Calculator", "Photos", "Fit", "Google Contacts",
                             "Clock", "Email", "News & Weather", "Slides", "Sheets", "Documents");
  private Context context;

  public AndroidInstalledApplications(Context context) {
    super();
    this.context = context;
  }

  public List<String> getNamesOfInstalledApplications() {
    PackageManager pm = context.getPackageManager();
    List<String> appNames = Lists.newArrayList();
    List<PackageInfo> installed = pm.getInstalledPackages(0);
    for (PackageInfo packageInfo : installed) {
      if (packageInfo.versionName == null) {
        continue;
      }
      final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
      String appname = applicationInfo.loadLabel(pm).toString();

      if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 &&
              !whitelist.contains(appname)) {
        Log.i(PacoConstants.TAG, "Skipping: " + appname);
        continue;
      }

      String versionCode = packageInfo.versionName;
      appNames.add(appname + " (" + versionCode + ")");
    }
    Collections.sort(appNames);
    return appNames;
  }

  /**
   * Resolves an Android package name to the name of the app, if it is visible to the user.
   * @param packageName The package name of the application
   * @return The application name, or an empty string if the package was not found
   */
  public String getApplicationName(String packageName) {
    PackageManager packageManager = context.getPackageManager();
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
      if (applicationInfo == null) {
        return "";
      }
      return applicationInfo.loadLabel(packageManager).toString();
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(PacoConstants.TAG, "No app name found for package " + packageName);
      return "";
    }
  }

  /**
   * Get a list of all applications, and the permissions that were granted to them. For packages
   * targeting SDK version 21 or lower, this means "permissions requested at install time"; for
   * packages targeting SDK 22 or newer, this means "permissions granted during runtime".
   * @return A map with the names of installed packages as keys, and a list of the granted
   *         permissions as values
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public Map<String, List<String>> getGrantedPermissions() {
    PackageManager packageManager = context.getPackageManager();
    List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

    Map<String, List<String>> result = new HashMap();
    for (PackageInfo packageInfo : installedPackages) {
      if (packageInfo.requestedPermissions == null) {
        // This package didn't request any permissions
        continue;
      }
      List<String> grantedPermissions = new ArrayList();
      // Traverse list of requested permissions for package
      for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
        // Check if permission was granted (in case of pre-Marshmallow apps: if permission was
        // requested at install time.
        if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
          grantedPermissions.add(packageInfo.requestedPermissions[i]);
        }
      }
      result.put(packageInfo.packageName, grantedPermissions);
    }

    return result;
  }

  /**
   * Return the package names for all installed applications whose name corresponds to the appLabel,
   * e.g. if the user has Google & Facebook messenger installed, this function will return
   * ["com.facebook.orca","com.google.android.apps.messaging"] for name "Messenger".
   * This function only does exact matching, not partial matching.
   * @param appLabel The full name of the app as it is displayed in the settings
   * @return A list of package names for apps having the specified name, or an empty list if none
   *    are found
   */
  public ArrayList<String> getPackageNameFromAppLabel(CharSequence appLabel) {
    ArrayList<String> matchingPackages = new ArrayList();

    PackageManager packageManager = context.getPackageManager();
    // The only way to do this is to traverse all applications, and see which ones have the label we want
    for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
      CharSequence currentAppLabel = appInfo.loadLabel(packageManager);
      if (currentAppLabel.equals(appLabel)) {
        matchingPackages.add(appInfo.packageName);
      }
    }
    return matchingPackages;
  }


}
