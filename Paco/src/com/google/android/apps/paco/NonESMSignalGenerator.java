/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.SignalTimeDAO;

public class NonESMSignalGenerator {

  private SignalSchedule schedule;
  private Context context;
  private Long experimentId;
  private ExperimentProviderUtil experimentProvider;

  public NonESMSignalGenerator(SignalSchedule schedule, Long experimentId, ExperimentProviderUtil experimentProvider) {
    this.schedule = schedule;
    this.experimentId = experimentId;
    this.experimentProvider = experimentProvider;
  }

  public DateTime getNextAlarmTime(DateTime now) {
    if (schedule.getSignalTimes() == null || schedule.getSignalTimes().size() == 0) {
      return null;
    }

    switch (schedule.getScheduleType()) {
    case SignalSchedule.DAILY:
      return scheduleDaily(now);
    case SignalSchedule.WEEKDAY:
      return scheduleWeekday(now);
    case SignalSchedule.WEEKLY:
      return scheduleWeekly(now);
    case SignalSchedule.MONTHLY:
      return scheduleMonthly(now);
    default:
      return null;
    }

  }

  private DateTime scheduleMonthly(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();

    if (schedule.getByDayOfMonth()) {
      int nowDOM = nowMidnight.getDayOfMonth();
      if (nowDOM == schedule.getDayOfMonth()) {
        DateTime nextTimeToday = getNextTimeToday(now, nowMidnight);
        if (nextTimeToday != null) {
          return nextTimeToday;
        }
      }
      DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
      return getFirstScheduledTimeOnDay(nextDay);
    } else {
      DateTime nextDay = getNextScheduleDay(nowMidnight);
      if (nextDay.equals(nowMidnight)) {
        DateTime nextTimeToday = getNextTimeToday(now, nextDay);
        if (nextTimeToday != null) {
          return nextTimeToday;
        }
        nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
        return getFirstScheduledTimeOnDay(nextDay);
      } else {
        return getFirstScheduledTimeOnDay(nextDay);
      }
    }
  }

  private DateTime getFirstScheduledTimeOnDay(DateTime nextDay) {
    return nextDay.withMillisOfDay(schedule.getSignalTimes().get(0).getFixedTimeMillisFromMidnight());
  }

  private DateTime scheduleWeekly(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    int nowDow = nowMidnight.getDayOfWeek(); // joda starts Monday, I start Sunday
    Integer nowDowIndex = SignalSchedule.DAYS_OF_WEEK[nowDow == 7 ? 0 : nowDow]; // joda is 1 based, and starts on Monday. we are 0-based, Sunday-start
    if ((schedule.getWeekDaysScheduled() & nowDowIndex) == nowDowIndex) {
      DateTime nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return getFirstScheduledTimeOnDay(nextDay);
  }

  private DateTime scheduleDaily(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    if (nextRepeatDaily(nowMidnight).equals(nowMidnight)) {
      DateTime nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return getFirstScheduledTimeOnDay(nextDay);
  }

  private DateTime scheduleWeekday(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    if (nowMidnight.getDayOfWeek() < DateTimeConstants.SATURDAY) { // jodatime starts with Monday = 0
      DateTime nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return getFirstScheduledTimeOnDay(nextDay);
  }


  private DateTime getNextTimeToday(DateTime now, DateTime nowMidnight) {
    return getNextTimeTodayForSchedule(now, nowMidnight, schedule, experimentId);
  }

  class SignalTimeHolder {
    DateTime scheduledTime;
    DateTime responseTime;
    DateTime chosenTime;
    SignalTime signalTime;

    public SignalTimeHolder(DateTime scheduledTime, DateTime responseTime, DateTime chosenTime,
                                    SignalTime signalTime) {
      super();
      this.scheduledTime = scheduledTime;
      this.responseTime = responseTime;
      this.chosenTime = chosenTime;
      this.signalTime = signalTime;
    }



  }
  //  Visible for Testing
  public DateTime getNextTimeTodayForSchedule(DateTime now, DateTime nowMidnight, SignalSchedule schedule, Long experimentId) {
    int nowAsOffsetFromMidnight = now.getMillisOfDay();
    List<SignalTimeHolder> previousTimes = Lists.newArrayList();
    for (int i=0; i < schedule.getSignalTimes().size(); i++) {
      SignalTime signalTime = schedule.getSignalTimes().get(i);
      SignalTimeHolder signalTimeHolder = getTimeForSignalType(signalTime, previousTimes, nowMidnight, experimentId);

      if (signalTimeHolder.chosenTime != null && signalTimeHolder.chosenTime.getMillisOfDay() > nowAsOffsetFromMidnight) {
        return signalTimeHolder.chosenTime;
      }
      previousTimes.add(signalTimeHolder);
    }
    return null;
  }

  private SignalTimeHolder getTimeForSignalType(SignalTime signalTime, List<SignalTimeHolder> previousTimes, DateTime nowMidnight, Long experimentId) {
    if (signalTime.getType() == SignalTimeDAO.FIXED_TIME) {
      return getNextTimeForFixedType(signalTime, previousTimes, nowMidnight);
    } else if (signalTime.getType() == SignalTimeDAO.OFFSET_TIME) {
      return getNextTimeForOffsetType(signalTime, previousTimes, experimentId);
    } else {
      return createNullSignalTimeHolder(signalTime);
    }
  }

  private SignalTimeHolder getNextTimeForOffsetType(SignalTime signalTime, List<SignalTimeHolder> previousTimes, Long experimentId) {
    if (previousTimes.size() == 0) {
      return createNullSignalTimeHolder(signalTime); // we don't allow offset types as the first signalTime
    }

    SignalTimeHolder previousTimePair = previousTimes.get(previousTimes.size() - 1);
    DateTime basis = computeBasisTimeForOffsetType(signalTime, previousTimePair, experimentId);
    if (basis != null) {
      DateTime chosenTime = basis.plusMillis(signalTime.getOffsetTimeMillis());
      return new SignalTimeHolder(null, null, chosenTime, signalTime);
    } else {
      return createNullSignalTimeHolder(signalTime);
    }
  }

  private SignalTimeHolder getNextTimeForFixedType(SignalTime signalTime, List<SignalTimeHolder> previousTimes,
                                                       DateTime nowMidnight) {
    if (previousTimes.size() == 0 || signalTime.getMissedBasisBehavior() == SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME) {
      return new SignalTimeHolder(null, null, nowMidnight.toDateTime().plusMillis(signalTime.getFixedTimeMillisFromMidnight()), signalTime);
    } else if (signalTime.getMissedBasisBehavior() == SignalTimeDAO.MISSED_BEHAVIOR_SKIP && previousEventHasResponse(previousTimes)) {
        return new SignalTimeHolder(null, null, nowMidnight.toDateTime().plusMillis(signalTime.getFixedTimeMillisFromMidnight()), signalTime);
    } else {
      return createNullSignalTimeHolder(signalTime);
    }
  }

  private boolean previousEventHasResponse(List<SignalTimeHolder> previousTimes) {
    SignalTimeHolder previousTime = previousTimes.get(previousTimes.size() - 1);
    if (previousTime.scheduledTime == null) {
      retrieveEventForPreviousTime(previousTime, experimentId);
    }
    return previousTime.responseTime != null;
  }

  private SignalTimeHolder createNullSignalTimeHolder(SignalTime signalTime) {
    return new SignalTimeHolder(null, null, null, signalTime);
  }

  private DateTime computeBasisTimeForOffsetType(SignalTime signalTime, SignalTimeHolder previousTimePair, Long experimentId) {
    boolean eventRecorded = false;
    if (previousTimePair.scheduledTime == null) {
      eventRecorded = retrieveEventForPreviousTime(previousTimePair, experimentId);
    }

    if (!eventRecorded) {
      // if the participant has not responded to the last event yet, we can only calculate
      // based on the scheduled time of the previous event.
      // once the user responds or the event timesout, we will recalculate.
      return previousTimePair.chosenTime;
    }
    if (signalTime.getBasis() == SignalTimeDAO.OFFSET_BASIS_SCHEDULED_TIME) {
      return previousTimePair.scheduledTime;
    } else if (signalTime.getBasis() == SignalTimeDAO.OFFSET_BASIS_RESPONSE_TIME){
      DateTime basis = previousTimePair.responseTime;
      if (basis == null && signalTime.getMissedBasisBehavior() == SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME) {
        basis = previousTimePair.scheduledTime; // fallback to the scheduled time if we should
      }
      return basis;
    }
    return null;
  }

  /**
   *
   * @param previousTimePair
   * @param experimentId
   * @return boolean if an event has been recorded yet. This is not true when the notification is still out but unresponded and un-timedout.
   */
  private boolean retrieveEventForPreviousTime(SignalTimeHolder previousTimePair, Long experimentId) {
    // we depend on the previous time, but we haven't loaded it yet. Do that now.
    Event event = experimentProvider.getEvent(experimentId, previousTimePair.chosenTime);
    if (event != null) {
      previousTimePair.scheduledTime = event.getScheduledTime();
      previousTimePair.responseTime = event.getResponseTime();
      return true;
    }
    return false;
  }

//      if (signalTime.getType() == SignalTimeDAO.FIXED_TIME) {
//        int currentSignalTimeOffset = signalTime.getFixedTimeMillisFromMidnight();
//        if (isSignalTimeAfterNow(currentSignalTimeOffset, nowAsOffsetFromMidnight)) {
//          return nowMidnight.toDateTime().withMillisOfDay(currentSignalTimeOffset);
//        } else {
//          previousTime = new Long(nowMidnight.toDateTime().withMillisOfDay(currentSignalTimeOffset).getMillisOfDay());
//        }

  //      } else if (signalTime.getType() == SignalTimeDAO.OFFSET_TIME && previousTime != null) {
//        ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
//        Event event = eu.getEvent(schedule.getExperimentId(), previousTime);
//
//        if (signalTime.getBasis() == SignalTimeDAO.OFFSET_BASIS_RESPONSE_TIME) {
//          if (event.getResponseTime() != null) {
//            DateTime offsetFromResponseTime = event.getResponseTime().plusMillis(signalTime.getOffsetTimeMillis());
//            if (offsetFromResponseTime.getMillisOfDay() > nowAsOffsetFromMidnight) {
//              return offsetFromResponseTime;
//            } else {
//              previousTime = new Long(event.getScheduledTime().getMillisOfDay());
//            }
//          } else if (event.getResponseTime() == null && signalTime.getMissedBasisBehavior() == SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME) {
//            DateTime offsetFromScheduledTime = event.getScheduledTime().plusMillis(signalTime.getOffsetTimeMillis());
//            if (offsetFromScheduledTime.getMillisOfDay() > nowAsOffsetFromMidnight) {
//              return offsetFromScheduledTime;
//            } else {
//              previousTime = new Long(offsetFromScheduledTime.getMillisOfDay());
//            }
//          } else if (event.getResponseTime() == null && signalTime.getMissedBasisBehavior() == SignalTimeDAO.MISSED_BEHAVIOR_SKIP) {
//            createMissedEvent(eu);
//            return null;
//          }
//        }
//      }
//
//    }


  private void createMissedEvent(ExperimentProviderUtil eu) {

  }

  private DateTime getNextScheduleDay(DateTime midnightTomorrow) {

    switch (schedule.getScheduleType()) {
    case SignalSchedule.DAILY:
      return nextRepeatDaily(midnightTomorrow);

    case SignalSchedule.WEEKDAY:
      int tomorrowDOW = midnightTomorrow.getDayOfWeek();
      if (tomorrowDOW > DateTimeConstants.FRIDAY) {
        return midnightTomorrow.plusDays(8 - tomorrowDOW);
      } else {
        return midnightTomorrow;
      }

    case SignalSchedule.WEEKLY:
      int scheduleDays = schedule.getWeekDaysScheduled();
      if (scheduleDays == 0) {
        return null;
      }
      for (int i=0; i < 8; i++) { // go at least to the same day next week.
        int midnightTomorrowDOW = midnightTomorrow.getDayOfWeek();
        Integer nowDowIndex = SignalSchedule.DAYS_OF_WEEK[midnightTomorrowDOW == 7 ? 0 : midnightTomorrowDOW]; // joda is 1 based & counts Monday as first day of the week so Sunday is 7 instead of 0.
        if ((scheduleDays & nowDowIndex) == nowDowIndex) {
          return nextRepeatWeekly(midnightTomorrow);
        }
        midnightTomorrow = midnightTomorrow.plusDays(1);

      }
      throw new IllegalStateException("Cannot get to here. Weekly must repeat at least once a week");

    case SignalSchedule.MONTHLY:
      if (schedule.getByDayOfMonth()) {
        int midnightDOM = midnightTomorrow.getDayOfMonth();
        int scheduledDOM = schedule.getDayOfMonth();
        if (midnightDOM == scheduledDOM) {
          return midnightTomorrow;
        } else if (midnightDOM > scheduledDOM) {
          MutableDateTime mutableDateTime = midnightTomorrow.plusMonths(1).toMutableDateTime();
          mutableDateTime.setDayOfMonth(scheduledDOM);
          return nextRepeatMonthly(mutableDateTime.toDateTime());
        } else {
          return nextRepeatMonthly(midnightTomorrow.plusDays(scheduledDOM - midnightDOM));
        }
      } else {
        Integer nthOfMonth = schedule.getNthOfMonth();
        Integer dow = getDOWFromIndexedValue(); // only one selection, so take log2 to get index of dow
        DateMidnight nthDowDate = getNthDOWOfMonth(midnightTomorrow, nthOfMonth, dow).toDateMidnight();
        DateTime returnDate = null;
        if (nthDowDate.equals(midnightTomorrow)) {
          returnDate = midnightTomorrow;
        } else if (nthDowDate.isAfter(midnightTomorrow)) {
          returnDate = nthDowDate.toDateTime();
        } else {
          returnDate = getNthDOWOfMonth(midnightTomorrow.plusMonths(1), nthOfMonth, dow).toDateTime();
        }
        return nextRepeatMonthly(returnDate);
      }
    default:
      throw new IllegalStateException("Schedule has an unknown type: "
          + schedule.getScheduleType());
    }
  }

  private Integer getDOWFromIndexedValue() {
    Integer dow = (int) (Math.log(schedule.getWeekDaysScheduled()) / Math.log(2));
    return dow;
  }

  // @VisibleForTesting
  DateTime getNthDOWOfMonth(DateTime midnightTomorrow, Integer nthOfMonth, Integer dow) {
    int dtconstDow = dow == 0 ? 7 : dow;
    DateTime first = midnightTomorrow.withDayOfMonth(1);
    if (first.getDayOfWeek() > dtconstDow) {
      return first.plusWeeks(nthOfMonth).withDayOfWeek(dtconstDow);
    } else {
      return first.plusWeeks(nthOfMonth - 1).withDayOfWeek(dtconstDow);
    }
  }

  private DateTime nextRepeatDaily(DateTime midnightTomorrow) {
    if (schedule.getRepeatRate() == 1) {
      return midnightTomorrow;
    }
    int distanceBetweenStartAndTomorrow = Days.daysBetween(new DateTime(schedule.getBeginDate()).toDateMidnight(),
        midnightTomorrow).getDays();
    if (distanceBetweenStartAndTomorrow == 0 || distanceBetweenStartAndTomorrow == schedule.getRepeatRate()) {
      return midnightTomorrow;
    } else if (distanceBetweenStartAndTomorrow > schedule.getRepeatRate()) {
      int remainder = distanceBetweenStartAndTomorrow % schedule.getRepeatRate();
      return midnightTomorrow.plusDays(schedule.getRepeatRate() - remainder);
    } else {
      return midnightTomorrow.plusDays(schedule.getRepeatRate() - distanceBetweenStartAndTomorrow);
    }
  }

  private DateTime nextRepeatWeekly(DateTime midnightNextDay) {
    if (schedule.getRepeatRate() == 1) {
      return midnightNextDay;
    }
    int distanceBetweenStartAndTomorrow = Weeks.weeksBetween(new DateTime(schedule.getBeginDate()).toDateMidnight(),
        midnightNextDay).getWeeks();
    if (distanceBetweenStartAndTomorrow == 0 || distanceBetweenStartAndTomorrow == schedule.getRepeatRate()) {
      if ((distanceBetweenStartAndTomorrow == 0 && midnightNextDay.getDayOfWeek() <= new DateMidnight(schedule.getBeginDate()).getDayOfWeek())) {
        // we crossed a week boundary, so add one week.
        return midnightNextDay.plusWeeks(schedule.getRepeatRate() - 1);
      }
      return midnightNextDay;
    } else if (distanceBetweenStartAndTomorrow > schedule.getRepeatRate()) {
      int remainder = distanceBetweenStartAndTomorrow % schedule.getRepeatRate();
      return midnightNextDay.plusWeeks(schedule.getRepeatRate() - remainder);
    } else {
      return midnightNextDay.plusWeeks(schedule.getRepeatRate() - distanceBetweenStartAndTomorrow);
    }
  }

  private DateTime nextRepeatMonthly(DateTime midnightTomorrow) {
    if (schedule.getRepeatRate() == 1) {
      return midnightTomorrow;
    }
    if (schedule.getByDayOfMonth()) {
      int distanceBetweenStartAndTomorrow = Months.monthsBetween(new DateTime(schedule.getBeginDate()).toDateMidnight(),
          midnightTomorrow).getMonths();
      if (distanceBetweenStartAndTomorrow == 0 || distanceBetweenStartAndTomorrow == schedule.getRepeatRate()) {
        if ((distanceBetweenStartAndTomorrow == 0 && midnightTomorrow.getDayOfMonth() <= new DateMidnight(schedule.getBeginDate()).getDayOfMonth())) {
          // we crossed a month boundary, so add one month.
          return midnightTomorrow.plusMonths(schedule.getRepeatRate() - 1);
        }
        return midnightTomorrow;
      } else if (distanceBetweenStartAndTomorrow > schedule.getRepeatRate()) {
        int remainder = distanceBetweenStartAndTomorrow % schedule.getRepeatRate();
        return midnightTomorrow.plusMonths(schedule.getRepeatRate() - remainder);
      } else {
        return midnightTomorrow.plusMonths(schedule.getRepeatRate() - distanceBetweenStartAndTomorrow);
      }
    } else {
      int distanceBetweenStartAndTomorrow = Months.monthsBetween(new DateTime(schedule.getBeginDate()).toDateMidnight(),
          midnightTomorrow).getMonths();
      if (distanceBetweenStartAndTomorrow == 0 || distanceBetweenStartAndTomorrow == schedule.getRepeatRate()) {
        if ((distanceBetweenStartAndTomorrow == 0 && midnightTomorrow.getDayOfMonth() <= new DateMidnight(schedule.getBeginDate()).getDayOfMonth())) {
          // we crossed a month boundary, so add one month.
          return getNthDOWOfMonth(midnightTomorrow.plusMonths(schedule.getRepeatRate() - 1), schedule.getNthOfMonth(), getDOWFromIndexedValue());
        }
        return midnightTomorrow;
      } else if (distanceBetweenStartAndTomorrow > schedule.getRepeatRate()) {
        int remainder = distanceBetweenStartAndTomorrow % schedule.getRepeatRate();
        return getNthDOWOfMonth(midnightTomorrow.plusMonths(schedule.getRepeatRate() - remainder), schedule.getNthOfMonth(), getDOWFromIndexedValue());
      } else {
        return getNthDOWOfMonth(midnightTomorrow.plusMonths(schedule.getRepeatRate() - distanceBetweenStartAndTomorrow), schedule.getNthOfMonth(), getDOWFromIndexedValue());
      }
    }
  }

}
