package com.google.android.apps.paco;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.TriggerDAO;

public class ProcessService extends Service {

  private ActivityManager am;
  private ExperimentProviderUtil experimentProviderUtil;
  private List<Experiment> experimentsNeedingEvent;
  protected boolean running;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (running) {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart() -- Already running");
      return;
    } else {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart()");

      am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

      final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                      "Paco App Usage Poller Service wakelock");
      wl.acquire();

      experimentProviderUtil = new ExperimentProviderUtil(getApplicationContext());
      experimentsNeedingEvent = initializeExperimentsWatchingAppUsage();

      Runnable runnable = new Runnable() {

        public void run() {
          running = true;
          List<String> tasksOfInterest = initializeTasksToWatch();
          List<String> previousTaskNames = null;          
          List<String> tasksOfInterestForClosing = initializeCloseTriggerTasksToWatch();          
          
          boolean inBrowser = BroadcastTriggerReceiver.isInBrowser(getApplicationContext());

          try {
            while (pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              synchronized (this) {
                try {
                  Log.i(PacoConstants.TAG, "polling on: runnable instance = " + this.toString());
                  List<String> recentTaskNames = getRecentTaskNames();

                  List<String> newlyUsedTasks = checkForNewlyUsedTasks(previousTaskNames, tasksOfInterest,
                                                                       recentTaskNames);
                  if (newlyUsedTasks.size() > 0 && isBrowserTask(newlyUsedTasks.get(0))
                      && BroadcastTriggerReceiver.shouldLogActions(getApplicationContext())) {
                    BroadcastTriggerReceiver.createBrowserHistoryStartSnapshot(getApplicationContext());
                    BroadcastTriggerReceiver.toggleInBrowser(getApplicationContext(), true);
                    inBrowser = true;
                  } else if (inBrowser == true && newlyUsedTasks.size() > 0 && !isBrowserTask(newlyUsedTasks.get(0))) {
                    inBrowser = false;
                    BroadcastTriggerReceiver.toggleInBrowser(getApplicationContext(), false);
                    BroadcastTriggerReceiver.createBrowserHistoryEndSnapshot(getApplicationContext());
                  }
                  
                  if (newlyUsedTasks.size() > 0) {
                    createTriggersForNewlyUsedTasksOfInterest(tasksOfInterest, newlyUsedTasks);
                    markNewlyUsedTaskToWatchForClosing(tasksOfInterestForClosing, newlyUsedTasks.get(0));
                    createTriggerForNewlyStoppedTaskOfInterest(newlyUsedTasks.get(0));
                    
                  }
                  
                  if (BroadcastTriggerReceiver.shouldLogActions(getApplicationContext())) {
                    logProcessesUsedSinceLastPolling(newlyUsedTasks);
                  }

                  previousTaskNames = recentTaskNames;
                  int sleepTime = BroadcastTriggerReceiver.getFrequency(ProcessService.this) * 1000;
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
            Log.i(PacoConstants.TAG, "polling stopping: instance = " + ProcessService.this.toString());
          } finally {
            previousTaskNames = null;
            wl.release();
            stopSelf();
            running = false;
          }
        }

        private void markNewlyUsedTaskToWatchForClosing(List<String> tasksOfInterestForClosing,
                                                         String mruTask) {
          if (tasksOfInterestForClosing.contains(mruTask)) {
            BroadcastTriggerReceiver.setAppToWatchStarted(getApplicationContext(), mruTask);
          }
                   
        }

        private boolean isBrowserTask(String topTask) {
          return topTask.equals("com.android.browser/com.android.browser.BrowserActivity")
                 || topTask.startsWith("com.android.chrome/") || topTask.startsWith("org.mozilla.firefox/");
          // TODO add more browser process names, e.g., Chrome, Firefox, vendor
          // specific?
        }

        private void createTriggersForNewlyUsedTasksOfInterest(List<String> tasksOfInterest, List<String> newlyUsedTasks) {
          List<String> tasksToSendTrigger = Lists.newArrayList();
          for (int i = 0; i < newlyUsedTasks.size(); i++) {
            String taskName = newlyUsedTasks.get(i);
            if (tasksOfInterest.contains(taskName)) {
              tasksToSendTrigger.add(taskName);
            }
          }
          
          for (String taskName : tasksToSendTrigger) {
            triggerAppUsed(taskName); 
          }
          
        }

        private void createTriggerForNewlyStoppedTaskOfInterest(String mruTask) {         
          String appStarted = BroadcastTriggerReceiver.getAppToWatch(getApplicationContext());
          if (appStarted != null && !mruTask.equals(appStarted)) {
            BroadcastTriggerReceiver.unsetAppToWatchStarted(getApplicationContext());
            triggerAppClosed(appStarted);
          }
        }


        protected List<String> checkForNewlyUsedTasks(List<String> lastTaskNames, List<String> tasksOfInterest,
                                                      List<String> recentTaskNames) {
          if (lastTaskNames == null) {
            return Collections.EMPTY_LIST; // skip first run as we need a
                                           // previous run to compare against
          }

          List<String> newlyUsedTasks = Lists.newArrayList();
          for (int i = 0; i < recentTaskNames.size(); i++) {
            String taskName = recentTaskNames.get(i);
            int indexOfTaskNameInLastTaskNames = lastTaskNames.indexOf(taskName);
            if (indexOfTaskNameInLastTaskNames == -1 || i < indexOfTaskNameInLastTaskNames) {
              newlyUsedTasks.add(taskName);
            }
          }
          return newlyUsedTasks;
        }

        private List<String> getRecentTaskNames() {
          List<RecentTaskInfo> recentTasks = am.getRecentTasks(30, 0);
          List<String> recentTaskNames = Lists.newArrayList();

          for (int i = 0; i < recentTasks.size(); i++) {
            RecentTaskInfo recentTaskInfo = recentTasks.get(i);
            String taskName = recentTaskInfo.baseIntent.getComponent().flattenToString();
            recentTaskNames.add(taskName);
          }
          return recentTaskNames;
        }

        private List<String> initializeTasksToWatch() {
          List<String> tasks = Lists.newArrayList();
          ExperimentProviderUtil eu = new ExperimentProviderUtil(ProcessService.this);
          DateTime now = new DateTime();
          List<Experiment> joined = eu.getJoinedExperiments();
          for (Experiment experiment : joined) {
            if (!experiment.isOver(now) && experiment.hasAppUsageTrigger()) {
              Trigger trigger = (Trigger) experiment.getSignalingMechanisms().get(0);
              if (trigger != null) {
                tasks.add(trigger.getSourceIdentifier());
              }
            }
          }
          return tasks;
        }
        
        private List<String> initializeCloseTriggerTasksToWatch() {
          List<String> tasks = Lists.newArrayList();
          ExperimentProviderUtil eu = new ExperimentProviderUtil(ProcessService.this);
          DateTime now = new DateTime();
          List<Experiment> joined = eu.getJoinedExperiments();
          for (Experiment experiment : joined) {
            if (!experiment.isOver(now) && experiment.hasAppCloseTrigger()) {
              Trigger trigger = (Trigger) experiment.getSignalingMechanisms().get(0);
              if (trigger != null) {
                tasks.add(trigger.getSourceIdentifier());
              }
            }
          }
          return tasks;
        }

        private void triggerAppUsed(String appIdentifier) {
          Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
          triggerCodeForAppTrigger(appIdentifier, TriggerDAO.APP_USAGE);
        }
        
        private void triggerAppClosed(String appIdentifier) {
            Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
            triggerCodeForAppTrigger(appIdentifier, TriggerDAO.APP_CLOSED);
          }

		private void triggerCodeForAppTrigger(String appIdentifier,
				int triggerCode) {
			Context context = getApplicationContext();
			  Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
			  broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME,
			                                         DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
			  
			broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerCode);
			  broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, appIdentifier);
			  context.startService(broadcastTriggerServiceIntent);
		}

      };
      (new Thread(runnable)).start();
    }
  }

  // create PacoEvent for list of apps in mru order
  protected void logProcessesUsedSinceLastPolling(List<String> newlyUsedTasks) {
    if (newlyUsedTasks.isEmpty()) {
      return;
    }

    List<String> prettyAppNames = getNamesForApps(newlyUsedTasks);
    String usedAppsPrettyNamesString = Joiner.on(",").join(prettyAppNames);
    String usedAppsNamesString = Joiner.on(",").join(newlyUsedTasks);
    for (Experiment experiment : experimentsNeedingEvent) {
      Event event = createAppsUsedPacoEvent(usedAppsPrettyNamesString, usedAppsNamesString, experiment);
      experimentProviderUtil.insertEvent(event);
    }

  }

  private List<String> getNamesForApps(List<String> newlyUsedTasks) {
    List<String> appNames = Lists.newArrayList();
    PackageManager pm = getApplicationContext().getPackageManager();
    for (String activityName : newlyUsedTasks) {
      ApplicationInfo info = null;
      try {
        info = pm.getApplicationInfo(getPackageFromActivity(activityName), 0);
        String appName = pm.getApplicationLabel(info).toString();
        if (Strings.isNullOrEmpty(appName)) {
          appName = activityName;
        } else if (appName.equals("Google Search")) {
          String[] parts = appName.split(".");
          if (parts.length > 0) {
            String simpleActivityName = parts[parts.length - 1];
            if (simpleActivityName.equals("GEL")) {
              appName = "Launcher";
            }
          }
        }
        if (appName.equals("Launcher")) {
          appName = "Home";
        }
        appNames.add(appName);
      } catch (final NameNotFoundException e) {
        appNames.add(activityName);
      }

    }
    return appNames;
  }

  private String getPackageFromActivity(String activityName) {
    int slashIndex = activityName.indexOf("/");
    if (slashIndex != -1) {
      return activityName.substring(0, slashIndex);
    } else {
      return activityName;
    }
  }

  private Event createAppsUsedPacoEvent(String usedAppsPrettyNamesString, String usedAppsTaskNamesString,
                                        Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    event.setExperimentVersion(experiment.getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer(usedAppsPrettyNamesString);
    responseForInput.setName("apps_used");
    event.addResponse(responseForInput);

    Output usedAppsNamesResponse = new Output();
    usedAppsNamesResponse.setAnswer(usedAppsTaskNamesString);
    usedAppsNamesResponse.setName("apps_used_raw");
    event.addResponse(usedAppsNamesResponse);
    return event;
  }

  private List<Experiment> initializeExperimentsWatchingAppUsage() {
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();
    List<Experiment> experimentsNeedingEvent = Lists.newArrayList();
    DateTime now = DateTime.now();
    for (Experiment experiment2 : joined) {
      if (!experiment2.isOver(now) && experiment2.isLogActions()) {
        experimentsNeedingEvent.add(experiment2);
      }
    }
    return experimentsNeedingEvent;
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
