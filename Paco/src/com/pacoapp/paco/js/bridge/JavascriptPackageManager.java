package com.pacoapp.paco.js.bridge;

import java.util.List;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.pacoapp.paco.sensors.android.AndroidInstalledApplications;

public class JavascriptPackageManager {

  private Context context;

  public JavascriptPackageManager(Context context) {
    this.context = context;
  }

  /**
   * Returns -1 if there is no step sensor in the phone.
   * @return
   */
  @JavascriptInterface
  public List<String> getNamesOfInstalledApplications() {
    return new AndroidInstalledApplications(context).getNamesOfInstalledApplications();
  }

}
