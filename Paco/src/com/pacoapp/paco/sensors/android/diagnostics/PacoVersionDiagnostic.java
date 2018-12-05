package com.pacoapp.paco.sensors.android.diagnostics;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.common.base.Strings;
import com.pacoapp.paco.R;

public class PacoVersionDiagnostic extends Diagnostic {

  public PacoVersionDiagnostic(Context context) {
    super(context.getString(R.string.diagnostic_paco_version));
  }

  @Override
  public void run(Context context) {
    String pacoVersion = pacoVersion = getVersion(context);
    if (Strings.isNullOrEmpty(pacoVersion)) {
      pacoVersion = "?";
    }
    setValue(context.getString(R.string.diagnostic_paco_version) + ": " + pacoVersion);
  }

  private String getVersion(Context context) {
    PackageInfo pInfo = null;
    try {
      pInfo = context.getPackageManager().getPackageInfo((String) context.getText(R.string.app_package), PackageManager.GET_META_DATA);
      return pInfo.versionName;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return "unknown";
  }
}
