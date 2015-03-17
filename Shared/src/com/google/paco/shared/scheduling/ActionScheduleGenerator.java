package com.google.paco.shared.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.paco.shared.model.SignalTimeDAO;
import com.google.paco.shared.model2.ActionTrigger;
import com.google.paco.shared.model2.EventStore;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.PacoAction;
import com.google.paco.shared.model2.PacoNotificationAction;
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.google.paco.shared.model2.SignalTime;
import com.google.paco.shared.util.TimeUtil;

public class ActionScheduleGenerator {

  public static List<ActionSpecification> getAllAlarmsWithinOneMinuteofNow(DateTime now,
                                                                           List<ExperimentDAO> experiments,
                                                                           EsmSignalStore alarmStore,
                                                                           EventStore eventStore) {
    List<ActionSpecification> times = arrangeExperimentsByNextTimeFrom(experiments, now, alarmStore, eventStore);
    List<ActionSpecification> matchingTimes = new ArrayList<ActionSpecification>();
    for (ActionSpecification time : times) {
      if (new Interval(now, time.time).toDurationMillis() < 60000) {
        matchingTimes.add(time);
      }
    }
    return matchingTimes;
  }

  public static List<ActionSpecification> arrangeExperimentsByNextTime(List<ExperimentDAO> experiments,
                                                                       EsmSignalStore alarmStore,
                                                                       EventStore eventStore) {
    return arrangeExperimentsByNextTimeFrom(experiments, new DateTime(), alarmStore, eventStore);
  }

  static List<ActionSpecification> arrangeExperimentsByNextTimeFrom(List<ExperimentDAO> experiments,
                                                                       DateTime now,
                                                                       EsmSignalStore alarmStore,
                                                                       EventStore eventStore) {
    List<ActionSpecification> times = new ArrayList<ActionSpecification>();
    for (ExperimentDAO experiment : experiments) {
      ActionScheduleGenerator actionScheduleGenerator = new ActionScheduleGenerator(experiment);
      ActionSpecification nextTimeFromNow = actionScheduleGenerator.getNextTimeFromNow(now, alarmStore, eventStore);
      if (nextTimeFromNow != null) {
        times.add(nextTimeFromNow);
      }
    }
    Collections.sort(times);
    return times;
  }



  private ExperimentDAO experiment;

  public ActionScheduleGenerator(ExperimentDAO experiment) {
    this.experiment = experiment;
  }

  ActionSpecification getNextTimeFromNow(DateTime now, EsmSignalStore alarmStore, EventStore eventStore) {
    if (now == null || isOver(now, experiment)) {
      return null;
    }

    ActionSpecification nextTimeActionSpecification = null;

    if (isExperimentNotStartedYet(now, experiment)) {
      now = getEarliestStartDate(experiment).toDateTime();
    }

    // new build up list of referent times and which component scheduled it
    List<ExperimentGroup> groups = experiment.getGroups();
    DateTime currentNearestTime = null;
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();

      for (ActionTrigger actionTrigger : actionTriggers) {
        if (actionTrigger instanceof ScheduleTrigger) {
          ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
          List<Schedule> schedules = scheduleTrigger.getSchedules();
          for (Schedule schedule : schedules) {
            DateTime nextTimeForSchedule = null;
            if (schedule.getScheduleType().equals(Schedule.ESM)) {
              nextTimeForSchedule = scheduleESM(now, schedule, alarmStore,
                                                experiment.getId(), experimentGroup.getName(), actionTrigger.getId());
            } else {
              nextTimeForSchedule = getNextAlarmTime(now, experiment.getId(), schedule, eventStore,
                                                     experimentGroup.getName(), actionTrigger.getId());
            }

            if (nextTimeForSchedule != null &&
                    (currentNearestTime == null ||
                     nextTimeForSchedule.isBefore(currentNearestTime))) {
              currentNearestTime = nextTimeForSchedule;
              final List<PacoAction> actions = scheduleTrigger.getActions();
              PacoNotificationAction notificationAction = null;
              for (PacoAction pacoAction : actions) {
                if (pacoAction.getActionCode() == PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE) {
                  notificationAction = (PacoNotificationAction) pacoAction;
                }
              }
              nextTimeActionSpecification = new ActionSpecification(currentNearestTime, experiment,
                                                                    experimentGroup, actionTrigger,
                                                                    notificationAction, schedule.getId());
            }
          }
        }
      }

    }
    return nextTimeActionSpecification;
//
//// Old: method for old model
//    for (SignalingMechanism signalingMechanism : getSignalingMechanisms()) {
//      if (signalingMechanism instanceof SignalSchedule) {
//        DateTime nextTimeForSignalGroup = null;
//        SignalSchedule schedule = (SignalSchedule) signalingMechanism;
//        if (schedule.getScheduleType().equals(SignalSchedule.ESM)) {
//          nextTimeForSignalGroup = scheduleESM(now, context);
//        } else {
//          nextTimeForSignalGroup = schedule.getNextAlarmTime(now, context, this.getServerId());
//        }
//        if (nextTimeForSignalGroup != null && (nextNearestTime == null || nextTimeForSignalGroup.isBefore(nextNearestTime))) {
//          nextNearestTime = nextTimeForSignalGroup;
//        }
//      }
//    }
//    return nextNearestTime;
  }

  public static boolean isOver(DateTime now, ExperimentDAO experiment) {
    return areAllGroupsFixedDuration(experiment) && now.isAfter(getEndDateTime(experiment));
  }


  public static boolean areAllGroupsFixedDuration(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration()) {
        return false;
      }
    }
    return true;
  }

  public static boolean isExperimentNotStartedYet(DateTime now, ExperimentDAO experiment) {
    return areAllGroupsFixedDuration(experiment) && now.isBefore(getEarliestStartDate(experiment));
  }

  /**
   * This is only from the fixed duration experiments.
   * It makes no sense to ask this question of ongoing experiments.
   *
   * @param experiment
   * @return
   */
  public static DateMidnight getEarliestStartDate(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    DateMidnight currentEarliestStartDate = null;
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration()) {
        continue;
      }
      DateMidnight startDate = TimeUtil.unformatDate(experimentGroup.getStartDate()).toDateMidnight();
      if (currentEarliestStartDate == null || startDate.isBefore(currentEarliestStartDate)) {
        currentEarliestStartDate = startDate;
      }
    }
    return currentEarliestStartDate;
  }

//public DateTime getStartDateTime(ExperimentGroup experimentGroup) {
//  DateTime firstTime = null;
//  for (ActionTrigger actionTrigger : experimentGroup.getActionTriggers()) {
//    DateTime firstTimeForGroup = null;
//    if (actionTrigger instanceof ScheduleTrigger) {
//      ScheduleTrigger scheduleTrigger = (ScheduleTrigger) actionTrigger;
//      List<Schedule> schedules = scheduleTrigger.getSchedules();
//      for (Schedule schedule : schedules) {
//        final Integer scheduleType = schedule.getScheduleType();
//        if (scheduleType.equals(Schedule.WEEKDAY) ||
//                scheduleType.equals(Schedule.WEEKLY) ||
//                scheduleType.equals(Schedule.DAILY) ||
//                scheduleType.equals(Schedule.MONTHLY)) {
//          List<SignalTime> times = schedule.getSignalTimes();
//          DateTime firstTimeForDay = new DateTime().plus(times.get(0).getFixedTimeMillisFromMidnight());
//          firstTimeForGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getStartDate())).toDateTime()
//                                                                      .withMillisOfDay(firstTimeForDay.getMillisOfDay());
//        } else {
//          firstTimeForGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getStartDate())).toDateTime();
//        }
//      }
//    } else {
//      firstTimeForGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getStartDate())).toDateTime();
//    }
//    if (firstTime == null || firstTimeForGroup.isBefore(firstTime)) {
//      firstTime = firstTimeForGroup;
//    }
//  }
//  return firstTime;
//}


  public static DateTime getLastEndTime(ExperimentDAO experiment) {
    List<ExperimentGroup> groups = experiment.getGroups();
    DateTime currentLatestEndTime = null;
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration()) {
        continue;
      }
      DateTime endDate = TimeUtil.unformatDate(experimentGroup.getEndDate());
      if (currentLatestEndTime == null || endDate.isAfter(currentLatestEndTime)) {
        currentLatestEndTime = endDate;
      }
    }
    return currentLatestEndTime;
  }

  public static DateTime getEndDateTime(ExperimentDAO experiment) {
    DateTime lastTime = null;

    for (ExperimentGroup experimentGroup : experiment.getGroups()) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        DateTime lastTimeForSignalGroup = null;
        if (actionTrigger instanceof ScheduleTrigger) {
          ScheduleTrigger scheduledTrigger = (ScheduleTrigger)actionTrigger;
          List<Schedule> schedules = scheduledTrigger.getSchedules();
          for (Schedule schedule : schedules) {

            if (schedule.getScheduleType().equals(Schedule.WEEKDAY)) {
              List<SignalTime> times = schedule.getSignalTimes();
              SignalTime lastSignalTime = times.get(times.size() - 1);
              if (lastSignalTime.getType() == SignalTimeDAO.FIXED_TIME) {
                // TODO actually compute the last time based on all of the rules for offset times and skip if missed rules
                DateTime lastTimeForDay = new DateTime().plus(lastSignalTime.getFixedTimeMillisFromMidnight());
                lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getEndDate())).toDateTime()
                        .withMillisOfDay(lastTimeForDay.getMillisOfDay());
              } else {
                lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getEndDate())).plusDays(1).toDateTime();
              }
            } else {
              lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getEndDate())).plusDays(1).toDateTime();
            }
          }
        } else {
          lastTimeForSignalGroup = new DateMidnight(TimeUtil.unformatDate(experimentGroup.getEndDate())).plusDays(1).toDateTime();
        }
        if (lastTime == null || lastTimeForSignalGroup.isAfter(lastTime)) {
          lastTime = lastTimeForSignalGroup;
        }
      }
    }
    return lastTime;
  }


  //@VisibleForTesting
  DateTime scheduleESM(DateTime now, Schedule schedule, EsmSignalStore alarmStore,
                       Long experimentId, String groupName, Long actionTriggerId) {

    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(now)) {
      now = TimeUtil.skipWeekends(now);
    }
    ensureScheduleIsGeneratedForPeriod(now, alarmStore, schedule, experimentId, groupName, actionTriggerId);
    // generate at least the next period, so we always have a next time for
    // ESMs.
    DateTime nextPeriod = now.plusDays(schedule.convertEsmPeriodToDays());
    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(nextPeriod)) {
      nextPeriod = TimeUtil.skipWeekends(nextPeriod);
    }

    ensureScheduleIsGeneratedForPeriod(nextPeriod, alarmStore, schedule, experimentId, groupName, actionTriggerId);
    DateTime next = lookupNextTimeOnEsmSchedule(now, alarmStore, schedule, experimentId, groupName, actionTriggerId); // anymore this period
    if (next != null) {
      return next;
    }
    return lookupNextTimeOnEsmSchedule(nextPeriod, alarmStore, schedule, experimentId, groupName, actionTriggerId);
  }

  private DateTime lookupNextTimeOnEsmSchedule(DateTime now, EsmSignalStore alarmStore,
                                               Schedule schedule, Long experimentId,
                                               String groupName, Long actionTriggerId) {
    final long periodStartInMillis = getPeriodStart(now, schedule).getMillis();
    List<DateTime> signals = alarmStore.getSignals(experimentId, periodStartInMillis, groupName, actionTriggerId, schedule.getId());

    DateTime next = getNextSignalAfterNow(now, signals);
    if (next != null) {
      return next;
    }
    DateTime nextPeriod = now.plusDays(schedule.convertEsmPeriodToDays());
    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(nextPeriod)) {
      nextPeriod = TimeUtil.skipWeekends(nextPeriod);
    }

    ensureScheduleIsGeneratedForPeriod(nextPeriod, alarmStore, schedule, experimentId, groupName, actionTriggerId);
    signals = alarmStore.getSignals(experiment.getId(), getPeriodStart(nextPeriod, schedule).getMillis(),
                                    groupName, actionTriggerId, schedule.getId());
    return getNextSignalAfterNow(now, signals);
  }

  private DateTime getNextSignalAfterNow(DateTime now, List<DateTime> signals) {
    if (signals.size() == 0) {
      return null;
    }
    Collections.sort(signals);
    for (DateTime dateTime : signals) {
      if (!now.isAfter(dateTime)) {
        return dateTime;
      }
    }
    return null;
  }

  private void ensureScheduleIsGeneratedForPeriod(DateTime now, EsmSignalStore alarmStore,
                                                  Schedule schedule, Long experimentId,
                                                  String groupName, Long actionTriggerId) {
    DateMidnight periodStart = getPeriodStart(now, schedule);
    List<DateTime> signalTimes = alarmStore.getSignals(experimentId,
        periodStart.getMillis(), groupName, actionTriggerId, schedule.getId());

    if (signalTimes.size() == 0) {
      generateNextPeriod(periodStart, alarmStore, schedule, experimentId, groupName, actionTriggerId);
    }

  }

  DateMidnight getPeriodStart(DateTime now, Schedule schedule) {
    switch (schedule.getEsmPeriodInDays()) {
    case Schedule.ESM_PERIOD_DAY:
      return now.toDateMidnight();
    case Schedule.ESM_PERIOD_WEEK:
      return now.dayOfWeek().withMinimumValue().toDateMidnight();
    case Schedule.ESM_PERIOD_MONTH:
      return now.dayOfMonth().withMinimumValue().toDateMidnight();
    default:
      throw new IllegalStateException("Cannot get here.");
    }
  }

  private void generateNextPeriod(DateMidnight generatingPeriodStart, EsmSignalStore alarmStore,
                                  Schedule schedule, Long experimentId, String groupName, Long actionTriggerId) {
    if (isOver(generatingPeriodStart.toDateTime(), experiment)) {
      return;
    }
    alarmStore.deleteSignalsForPeriod(experimentId,
                                      generatingPeriodStart.getMillis(),
                                      groupName, actionTriggerId, schedule.getId());

    List<DateTime> signalTimes = generateSignalTimesForPeriod(generatingPeriodStart, schedule,
                                                              experimentId, groupName, actionTriggerId);
    storeSignalTimes(generatingPeriodStart, signalTimes, alarmStore,
                     experimentId, groupName, actionTriggerId, schedule.getId());
  }

  private List<DateTime> generateSignalTimesForPeriod(DateMidnight periodStart,
                                                      Schedule schedule, Long experimentId, String groupName, Long actionTriggerId) {
    return new EsmGenerator2().generateForSchedule(periodStart.toDateTime(), schedule);
  }

  private void storeSignalTimes(DateMidnight periodStart, List<DateTime> times, EsmSignalStore alarmStore,
                                Long experimentId, String groupName, Long actionTriggerId, Long scheduleId) {
    long periodStartMillis = periodStart.getMillis();
    for (DateTime alarmTime : times) {
      alarmStore.storeSignal(periodStartMillis, experimentId, alarmTime.getMillis(), groupName, actionTriggerId, scheduleId);
    }
  }

  public DateTime getNextAlarmTime(DateTime dateTime, Long experimentServerId, Schedule schedule,
                                   EventStore eventStore, String groupName, Long actionTriggerId) {
    if (!schedule.getScheduleType().equals(Schedule.ESM)) {
      return new NonESMSignalGenerator(schedule, experimentServerId, eventStore, groupName, actionTriggerId).getNextAlarmTime(dateTime);
    }
    return null;  // TODO (bobevans) move the esm handling in Experiment to here.
  }
}
