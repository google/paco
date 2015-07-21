package com.pacoapp.paco.sensors.android.procmon;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;

// TODO refactor this to compute increased foreground time for an app from it's previous foreground time.

@SuppressLint("NewApi")
public class LollipopProcessMonitorService extends Service {

  protected boolean running;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (running) {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart() -- Already running");
      stopSelf();
      return;
    } else {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart()");

      UsageStatsManager am = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
      final AppUsageEventsService usageEventsService = new AppUsageEventsService(am,
                                                                     BroadcastTriggerReceiver.getFrequency(getApplicationContext()));
      if (!usageEventsService.canGetStats()) {
        Log.i(PacoConstants.TAG, "no access to Usage Stats. Please turn on setting.");
        stopSelf();
        return;
      }

      final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                      "Paco App Usage Poller Service wakelock");
      wl.acquire();

      Runnable runnable = new Runnable() {
        public void run() {
          running = true;
          LollipopAppUsageMonitor lpcm = createAppUsageMonitor(usageEventsService);

          try {
            while (pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              synchronized (this) {
                try {
                  Log.i(PacoConstants.TAG, "polling on: runnable instance = " + this.toString());
                  Log.i(PacoConstants.TAG, "==================================");
                  lpcm.detectUsageEvents();
                  int sleepTime = 1000; //BroadcastTriggerReceiver.getFrequency(LollipopProcessMonitorService.this) * 1000;
                  Log.i(PacoConstants.TAG, "sleepTime = " + sleepTime);
                  wait(sleepTime);
                } catch (Exception e) {
                }
              }
            }
            // if (!pm.isScreenOn()) {
            // BroadcastTriggerReceiver.createBrowserHistoryEndSnapshot(getApplicationContext());
            // //testIfUserHasResponded
            // //createNotificationIfNotResponded
            //
            // }
            Log.i(PacoConstants.TAG, "polling stopping: instance = " + LollipopProcessMonitorService.this.toString());
          } finally {
            wl.release();
            stopSelf();
            running = false;
          }
        }

        public LollipopAppUsageMonitor createAppUsageMonitor(final AppUsageEventsService usageEventsService) {
          ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(getApplicationContext());
          AppUsageTriggerHelper apmh = new AppUsageTriggerHelper(experimentProviderUtil);
          List<String> appOpenTasks = apmh.getAppStartTasksToWatch();
          List<String> appCloseTasks = apmh.getAppCloseTasksToWatch();
          List<Experiment> experimentsWatchingAppUsage = apmh.initializeExperimentsWatchingAppUsage();

          AppUsageEventLogger pueb = new AppUsageEventLogger(getApplicationContext(),
                                                             experimentsWatchingAppUsage,
                                                             experimentProviderUtil);

          LollipopAppUsageMonitor lpcm = new LollipopAppUsageMonitor(appOpenTasks,
                                                                               appCloseTasks,
                                                                               getApplicationContext(),
                                                                               usageEventsService,
                                                                               pueb);
          return lpcm;
        }

      };
      (new Thread(runnable)).start();
    }
  }


  @Override
  public IBinder onBind(Intent intent) {
    // We don't provide binding, so return null
    return null;
  }

  @Override
  public void onDestroy() {
    Log.i(PacoConstants.TAG, "Paco App Usage poller.onDestroy()");
  }

}
