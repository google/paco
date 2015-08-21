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
package com.google.paco.shared.scheduling;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import com.pacoapp.paco.shared.model.SignalingMechanismDAO;
import com.pacoapp.paco.shared.model2.EventInterface;
import com.pacoapp.paco.shared.model2.EventStore;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.scheduling.NonESMSignalGenerator;

public class NonESMSignalGeneratorTest extends TestCase {

  private Long NULL_EXPERIMENT_ID = null;

  class TestEventStore implements EventStore {

    @Override
    public EventInterface getEvent(Long experimentId, DateTime scheduledTime, String groupName, Long actionTriggerId,
                                   Long scheduleId) {
      return null;
    }

    @Override
    public void updateEvent(EventInterface correspondingEvent) {

    }

    @Override
    public void insertEvent(EventInterface event) {

    }

  }

  private Schedule createDailyScheduleWithTimes(List<SignalTime> times, int repeatRate) {
    return createSchedule(times, Schedule.DAILY, repeatRate,
        createDateTime_ThursdayAtHour(0), null, false, 1, null);
  }

  private Schedule createWeeklyScheduleWithTimes(List<SignalTime> times, int repeatRate, Integer daysRepeated) {
    return createSchedule(times, Schedule.WEEKLY, repeatRate,
        createDateTime_ThursdayAtHour(0), daysRepeated, false, 1, null);
  }

  private Schedule createWeekdayScheduleWithTimes(List<SignalTime> times) {
    return createSchedule(times, Schedule.WEEKDAY, 1, createDateTime_ThursdayAtHour(0),
        null, false, 1, null);
  }

  private Schedule createMonthlyScheduleByDayOfMonthWithTimes(List<SignalTime> times,
      int repeatRate, boolean byDayOfMonth, int dayOfMonth) {
    return createSchedule(times, Schedule.MONTHLY, repeatRate,
        createDateTime_ThursdayAtHour(0), null, byDayOfMonth, dayOfMonth, null);
  }

  private Schedule createMonthlyScheduleByNthWeekWithTimes(List<SignalTime> times,
      Integer repeatRate, Integer nthOfMonth, Integer weekDaysScheduled) {
    return createSchedule(times, Schedule.MONTHLY, repeatRate,
        createDateTime_ThursdayAtHour(0), weekDaysScheduled, false, null,
        nthOfMonth);
  }


  private Schedule createSchedule(List<SignalTime> times, int scheduleType,
      int repeatRate, DateTime beginDate, Integer weekDaysScheduled, boolean byDayOfMonth,
      Integer dayOfMonth, Integer nthOfMonth) {
    Schedule schedule = new Schedule(scheduleType,
                                     byDayOfMonth, dayOfMonth,
                                     null,null,null, null,
                                     nthOfMonth, repeatRate, times,
                                     weekDaysScheduled, true, 479, 59,
                                     SignalingMechanismDAO.SNOOZE_COUNT_DEFAULT,
                                     SignalingMechanismDAO.SNOOZE_TIME_DEFAULT);
    schedule.setBeginDate(beginDate.getMillis());

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


  public static SignalTime createFixedSignalTimeSkipOnMissed(DateTime twoPm) {
    return new SignalTime(SignalTime.FIXED_TIME,SignalTime.OFFSET_BASIS_RESPONSE_TIME,
                          twoPm.getMillisOfDay(), SignalTime.MISSED_BEHAVIOR_SKIP,
                          0, null);
  }

  public static SignalTime createFixedSignalTimeUseScheduledTimeOnMiss(DateTime twoPm) {
    return new SignalTime(SignalTime.FIXED_TIME,SignalTime.OFFSET_BASIS_RESPONSE_TIME,
                          twoPm.getMillisOfDay(), SignalTime.MISSED_BEHAVIOR_USE_SCHEDULED_TIME,
                          0, null);
  }

  public static SignalTime createFixedSignalTimeUseScheduledTimeOnMissWithNullInit(DateTime twoPm) {
    final SignalTime signalTime = new SignalTime();
    signalTime.setType(SignalTime.FIXED_TIME);
    signalTime.setFixedTimeMillisFromMidnight(twoPm.getMillisOfDay());

    return signalTime;
  }

  public static SignalTime createOffsetSignalTimeResponseBasisSkipOnMiss(int offsetMillis) {
    return new SignalTime(SignalTime.OFFSET_TIME, SignalTime.OFFSET_BASIS_RESPONSE_TIME,
                          0, SignalTime.MISSED_BEHAVIOR_SKIP,
                          offsetMillis, null);
  }

  public static SignalTime createOffsetSignalTimeResponseBasisUseScheduledTimeOnMiss(int offsetMillis) {
    return new SignalTime(SignalTime.OFFSET_TIME, SignalTime.OFFSET_BASIS_RESPONSE_TIME,
                          0, SignalTime.MISSED_BEHAVIOR_USE_SCHEDULED_TIME,
                          offsetMillis, null);
  }


  public void testNullTimes() throws Exception {
    List<SignalTime> times = null;
    Schedule schedule = createDailyScheduleWithTimes(times, 1);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));

  }

  public void testEmptyTimes() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    Schedule schedule = createDailyScheduleWithTimes(times, 1);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));
  }

  public void testOnceDaily_2pm_At1Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testOnceDaily_2pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testOnceDaily_2pm_At2Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(14);
    assertEquals(twoPm.plusDays(1), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testOnceDaily_2pm_At3Pm_onFriday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }

  public void testOnceWeekday_2pm_At3Pm_onFriday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeekdayScheduleWithTimes(times);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }

  public void testTwiceDaily_12pm_2pm_At1Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(createFixedSignalTimeSkipOnMissed(twelvePm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testTwiceDaily_2pm_3pm_At1Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    times.add(createFixedSignalTimeSkipOnMissed(threePm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testTwoFixedTimes() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(createFixedSignalTimeUseScheduledTimeOnMissWithNullInit(twelvePm));
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeUseScheduledTimeOnMissWithNullInit(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }


  public void testTwiceDaily_12pm_2pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twelvePm));

    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twoPm));

    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  //change order
  public void testTwiceDaily_2pm_12pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();

    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twelvePm));

    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twoPm));

    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(generator.getNextAlarmTime(threePm)));
  }


  public void testOnceDaily_12pm_2pm_At3Pm_onFriday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twelvePm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);


    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(twoPm));

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twelvePm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePmFriday)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 2);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 2);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmFriday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 2);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmSaturday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 2);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_FridayAtHour(15).plusDays(1);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery2DaysOnceDaily_2pm_At1PmSaturday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 2);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(1);
    assertEquals(twoPm.plusDays(2), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 3);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_1pmFriday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 3);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_FridayAtHour(13);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_1pmSunday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 3);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(2);
    assertEquals(twoPm.plusDays(3), new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testRepeatEvery3DaysOnceDaily_2pm_3pmSunday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 3);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_FridayAtHour(15).plusDays(2);
    assertEquals(twoPm.plusDays(6), new DateTime(generator.getNextAlarmTime(onePm)));
  }


  public void testRepeatEvery4DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 4);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testRepeatEvery4DaysOnceDaily_2pm_At3PmMonday() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 4);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(4);
    assertEquals(twoPm.plusDays(8), new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testNullTimesWeeklyNoDaysSelected() throws Exception {
    List<SignalTime> times = null;
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, null);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);
    assertNull(generator.getNextAlarmTime(createDateTime_ThursdayAtHour(13)));
  }

  public void testWeeklyThursday2pm_Thursday1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testWeeklyThursday2pm_Thursday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  public void testWeeklyThursday2pm_Sunday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  public void testWeeklyThursdayFriday2pm_Thursday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | Schedule.THURSDAY) | Schedule.FRIDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(1), nextAlarmTime);
  }

  public void testWeeklyThursdayFriday2pm_Sunday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | Schedule.THURSDAY) | Schedule.FRIDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }


  public void testWeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | Schedule.THURSDAY) | Schedule.MONDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4);
    assertEquals(monday, nextAlarmTime);

    assertEquals(monday.plusDays(3), new DateTime(generator.getNextAlarmTime(monday)));
  }

  public void testRepeatEvery2WeeklyThursday2pm_Thursday1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(13);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm, nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_Friday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_FridayAtHour(15);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursday2pm_twoFridaysAway3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_FridayAtHour(15).plusWeeks(2);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(28), nextAlarmTime);
  }

  public void testRepeatEvery2WeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | Schedule.THURSDAY) | Schedule.MONDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4).plusWeeks(1);
    assertEquals(monday, nextAlarmTime);

    assertEquals(monday.plusDays(3), new DateTime(generator.getNextAlarmTime(monday)));
  }

  public void testRepeatEvery2WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | Schedule.THURSDAY) | Schedule.TUESDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);

    DateTime nextAlarmTime = generator.getNextAlarmTime(monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(1);
    assertEquals(weekFromTuesday, nextAlarmTime);

    assertEquals(weekFromTuesday.plusDays(2), new DateTime(generator.getNextAlarmTime(weekFromTuesday)));
  }

  public void testRepeatEvery3WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 3, 0 | Schedule.THURSDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = generator.getNextAlarmTime(threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(21), nextAlarmTime);
  }

  public void testRepeatEvery3WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    Schedule schedule = createWeeklyScheduleWithTimes(times, 3, (0 | Schedule.THURSDAY) | Schedule.TUESDAY);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);

    DateTime nextAlarmTime = generator.getNextAlarmTime(monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(2);
    assertEquals(weekFromTuesday, nextAlarmTime);

    assertEquals(weekFromTuesday.plusDays(2), new DateTime(generator.getNextAlarmTime(weekFromTuesday)));
  }

  public void testMonthlyByDayofMonthTuesday2pm_1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime thursday1Pm = midnightDayOfMonthDue.plusHours(13);
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday1Pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testMonthlyByDayofMonthTuesday2pm_3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime thursday3Pm = midnightDayOfMonthDue.plusHours(15);
    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday3Pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  public void testMonthlyByDayofMonthThursday2pm_Friday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime friday3pm = midnightDayOfMonthDue.plusHours(15).plusDays(1);
    DateTime nextAlarmTime = generator.getNextAlarmTime(friday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  public void testMonthlyByDayofMonthThursday2pm_PreviousWednesday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testMonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();

    Schedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 1, 1, 0 | Schedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 1, 5, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();

    Schedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 1, 0 | Schedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);
    DateTime nextAlarmTime = generator.getNextAlarmTime(wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 2, 2, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Friday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime today = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(today));

    Schedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | Schedule.WEDNESDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime nextAlarmTime = generator.getNextAlarmTime(today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 2, 16, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime today = new DateTime(2010, 12, 20, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(today));

    Schedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | Schedule.FRIDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime nextAlarmTime = generator.getNextAlarmTime(today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 2, 18, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery3MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime today = new DateTime(2010, 12, 20, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(today));

    Schedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 3, 3, 0 | Schedule.FRIDAY);
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime nextAlarmTime = generator.getNextAlarmTime(today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 18, 14, 0, 0, 0), nextAlarmTime);
  }


  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true, midnightDayOfMonthDue.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime tuesday1pm = midnightDayOfMonthDue.minusHours(1);
    DateTime nextAlarmTime = generator.getNextAlarmTime(tuesday1pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday2pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2pm));

    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true, thursday2pm.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday2pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 2, 17, 14, 0, 0, 0), nextAlarmTime);
  }

  public void testRepeatEvery3MonthlyByDayofMonthThursday2pm_Thursday3pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime thursday2pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(createFixedSignalTimeSkipOnMissed(thursday2pm));

    Schedule schedule = createMonthlyScheduleByDayOfMonthWithTimes(times, 3, true, thursday2pm.getDayOfMonth());
    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime nextAlarmTime = generator.getNextAlarmTime(thursday2pm.plusHours(1));

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 17, 14, 0, 0, 0), new DateTime(nextAlarmTime));
  }


  public void testDailyFixedTimePlus60minuteOffsetFromResponseTimeAt1pm() throws Exception {
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(60 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, null, null, null);

    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(generator.getNextAlarmTime(onePm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTime2pm_3pmMissedResponse() throws Exception {
    EventStore mockEp = new TestEventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {
          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }
          @Override
          public DateTime getResponseTime() {
            return null;
          }
        };
      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(120 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime fourPm = createDateTime_ThursdayAtHour(16);
    DateTime twoPmTomorrow = twoPm.plusDays(1);
    DateTime threePm = createDateTime_ThursdayAtHour(15);

    assertEquals(twoPmTomorrow, new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTime2pm_3pmResponded() throws Exception {
    //setup
    EventStore mockEp = new TestEventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {
          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            return scheduledTime.plusMinutes(10);
          }
        };
      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(120 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime four10Pm = createDateTime_ThursdayAtHour(16).plusMinutes(10);
    DateTime twoPmTomorrow = twoPm.plusDays(1);
    DateTime threePm = createDateTime_ThursdayAtHour(15);

    assertEquals(four10Pm, new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTime2pm_430pmResponded() throws Exception {
    EventStore mockEp = new TestEventStore() {
      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {
          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            return scheduledTime.plusMinutes(10);
          }
        };

      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(120 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime twoPmTomorrow = twoPm.plusDays(1);
    DateTime four30Pm = createDateTime_ThursdayAtHour(16).plusMinutes(30);

    assertEquals(twoPmTomorrow, new DateTime(generator.getNextAlarmTime(four30Pm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTime2pm_3pmNotRespondedYet() throws Exception {
    EventStore mockEp = new TestEventStore() {
      @Override
      public EventInterface getEvent(Long experimentId, DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return null;
      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(120 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime fourPm = createDateTime_ThursdayAtHour(16);
    DateTime twoPmTomorrow = twoPm.plusDays(1);
    DateTime threePm = createDateTime_ThursdayAtHour(15);

    assertEquals(fourPm, new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeWithUseScheduledTime2pm_3pmMissedResponse() throws Exception {
    EventStore mockEp = new TestEventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            return null;
          }
        };
      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(createFixedSignalTimeSkipOnMissed(twoPm));
    times.add(createOffsetSignalTimeResponseBasisUseScheduledTimeOnMiss(120 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime fourPm = createDateTime_ThursdayAtHour(16);
    DateTime twoPmTomorrow = twoPm.plusDays(1);
    DateTime threePm = createDateTime_ThursdayAtHour(15);

    assertEquals(fourPm, new DateTime(generator.getNextAlarmTime(threePm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenFixedMissedFirstResponse() throws Exception {
    EventStore mockEp = new TestEventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            return null;
          }
        };
      }
    };
    List<SignalTime> times = new ArrayList<SignalTime>();
    DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    DateTime sixPm = eightAm.plusHours(10);
    times.add(createFixedSignalTimeSkipOnMissed(sixPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);



    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(nineAm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenFixedRespondedFirstAndSecond() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    DateTime sixPm = eightAm.plusHours(10);
    times.add(createFixedSignalTimeSkipOnMissed(sixPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    EventStore mockEp = new TestEventStore() {
      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            if (scheduledTime.equals(eightAm)) {
              return eightAm.plusMinutes(10);
            } else if (scheduledTime.equals(eightAm.plusMinutes(40))) {
              return eightAm.plusMinutes(50);
            }  else {
              return null;
            }

          }
        };
      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eight10Am.plusMinutes(30), new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(sixPm, new DateTime(generator.getNextAlarmTime(nineAm)));
    assertEquals(sixPm, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenFixedWithSkipNotRespondedToMiddle() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    DateTime sixPm = eightAm.plusHours(10);
    times.add(createFixedSignalTimeSkipOnMissed(sixPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);


    EventStore mockEp = new TestEventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            if (scheduledTime.equals(eightAm)) {
              return eightAm.plusMinutes(10);
            } else {
              return null;
            }
          }

        };      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eight10Am.plusMinutes(30), new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(nineAm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenFixedWithUseScheduledNotRespondedToMiddle() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    DateTime sixPm = eightAm.plusHours(10);
    times.add(createFixedSignalTimeUseScheduledTimeOnMiss(sixPm));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);
    EventStore mockEp = new TestEventStore() {
      public EventInterface getEvent(Long experimentServerId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            if (scheduledTime.equals(eightAm)) {
              return eightAm.plusMinutes(10);
            } else {
              return null;
            }

          }
        };
      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eight10Am.plusMinutes(30), new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(sixPm, new DateTime(generator.getNextAlarmTime(nineAm)));
    assertEquals(sixPm, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenAnotherOffset_NotRespondedToFirst() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(10 * 60 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    EventStore mockEp = new TestEventStore() {
      public EventInterface getEvent(Long experimentServerId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
              return null;
          }
        };
      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenAnotherOffset_RespondedToFirst() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(10 * 60 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    EventStore mockEp = new TestEventStore() {
      public EventInterface getEvent(Long experimentServerId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            if (scheduledTime.equals(eightAm)) {
              return eightAm.plusMinutes(10);
            } else {
              return null;
            }

          }
        };
      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eight10Am.plusMinutes(30), new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }

  public void testDailyFixedTimeThenOffsetFromResponseTimeThenAnotherOffset_RespondedToFirstAndSecond() throws Exception {

    List<SignalTime> times = new ArrayList<SignalTime>();
    final DateTime eightAm = createDateTime_ThursdayAtHour(8);
    times.add(createFixedSignalTimeSkipOnMissed(eightAm));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(30 * 60 * 1000));
    times.add(createOffsetSignalTimeResponseBasisSkipOnMiss(10 * 60 * 60 * 1000));
    Schedule schedule = createDailyScheduleWithTimes(times, 1);

    EventStore mockEp = new TestEventStore() {
      public EventInterface getEvent(Long experimentServerId, final DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        return new EventInterface() {

          @Override
          public DateTime getScheduledTime() {
            return scheduledTime;
          }

          @Override
          public DateTime getResponseTime() {
            if (scheduledTime.equals(eightAm)) {
              return eightAm.plusMinutes(10);
            } else if (scheduledTime.equals(eightAm.plusMinutes(40))) {
              return eightAm.plusMinutes(50);
            }  else {
              return null;
            }

          }
        };
      }
    };

    NonESMSignalGenerator generator = new NonESMSignalGenerator(schedule, NULL_EXPERIMENT_ID, mockEp, null, null);

    DateTime sevenAm = createDateTime_ThursdayAtHour(7);
    DateTime eight10Am = sevenAm.plusHours(1).plusMinutes(10);
    DateTime nineAm = sevenAm.plusHours(2);
    DateTime fourPm = sevenAm.plusHours(9);
    DateTime sevenPm = fourPm.plusHours(3);
    DateTime eightAmTomorrow = eightAm.plusDays(1);

    assertEquals(eightAm, new DateTime(generator.getNextAlarmTime(sevenAm)));
    assertEquals(eight10Am.plusMinutes(30), new DateTime(generator.getNextAlarmTime(eight10Am)));
    assertEquals(eight10Am.plusMinutes(30).plusMinutes(10).plusMinutes(10 * 60), new DateTime(generator.getNextAlarmTime(fourPm)));
    assertEquals(eightAmTomorrow, new DateTime(generator.getNextAlarmTime(sevenPm)));
  }
}

