package com.pacoapp.paco.sensors.android.procmon;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.util.ExperimentHelper;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

// TODO refactor this to compute increased foreground time for an app from it's previous foreground time.

@SuppressLint("NewApi")
public class LollipopProcessMonitorService extends Service {

  private static Logger Log = LoggerFactory.getLogger(LollipopProcessMonitorService.class);

  protected boolean running;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (running) {
      Log.info("Paco App Usage Poller.onStart() -- Already running");
      stopSelf();
      return;
    } else {
      Log.info("Paco App Usage Poller.onStart()");

      UsageStatsManager am = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
      final AppUsageEventsService usageEventsService = new AppUsageEventsService(am,
                                                                     BroadcastTriggerReceiver.getFrequency(getApplicationContext()));
      if (!usageEventsService.canGetStats()) {
        Log.info("no access to Usage Stats. Please turn on setting.");
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
                  //Log.info("polling on: runnable instance = " + this.toString());
                  //Log.info("==================================");
                  lpcm.detectUsageEvents();
                  int sleepTime = 1000; //BroadcastTriggerReceiver.getFrequency(LollipopProcessMonitorService.this) * 1000;
                  //Log.info("sleepTime = " + sleepTime);
                  wait(sleepTime);
                } catch (Exception e) {
                }
              }
            }
            if (!pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              createScreenOffPacoEvents(getApplicationContext());
            }
            Log.info("polling stopping: instance = " + LollipopProcessMonitorService.this.toString());
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

  protected void createScreenOffPacoEvents(Context context) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();
    for (Experiment experiment : joined) {
      List<ExperimentGroup> groups = ExperimentHelper.getGroupsThatCareAboutActionLogging(experiment.getExperimentDAO());
      for (ExperimentGroup experimentGroup : groups) {
        Event event = createScreenOffPacoEvent(experiment, experimentGroup.getName());
        experimentProviderUtil.insertEvent(event);
      }
    }
  }

  protected Event createScreenOffPacoEvent(Experiment experiment, String groupName) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentGroupName(groupName);
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();

    responseForInput.setAnswer(new DateTime().toString());
    responseForInput.setName("userNotPresent");
    event.addResponse(responseForInput);
    return event;
}

  @Override
  public IBinder onBind(Intent intent) {
    // We don't provide binding, so return null
    return null;
  }

  @Override
  public void onDestroy() {
    Log.info("Paco App Usage poller.onDestroy()");
  }

}
