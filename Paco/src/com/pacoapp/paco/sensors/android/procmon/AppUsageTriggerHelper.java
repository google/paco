package com.pacoapp.paco.sensors.android.procmon;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;

public class AppUsageTriggerHelper {
  private ExperimentProviderUtil eu;

  public AppUsageTriggerHelper(ExperimentProviderUtil experimentProviderUtil) {
    super();
    this.eu = experimentProviderUtil;
  }


  public List<String> getAppStartTasksToWatch() {
    List<String> tasks = Lists.newArrayList();
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


  public List<String> getAppCloseTasksToWatch() {
    List<String> tasks = Lists.newArrayList();
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

  public List<Experiment> initializeExperimentsWatchingAppUsage() {
    List<Experiment> joined = eu.getJoinedExperiments();
    List<Experiment> experimentsNeedingEvent = Lists.newArrayList();
    DateTime now = DateTime.now();
    for (Experiment experiment2 : joined) {
      if (!ActionScheduleGenerator.isOver(now, experiment2.getExperimentDAO())
              && ExperimentHelper.isLogActions(experiment2.getExperimentDAO())) {
        experimentsNeedingEvent.add(experiment2);
      }
    }
    return experimentsNeedingEvent;
  }




}
