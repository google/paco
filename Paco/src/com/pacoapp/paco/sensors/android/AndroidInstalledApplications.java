package com.pacoapp.paco.sensors.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class AndroidInstalledApplications {

  private static Logger Log = LoggerFactory.getLogger(AndroidInstalledApplications.class);

  public static final String PACKAGE_NAME = "packageName";
  public static final String APP_NAME = "appName";

  public static final String SHARED_PREFERENCES_KEY = "InstalledApplications";

  private static final List<String> whitelist =
    Lists.newArrayList("Chrome", "Gmail", "Phone", "Camera", "Messaging", "Google App", "Google",
                             "Maps", "Drive", "Google Play Movies & TV", "Google+", "Google Play Store",
                             "Google Play Newsstand", "Contacts", "Calendar", "Google Play Music",
                             "Google Play Books", "Hangouts", "Gallery", "Google Play Games", "YouTube",
                             "Keep", "Wallet", "Earth", "Calculator", "Photos", "Fit", "Google Contacts",
                             "Clock", "Email", "News & Weather", "Slides", "Sheets", "Documents");
  private PackageManager packageManager;
  private SharedPreferences sharedPreferences;

  public AndroidInstalledApplications(Context context) {
    super();
    this.packageManager = context.getPackageManager();
    this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
  }

  public List<String> getNamesOfInstalledApplications() {
    List<String> appNames = Lists.newArrayList();
    List<PackageInfo> installed = packageManager.getInstalledPackages(0);
    for (PackageInfo packageInfo : installed) {
      if (packageInfo.versionName == null) {
        continue;
      }
      final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
      String appname = applicationInfo.loadLabel(packageManager).toString();

      if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 &&
              !whitelist.contains(appname)) {
        Log.info("Skipping: " + appname);
        continue;
      }

      String versionCode = packageInfo.versionName;
      appNames.add(appname + " (" + versionCode + ")");
    }
    Collections.sort(appNames);
    return appNames;
  }

  /**
   * Resolves an Android package name to the name of the app, if it is visible to the user. This
   * will also work for packages that were uninstalled, as long as they were cached before by
   * calling the cacheApplicationNames() method.
   * @param packageName The package name of the application
   * @return The application name, or an empty string if the package was not found
   */
  public String getApplicationName(String packageName) {
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
      if (applicationInfo == null) {
        throw new PackageManager.NameNotFoundException();
      }
      return applicationInfo.loadLabel(packageManager).toString();
    } catch (PackageManager.NameNotFoundException e) {
      Log.info("No app name found for package " + packageName + ", trying cache");
      // Try to get it from cache, return empty string if not found
      return sharedPreferences.getString(packageName, "");
    }
  }

  /**
   * Get a list of all applications, and the permissions that were granted to them. For packages
   * targeting SDK version 21 or lower, this means "permissions requested at install time"; for
   * packages targeting SDK 22 or newer, this means "permissions granted during runtime".
   * Packages that do not have a launchable activity (i.e. are not visible in the launcher) will not
   * be returned. Similarly, system apps not in the whitelist are not returned.
   * @return A map with the names of installed packages as keys, and a list of the granted
   *         permissions as values
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public Map<String, List<String>> getGrantedPermissions() {
    List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

    Map<String, List<String>> result = new HashMap();
    for (PackageInfo packageInfo : installedPackages) {
      if (packageInfo.requestedPermissions == null) {
        // This package didn't request any permissions
        continue;
      }
      if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) == null) {
        // This package has no launchable activities
        continue;
      }
      if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 &&
              !whitelist.contains(getApplicationName(packageInfo.packageName))) {
        // This is a system app not in the Paco whitelist
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

    // The only way to do this is to traverse all applications, and see which ones have the label we want
    for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
      CharSequence currentAppLabel = appInfo.loadLabel(packageManager);
      if (currentAppLabel.equals(appLabel)) {
        matchingPackages.add(appInfo.packageName);
      }
    }
    return matchingPackages;
  }

  /**
   * Cache pairs of all package names and their corresponding app names in the shared preferences
   * store, so we can query even for application names of packages that have been uninstalled.
   */
  public void cacheApplicationNames() {
    Log.info("Caching names of installed applications");
    SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();

    for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
      String packageName = appInfo.packageName.toString();
      String appName = appInfo.loadLabel(packageManager).toString();
      preferencesEditor.putString(packageName, appName);
    }
    preferencesEditor.commit();
  }
}
