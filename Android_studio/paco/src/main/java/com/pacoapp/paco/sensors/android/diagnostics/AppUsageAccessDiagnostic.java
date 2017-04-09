package com.pacoapp.paco.sensors.android.diagnostics;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.pacoapp.paco.R;

@SuppressLint("NewApi")
public class AppUsageAccessDiagnostic extends Diagnostic<String> {

  public AppUsageAccessDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_app_usage_access_type));
  }

  @Override
  public void run(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      boolean hasAccess = false;
      try {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
        hasAccess = (mode == AppOpsManager.MODE_ALLOWED);
     } catch (PackageManager.NameNotFoundException e) {
       e.printStackTrace();
     }
     
      setValue(context.getString(R.string.diagnostics_app_usage_access_label) + ": " + hasAccess);
    } else {
      setValue(context.getString(R.string.diagnostics_app_usage_access_label) + ": n/a");
    }

  }

}
