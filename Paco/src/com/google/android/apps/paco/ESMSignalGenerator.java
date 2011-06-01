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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;

public class ESMSignalGenerator {

  public static final int DAYS_OF_WEEK_TO_SIGNAL = 5;
  public static final int DEFAULT_SIGNAL_FREQUENCY = 5;

  /**
   * TODO (bobevans): deal with the case where the endDateTime is in the middle of the week, so that
   * we don't extend beyond the end date
   * 
   * @param firstDayOfPeriod
   * @param startHour
   * @param endHour
   * @param signalFrequency
   * @param endDateTime
   * @return
   */
  public List<DateTime> generateSignalTimesForPeriod(DateMidnight firstDayOfPeriod, 
      int startHour, int endHour, int signalFrequency, DateTime endDateTime) {
    Random random = new Random();
    int[] daysOfWeekChosen = pickDaysOfWeek(random, signalFrequency);
    return pickSignalTimesOnChosenDays(firstDayOfPeriod, 
        daysOfWeekChosen, startHour, endHour, random);
  }

  @SuppressWarnings("unchecked")
  private List<DateTime> pickSignalTimesOnChosenDays(DateMidnight firstDayOfPeriod,
      int[] daysOfWeekChosen, int startHour, int endHour, Random random) {
    int durationInMinutes = (endHour - startHour) * 60;
    int halfDayInMinutes = durationInMinutes / 2;
    List<DateTime> times = new ArrayList<DateTime>();
    for (int dayOfTheWeek = 0; dayOfTheWeek < daysOfWeekChosen.length; dayOfTheWeek++) {
      int timesForDayOfWeek = daysOfWeekChosen[dayOfTheWeek];

      boolean secondHalfOfDay = random.nextBoolean();
      boolean firstPass = true;
      for (int j = 0; j < timesForDayOfWeek; j++) {
        // this assumes a max of twice per day for signals (3-10 per week
        // scheme).
        int startTimeInMinutes = 0;
        int endTimeInMinutes = 0;
        if (!firstPass) {
          secondHalfOfDay = !secondHalfOfDay;
        } else {
          firstPass = false;
        }
        if (secondHalfOfDay) {
          endTimeInMinutes = endHour * 60;
          startTimeInMinutes = endTimeInMinutes - halfDayInMinutes;

        } else {
          startTimeInMinutes = startHour * 60;
          endTimeInMinutes = startTimeInMinutes + halfDayInMinutes;

        }
        generateTimeWithin(startTimeInMinutes, endTimeInMinutes, times, firstDayOfPeriod,
            dayOfTheWeek, random);

      }
    }

    // old scheme based on a max times per week and a max times per day
    // for (Integer dayOfTheWeek : daysOfWeekChosen) {
    // List<DateTime> timesThatDay = new ArrayList<DateTime>();
    // DateMidnight currentDay = firstDayOfWeek.plusDays(dayOfTheWeek);
    // DateTime startTime = currentDay.toDateTime().plusHours(startHour);
    // int rangeInMinutes = (endHour - startHour) * 60;
    //      
    // int timesPerDaySignalled = 0;
    // while (timesPerDaySignalled < MAX_TIMES_PER_DAY) {
    // DateTime nextCandidate =
    // startTime.plusMinutes(random.nextInt(rangeInMinutes));
    // if (!isWithinTimeoutMinutesOfAnotherSignal(nextCandidate, timesThatDay))
    // {
    // timesThatDay.add(nextCandidate);
    // timesPerDaySignalled++;
    // }
    // }
    // times.addAll(timesThatDay);
    // }
    Collections.sort(times);
    return times;
  }

  private void generateTimeWithin(int startOfDayInMinutes, int endOfDayInMinutes,
      List<DateTime> times, DateMidnight firstDayOfWeek, int dayOfTheWeek, Random random) {
    DateMidnight currentDay = firstDayOfWeek.plusDays(dayOfTheWeek);
    DateTime startTime = currentDay.toDateTime().plusMinutes(startOfDayInMinutes);
    int rangeInMinutes = endOfDayInMinutes - startOfDayInMinutes;

    boolean foundTime = false;
    while (!foundTime) {
      DateTime nextCandidate = startTime.plusMinutes(random.nextInt(rangeInMinutes));
      if (!isWithinTimeoutMinutesOfAnotherSignal(nextCandidate, times)) {
        times.add(nextCandidate);
        return;
      }
    }
  }

  // TODO (bobevans): make this parameterized about when signalling happens because
  // this is currently hardwired to weekday period
  private int[] pickDaysOfWeek(Random random, int signalsPerWeek) {
    int[] daysOfWeekChosenArray = new int[DAYS_OF_WEEK_TO_SIGNAL];
    int numberChosen = 0;
    while (numberChosen < signalsPerWeek) {
      Integer dayOfWeek = random.nextInt(DAYS_OF_WEEK_TO_SIGNAL);
      if ((signalsPerWeek > DAYS_OF_WEEK_TO_SIGNAL && daysOfWeekChosenArray[dayOfWeek] < 2)
          || (signalsPerWeek < (DAYS_OF_WEEK_TO_SIGNAL + 1) 
              && daysOfWeekChosenArray[dayOfWeek] < 1)) {
        daysOfWeekChosenArray[dayOfWeek] = daysOfWeekChosenArray[dayOfWeek] + 1;
        numberChosen++;
      }
    }
    return daysOfWeekChosenArray;
  }

  private  boolean isWithinTimeoutMinutesOfAnotherSignal(DateTime nextCandidate,
      List<DateTime> times) {
    for (DateTime dateTime : times) {
      Interval interval;
      if (nextCandidate.isBefore(dateTime)) {
        interval = new Interval(nextCandidate, dateTime);
      } else {
        interval = new Interval(dateTime, nextCandidate);
      } // equal is ok, just can't create an interval that is backwards.
      if (interval.toPeriod(PeriodType.minutes()).getMinutes() < 60) {
        return true;
      }
    }
    return false;
  }


}
