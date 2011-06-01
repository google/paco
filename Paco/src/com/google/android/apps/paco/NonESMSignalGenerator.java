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

import java.util.Collections;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

public class NonESMSignalGenerator {

  private SignalSchedule schedule;

  public NonESMSignalGenerator(SignalSchedule schedule) {
    this.schedule = schedule;
  }

  public Long getNextAlarmTime(DateTime now) {
    if (schedule.getTimes() == null || schedule.getTimes().size() == 0) {
      return null;
    }
    Collections.sort(schedule.getTimes());
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
  
  private Long scheduleMonthly(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    
    if (schedule.getByDayOfMonth()) {
      int nowDOM = nowMidnight.getDayOfMonth();
      if (nowDOM == schedule.getDayOfMonth()) {
        Long nextTimeToday = getNextTimeToday(now, nowMidnight);
        if (nextTimeToday != null) {
          return nextTimeToday;
        }
      }
      DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
      return nextDay.plus(schedule.getTimes().get(0)).getMillis();
    } else {
      DateTime nextDay = getNextScheduleDay(nowMidnight);
      if (nextDay.equals(nowMidnight)) {
        Long nextTimeToday = getNextTimeToday(now, nextDay);
        if (nextTimeToday != null) {
          return nextTimeToday;
        }
        nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
        return nextDay.plus(schedule.getTimes().get(0)).getMillis();
      } else {
        return nextDay.plus(schedule.getTimes().get(0)).getMillis();
      }
    }
  }

  private Long scheduleWeekly(DateTime now) {
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    int nowDow = nowMidnight.getDayOfWeek(); // joda starts Monday, I start Sunday
    Integer nowDowIndex = SignalSchedule.DAYS_OF_WEEK[nowDow == 7 ? 0 : nowDow]; // joda is 1 based, and starts on Monday. we are 0-based, Sunday-start
    if ((schedule.getWeekDaysScheduled() & nowDowIndex) == nowDowIndex) {
      Long nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return nextDay.plus(schedule.getTimes().get(0)).getMillis();
  }

  private Long scheduleDaily(DateTime now) {    
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    if (nextRepeatDaily(nowMidnight).equals(nowMidnight)) {
      Long nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return nextDay.plus(schedule.getTimes().get(0)).getMillis();
  }

  private Long scheduleWeekday(DateTime now) {    
    DateTime nowMidnight = now.toDateMidnight().toDateTime();
    if (nowMidnight.getDayOfWeek() < DateTimeConstants.SATURDAY) { // jodatime starts with Monday = 0
      Long nextTimeToday = getNextTimeToday(now, nowMidnight);
      if (nextTimeToday != null) {
        return nextTimeToday;
      }
    }
    DateTime nextDay = getNextScheduleDay(nowMidnight.plusDays(1));
    return nextDay.plus(schedule.getTimes().get(0)).getMillis();
  }

  private Long getNextTimeToday(DateTime now, DateTime nowMidnight) {
    long nowAsOffsetFromMidnight = (now.getHourOfDay() * 60 * 60 * 1000) 
      + (now.getMinuteOfHour() * 60 * 1000) 
      + (now.getSecondOfMinute() * 1000);
      
    for (Long time : schedule.getTimes()) {
      if (time > nowAsOffsetFromMidnight) {
        return nowMidnight.getMillis() + time;
      }      
    }
    return null;
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
