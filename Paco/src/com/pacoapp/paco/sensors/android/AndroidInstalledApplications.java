package com.pacoapp.paco.sensors.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.common.collect.Lists;
import com.pacoapp.paco.PacoConstants;

public class AndroidInstalledApplications {

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
