package com.pacoapp.paco.sensors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.pacoapp.paco.sensors.android.AndroidKitKatStepSensor;
import com.pacoapp.paco.sensors.jawbone.JawboneServiceConnector;

public class StepSensorFactory {

  public static final int ANDROID_STEP_SENSOR = 1;
  public static final int JAWBONE_STEP_SENSOR = 2;


  public static StepSensor getStepSensor(Context context, int type) {
    switch(type) {
    case ANDROID_STEP_SENSOR:
      return getAndroidKitKatStepSensor(context);
    case JAWBONE_STEP_SENSOR:
      return getJawboneStepSensor(context);
      default:
        throw new IllegalArgumentException("unknown step sensor type");
    }

  }

  private static StepSensor getJawboneStepSensor(Context context) {
    return new JawboneServiceConnector(context);
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
