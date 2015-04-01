package com.google.android.apps.paco.js.bridge;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.android.apps.paco.sensors.StepSensor;
import com.google.android.apps.paco.sensors.StepSensorFactory;

public class JavascriptSensorManager {

  private Context context;

  public JavascriptSensorManager(Context context) {
    this.context = context;
  }

  /**
   * Returns -1 if there is no step sensor in the phone.
   * @return
   */
  @JavascriptInterface
  public Integer getStepCountFrom(int type) {
    StepSensor stepSensor = StepSensorFactory.getStepSensor(context, type);
    if (stepSensor != null) {
      return stepSensor.getStepCount();
    }
    return -1;
  }

  /**
   * Returns -1 if there is no step sensor in the phone.
   * @return
   */
  @JavascriptInterface
  public Integer getStepCount() {
    StepSensor stepSensor = StepSensorFactory.getStepSensor(context, StepSensorFactory.ANDROID_STEP_SENSOR);
    if (stepSensor != null) {
      return stepSensor.getStepCount();
    }
    return -1;
  }


}
