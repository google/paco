package com.pacoapp.paco.js.bridge;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.pacoapp.paco.sensors.StepSensor;
import com.pacoapp.paco.sensors.StepSensorFactory;

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
  public int getStepCountFrom(int type) {
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
  public int getStepCount() {
    StepSensor stepSensor = StepSensorFactory.getStepSensor(context, StepSensorFactory.ANDROID_STEP_SENSOR);
    if (stepSensor != null) {
      return stepSensor.getStepCount();
    }
    return -1;
  }


}
