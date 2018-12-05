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
package com.pacoapp.paco.shared.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Minutes;

import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.util.TimeUtil;


public class EsmGenerator2 {

  public static final Minutes BUFFER_MILLIS  = Minutes.minutes(59);
  private Schedule schedule;
  private DateTime periodStartDate;
  private ArrayList<DateTime> times;

  // divide time period evenly into blocks
  // pick "frequency" number of time blocks randomly
  // pick random times within each chosen block
  // skip times within 1 hour of other times
  public List<DateTime> generateForSchedule(DateTime startDate, Schedule schedule2) {
    this.schedule = schedule2;
    this.periodStartDate = adjustStartDateToBeginningOfPeriod(startDate);
    times = new ArrayList<DateTime>();

    if (schedule2.getEsmFrequency() == null || schedule2.getEsmFrequency() == 0) {
      return times;
    }
    List<Integer> schedulableDays;
    switch (schedule2.getEsmPeriodInDays()) {
    case Schedule.ESM_PERIOD_DAY:
      if (!schedule2.getEsmWeekends() && TimeUtil.isWeekend(periodStartDate)) {
        return times;
      } else {
        schedulableDays = Arrays.asList(1);
      }
      break;
    case Schedule.ESM_PERIOD_WEEK:
      schedulableDays = getPeriodDaysForWeek();
      break;
    case Schedule.ESM_PERIOD_MONTH:
      schedulableDays = getPeriodDaysForMonthOf(periodStartDate);
      break;
    default:
      throw new IllegalStateException("Cannot get here.");
    }

    Minutes dayLengthIntervalInMinutes = Minutes.minutesIn(new Interval(schedule2.getEsmStartHour(), schedule2.getEsmEndHour()));
    Minutes totalMinutesInPeriod = dayLengthIntervalInMinutes.multipliedBy(schedulableDays.size());
    Minutes sampleBlockTimeInMinutes = totalMinutesInPeriod.dividedBy(schedule2.getEsmFrequency());
    Minutes timeoutInMinutes = Minutes.minutes(schedule2.getMinimumBuffer());
    Random rand = new Random();
    for (int signal = 0; signal < schedule2.getEsmFrequency(); signal++) {

      int candidateTimeInBlock;
      DateTime candidateTime;
      int periodAttempts = 1000;
      do {
        candidateTimeInBlock = rand.nextInt(sampleBlockTimeInMinutes.getMinutes());
        // map candidatePeriod and candidateTime back onto days of period
        // note, sometimes a candidate period will map across days in period
        // because start and end hours make for non-contiguous days
        int totalMinutesToAdd = sampleBlockTimeInMinutes.getMinutes() * signal + candidateTimeInBlock;
        int daysToAdd = totalMinutesToAdd / dayLengthIntervalInMinutes.getMinutes();
        int minutesToAdd = 0;
        if (totalMinutesToAdd <= dayLengthIntervalInMinutes.getMinutes()) { // within one day
          minutesToAdd = totalMinutesToAdd;
        } else {
          minutesToAdd = totalMinutesToAdd % dayLengthIntervalInMinutes.getMinutes();
        }

        DateTime plusDays = periodStartDate.plusDays(schedulableDays.get(daysToAdd) - 1);
        candidateTime = plusDays.withMillisOfDay(schedule2.getEsmStartHour().intValue()).plusMinutes(minutesToAdd);
        periodAttempts--;
      } while (periodAttempts > 0 &&
          (!isMinimalBufferedDistanceFromOtherTimes(candidateTime, timeoutInMinutes)
              || (!schedule2.getEsmWeekends() && TimeUtil.isWeekend(candidateTime))));
      if (isMinimalBufferedDistanceFromOtherTimes(candidateTime, timeoutInMinutes) &&
		  (schedule2.getEsmWeekends() || !TimeUtil.isWeekend(candidateTime))) {
        times.add(candidateTime);
      }

    }
    return times;
  }

  private DateTime adjustStartDateToBeginningOfPeriod(DateTime startDate) {
    switch (schedule.getEsmPeriodInDays()) {
    case Schedule.ESM_PERIOD_DAY:
      return startDate;
    case Schedule.ESM_PERIOD_WEEK:
      return startDate.dayOfWeek().withMinimumValue();
    case Schedule.ESM_PERIOD_MONTH:
      return startDate.dayOfMonth().withMinimumValue();
    default:
      throw new IllegalStateException("Cannot get here.");
    }
  }

  private List<Integer> getPeriodDaysForWeek() {
    List<Integer> periods;
    if (schedule.getEsmWeekends()) {
      periods = Arrays.asList(1,2,3,4,5,6,7);
    } else {
      periods = Arrays.asList(1,2,3,4,5);
    }
    return periods;
  }

  private List<Integer> getPeriodDaysForMonthOf(DateTime startDate) {
    int dow = startDate.getDayOfWeek();
    int day = 1;
    int lastDayOfMonth = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
    List<Integer> validPeriods = new ArrayList<Integer>();
    while (day < lastDayOfMonth + 1) {
      if (schedule.getEsmWeekends() || !TimeUtil.isWeekend(dow)) {
        validPeriods.add(day);
      }
      dow++;
      if (dow == 8) {
        dow = 1;
      }
      day++;
    }
    return validPeriods;
  }

  private boolean isMinimalBufferedDistanceFromOtherTimes(DateTime plusMinutes, Minutes timeoutInMinutes) {
    for (DateTime time : times) {

      Minutes minutesBetween;
      if (time.isAfter(plusMinutes)) {
        minutesBetween = Minutes.minutesBetween(plusMinutes, time);
      } else {
        minutesBetween = Minutes.minutesBetween(time, plusMinutes);
      }

      if (minutesBetween.isLessThan(timeoutInMinutes)) {
        return false;
      }
    }
    return true;
  }



}
