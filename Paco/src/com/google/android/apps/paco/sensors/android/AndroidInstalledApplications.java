package com.google.android.apps.paco.sensors.android;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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
    List<ApplicationInfo> installed = pm.getInstalledApplications(0);
    for (ApplicationInfo applicationInfo : installed) {
      appNames.add(applicationInfo.name);
    }
    return appNames;
  }

}
