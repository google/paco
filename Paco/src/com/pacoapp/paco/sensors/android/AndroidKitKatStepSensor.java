package com.pacoapp.paco.sensors.android;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.sensors.StepSensor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AndroidKitKatStepSensor implements StepSensor {

  private static Logger Log = LoggerFactory.getLogger(AndroidKitKatStepSensor.class);

  private Context context;

  public AndroidKitKatStepSensor(Context context) {
    super();
    this.context = context;
  }

  public Integer getStepCount() {
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
      SensorManager mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
      Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
      if (mStepCountSensor == null) {
        return -1;
      }

      StepCountSensorFuture future = new StepCountSensorFuture(mSensorManager, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
      try {
        return future.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return -1;

  }

  public class StepCountSensorFuture implements Future<Integer> {

    private volatile Integer result = null;
    private volatile boolean cancelled = false;
    private final CountDownLatch countDownLatch;
    private SensorEventListener listener;
    private SensorManager sensorManager;

    public StepCountSensorFuture(SensorManager mSensorManager, Sensor mStepCountSensor, int sensorDelayNormal) {
      this.sensorManager = mSensorManager;

      countDownLatch = new CountDownLatch(1);

      this.listener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
          Log.debug("onAccuracyChanged - accuracy: " + accuracy);
        }

        public void onSensorChanged(SensorEvent event) {
          if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            final Integer f = Integer.valueOf((int)event.values[0]);
            sensorManager.unregisterListener(this);
            onResult(f);
          } else {
            onResult(-1);
            Log.debug("Unknown sensor type");
          }
        }
      };
      sensorManager.registerListener(listener, mStepCountSensor, sensorDelayNormal);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (isDone()) {
            return false;
        } else {
            countDownLatch.countDown();
            cancelled = true;
            return !isDone();
        }
    }

    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result;
    }

    @Override
    public Integer get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        return result;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    public void onResult(final Integer result) {
        this.result = result;
        countDownLatch.countDown();
    }

}

}
