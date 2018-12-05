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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Minutes;

import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.scheduling.EsmGenerator2;
import com.pacoapp.paco.shared.util.TimeUtil;

public class ESMSignalGeneratorTest extends TestCase {

  private final class EsmRunnable<V> implements RunnableFuture<V> {
    @Override
    public void run() {
      runGeneration();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isCancelled() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isDone() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      // TODO Auto-generated method stub
      return null;
    }
  }

  public void test1xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test1xPerDayWeekendDayFails() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 19, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(0, signals.size());
  }

  public void testEsmDailyNoWeekendFailsToDoNextWeek() throws Exception {
    DateTime startDate = new DateTime(2012, 3, 23, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());

    DateTime nextPeriod = startDate.plusDays(schedule.convertEsmPeriodToDays());
    signals = esmGen.generateForSchedule(nextPeriod, schedule);
    assertTrue(TimeUtil.isWeekend(nextPeriod));
    assertEquals(0, signals.size());

    if (schedule.convertEsmPeriodToDays() == 1 && !schedule.getEsmWeekends() && TimeUtil.isWeekend(nextPeriod)) {
      nextPeriod = TimeUtil.skipWeekends(nextPeriod);
    }
    assertFalse(TimeUtil.isWeekend(nextPeriod));
    signals = esmGen.generateForSchedule(nextPeriod, schedule);
    assertEquals(8, signals.size());


  }

  public void testEsmWeeklyNoWeekendFailsToDoNextWeek() throws Exception {
    DateTime startDate = new DateTime(2012, 3, 23, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());

    DateTime nextPeriod = startDate.plusDays(schedule.convertEsmPeriodToDays());
    signals = esmGen.generateForSchedule(nextPeriod, schedule);
    assertFalse(TimeUtil.isWeekend(nextPeriod));
    assertEquals(8, signals.size());

  }


  private Interval createDayInterval(DateTime startDate, long endHourMillis, long startHourMillis) {
    return new Interval(startDate.plus(startHourMillis), startDate.plus(endHourMillis));
  }

  public void test2xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 2;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(2, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test8xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis), endHourMillis, startHourMillis, signals, esmWeekends);
    Minutes minimumBufferInMinutes = Minutes.minutes(schedule.getMinimumBuffer());
    assertSignalsRespectMinimumBuffer(signals, minimumBufferInMinutes);
  }

  public void test8xPerDayEqualBuffer() throws Exception {
    final EsmRunnable r1 = new EsmRunnable();
    final EsmRunnable r2 = new EsmRunnable();
    final EsmRunnable r3 = new EsmRunnable();
    final EsmRunnable r4 = new EsmRunnable();
    final EsmRunnable r5 = new EsmRunnable();
//    new Thread(r1).run();
//    new Thread(r2).run();
//    new Thread(r3).run();
//    new Thread(r4).run();
//    new Thread(r5).run();
    for(int i=0; i< 2; i++) {
      runGeneration();
    }
    //r1.get();r2.get();r3.get();r4.get();r5.get();
  }

  public void runGeneration() {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(22).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis), endHourMillis, startHourMillis, signals, esmWeekends);
    Minutes minimumBufferInMinutes = Minutes.minutes(schedule.getMinimumBuffer());
    assertSignalsRespectMinimumBuffer(signals, minimumBufferInMinutes);
  }


  public void testMinimumBufferAssertion() throws Exception {
    List<DateTime> badSignals = new ArrayList<DateTime>();

    DateTime startTime = new DateTime();
    DateTime endTime = startTime.plusMinutes(10);
    badSignals.add(startTime);
    badSignals.add(endTime);
    Minutes minimumBufferInMinutes = Minutes.minutes(15);
    try {
      assertSignalsRespectMinimumBuffer(badSignals, minimumBufferInMinutes);
      fail("should have thrown an exception");
    } catch (AssertionFailedError a) {}
  }

  private void assertSignalsRespectMinimumBuffer(List<DateTime> signals, Minutes minimumBufferInMinutes) {
    Collections.sort(signals, DateTimeComparator.getInstance());
    DateTime lastSignal = signals.get(0);
    for (int i = 1; i < signals.size(); i++) {
      assertTrue("comparing " +lastSignal+", "+signals.get(i),
              !Minutes.minutesBetween(lastSignal,
                                     signals.get(i)).isLessThan(minimumBufferInMinutes));
      lastSignal = signals.get(i);
    }
  }

  public void test1xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test2xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 2;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(2, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }


  public void test5xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 5;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(5, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test8xPerDayForThreeDays() throws Exception {
    DateTime startDate = new DateTime(2016, 4, 27, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(22).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = Schedule.ESM_PERIOD_DAY;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis), endHourMillis, startHourMillis, signals, esmWeekends);
    Minutes minimumBufferInMinutes = Minutes.minutes(schedule.getMinimumBuffer());
    assertSignalsRespectMinimumBuffer(signals, minimumBufferInMinutes);
  }


  public void test10xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test5xPerWeekWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 5;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(5, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test10xPerWeekWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = Schedule.ESM_PERIOD_WEEK;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(createWeekInterval(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test30xPerMonthWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 30;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(30, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test1xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test10xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test15xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 15;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(15, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  /** Actual number of weekdays in this month
   *
   * @throws Exception
   */
  public void test23xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 23;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(23, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void test15xPerMonthWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 15;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(15, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  public void testMonthStartDateGetsAdjustedIfIncorrect() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 9, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 31;
    int esmPeriod = Schedule.ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    Schedule schedule = getScheduleWith(startDate, startHourMillis, endHourMillis,
        esmPeriod, esmFrequency, esmWeekends);

    EsmGenerator2 esmGen = new EsmGenerator2();
    List<DateTime> signals = esmGen.generateForSchedule(startDate, schedule);

    assertEquals(31, signals.size());
    assertAllSignalsAreValid(createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);

  }


  private Interval createMonthIntervalForCalendarMonthOf(DateTime startDate, long endHourMillis, long startHourMillis,
      Schedule schedule, Boolean esmWeekends) {
    int daysInPeriod = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
    DateTime firstOfMonth = startDate.dayOfMonth().withMinimumValue();
    return new Interval(firstOfMonth.plus(startHourMillis), firstOfMonth.plusDays(daysInPeriod).plus(endHourMillis));
  }


  private Interval createWeekInterval(DateTime startDate, long endHourMillis, long startHourMillis,
      Schedule schedule, Boolean esmWeekends) {
    int daysInPeriod = schedule.convertEsmPeriodToDays();

    if (!esmWeekends) {
      daysInPeriod -= 2;
    }
    startDate = startDate.dayOfWeek().withMinimumValue();
    return new Interval(startDate.plus(startHourMillis), startDate.plusDays(daysInPeriod).plus(endHourMillis));
  }

  private Schedule getScheduleWith(DateTime startDate, long startHourMillis,
      long endHourMillis, int esmPeriod, int esmFrequency, boolean esmWeekends) {
    Schedule schedule = new Schedule(Schedule.ESM,
        false, null, // Not important to ESM testing
        endHourMillis, esmFrequency, esmPeriod, startHourMillis,
        null, null, null, null, // Not important to ESM testing
        esmWeekends, 15, 59, PacoNotificationAction.SNOOZE_COUNT_DEFAULT, PacoNotificationAction.SNOOZE_TIME_DEFAULT);
    schedule.setBeginDate(startDate.getMillis());
    return schedule;
  }

  private void assertAllSignalsAreValid(Interval interval, long endHourMillis, long startHourMillis,
      List<DateTime> signals, boolean esmWeekends) {
    for (DateTime dateTime : signals) {
      System.out.println("Datetime: " + dateTime);
      assertSignalIsWithinValidHoursAndInterval(interval, endHourMillis, startHourMillis, dateTime, esmWeekends);
    }
  }

  private void assertSignalIsWithinValidHoursAndInterval(Interval period, long endHourMillis, long startHourMillis,
      DateTime dateTime, boolean esmWeekends) {
    assertTrue("before start: " + dateTime, !dateTime.isBefore(dateTime.withMillisOfDay((int)startHourMillis)));
    assertTrue("after end: " + dateTime, !dateTime.isAfter(dateTime.withMillisOfDay((int)endHourMillis)));
    assertTrue("Periods doesn't contain: " + dateTime, period.contains(dateTime));
    if (!esmWeekends) {
      assertTrue("Is weekend when no weekends" + dateTime, dateTime.getDayOfWeek() != DateTimeConstants.SATURDAY &&
          dateTime.getDayOfWeek() != DateTimeConstants.SUNDAY);
    }
  }

}
