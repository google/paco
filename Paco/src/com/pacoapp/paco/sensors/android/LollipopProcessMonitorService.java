package com.pacoapp.paco.sensors.android;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

// TODO refactor this to compute increased foreground time for an app from it's previous foreground time.

@SuppressLint("NewApi")
public class LollipopProcessMonitorService extends Service {

  private ExperimentProviderUtil experimentProviderUtil;
  private List<Experiment> experimentsNeedingEvent;
  protected boolean running;
  private UsageStatsManager am;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (running) {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart() -- Already running");
      return;
    } else {
      Log.i(PacoConstants.TAG, "Paco App Usage Poller.onStart()");

      am = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
      if (!canGetStats(am)) {
        Log.i(PacoConstants.TAG, "no access to Usage Stats. Please turn on setting.");
      }

      final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                      "Paco App Usage Poller Service wakelock");
      wl.acquire();

      experimentProviderUtil = new ExperimentProviderUtil(getApplicationContext());
      experimentsNeedingEvent = initializeExperimentsWatchingAppUsage();

      Runnable runnable = new Runnable() {

        public void run() {
          running = true;
          List<String> tasksOfInterest = initializeAppStartedTasksToWatch();
          Map<String, UsageStats> previousStats = Maps.newHashMap();
          List<String> tasksOfInterestForClosing = initializeCloseAppTasksToWatch();

          boolean inBrowser = BroadcastTriggerReceiver.isInBrowser(getApplicationContext());

          try {
            while (pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              synchronized (this) {
                try {
                  Log.i(PacoConstants.TAG, "polling on: runnable instance = " + this.toString());
                  List<UsageStats> recentUsageStats = getRecentStats();

                  List<UsageStats> newlyUsedApps = checkForNewlyUsedApps(previousStats, tasksOfInterest,
                                                                       recentUsageStats);
                  if (newlyUsedApps.size() > 0 && isBrowserTask(newlyUsedApps)
                      && BroadcastTriggerReceiver.shouldLogActions(getApplicationContext())) {
                    BroadcastTriggerReceiver.createBrowserHistoryStartSnapshot(getApplicationContext());
                    BroadcastTriggerReceiver.toggleInBrowser(getApplicationContext(), true);
                    inBrowser = true;
                  } else if (inBrowser == true && newlyUsedApps.size() > 0 && !isBrowserTask(newlyUsedApps)) {
                    inBrowser = false;
                    BroadcastTriggerReceiver.toggleInBrowser(getApplicationContext(), false);
                    BroadcastTriggerReceiver.createBrowserHistoryEndSnapshot(getApplicationContext());
                  }

                  if (newlyUsedApps.size() > 0) {
                    createTriggersForNewlyUsedTasksOfInterest(tasksOfInterest, newlyUsedApps);
                    markNewlyUsedTaskToWatchForClosing(tasksOfInterestForClosing, newlyUsedApps);
                    createTriggerForNewlyStoppedTaskOfInterest(newlyUsedApps);

                  }

                  if (BroadcastTriggerReceiver.shouldLogActions(getApplicationContext())) {
                    logProcessesUsedSinceLastPolling(newlyUsedApps);
                  }

                  for (UsageStats recentStat : recentUsageStats) {
                    previousStats.put(recentStat.getPackageName(), recentStat);
                  }

                  int sleepTime = BroadcastTriggerReceiver.getFrequency(LollipopProcessMonitorService.this) * 1000;
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
            previousStats = null;
            wl.release();
            stopSelf();
            running = false;
          }
        }

        private void markNewlyUsedTaskToWatchForClosing(List<String> tasksOfInterestForClosing,
                                                         List<UsageStats> newlyUsedApps) {

          List<String> appsOpenedToWatchForClosing = Lists.newArrayList();

          for (UsageStats usageStats : newlyUsedApps) {
            if (tasksOfInterestForClosing.contains(usageStats.getPackageName())) {
              appsOpenedToWatchForClosing.add(usageStats.getPackageName());
            }
          }
          if (!appsOpenedToWatchForClosing.isEmpty()) {
            BroadcastTriggerReceiver.setAppToWatchStarted(getApplicationContext(), Joiner.on(",").join(appsOpenedToWatchForClosing));
          }
        }

        private boolean isBrowserTask(List<UsageStats> topTasks) {
          for (UsageStats usageStats : topTasks) {
            String packageName = usageStats.getPackageName();
            if (packageName.startsWith("com.android.browser")
                 || packageName.startsWith("com.android.chrome")
                 || packageName.startsWith("org.mozilla.firefox")) {
              return true;
            }
          }
          return false;
        }

        private void createTriggersForNewlyUsedTasksOfInterest(List<String> tasksOfInterest, List<UsageStats> newlyUsedApps) {
          List<String> tasksToSendTrigger = Lists.newArrayList();
          for (int i = 0; i < newlyUsedApps.size(); i++) {
            String taskName = newlyUsedApps.get(i).getPackageName();
            if (tasksOfInterest.contains(taskName)) {
              tasksToSendTrigger.add(taskName);
            }
          }

          for (String taskName : tasksToSendTrigger) {
            triggerAppUsed(taskName);
          }

        }

        private void createTriggerForNewlyStoppedTaskOfInterest(List<UsageStats> newlyUsedApps) {
          List<String> newAppNames = Lists.newArrayList();
          for (UsageStats usageStats : newlyUsedApps) {
            newAppNames.add(usageStats.getPackageName());
          }
          String appsStarted = BroadcastTriggerReceiver.getAppToWatch(getApplicationContext());
          if (appsStarted != null) {
            Iterable<String> appsStartedList = Splitter.on(",").split(appsStarted);
            for (String appStarted : appsStartedList) {
              if (!newAppNames.contains(appsStarted)) {
             // TODO not that this is flushing all of them, in some world, not this one,
                // it is possible that we should just remove the one that is no longer new.
                BroadcastTriggerReceiver.unsetAppToWatchStarted(getApplicationContext());
                triggerAppClosed(appStarted);
              }
            }

          }
        }


        protected List<UsageStats> checkForNewlyUsedApps(Map<String, UsageStats> previousStats,
                                                     List<String> tasksOfInterest,
                                                     List<UsageStats> recentStats) {
          if (previousStats == null) {
            return Collections.EMPTY_LIST; // skip first run as we need a
                                           // previous run to compare against
          }

          List<UsageStats> newlyUsedTaskNames = Lists.newArrayList();
          for (int i = 0; i < recentStats.size(); i++) {
            UsageStats recentStat = recentStats.get(i);
            UsageStats previousStat = previousStats.get(recentStat.getPackageName());
            if (previousStat == null || previousStat.getLastTimeUsed() < recentStat.getLastTimeUsed()) {
              newlyUsedTaskNames.add(recentStat);
            }
          }
          return newlyUsedTaskNames;
        }

        private List<UsageStats> getRecentStats() {
          long endTime = Calendar.getInstance().getTimeInMillis();
          // NOTE, we look back 4 seconds to capture anything that just happened.
          List<UsageStats> ls = am.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, endTime - 4000, endTime);
//
//          if (ls != null) {
//            Collections.sort(ls, new Comparator<UsageStats>() {
//
//              @Override
//              public int compare(UsageStats lhs, UsageStats rhs) {
//                long lhsLastTimeUsed = lhs.getLastTimeUsed();
//                long rhsLastTimeUsed = rhs.getLastTimeUsed();
//                if (lhsLastTimeUsed == rhsLastTimeUsed) {
//                  return 0;
//                } else if (rhsLastTimeUsed < lhsLastTimeUsed) {
//                  return 1;
//                } else {
//                  return -1;
//                }
//              }
//            });
//          }
//
          return ls;
        }

        //        private List<String> XXXgetRecentTaskNames() {
//          List<RecentTaskInfo> recentTasks = am.getRecentTasks(30, 0);
//          List<String> recentTaskNames = Lists.newArrayList();
//
//          for (int i = 0; i < recentTasks.size(); i++) {
//            RecentTaskInfo recentTaskInfo = recentTasks.get(i);
//            String taskName = recentTaskInfo.baseIntent.getComponent().flattenToString();
//            recentTaskNames.add(taskName);
//          }
//          return recentTaskNames;
//        }

        private List<String> initializeAppStartedTasksToWatch() {
          List<String> tasks = Lists.newArrayList();
          ExperimentProviderUtil eu = new ExperimentProviderUtil(LollipopProcessMonitorService.this);
          DateTime now = new DateTime();
          List<Experiment> joined = eu.getJoinedExperiments();
          for (Experiment experiment : joined) {
            if (!ActionScheduleGenerator.isOver(now, experiment.getExperimentDAO())) {
              List<ExperimentGroup> experimentGroups = experiment.getExperimentDAO().getGroups();
              for (ExperimentGroup experimentGroup : experimentGroups) {
                List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
                for (ActionTrigger actionTrigger : actionTriggers) {
                  if (actionTrigger instanceof InterruptTrigger) {
                    InterruptTrigger interrupt = (InterruptTrigger) actionTrigger;
                    List<InterruptCue> cues = interrupt.getCues();
                    for (InterruptCue interruptCue : cues) {
                      if (interruptCue.getCueCode() == InterruptCue.APP_USAGE) {
                        tasks.add(interruptCue.getCueSource());
                      }
                    }
                  }
                }
              }
            }
          }
          return tasks;
        }


        private List<String> initializeCloseAppTasksToWatch() {
          List<String> tasks = Lists.newArrayList();
          ExperimentProviderUtil eu = new ExperimentProviderUtil(LollipopProcessMonitorService.this);
          DateTime now = new DateTime();
          List<Experiment> joined = eu.getJoinedExperiments();
          for (Experiment experiment : joined) {
            if (!ActionScheduleGenerator.isOver(now, experiment.getExperimentDAO())) {
              List<ExperimentGroup> experimentGroups = experiment.getExperimentDAO().getGroups();
              for (ExperimentGroup experimentGroup : experimentGroups) {
                List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
                for (ActionTrigger actionTrigger : actionTriggers) {
                  if (actionTrigger instanceof InterruptTrigger) {
                    InterruptTrigger interrupt = (InterruptTrigger) actionTrigger;
                    List<InterruptCue> cues = interrupt.getCues();
                    for (InterruptCue interruptCue : cues) {
                      if (interruptCue.getCueCode() == InterruptCue.APP_CLOSED) {
                        tasks.add(interruptCue.getCueSource());
                      }
                    }
                  }
                }
              }
            }
          }
          return tasks;
        }

        private void triggerAppUsed(String appIdentifier) {
          Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
          triggerCodeForAppTrigger(appIdentifier, InterruptCue.APP_USAGE);
        }

        private void triggerAppClosed(String appIdentifier) {
            Log.i(PacoConstants.TAG, "Paco App Usage poller trigger app used: " + appIdentifier);
            triggerCodeForAppTrigger(appIdentifier, InterruptCue.APP_CLOSED);
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

  private boolean canGetStats(UsageStatsManager am2) {
    Calendar calendar = Calendar.getInstance();
    long endTime = calendar.getTimeInMillis();
    calendar.add(Calendar.YEAR, -1);
    long startTime = calendar.getTimeInMillis();


    List<UsageStats> usageStatsList = am2.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,startTime,endTime);
    return !usageStatsList.isEmpty();
  }

  // create PacoEvent for list of apps in mru order
  protected void logProcessesUsedSinceLastPolling(List<UsageStats> newlyUsedApps) {
    if (newlyUsedApps.isEmpty()) {
      return;
    }
    List<String> packageNames = Lists.newArrayList();
    for (UsageStats usageStats : newlyUsedApps) {
      packageNames.add(usageStats.getPackageName());
    }
    List<String> prettyAppNames = getNamesForApps(newlyUsedApps);
    String usedAppsPrettyNamesString = Joiner.on(",").join(prettyAppNames);
    String usedAppsNamesString = Joiner.on(",").join(packageNames);
    for (Experiment experiment : experimentsNeedingEvent) {
      Event event = createAppsUsedPacoEvent(usedAppsPrettyNamesString, usedAppsNamesString, experiment);
      experimentProviderUtil.insertEvent(event);
    }

  }

  private List<String> getNamesForApps(List<UsageStats> newlyUsedApps) {
    List<String> appNames = Lists.newArrayList();
    PackageManager pm = getApplicationContext().getPackageManager();
    for (UsageStats stat : newlyUsedApps) {
      String activityName = stat.getPackageName();
      ApplicationInfo info = null;
      try {
        info = pm.getApplicationInfo(activityName, 0);
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

  private Event createAppsUsedPacoEvent(String usedAppsPrettyNamesString, String usedAppsTaskNamesString,
                                        Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
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
      if (!ActionScheduleGenerator.isOver(now, experiment2.getExperimentDAO()) && ExperimentHelper.isLogActions(experiment2.getExperimentDAO())) {
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
