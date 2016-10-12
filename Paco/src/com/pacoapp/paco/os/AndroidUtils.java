package com.pacoapp.paco.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AndroidUtils {

  private static Logger Log = LoggerFactory.getLogger(AndroidUtils.class);

  private AndroidUtils() {
    super();
  }

  public static String getAppVersion(Context context) {
    String version = "";
    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      version = pInfo.versionName;
    } catch (NameNotFoundException e1) {
      Log.error("Cannot rerieve app version", e1);
    }
    return version;
  }



}
