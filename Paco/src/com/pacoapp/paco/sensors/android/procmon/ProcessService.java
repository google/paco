package com.pacoapp.paco.sensors.android.procmon;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.sensors.android.BroadcastTriggerService;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

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

public class ProcessService extends Service {

  private static Logger Log = LoggerFactory.getLogger(ProcessService.class);

  private ActivityManager am;
  private ExperimentProviderUtil experimentProviderUtil;
  private List<Experiment> experimentsNeedingEvent;
  protected boolean running;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (running) {
      Log.info("Paco App Usage Poller.onStart() -- Already running");
      return;
    } else {
      Log.info("Paco App Usage Poller.onStart()");

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
          List<String> tasksOfInterest = initializeAppStartedTasksToWatch();
          List<String> previousTaskNames = null;
          List<String> tasksOfInterestForClosing = initializeCloseAppTasksToWatch();

          try {
            while (pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              synchronized (this) {
                try {
                  //Log.info("polling on: runnable instance = " + this.toString());
                  List<String> recentTaskNames = getRecentTaskNames();

                  List<String> newlyUsedTasks = checkForNewlyUsedTasks(previousTaskNames, tasksOfInterest,
                                                                       recentTaskNames);

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
                  //Log.info("sleepTime = " + sleepTime);
                  wait(sleepTime);
                } catch (Exception e) {
                }
              }
            }
            if (!pm.isScreenOn() && BroadcastTriggerReceiver.shouldWatchProcesses(getApplicationContext())) {
              createScreenOffPacoEvents(getApplicationContext());
            }
            //
            Log.info("polling stopping: instance = " + ProcessService.this.toString());
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

        private List<String> initializeAppStartedTasksToWatch() {
          List<String> tasks = Lists.newArrayList();
          ExperimentProviderUtil eu = new ExperimentProviderUtil(ProcessService.this);
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
          ExperimentProviderUtil eu = new ExperimentProviderUtil(ProcessService.this);
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
          Log.info("Paco App Usage poller trigger app used: " + appIdentifier);
          triggerCodeForAppTrigger(appIdentifier, InterruptCue.APP_USAGE);
        }

        private void triggerAppClosed(String appIdentifier) {
            Log.info("Paco App Usage poller trigger app used: " + appIdentifier);
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

  // create PacoEvent for list of apps in mru order
  protected void logProcessesUsedSinceLastPolling(List<String> newlyUsedTasks) {
    if (newlyUsedTasks.isEmpty()) {
      return;
    }

    List<String> prettyAppNames = getNamesForApps(newlyUsedTasks);
    String usedAppsPrettyNamesString = Joiner.on(",").join(prettyAppNames);
    String usedAppsNamesString = Joiner.on(",").join(newlyUsedTasks);
    for (Experiment experiment : experimentsNeedingEvent) {
      List<ExperimentGroup> groupsThatCare = ExperimentHelper.getGroupsThatCareAboutActionLogging(experiment.getExperimentDAO());
      for (ExperimentGroup experimentGroup : groupsThatCare) {
        Event event = createAppsUsedPacoEvent(usedAppsPrettyNamesString, usedAppsNamesString, experiment, experimentGroup);
        experimentProviderUtil.insertEvent(event);
      }
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
                                        Experiment experiment, ExperimentGroup experimentGroup) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    if (experimentGroup != null) {
      event.setExperimentGroupName(experimentGroup.getName());
    }
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
    Log.info("Paco App Usage poller.onDestroy()");
  }

  protected void createScreenOffPacoEvents(Context context) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();

    for (Experiment experiment : joined) {
      List<ExperimentGroup> groupsThatCare = ExperimentHelper.getGroupsThatCareAboutActionLogging(experiment.getExperimentDAO());
      for (ExperimentGroup experimentGroup : groupsThatCare) {
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

}
