package com.google.android.apps.paco.utils;

import android.content.Context;

import com.google.android.apps.paco.sensors.googlefit.GoogleFitHistoryApiConnector;

public class JavascriptSensorManager {

  private Context context;
  private GoogleFitHistoryApiConnector api;

  public JavascriptSensorManager(Context context) {
    this.context = context;
  }

  public Integer getStepCount() {
    api = new GoogleFitHistoryApiConnector(context);
    return api.getStepCount();
  }

//  public void unregisterStepCountDetector() {
//    api.unregister();
//  }


}
