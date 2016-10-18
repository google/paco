package com.pacoapp.paco.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Cleanup class that upon reboot 
 * 1) creates an event to register boot time for experiments that have logging of that turned on
 * 2) eventually will create events that would have fired and expired during phone off time
 */
public class BootupService extends Service {

  private static Logger Log  = LoggerFactory.getLogger(BootupService.class);


  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }


  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    Log.debug("BootupService onStart");

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Paco BootupService wakelock");
    wl.acquire();


    Runnable runnable = new Runnable() {
      public void run() {
        try {
         work();

        } finally {
          wl.release();
          stopSelf();
        }
      }

    };
    (new Thread(runnable)).start();
  }

  private void work() {
    Log.debug("Starting work in BootupService");
    BroadcastTriggerReceiver.createPhoneStateLogEvents(getApplicationContext(), "true");
  }

}
