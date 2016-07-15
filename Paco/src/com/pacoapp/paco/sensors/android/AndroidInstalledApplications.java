package com.pacoapp.paco.sensors.android;

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

  public String getPackageNameFromAppLabel(CharSequence appLabel) {
    PackageManager packageManager = context.getPackageManager();
    // The only way to do this is to traverse all applications, and see which ones have the label we want
    for (ApplicationInfo appInfo : packageManager.getInstalledApplications(0)) {
      CharSequence currentAppLabel = appInfo.loadLabel(packageManager);
      if (currentAppLabel.equals(appLabel)) {
        // TODO: do not just return here, create a list instead
        return appInfo.packageName;
      }
    }
    return null;
  }


}
