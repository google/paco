package com.google.android.apps.paco.sensors.googlefit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.android.apps.paco.PacoConstants;

public class GoogleFitHistoryApiConnector {

  private Context context;
  private RunnableFuture<Integer> resultPromise;

  public GoogleFitHistoryApiConnector(Context context) {
    super();
    this.context = context;
  }

  public Integer getStepCount() {
    SensorManager mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
    Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    StepCountCheckFuture future = new StepCountCheckFuture(mSensorManager, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
    try {
      return future.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

//    mSensorManager.registerListener(this, );

    return null;
  }

  public void unregister() {
//    SensorManager mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
//    mSensorManager.unregisterListener(this);
  }

  SensorEventListener listener = new SensorEventListener() {
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      Log.d(PacoConstants.TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {
      /*
       * if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) { String msg = ""
       * + (int) event.values[0]; Log.d(PacoConstants.TAG, msg); } else
       */
      if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        String msg = "Count: " + (int) event.values[0];
        Log.d(PacoConstants.TAG, msg);
      }
      /*
       * else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) { String
       * msg = "Detected at " + DateTime.now(); Log.d(PacoConstants.TAG, msg); }
       */
      else {
        Log.d(PacoConstants.TAG, "Unknown sensor type");
      }
    }
  };


  public class StepCountCheckFuture implements Future<Integer> {

    private volatile Integer result = null;
    private volatile boolean cancelled = false;
    private final CountDownLatch countDownLatch;
    private SensorEventListener listener;
    private SensorManager sensorManager;

    public StepCountCheckFuture(SensorManager mSensorManager, Sensor mStepCountSensor, int sensorDelayNormal) {
      this.sensorManager = mSensorManager;
      countDownLatch = new CountDownLatch(1);
      this.listener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
          Log.d(PacoConstants.TAG, "onAccuracyChanged - accuracy: " + accuracy);
        }

        public void onSensorChanged(SensorEvent event) {
          if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            final Integer f = Integer.valueOf((int)event.values[0]);
            sensorManager.unregisterListener(this);
            onResult(f);
          } else {
            Log.d(PacoConstants.TAG, "Unknown sensor type");
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
