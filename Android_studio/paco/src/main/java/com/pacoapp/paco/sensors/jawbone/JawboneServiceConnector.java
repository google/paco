package com.pacoapp.paco.sensors.jawbone;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bodymedia.mobile.jrs.JawboneDataService;
import com.pacoapp.paco.sensors.StepSensor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * This is a one-shot connector used by a script to retrieve the current step count from the JRS service.
 *
 *This is based on the example app from Jawbone research showing how to use the JRS service.
 */
public class JawboneServiceConnector implements StepSensor {

  private static Logger Log = LoggerFactory.getLogger(JawboneServiceConnector.class);

  private static final String JAWBONE_SERVICE_DOMAIN = "com.bodymedia.mobile.jrs.service.JawboneService";

  // Error codes
  private static final int ERROR_UNKNOWN = -1;
  private static final int ERROR_KEY_NOT_FOUND = -100;
  private static final int ERROR_REQUIRES_PAIRING = -200;
  private static final int ERROR_CONNECTING_TO_DEVICE = -300;
  private static final int ERROR_WAIT_DATA_TIMEOUT = -400;
  private static final int ERROR_LOADING_DATA = -500;

  private JawboneDataService jawboneService = null;
  protected boolean bound;
  private Context context;

  public JawboneServiceConnector(Context context) {
    this.context = context;
    bindService();
  }

  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      jawboneService = JawboneDataService.Stub.asInterface(service);
      bound = true;
      Log.info("Jawbone service connected");
      //showKeys();
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      unbindState("surprise unbound");
    }

  };

  public void showKeys() {
    if (!bound) {
      Log.error("Showkeys can't. Service not bound");
    } else {
      try {
        List<String> keys = jawboneService.getKeys();
        for (String string : keys) {
          Log.info("key: " + string);
        }
      } catch (RemoteException e) {
        Log.error("Remote exception getting keys: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public Integer getStepCount() {
    return queryForValue("steps_today");
  }

  /**
   * To ask service some value we need to call JawboneService.getValue() method
   * with existing key. If key not found than service return error code -1.
   *
   * @param key
   *          one of the existing keys.
   * @return
   */
  Integer queryForValue(final String key) {
    if (!bound)
      return -1;

    Log.info("Calling service for data");

    final StepCountSensorFuture future = new StepCountSensorFuture(jawboneService, key);
    new Thread(new Runnable() {
      @Override
      public void run() {
//        try {
//          final int value = jawboneService.getValue(key);
//          if (isErrorCode(value)) {
//            Log.error("Error code received: " + value);
//            Log.error("Error message: " + getErrorMessage(value));
//            return;
//          }
//
//          Log.info("Received response: " + String.valueOf(value));
//        } catch (RemoteException e) {
//          e.printStackTrace();
//        }
//        context.unbindService(mConnection);
//        unbindState("Done with request. Unbinding service");

        future.retrieveValue(key);

      }

    }).start();
    try {
      return future.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return -1;
  }


  private void unbindState(String string) {
    jawboneService = null;
    bound = false;
    Log.info(string);
  }

  private void bindService() {
    Intent intent = new Intent(JAWBONE_SERVICE_DOMAIN);

    // check if service exists
    if (context.getPackageManager().queryIntentServices(intent, 0).isEmpty()) {
      Log.error("message service not found");
      return;
    }

    // bind to service and wait binder in ServiceConnection interface
    context.bindService(createExplicitFromImplicitIntent(context, intent), mConnection, Context.BIND_AUTO_CREATE);
    Log.info("Binding");
  }

    private boolean isErrorCode(int value) {
    int code = value;
    return code == ERROR_UNKNOWN || code == ERROR_KEY_NOT_FOUND || code == ERROR_REQUIRES_PAIRING
           || code == ERROR_WAIT_DATA_TIMEOUT || code == ERROR_CONNECTING_TO_DEVICE || code == ERROR_LOADING_DATA;
  }

  private String getErrorMessage(int errorCode) {
    switch (errorCode) {
    case ERROR_KEY_NOT_FOUND:
      return "key does not exist";
    case ERROR_REQUIRES_PAIRING:
      return "Needs pairing";
    case ERROR_CONNECTING_TO_DEVICE:
      return "still connecting to device";
    case ERROR_WAIT_DATA_TIMEOUT:
      return "wait data timeout";
    case ERROR_LOADING_DATA:
      return "error loading data";
    }

    return "unknown error";
  }

  /***
   * From Jawbone engineer
   *
   * Android L (lollipop, API 21) introduced a new problem when trying to invoke
   * implicit intent,
   * "java.lang.IllegalArgumentException: Service Intent must be explicit"
   *
   * If you are using an implicit intent, and know only 1 target would answer
   * this intent, This method will help you turn the implicit intent into the
   * explicit form.
   *
   * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
   *
   * @param context
   * @param implicitIntent
   *          - The original implicit intent
   * @return Explicit Intent created from the implicit original intent
   */
  public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
    // Retrieve all services that can match the given intent
    PackageManager pm = context.getPackageManager();
    List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

    // Make sure only one match was found
    if (resolveInfo == null || resolveInfo.isEmpty()) {
      return null;
    }

    // Get component info and create ComponentName
    ResolveInfo serviceInfo = resolveInfo.get(0);
    String packageName = serviceInfo.serviceInfo.packageName;
    String className = serviceInfo.serviceInfo.name;
    ComponentName component = new ComponentName(packageName, className);

    // Create a new intent. Use the old one for extras and such reuse
    Intent explicitIntent = new Intent(implicitIntent);

    // Set the component to be explicit
    explicitIntent.setComponent(component);

    return explicitIntent;
  }


  public class StepCountSensorFuture implements Future<Integer> {

    private volatile Integer result = null;
    private volatile boolean cancelled = false;
    private final CountDownLatch countDownLatch;
    private JawboneDataService jawboneService;

    public StepCountSensorFuture(JawboneDataService jawboneService, String key) {
      this.jawboneService = jawboneService;
      countDownLatch = new CountDownLatch(1);
    }

    public void retrieveValue(String key) {
      try {
        int value = jawboneService.getValue(key);
        if (isErrorCode(value)) {
          Log.error("Error code received: " + value);
          Log.error("Error message: " + getErrorMessage(value));
        }
        onResult(value);
      } catch (RemoteException e) {
        Log.error("RemoteException received: " + e);
        onResult(-1);
      }
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
