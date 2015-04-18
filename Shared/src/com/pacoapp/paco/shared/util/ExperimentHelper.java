package com.pacoapp.paco.shared.util;

import java.util.ArrayList;
import java.util.List;

import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;

public class ExperimentHelper {

  public static Input2 getInputWithName(ExperimentDAO experiment, String name, String groupName) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    List<Input2> inputs = null;
    if (groupName == null || groupName.isEmpty()) {
      inputs = getInputs(experiment);
    } else {
      ExperimentGroup group = experiment.getGroupByName(groupName);
      if (group != null) {
        inputs = group.getInputs();
      }
    }
    if (inputs != null) {
      for (Input2 input : inputs) {
        if (name.equals(input.getName())) {
          return input;
        }
      }
    }
    return null;
  }

  public static List<Input2> getInputs(ExperimentDAO experiment) {
    List<Input2> inputs = new java.util.ArrayList<Input2>();
    for (ExperimentGroup group : experiment.getGroups()) {
      inputs.addAll(group.getInputs());
    }
    return inputs;
  }

  public static boolean declaresLogAppUsageAndBrowserCollection(ExperimentDAO experiment) {
    return experiment.getExtraDataCollectionDeclarations() != null
            && experiment.getExtraDataCollectionDeclarations().contains(ExperimentDAO.APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION);
  }

  public static boolean hasUserEditableSchedule(ExperimentDAO experiment) {
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger.getUserEditable() && actionTrigger instanceof ScheduleTrigger) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean hasAppUsageTrigger(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger)actionTrigger;
          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            if (interruptCue.getCueCode() == InterruptCue.APP_USAGE ||
                    interruptCue.getCueCode() == InterruptCue.APP_CLOSED) {
              return true;
            }
          }

        }
      }
    }
    return false;
  }

  public static boolean hasAppClosedTrigger(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger)actionTrigger;
          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            if (interruptCue.getCueCode() == InterruptCue.APP_CLOSED) {
              return true;
            }
          }

        }
      }
    }
    return false;
  }



  public static boolean isLogActions(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (experimentGroup.getLogActions()) {
        return true;
      }
    }
    return false;
  }

  public static boolean shouldWatchProcesses(ExperimentDAO experiment) {
    return hasAppUsageTrigger(experiment) || isLogActions(experiment);
  }

  public static class Pair<S, T> {
    public S first;
    public T second;

    public Pair(S first, T second) {
      super();
      this.first = first;
      this.second = second;
    }


  }

  public static List<Pair<ExperimentGroup, InterruptTrigger>> shouldTriggerBy(ExperimentDAO experiment, int event, String sourceIdentifier) {
    List<Pair<ExperimentGroup, InterruptTrigger>> groupsThatTrigger = new ArrayList();
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger) actionTrigger;
          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            boolean cueCodeMatches = interruptCue.getCueCode() == event;
            if (!cueCodeMatches) {
              continue;
            }

            boolean usesSourceId = interruptCue.getCueCode() == InterruptCue.PACO_ACTION_EVENT || interruptCue.getCueCode() == InterruptCue.APP_USAGE;
            boolean sourceIdsMatch;
            boolean triggerSourceIdIsEmpty = interruptCue.getCueSource() == null || interruptCue.getCueSource().isEmpty() ;
            if (usesSourceId) {
              boolean paramEmpty = sourceIdentifier == null || sourceIdentifier.isEmpty();
              sourceIdsMatch = (paramEmpty && triggerSourceIdIsEmpty) ||
                interruptCue.getCueSource().equals(sourceIdentifier);
            } else {
              sourceIdsMatch = true;
            }
            if (cueCodeMatches && sourceIdsMatch) {
              groupsThatTrigger.add(new Pair(experimentGroup, trigger));
            }
          }
        }
      }
    }
    return groupsThatTrigger;
  }

  public static boolean isAnyGroupOngoingDuration(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration()) {
        return true;
      }
    }
    return false;
  }

  public static List<ExperimentGroup> isBackgroundListeningForSourceId(ExperimentDAO experiment, String sourceIdentifier) {
    List<ExperimentGroup> listeningExperimentGroups  = new ArrayList();
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (experimentGroup.getBackgroundListen() && experimentGroup.getBackgroundListenSourceIdentifier().equals(sourceIdentifier)) {
        listeningExperimentGroups.add(experimentGroup);
      }
    }
    return listeningExperimentGroups;
  }

}
