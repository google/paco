package com.google.android.apps.paco.sensors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.android.apps.paco.sensors.android.AndroidKitKatStepSensor;

public class StepSensorFactory {

  public static StepSensor getStepSensor(Context context) {
    return getAndroidKitKatStepSensor(context);
  }

  private static StepSensor getAndroidKitKatStepSensor(Context context) {
    if (hasAndroidStepCounterSensor(context)) {
      return new AndroidKitKatStepSensor(context);
    }
    return null;
  }

  public static boolean hasAndroidStepCounterSensor(Context context) {
    final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
    return sdkVersion >= Build.VERSION_CODES.KITKAT &&
            context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
  }

}
