package com.pacoapp.paco.shared.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;

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

  public static boolean declaresInstalledAppDataCollection(ExperimentDAO experiment) {
    return experiment.getExtraDataCollectionDeclarations() != null
            && experiment.getExtraDataCollectionDeclarations().contains(ExperimentDAO.APP_INSTALL_DATA_COLLECTION);
  }

  /**
   * Returns whether the experiment has the accessibility logging flag set by the experiment
   * organiser.
   * @param experiment The running experiment
   * @return Whether accessibility logging is enabled for the experiment.
   */
  public static boolean declaresAccessibilityLogging(ExperimentDAO experiment) {
    return experiment.getExtraDataCollectionDeclarations() != null
            && experiment.getExtraDataCollectionDeclarations().contains(ExperimentDAO.ACCESSIBILITY_LOGGING);
  }

  public static boolean hasUserEditableSchedule(ExperimentDAO experiment) {
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : triggers) {
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
            List<Schedule> schedules = scheduleTrigger.getSchedules();
            for (Schedule schedule : schedules) {
              if (schedule.getUserEditable()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  public static boolean hasAppUsageTrigger(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if ((GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) || (!ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup))) {
        continue;
      } else {
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
    }
    return false;
  }

  public static boolean hasAppClosedTrigger(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType()) || !ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup)) {
        continue;
      } else {
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
    }
    return false;
  }

  public static boolean isLogPhoneOnOff(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && experimentGroup.getLogShutdown()) {
          return true;
        }
      }
    }
    return false;
  }


  public static boolean isLogActions(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && experimentGroup.getLogActions()) {
          return true;
        }
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

  public static class Trio<S, T, U> {
    public S first;
    public T second;
    public U third;

    public Trio(S first, T second, U third) {
      super();
      this.first = first;
      this.second = second;
      this.third = third;
    }
  }


  @SuppressWarnings("unchecked")
  public static List<Trio<ExperimentGroup, InterruptTrigger, InterruptCue>> shouldTriggerBy(ExperimentDAO experiment,
                                                                                            int event,
                                                                                            String sourceIdentifier,
                                                                                            String packageName,
                                                                                            String className,
                                                                                            String eventText,
                                                                                            String eventContentDescription) {
    List<Trio<ExperimentGroup, InterruptTrigger, InterruptCue>> groupsThatTrigger = new ArrayList();
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType()) || !ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup)) {
        continue;
      }

      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger) actionTrigger;
          if (!withinTriggerTimeWindow(trigger)) {
            continue;
          }

          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            boolean cueCodeMatches = interruptCue.getCueCode() == event;
            if (!cueCodeMatches) {
              continue;
            }

            boolean usesSourceId = interruptUsesSourceId(interruptCue);
            boolean cueFiltersMatch;
            boolean isExperimentTrigger = isExperimentEventTrigger(interruptCue);

            boolean triggerSourceIdIsEmpty = interruptCue.getCueSource() == null || interruptCue.getCueSource().isEmpty() ;
            if (usesSourceId) {
              if (isAccessibilityRelatedCueCodeAndMatchesPatterns(interruptCue.getCueCode())) {
                cueFiltersMatch = isMatchingAccessibilitySource(packageName, className, eventContentDescription, eventText, interruptCue);
              } else {
                boolean paramEmpty = sourceIdentifier == null || sourceIdentifier.isEmpty();
                cueFiltersMatch = (paramEmpty && triggerSourceIdIsEmpty) ||
                        interruptCue.getCueSource().equals(sourceIdentifier);
              }
            } else if (isExperimentTrigger) {
              boolean paramExists = sourceIdentifier != null && !sourceIdentifier.isEmpty();
              boolean sameExperiment = Long.parseLong(sourceIdentifier) == experiment.getId();
              cueFiltersMatch = paramExists == true && sameExperiment == true;
            } else {
              cueFiltersMatch = true;
            }
            if (cueCodeMatches && cueFiltersMatch) {
              groupsThatTrigger.add(new Trio<ExperimentGroup, InterruptTrigger, InterruptCue>(experimentGroup, trigger, interruptCue));
            }
          }
        }
      }
    }
    return groupsThatTrigger;
  }

  private static boolean withinTriggerTimeWindow(InterruptTrigger trigger) {
    if (!trigger.getTimeWindow()) {
      return true;
    }
    if (!trigger.getWeekends()) {
      int dow = DateTime.now().getDayOfWeek();
      if (dow == DateTimeConstants.SATURDAY || dow == DateTimeConstants.SUNDAY) {
        return false;
      }
    }
    int startTime = trigger.getStartTimeMillis();
    int endTime = trigger.getEndTimeMillis();
    int todayMillis = new DateTime().getMillisOfDay();
    return todayMillis >= startTime && todayMillis < endTime;
  }

  private static boolean isExperimentEventTrigger(InterruptCue interruptCue) {
    return interruptCue.getCueCode() == InterruptCue.PACO_EXPERIMENT_JOINED_EVENT
            || interruptCue.getCueCode() == InterruptCue.PACO_EXPERIMENT_ENDED_EVENT
            || interruptCue.getCueCode() == InterruptCue.PACO_EXPERIMENT_RESPONSE_RECEIVED_EVENT;
  }

  private static boolean interruptUsesSourceId(InterruptCue interruptCue) {
    return interruptCue.getCueCode() == InterruptCue.PACO_ACTION_EVENT
            || interruptCue.getCueCode() == InterruptCue.APP_USAGE
            || interruptCue.getCueCode() == InterruptCue.APP_CLOSED
            || interruptCue.getCueCode() == InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_CREATED
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_TRAY_SWIPE_DISMISS
            || interruptCue.getCueCode() == InterruptCue.NOTIFICATION_CLICKED
            ;
  }

  private static boolean isMatchingAccessibilitySource(String packageName, String className, String eventContentDescription,
                                                       String eventText, InterruptCue interruptCue) {
    if (!Strings.isNullOrEmpty(interruptCue.getCueSource())) {
      if (packageName == null || !interruptCue.getCueSource().equals(packageName)) {
        return false;
      }
    }
    if (!Strings.isNullOrEmpty(interruptCue.getCueAEContentDescription())) {
      if ((eventContentDescription == null || !interruptCue.getCueAEContentDescription().equals(eventContentDescription)) &&
              (eventText == null || !interruptCue.getCueAEContentDescription().equals(eventText))) {
        return false;
      }
    }
    if (!Strings.isNullOrEmpty(interruptCue.getCueAEClassName())) {
      if (className == null || !interruptCue.getCueAEClassName().equals(className)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isAnyGroupOngoingDuration(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType()) ) {
        continue;
      } else {
        if (!experimentGroup.getFixedDuration()) {
          return true;
        }
      }
    }
    return false;
  }

  public static List<ExperimentGroup> isBackgroundListeningForSourceId(ExperimentDAO experiment, String sourceIdentifier) {
    List<ExperimentGroup> listeningExperimentGroups  = new ArrayList();
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (experimentGroup.getBackgroundListen()) {
        String sourceFilter = experimentGroup.getBackgroundListenSourceIdentifier();
        if (sourceFilter.equals("*") || sourceFilter.equals(sourceIdentifier)) {
          listeningExperimentGroups.add(experimentGroup);
        }
      }
    }
    return listeningExperimentGroups;
  }

  /**
   * Returns all experiment groups listening for accessibility events. If Paco gets extended to
   * capture multiple accessibility events (apart from just permission events), this method could
   * be extended to include a source identifier.
   * @param experiment The experiment for which to get matching experiment groups
   * @return A list of experiment groups listening for accessibility events
   */
  public static List<ExperimentGroup> isListeningForAccessibilityEvents(ExperimentDAO experiment) {
    List<ExperimentGroup> listeningExperimentGroups = new ArrayList();
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && ((experimentGroup.getAccessibilityListen()) || GroupTypeEnum.ACCESSIBILITY.equals(experimentGroup.getGroupType()))) {
          listeningExperimentGroups.add(experimentGroup);
        }
      }
    }
    return listeningExperimentGroups;
  }

  public static boolean doesAnyExperimentCareAboutAccessibilityEvents(List<ExperimentDAO> experiments) {
    for (ExperimentDAO experimentDAO : experiments) {
      if (!isListeningForAccessibilityEvents(experimentDAO).isEmpty()) {
        return true;
      }
      if (!getAccessibilityTriggers(experimentDAO).isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static List<InterruptTrigger> getAccessibilityTriggersForAllExperiments(List<ExperimentDAO> experiments) {
    List<InterruptTrigger> matching = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : experiments) {
      List<InterruptTrigger> triggers = getAccessibilityTriggers(experimentDAO);
      if (!triggers.isEmpty()) {
        matching.addAll(triggers);
      }
    }
    return matching;
  }

  public static List<InterruptTrigger> getAccessibilityTriggers(ExperimentDAO experiment) {
    List<InterruptTrigger> matching = Lists.newArrayList();

    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType()) || !ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup)) {
        continue;
      } else {
        List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : triggers) {
          if (actionTrigger instanceof InterruptTrigger) {
            InterruptTrigger trigger = (InterruptTrigger)actionTrigger;
            List<InterruptCue> cues = trigger.getCues();
            for (InterruptCue interruptCue : cues) {
              final Integer cueCode = interruptCue.getCueCode();
              if (cueCode == InterruptCue.PERMISSION_CHANGED
                      || isAccessibilityRelatedCueCode(cueCode)) {
                matching.add(trigger);
              }
            }

          }
        }
      }
    }
    return matching;
  }

  private static boolean isAccessibilityRelatedCueCode(Integer cueCode) {
    return cueCode == InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED
            || cueCode == InterruptCue.NOTIFICATION_CREATED
            || cueCode == InterruptCue.NOTIFICATION_TRAY_CANCELLED
            || cueCode == InterruptCue.NOTIFICATION_TRAY_CLEAR_ALL
            || cueCode == InterruptCue.NOTIFICATION_TRAY_OPENED
            || cueCode == InterruptCue.NOTIFICATION_TRAY_SWIPE_DISMISS
            || cueCode == InterruptCue.NOTIFICATION_CLICKED;
  }

  private static boolean isAccessibilityRelatedCueCodeAndMatchesPatterns(Integer cueCode) {
    return cueCode.equals(InterruptCue.ACCESSIBILITY_EVENT_VIEW_CLICKED)
            || cueCode.equals(InterruptCue.NOTIFICATION_CREATED)
            || cueCode.equals(InterruptCue.NOTIFICATION_TRAY_SWIPE_DISMISS)
            || cueCode.equals(InterruptCue.NOTIFICATION_CLICKED)
            ;
  }

  public static List<ExperimentGroup> getExperimentsLoggingNotificationEvents(List<ExperimentDAO> experiments) {
    List<ExperimentGroup> listeners = new ArrayList();
    for (ExperimentDAO experimentDAO : experiments) {
      List<ExperimentGroup> listeningGroups = isListeningForNotificationEvents(experimentDAO);
      if (!listeningGroups.isEmpty()) {
        listeners.addAll(listeningGroups);
      }
    }
    return listeners;
  }

  public static List<ExperimentGroup> isListeningForNotificationEvents(ExperimentDAO experiment) {
    List<ExperimentGroup> listeningExperimentGroups = new ArrayList();
    List<ExperimentGroup> experimentGroups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && (experimentGroup.getLogNotificationEvents() || GroupTypeEnum.NOTIFICATION.equals(experimentGroup.getGroupType()))) {
          listeningExperimentGroups.add(experimentGroup);
        }
      }
    }
    return listeningExperimentGroups;
  }


  public static List<ExperimentGroup> getGroupsThatCareAboutActionLogging(ExperimentDAO experiment) {
    List<ExperimentGroup> matchingGroups = Lists.newArrayList();
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && ((experimentGroup.getLogActions() != null && experimentGroup.getLogActions()) || ( GroupTypeEnum.APPUSAGE_ANDROID.equals(experimentGroup.getGroupType())))) {
          matchingGroups.add(experimentGroup);
        }
      }
    }
    return matchingGroups;
  }

}
