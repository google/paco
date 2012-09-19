package com.google.paco.shared.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.junit.Test;

import com.google.common.collect.Lists;

public class LegacySignalScheduleTest {
  // ESM Tests
  private final static int ESM_PERIOD_DAY = 1;
  private final static int ESM_PERIOD_WEEK = 2;
  private final static int ESM_PERIOD_MONTH = 3;
  private final static Minutes BUFFER_MILLIS = Minutes.minutes(5);

  private List<DateTime> generateForSchedule(DateTime startDate, SignalSchedule schedule) {
    List<DateTime> alarms = Lists.newArrayList();

    SignalScheduleIterator iterator = schedule.iterator();

    while (iterator.hasNext() && alarms.size() < 40) {
      alarms.add(iterator.next());
    }

    return alarms;
  }

  @Test
  public void test1xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test1xPerDayWeekendDayFails() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 19, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    // assertEquals(0, signals.size());
    // Our signal schedule always returns the next valid date assuming one exists.
    assertEquals(1, signals.size());
  }

  @Test
  public void testEsmDailyNoWeekendFailsToDoNextWeek() throws Exception {
    DateTime startDate = new DateTime(2012, 3, 23, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());

    DateTime nextPeriod = startDate.plusDays(convertEsmPeriodToDays(esmPeriod));
    signals = generateForSchedule(nextPeriod, schedule);
    assertTrue(isWeekend(nextPeriod));

    // our schedule always returns the next valid dates regardless
    // assertEquals(0, signals.size());
    assertEquals(8, signals.size());

    if (convertEsmPeriodToDays(esmPeriod) == 1 && !esmWeekends && isWeekend(nextPeriod)) {
      nextPeriod = skipWeekends(nextPeriod);
    }
    assertFalse(isWeekend(nextPeriod));
    signals = generateForSchedule(nextPeriod, schedule);
    assertEquals(8, signals.size());
  }

  @Test
  public void testEsmWeeklyNoWeekendFailsToDoNextWeek() throws Exception {
    DateTime startDate = new DateTime(2012, 3, 23, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());

    DateTime nextPeriod = startDate.plusDays(convertEsmPeriodToDays(esmPeriod));
    signals = generateForSchedule(nextPeriod, schedule);

    assertFalse(isWeekend(nextPeriod));
    assertEquals(8, signals.size());

  }


  private Interval createDayInterval(DateTime startDate, long endHourMillis, long startHourMillis) {
    return new Interval(startDate.plus(startHourMillis), startDate.plus(endHourMillis));
  }

  @Test
  public void test2xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 2;
    int esmPeriod = ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(2, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test8xPerDay() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 8;
    int esmPeriod = ESM_PERIOD_DAY;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(8, signals.size());
    assertAllSignalsAreValid(createDayInterval(startDate, endHourMillis, startHourMillis),
        endHourMillis, startHourMillis, signals, esmWeekends);
    assertSignalsRespectMinimumBuffer(signals);
  }

  @Test
  public void testMinimumBufferAssertion() throws Exception {
    List<DateTime> badSignals = new ArrayList<DateTime>();

    DateTime startTime = new DateTime();
    DateTime endTime = startTime.plus(BUFFER_MILLIS).minusMinutes(1);
    badSignals.add(startTime);
    badSignals.add(endTime);
    try {
      assertSignalsRespectMinimumBuffer(badSignals);
      fail("should have thrown an exception");
    } catch (AssertionError a) {
    }
  }

  private void assertSignalsRespectMinimumBuffer(List<DateTime> signals) {
    Collections.sort(signals, DateTimeComparator.getInstance());
    DateTime lastSignal = signals.get(0);
    for (int i = 1; i < signals.size(); i++) {
      assertTrue("comparing " + lastSignal + ", " + signals.get(i),
          !Minutes.minutesBetween(lastSignal, signals.get(i)).isLessThan(BUFFER_MILLIS));
      lastSignal = signals.get(i);
    }
  }

  @Test
  public void test1xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test2xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 2;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(2, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test5xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 5;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(5, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test10xPerWeekNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test5xPerWeekWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 5;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = true;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(5, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test10xPerWeekWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 20, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = ESM_PERIOD_WEEK;
    boolean esmWeekends = true;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(
        createWeekInterval(startDate, endHourMillis, startHourMillis, esmFrequency, esmWeekends),
        endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test30xPerMonthWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 30;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(30, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test1xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 1;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(1, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test10xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 10;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(10, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test15xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 15;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(15, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  /**
   * Actual number of weekdays in this month
   * 
   * @throws Exception
   */
  @Test
  public void test23xPerMonthNoWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 23;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = false;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(23, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void test15xPerMonthWeekends() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 1, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 15;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(15, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);
  }

  @Test
  public void testMonthStartDateGetsAdjustedIfIncorrect() throws Exception {
    DateTime startDate = new DateTime(2010, 12, 9, 0, 0, 0, 0);

    long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();
    long startHourMillis = Hours.hours(9).toStandardDuration().getMillis();
    int esmFrequency = 31;
    int esmPeriod = ESM_PERIOD_MONTH;
    boolean esmWeekends = true;

    SignalSchedule schedule =
        getScheduleWith(startDate, startHourMillis, endHourMillis, esmPeriod, esmFrequency,
            esmWeekends);


    List<DateTime> signals = generateForSchedule(startDate, schedule);

    assertEquals(31, signals.size());
    assertAllSignalsAreValid(
        createMonthIntervalForCalendarMonthOf(startDate, endHourMillis, startHourMillis, schedule,
            esmWeekends), endHourMillis, startHourMillis, signals, esmWeekends);

  }


  private Interval createMonthIntervalForCalendarMonthOf(DateTime startDate, long endHourMillis,
      long startHourMillis, SignalSchedule schedule, Boolean esmWeekends) {
    int daysInPeriod = startDate.dayOfMonth().withMaximumValue().getDayOfMonth();
    DateTime firstOfMonth = startDate.dayOfMonth().withMinimumValue();
    return new Interval(firstOfMonth.plus(startHourMillis), firstOfMonth.plusDays(daysInPeriod)
        .plus(endHourMillis));
  }


  private Interval createWeekInterval(DateTime startDate, long endHourMillis, long startHourMillis,
      int esmFrequency, Boolean esmWeekends) {
    int daysInPeriod = esmFrequency * 7; // convertEsmPeriodToDays(schedule);

    if (!esmWeekends) {
      daysInPeriod -= 2;
    }
    startDate = startDate.dayOfWeek().withMinimumValue();
    return new Interval(startDate.plus(startHourMillis), startDate.plusDays(daysInPeriod).plus(
        endHourMillis));
  }

  private SignalSchedule getScheduleWith(DateTime startDate, long startHourMillis,
      long endHourMillis, int esmPeriod, int esmFrequency, boolean esmWeekends) {

    // SignalSchedule schedule =
    // new SignalSchedule(1L, SignalSchedule.ESM, false,
    // null, // Not important to ESM testing
    // endHourMillis, esmFrequency, esmPeriod, startHourMillis, esmWeekends, null, null, null,
    // null, // Not important to ESM testing
    // startDate.getMillis(), true);

    RandomSignal signal = new RandomSignal();
    signal.setStartTime(LocalTime.fromMillisOfDay(startHourMillis));
    signal.setEndTime(LocalTime.fromMillisOfDay(endHourMillis));
    signal.setFrequency(esmFrequency);

    WeeklySchedule schedule = new WeeklySchedule();
    schedule.setOnDay(WeeklySchedule.Day.Monday);
    schedule.setOnDay(WeeklySchedule.Day.Tuesday);
    schedule.setOnDay(WeeklySchedule.Day.Wednesday);
    schedule.setOnDay(WeeklySchedule.Day.Thursday);
    schedule.setOnDay(WeeklySchedule.Day.Friday);

    if (esmWeekends) {
      schedule.setOnDay(WeeklySchedule.Day.Saturday);
      schedule.setOnDay(WeeklySchedule.Day.Sunday);
    }


    schedule.setEvery(1);
    schedule.setStartDate(startDate.toLocalDate());
    schedule.setEndDate(startDate.toLocalDate());


    SignalSchedule signalSchedule = new SignalSchedule();
    signalSchedule.setSignal(signal);
    signalSchedule.setSchedule(schedule);

    return signalSchedule;
  }

  private void assertAllSignalsAreValid(Interval interval, long endHourMillis,
      long startHourMillis, List<DateTime> signals, boolean esmWeekends) {
    for (DateTime dateTime : signals) {
      assertSignalIsWithinValidHoursAndInterval(interval, endHourMillis, startHourMillis, dateTime,
          esmWeekends);
    }
  }

  private void assertSignalIsWithinValidHoursAndInterval(Interval period, long endHourMillis,
      long startHourMillis, DateTime dateTime, boolean esmWeekends) {
    assertTrue("before start: " + dateTime,
        !dateTime.isBefore(dateTime.withMillisOfDay((int) startHourMillis)));
    assertTrue("after end: " + dateTime,
        !dateTime.isAfter(dateTime.withMillisOfDay((int) endHourMillis)));
    assertTrue("Periods doesn't contain: " + dateTime, period.contains(dateTime));
    if (!esmWeekends) {
      assertTrue("Is weekend when no weekends" + dateTime,
          dateTime.getDayOfWeek() != DateTimeConstants.SATURDAY
              && dateTime.getDayOfWeek() != DateTimeConstants.SUNDAY);
    }
  }

  private int convertEsmPeriodToDays(int esmPeriod) {
    switch (esmPeriod) {
      case ESM_PERIOD_DAY:
        return 1;
      case ESM_PERIOD_WEEK:
        return 7;
      case ESM_PERIOD_MONTH:
        return 30;
      default:
        return 1;
    }
  }

  private boolean isWeekend(DateTime dateTime) {
    return (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY || dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY);
  }

  private DateTime skipWeekends(DateTime plusDays) {
    if (plusDays.getDayOfWeek() == DateTimeConstants.SATURDAY) {
      return plusDays.plusDays(2);
    } else if (plusDays.getDayOfWeek() == DateTimeConstants.SUNDAY) {
      return plusDays.plusDays(1);
    }
    return plusDays;
  }

  // Non-ESM Tests
  private final static int DAILY = 1;
  private final static int WEEKLY = 2;
  private final static int WEEKDAY = 3;
  private final static int MONTHLY = 4;

  private final static int MONDAY = 1;
  private final static int TUESDAY = 2;
  private final static int WEDNESDAY = 4;
  private final static int THURSDAY = 8;
  private final static int FRIDAY = 16;

  private SignalSchedule createDailyScheduleWithTimes(List<Long> times, int repeatRate) {
    return createSchedule(times, DAILY, repeatRate, createDateTime_ThursdayAtHour(0), null, false,
        1, null);
  }

  private SignalSchedule createWeeklyScheduleWithTimes(List<Long> times, int repeatRate,
      Integer daysRepeated) {
    return createSchedule(times, WEEKLY, repeatRate, createDateTime_ThursdayAtHour(0),
        daysRepeated, false, 1, null);
  }

  private SignalSchedule createWeekdayScheduleWithTimes(List<Long> times) {
    return createSchedule(times, WEEKDAY, 1, createDateTime_ThursdayAtHour(0), null, false, 1, null);
  }

  private SignalSchedule createMonthlyScheduleByDayOfMonthWithTimes(List<Long> times,
      int repeatRate, boolean byDayOfMonth, int dayOfMonth) {
    return createSchedule(times, MONTHLY, repeatRate, createDateTime_ThursdayAtHour(0), null,
        byDayOfMonth, dayOfMonth, null);
  }

  private SignalSchedule createMonthlyScheduleByNthWeekWithTimes(List<Long> times,
      Integer repeatRate, Integer nthOfMonth, Integer weekDaysScheduled) {
    return createSchedule(times, MONTHLY, repeatRate, createDateTime_ThursdayAtHour(0),
        weekDaysScheduled, false, null, nthOfMonth);
  }

  private SignalSchedule createSchedule(List<Long> times, int scheduleType, int repeatRate,
      DateTime beginDate, Integer weekDaysScheduled, boolean byDayOfMonth, Integer dayOfMonth,
      Integer nthOfMonth) {

    FixedSignal signal = new FixedSignal();
    if (times != null) {
      for (long time : times) {
        signal.addTime(LocalTime.fromMillisOfDay(time));
      }
    }

    Schedule schedule;

    switch (scheduleType) {
      case DAILY:
        DailySchedule dailySchedule = new DailySchedule();
        schedule = dailySchedule;
        break;

      case WEEKLY:
        WeeklySchedule weeklySchedule = new WeeklySchedule();
        if (weekDaysScheduled != null) {
          if ((weekDaysScheduled & MONDAY) == MONDAY) {
            weeklySchedule.setOnDay(WeeklySchedule.Day.Monday);
          }
          if ((weekDaysScheduled & TUESDAY) == TUESDAY) {
            weeklySchedule.setOnDay(WeeklySchedule.Day.Tuesday);
          }
          if ((weekDaysScheduled & WEDNESDAY) == WEDNESDAY) {
            weeklySchedule.setOnDay(WeeklySchedule.Day.Wednesday);
          }
          if ((weekDaysScheduled & THURSDAY) == THURSDAY) {
            weeklySchedule.setOnDay(WeeklySchedule.Day.Thursday);
          }
          if ((weekDaysScheduled & FRIDAY) == FRIDAY) {
            weeklySchedule.setOnDay(WeeklySchedule.Day.Friday);
          }
        }
        schedule = weeklySchedule;
        break;

      case WEEKDAY:
        WeeklySchedule weekdaySchedule = new WeeklySchedule();
        weekdaySchedule.setOnDay(WeeklySchedule.Day.Monday);
        weekdaySchedule.setOnDay(WeeklySchedule.Day.Tuesday);
        weekdaySchedule.setOnDay(WeeklySchedule.Day.Wednesday);
        weekdaySchedule.setOnDay(WeeklySchedule.Day.Thursday);
        weekdaySchedule.setOnDay(WeeklySchedule.Day.Friday);
        schedule = weekdaySchedule;
        break;

      case MONTHLY:
        MonthlySchedule monthlySchedule = new MonthlySchedule();

        if (byDayOfMonth) {
          monthlySchedule.setOnDay(dayOfMonth);
        } else {
          if ((weekDaysScheduled & MONDAY) == MONDAY) {
            monthlySchedule.setOnDay(MonthlySchedule.Day.Monday);
          }
          if ((weekDaysScheduled & TUESDAY) == TUESDAY) {
            monthlySchedule.setOnDay(MonthlySchedule.Day.Tuesday);
          }
          if ((weekDaysScheduled & WEDNESDAY) == WEDNESDAY) {
            monthlySchedule.setOnDay(MonthlySchedule.Day.Wednesday);
          }
          if ((weekDaysScheduled & THURSDAY) == THURSDAY) {
            monthlySchedule.setOnDay(MonthlySchedule.Day.Thursday);
          }
          if ((weekDaysScheduled & FRIDAY) == FRIDAY) {
            monthlySchedule.setOnDay(MonthlySchedule.Day.Friday);
          }
          monthlySchedule.setOnWeek(nthOfMonth);
        }

        schedule = monthlySchedule;
        break;

      default:
        throw new UnsupportedOperationException("Unknown scheduleTye: " + scheduleType);
    }

    schedule.setEvery(repeatRate);
    schedule.setStartDate(beginDate.toLocalDate());

    SignalSchedule signalSchedule = new SignalSchedule();
    signalSchedule.setSignal(signal);
    signalSchedule.setSchedule(schedule);

    return signalSchedule;
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

  @Test
  public void testNullTimes() throws Exception {
    List<Long> times = null;
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);

    assertNull(getNextAlarmTime(schedule, createDateTime_ThursdayAtHour(13)));
  }

  @Test
  public void testEmptyTimes() throws Exception {
    List<Long> times = new ArrayList<Long>();
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);

    assertNull(getNextAlarmTime(schedule, createDateTime_ThursdayAtHour(13)));
  }

  @Test
  public void testOnceDaily_2pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(1), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testOnceDaily_2pm_At2Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime onePm = createDateTime_ThursdayAtHour(14);
    assertEquals(twoPm.plusDays(1), new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testOnceDaily_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(getNextAlarmTime(schedule, threePmFriday)));
  }

  @Test
  public void testOnceWeekday_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeekdayScheduleWithTimes(times);



    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(getNextAlarmTime(schedule, threePmFriday)));
  }

  @Test
  public void testTwiceDaily_12pm_2pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testTwiceDaily_2pm_3pm_At1Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    DateTime threePm = createDateTime_ThursdayAtHour(15);
    times.add(getHoursAndMinutesAsMillisOffset(threePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testTwiceDaily_12pm_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));

    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  // change order
  @Test
  public void testTwiceDaily_2pm_12pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();

    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));

    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));

    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twelvePm.plusDays(1), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testOnceDaily_12pm_2pm_At3Pm_onFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));

    DateTime twelvePm = createDateTime_ThursdayAtHour(12);
    times.add(getHoursAndMinutesAsMillisOffset(twelvePm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 1);



    DateTime threePmFriday = createDateTime_FridayAtHour(15);
    assertEquals(twelvePm.plusDays(2), new DateTime(getNextAlarmTime(schedule, threePmFriday)));
  }

  @Test
  public void testRepeatEvery2DaysOnceDaily_2pm_1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2);



    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testRepeatEvery2DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2);



    DateTime threePm = createDateTime_FridayAtHour(15);
    assertEquals(twoPm.plusDays(2), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testRepeatEvery2DaysOnceDaily_2pm_At3PmSaturday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2);



    DateTime threePm = createDateTime_FridayAtHour(15).plusDays(1);
    assertEquals(twoPm.plusDays(4), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testRepeatEvery2DaysOnceDaily_2pm_At1PmSaturday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 2);



    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(1);
    assertEquals(twoPm.plusDays(2), new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testRepeatEvery3DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(3), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testRepeatEvery3DaysOnceDaily_2pm_1pmFriday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3);



    DateTime onePm = createDateTime_FridayAtHour(13);
    assertEquals(twoPm.plusDays(3), new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testRepeatEvery3DaysOnceDaily_2pm_1pmSunday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3);



    DateTime onePm = createDateTime_FridayAtHour(13).plusDays(2);
    assertEquals(twoPm.plusDays(3), new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testRepeatEvery3DaysOnceDaily_2pm_3pmSunday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 3);



    DateTime onePm = createDateTime_FridayAtHour(15).plusDays(2);
    assertEquals(twoPm.plusDays(6), new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testRepeatEvery4DaysOnceDaily_2pm_At3Pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 4);



    DateTime threePm = createDateTime_ThursdayAtHour(15);
    assertEquals(twoPm.plusDays(4), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testRepeatEvery4DaysOnceDaily_2pm_At3PmMonday() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createDailyScheduleWithTimes(times, 4);



    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(4);
    assertEquals(twoPm.plusDays(8), new DateTime(getNextAlarmTime(schedule, threePm)));
  }

  @Test
  public void testNullTimesWeeklyNoDaysSelected() throws Exception {
    List<Long> times = null;
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, null);

    assertNull(getNextAlarmTime(schedule, createDateTime_ThursdayAtHour(13)));
  }

  @Test
  public void testWeeklyThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | THURSDAY);



    DateTime onePm = createDateTime_ThursdayAtHour(13);
    assertEquals(twoPm, new DateTime(getNextAlarmTime(schedule, onePm)));
  }

  @Test
  public void testWeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | THURSDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  @Test
  public void testWeeklyThursday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, 0 | THURSDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  @Test
  public void testWeeklyThursdayFriday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | THURSDAY) | FRIDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(1), nextAlarmTime);
  }

  @Test
  public void testWeeklyThursdayFriday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | THURSDAY) | FRIDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(7), nextAlarmTime);
  }

  @Test
  public void testWeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 1, (0 | THURSDAY) | MONDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4);
    assertEquals(monday, nextAlarmTime);

    assertEquals(monday.plusDays(3), new DateTime(getNextAlarmTime(schedule, monday)));
  }

  @Test
  public void testRepeatEvery2WeeklyThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | THURSDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(13);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm, nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | THURSDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2WeeklyThursday2pm_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | THURSDAY);



    DateTime threePm = createDateTime_FridayAtHour(15);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(14), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2WeeklyThursday2pm_twoFridaysAway3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, 0 | THURSDAY);



    DateTime threePm = createDateTime_FridayAtHour(15).plusWeeks(2);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(28), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2WeeklyThursdayMonday2pm_Sunday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | THURSDAY) | MONDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15).plusDays(3);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    DateTime monday = twoPm.plusDays(4).plusWeeks(1);
    assertEquals(monday, nextAlarmTime);

    assertEquals(monday.plusDays(3), new DateTime(getNextAlarmTime(schedule, monday)));
  }

  @Test
  public void testRepeatEvery2WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 2, (0 | THURSDAY) | TUESDAY);



    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(1);
    assertEquals(weekFromTuesday, nextAlarmTime);

    assertEquals(weekFromTuesday.plusDays(2),
        new DateTime(getNextAlarmTime(schedule, weekFromTuesday)));
  }

  @Test
  public void testRepeatEvery3WeeklyThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 3, 0 | THURSDAY);



    DateTime threePm = createDateTime_ThursdayAtHour(15);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, threePm);
    assertNotNull(nextAlarmTime);
    assertEquals(twoPm.plusDays(21), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery3WeeklyThursdayTuesday2pm_Monday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime twoPm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(twoPm));
    SignalSchedule schedule = createWeeklyScheduleWithTimes(times, 3, (0 | THURSDAY) | TUESDAY);



    DateTime monday3Pm = createDateTime_ThursdayAtHour(15).plusDays(4);

    DateTime nextAlarmTime = getNextAlarmTime(schedule, monday3Pm);
    assertNotNull(nextAlarmTime);
    DateTime weekFromTuesday = twoPm.plusDays(5).plusWeeks(2);
    assertEquals(weekFromTuesday, nextAlarmTime);

    assertEquals(weekFromTuesday.plusDays(2),
        new DateTime(getNextAlarmTime(schedule, weekFromTuesday)));
  }

  @Test
  public void testMonthlyByDayofMonthTuesday2pm_1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true,
            midnightDayOfMonthDue.getDayOfMonth());


    DateTime thursday1Pm = midnightDayOfMonthDue.plusHours(13);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, thursday1Pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  @Test
  public void testMonthlyByDayofMonthTuesday2pm_3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true,
            midnightDayOfMonthDue.getDayOfMonth());


    DateTime thursday3Pm = midnightDayOfMonthDue.plusHours(15);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, thursday3Pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  @Test
  public void testMonthlyByDayofMonthThursday2pm_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true,
            midnightDayOfMonthDue.getDayOfMonth());


    DateTime friday3pm = midnightDayOfMonthDue.plusHours(15).plusDays(1);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, friday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm.plusMonths(1), nextAlarmTime);
  }

  @Test
  public void testMonthlyByDayofMonthThursday2pm_PreviousWednesday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 1, true,
            midnightDayOfMonthDue.getDayOfMonth());


    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  @Test
  public void testMonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = new DateTime(2010, 12, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();

    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 1, 1, 0 | WEDNESDAY);


    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15).minusDays(1);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 1, 5, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2MonthlyByNthWeekOnDOWThursday2pm_PreviousWednesday3pm()
      throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = new DateTime(2011, 1, 5, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = thursday2Pm.toDateMidnight().toDateTime();

    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 1, 0 | WEDNESDAY);


    DateTime wednesday3pm = midnightDayOfMonthDue.plusHours(15);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, wednesday3pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 2, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Friday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2011, 1, 19, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));

    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | WEDNESDAY);


    DateTime nextAlarmTime = getNextAlarmTime(schedule, today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 16, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2011, 1, 21, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));

    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 2, 3, 0 | FRIDAY);


    DateTime nextAlarmTime = getNextAlarmTime(schedule, today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 18, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery3MonthlyByNthWeekOnDOW3rdFriday_Monday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime today = new DateTime(2011, 1, 21, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(today));

    SignalSchedule schedule = createMonthlyScheduleByNthWeekWithTimes(times, 3, 3, 0 | FRIDAY);


    DateTime nextAlarmTime = getNextAlarmTime(schedule, today);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 4, 15, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday1pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2Pm = createDateTime_ThursdayAtHour(14);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2Pm));

    DateTime midnightDayOfMonthDue = createDateTime_ThursdayAtHour(0);
    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true,
            midnightDayOfMonthDue.getDayOfMonth());


    DateTime tuesday1pm = midnightDayOfMonthDue.minusHours(1);
    DateTime nextAlarmTime = getNextAlarmTime(schedule, tuesday1pm);

    assertNotNull(nextAlarmTime);
    assertEquals(thursday2Pm, nextAlarmTime);
  }

  @Test
  public void testRepeatEvery2MonthlyByDayofMonthThursday2pm_Thursday2pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2pm = new DateTime(2011, 1, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2pm));

    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 2, true, thursday2pm.getDayOfMonth());


    DateTime nextAlarmTime = getNextAlarmTime(schedule, thursday2pm);

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 3, 17, 14, 0, 0, 0), nextAlarmTime);
  }

  @Test
  public void testRepeatEvery3MonthlyByDayofMonthThursday2pm_Thursday3pm() throws Exception {
    List<Long> times = new ArrayList<Long>();
    DateTime thursday2pm = new DateTime(2011, 1, 17, 14, 0, 0, 0);
    times.add(getHoursAndMinutesAsMillisOffset(thursday2pm));

    SignalSchedule schedule =
        createMonthlyScheduleByDayOfMonthWithTimes(times, 3, true, thursday2pm.getDayOfMonth());


    DateTime nextAlarmTime = getNextAlarmTime(schedule, thursday2pm.plusHours(1));

    assertNotNull(nextAlarmTime);
    assertEquals(new DateTime(2011, 4, 17, 14, 0, 0, 0), new DateTime(nextAlarmTime));
  }

  private DateTime getNextAlarmTime(SignalSchedule signalSchedule, DateTime now) {
    SignalScheduleIterator iterator = signalSchedule.iterator();
    iterator.advanceTo(now);

    return iterator.next();
  }
}
