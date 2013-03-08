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
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import junit.framework.TestCase;

import com.google.android.apps.paco.NonESMSignalGenerator;
import com.google.android.apps.paco.SignalSchedule;

public class NonESMSignalGeneratorTest extends TestCase {

  private SignalSchedule createDailyScheduleWithTimes(List<Long> times, int repeatRate) {
    return createSchedule(times, SignalSchedule.DAILY, repeatRate, 
        createDateTime_ThursdayAtHour(0), null, false, 1, null);
  }
  
  private SignalSchedule createWeeklyScheduleWithTimes(List<Long> times, int repeatRate, Integer daysRepeated) {
    return createSchedule(times, SignalSchedule.WEEKLY, repeatRate, 
        createDateTime_ThursdayAtHour(0), daysRepeated, false, 1, null);
  }
  
  private SignalSchedule createWeekdayScheduleWithTimes(List<Long> times) {
    return createSchedule(times, SignalSchedule.WEEKDAY, 1, createDateTime_ThursdayAtHour(0), 
        null, false, 1, null);
  }
  
  private SignalSchedule createMonthlyScheduleByDayOfMonthWithTimes(List<Long> times,
      int repeatRate, boolean byDayOfMonth, int dayOfMonth) {
    return createSchedule(times, SignalSchedule.MONTHLY, repeatRate, 
        createDateTime_ThursdayAtHour(0), null, byDayOfMonth, dayOfMonth, null);
  }
  
  private SignalSchedule createMonthlyScheduleByNthWeekWithTimes(List<Long> times,
      Integer repeatRate, Integer nthOfMonth, Integer weekDaysScheduled) {
    return createSchedule(times, SignalSchedule.MONTHLY, repeatRate, 
        createDateTime_ThursdayAtHour(0), weekDaysScheduled, false, null, 
        nthOfMonth);
  }


  private SignalSchedule createSchedule(List<Long> times, int scheduleType, 
      int repeatRate, DateTime beginDate, Integer weekDaysScheduled, boolean byDayOfMonth, 
      Integer dayOfMonth, Integer nthOfMonth) {
    SignalSchedule schedule = new SignalSchedule(1L, scheduleType, byDayOfMonth, dayOfMonth, 
        null,null,null,null, false, nthOfMonth, repeatRate, times, weekDaysScheduled, beginDate.getMillis(), true, 479 /* default timeout for non-esm*/);
    return schedule;
  }

  private DateTime createDateTime_ThursdayAtHour(int hour) {
    return createTimeAtHourOnNextDesiredDay(hour, DateTimeConstants.THURSDAY);
  }

  private DateTime createDateTime_FridayAtHour(int hour) {
    return createDateTime_ThursdayAtHour(hour).plusDays(1);
  }

  private DateTime createTimeAtHourOnNextDesiredDay(int desiredHour, int desiredDay) {
    DateTime today = createDateTime_TodayAtHour(desiredHour);
    int dow = today.getDayOfWeek();
    if (dow < desiredDay) {
      return today.plusDays(desiredDay - dow);
    } else if (dow > desiredDay) {
      return today.plusDays(7 - (dow - desiredDay));
    }
    return today;
  }  

  private DateTime createDateTime_TodayAtHour(int hour) {
    return new DateMidnight(2010, 12, 17).toDateTime().plusHours(hour);
  }
  

  public static long getHoursAndMinutesAsMillisOffset(DateTime twoPm) {
    return twoPm.getMillisOfDay();
  }


  public void testNullTimes() throws Exception {
    List<Long> times = null;
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));
  }
  
  public void testEmptyTimes() throws Exception {
    List<Long> times = new ArrayList<Long>();
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));
  }

  public void testOnceDaily_2pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testOnceDaily_2pm_At2Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_ThursdayAtHour(14);
    assertEquals(twoPm.plusDays(1), new DateTime(generator.getNextAlarmTime(onePm)));
  }  
  
  public void testOnceDaily_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }

  public void testOnceWeekday_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeekdayScheduleWithTimes(times); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }
  
  public void testTwiceDaily_12pm_2pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);    
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testTwiceDaily_2pm_3pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);    
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    times.add(getHoursAndMinutesAsMillisOffset(threePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testTwiceDaily_12pm_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }
  
  //change order
  public void testTwiceDaily_2pm_12pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  
  public void testOnceDaily_12pm_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twelvePm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }
  
  public void testRepeatEvery2DaysOnceDaily_2pm_1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }
  
  public void testRepeatEvery2DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmSaturday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_FridayAtHour(15).plusDays(1);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At1PmSaturday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(1);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(onePm)));
  }
  
  public void testRepeatEvery3DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_1pmFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_FridayAtHour(13);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_1pmSunday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(2);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_3pmSunday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime onePm = createDateTime_FridayAtHour(15).plusDays(2);
    assertEquals(twoPm.plusDays(6), new DateTime(generator.getNextAlarmTime(onePm)));
  }


  public void testRepeatEvery4DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 4); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery4DaysOnceDaily_2pm_At3PmMonday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 4); 
    
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(4);
    assertEquals(twoPm.plusDays(8), new DateTime(generator.getNextAlarmTime(threePm)));
  }
  
  public void testNullTimesWeeklyNoDaysSelected() throws Exception {
    List<Long> times = null;
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, null); 
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));
  }

  public void testWeeklyThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testWeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  public void testWeeklyThursday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  public void testWeeklyThursdayFriday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | SignalSchedule.THURSDAY) | SignalSchedule.FRIDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(1), nextAlarmTime);
  }

  public void testWeeklyThursdayFriday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | SignalSchedule.THURSDAY) | SignalSchedule.FRIDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }


  public void testWeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | SignalSchedule.THURSDAY) | SignalSchedule.MONDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4);
    assertEquals(monday, nextAlarmTime);
    
    assertEquals(monday.plusDays(3), new DateTime(generator.getNextAlarmTime(monday)));
  }

  public void testRepeatEvery2WeeklyThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(13);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm, nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_FridayAtHour(15);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_twoFridaysAway3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_FridayAtHour(15).plusWeeks(2);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(28), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | SignalSchedule.THURSDAY) | SignalSchedule.MONDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4).plusWeeks(1);
    assertEquals(monday, nextAlarmTime);
    
    assertEquals(monday.plusDays(3), new DateTime(generator.getNextAlarmTime(monday)));
  }

  public void testRepeatEvery2WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | SignalSchedule.THURSDAY) | SignalSchedule.TUESDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(1);
    assertEquals(weekFromTuesday, nextAlarmTime);
    
    assertEquals(weekFromTuesday.plusDays(2), new DateTime(generator.getNextAlarmTime(weekFromTuesday)));
  }

  public void testRepeatEvery3WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 3, 0 | SignalSchedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(21), nextAlarmTime);
  }

  public void testRepeatEvery3WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 3, (0 | SignalSchedule.THURSDAY) | SignalSchedule.TUESDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);

    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(2);
    assertEquals(weekFromTuesday, nextAlarmTime);
    
    assertEquals(weekFromTuesday.plusDays(2), new DateTime(generator.getNextAlarmTime(weekFromTuesday)));
  }
  
  public void testMonthlyByDayofMonthTuesday2pm_1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime thursday1Pm = midnightDayOfMonthDue.plusHours(13);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday1Pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testMonthlyByDayofMonthTuesday2pm_3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime thursday3Pm = midnightDayOfMonthDue.plusHours(15);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday3Pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  public void testMonthlyByDayofMonthThursday2pm_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime friday3pm = midnightDayOfMonthDue.plusHours(15).plusDays(1);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(friday3pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  public void testMonthlyByDayofMonthThursday2pm_PreviousWednesday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testMonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();   
        
    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 1, 1, 0 | SignalSchedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 1, 5, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();   
        
    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 1, 0 | SignalSchedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 2, 2, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));
            
    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | SignalSchedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(today);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 2, 16, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2010, 12, 20, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));
            
    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | SignalSchedule.FRIDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(today);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 2, 18, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery3MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2010, 12, 20, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));
            
    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 3, 3, 0 | SignalSchedule.FRIDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(today);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 3, 18, 14, 0, 0, 0), nextAlarmTime);
  }


  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));
    
    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime tuesday1pm = midnightDayOfMonthDue.minusHours(1);    
    DateTime nextAlarmTime = generator.getNextAlarmTime(tuesday1pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2pm));
    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true, thursday2pm.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday2pm);
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 2, 17, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery3MonthlyByDayofMonthThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2pm));
    
    SignalSchedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 3, true, thursday2pm.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule);
    
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday2pm.plusHours(1));
    
    assertNotNull(nextAlarmTime);    
    assertEquals(new DateTime(2011, 3, 17, 14, 0, 0, 0), new DateTime(nextAlarmTime));
  }

}
