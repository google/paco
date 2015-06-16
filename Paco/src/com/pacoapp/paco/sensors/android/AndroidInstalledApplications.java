package com.pacoapp.paco.sensors.android;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.common.collect.Lists;

public class AndroidInstalledApplications {

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
      if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
        continue;
      }
      String appname = applicationInfo.loadLabel(pm).toString();
      String versionCode = packageInfo.versionName;
      appNames.add(appname + " (" + versionCode + ")");
    }
    Collections.sort(appNames);
    return appNames;
  }


}
