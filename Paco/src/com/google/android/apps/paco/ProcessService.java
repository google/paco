package com.google.android.apps.paco;

import java.util.List;

import org.joda.time.DateTime;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.common.collect.Lists;

public class ProcessService extends Service {

  private ActivityManager am;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart()");

    am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Paco App Usage Poller Service wakelock");
    wl.acquire();

    Runnable runnable = new Runnable() {


      public void run() {
        List<String> tasks = initializeWatchedTasks();
        List<String> lastTaskNames = null;

        try {
          while (pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
            synchronized (this) {
              try {
                Log.i(PacoConstants.TAG, "polling on: instance = " + ProcessService.this.toString());
                lastTaskNames = checkForRecentlyUsedProcesses(lastTaskNames, tasks);
                wait(BroadcastTriggerReceiver.getFrequency(ProcessService.this) * 1000);
              } catch (Exception e) {
              }
            }
          }
          Log.i(PacoConstants.TAG, "polling stopping: instance = " +ProcessService.this.toString());
        } finally {
          lastTaskNames = null;
          wl.release();
          stopSelf();
        }
      }


      protected List<String> checkForRecentlyUsedProcesses(List<String> lastTaskNames, List<String> tasks) {
        List<RecentTaskInfo> recentTasks = am.getRecentTasks(30, 0);
        // these are sorted most-recently-used first
        List<String> recentTaskNames = Lists.newArrayList();
        List<String> tasksToSendTrigger = Lists.newArrayList();
        for (int i = 0; i < recentTasks.size(); i++) {
          RecentTaskInfo recentTaskInfo = recentTasks.get(i);
          String taskName = recentTaskInfo.baseIntent.getComponent().flattenToString();
          recentTaskNames.add(taskName);

          if (lastTaskNames == null) {
            continue; // skip first run as we need a previous run to compare against
          } else if (tasks.contains(taskName)) {
            int indexOfTaskNameInLastTaskNames = lastTaskNames.indexOf(taskName);
            if (indexOfTaskNameInLastTaskNames == -1 || i < indexOfTaskNameInLastTaskNames) {
              // TODO Is there an issue with launching the same service multiple times (this happens if we have more than one experiment watching processes...)
              tasksToSendTrigger.add(taskName);
            }
          }
        }

        for (String taskName : tasksToSendTrigger) {
          triggerAppUsed(taskName);
        }
        return recentTaskNames;
      }

      private List<String> initializeWatchedTasks() {
        List<String> tasks = Lists.newArrayList();
        ExperimentProviderUtil eu = new ExperimentProviderUtil(ProcessService.this);
        DateTime now = new DateTime();
        List<Experiment> joined = eu.getJoinedExperiments();
        for (Experiment experiment : joined) {
          if (!experiment.isOver(now) && experiment.shouldPoll()) {
            tasks.add(experiment.getTrigger().getSourceIdentifier());
          }
        }
        return tasks;
      }

      private void triggerAppUsed(String appIdentifier) {
        Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
        Context context = getApplicationContext();
        Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
        broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
        broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, Trigger.APP_USAGE);
        broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, appIdentifier);
        context.startService(broadcastTriggerServiceIntent);
      }


    };
    (new Thread(runnable)).start();

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
