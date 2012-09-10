package com.google.paco.shared.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeConstants;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.WeeklySchedule.Day;

public class LegacySignalScheduleTest {
  private final static int ESM_PERIOD_DAY = 1;
  private final static int ESM_PERIOD_WEEK = 2;
  private final static int ESM_PERIOD_MONTH = 3;
  private final static Minutes BUFFER_MILLIS = Minutes.minutes(15);

  private List<DateTime> generateForSchedule(DateTime startDate, SignalSchedule schedule) {
    List<DateTime> alarms = Lists.newArrayList();

    SignalScheduleIterator iterator = schedule.iterator();

    while (iterator.hasNext() && alarms.size() < 40) {
      alarms.add(iterator.next());
      System.out.println("added!");
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

    assertEquals(0, signals.size());
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

    DateTime nextPeriod = startDate.plusDays(convertEsmPeriodToDays(schedule));
    signals = generateForSchedule(nextPeriod, schedule);
    assertTrue(isWeekend(nextPeriod));
    assertEquals(0, signals.size());

    if (convertEsmPeriodToDays(schedule) == 1 && !esmWeekends
        && isWeekend(nextPeriod)) {
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

    DateTime nextPeriod = startDate.plusDays(convertEsmPeriodToDays(schedule));
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
    DateTime endTime = startTime.plusMinutes(10);
    badSignals.add(startTime);
    badSignals.add(endTime);
    try {
      assertSignalsRespectMinimumBuffer(badSignals);
      fail("should have thrown an exception");
    } catch (AssertionFailedError a) {
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
    System.out.println("signals = " + signals);
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
    int daysInPeriod = esmFrequency*7; //convertEsmPeriodToDays(schedule);

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
    schedule.setOnDay(Day.Monday);
    schedule.setOnDay(Day.Tuesday);
    schedule.setOnDay(Day.Wednesday);
    schedule.setOnDay(Day.Thursday);
    schedule.setOnDay(Day.Friday);

    if (esmWeekends) {
      schedule.setOnDay(Day.Saturday);
      schedule.setOnDay(Day.Sunday);
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
      System.out.println("Datetime: " + dateTime);
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

  private int convertEsmPeriodToDays(SignalSchedule signalSchedule) {
    return 1;
    /*
     * switch (getEsmPeriodInDays()) { case ESM_PERIOD_DAY: return 1; case ESM_PERIOD_WEEK: return
     * 7; case ESM_PERIOD_MONTH: return 30; default: return 1; }
     */
  }

  private boolean isWeekend(DateTime dateTime) {
    return (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY ||
            dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY);
  }

  private DateTime skipWeekends(DateTime plusDays) {
    if (plusDays.getDayOfWeek() == DateTimeConstants.SATURDAY) {
      return plusDays.plusDays(2);
    } else if (plusDays.getDayOfWeek() == DateTimeConstants.SUNDAY) {
      return plusDays.plusDays(1);
    }
    return plusDays;
  }
}
