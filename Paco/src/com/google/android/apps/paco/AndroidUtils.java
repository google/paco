package com.google.android.apps.paco;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AndroidUtils {

  private AndroidUtils() {
    super();
  }

  public static String getAppVersion(Context context) {
    String version = "";
    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      version = pInfo.versionName;
    } catch (NameNotFoundException e1) {
      Log.e(PacoConstants.TAG, "Cannot rerieve app version", e1);
    }
    return version;
  }
  
  

}
