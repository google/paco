// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import com.google.paco.shared.model.MonthlySchedule;
import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.WeeklySchedule;
import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class MonthlyScheduleTest {
  @Test
  public void testEquality() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    MonthlySchedule schedule2 = new MonthlySchedule();

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequality() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenWeekRepeatSet() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    MonthlySchedule schedule2 = new MonthlySchedule();

    schedule1.setWeekRepeat(3);
    schedule2.setWeekRepeat(3);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenWeekRepeatSet() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    MonthlySchedule schedule2 = new MonthlySchedule();

    schedule1.setWeekRepeat(3);
    schedule2.setWeekRepeat(4);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testType() {
    MonthlySchedule schedule = new MonthlySchedule();

    assertEquals(schedule.getType(), Schedule.Type.Monthly);
  }

  private MonthlySchedule createMonthlyByDay() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 11, 14));
    schedule.setEvery(2);
    schedule.setOnDay(5);
    schedule.setOnDay(19);

    return schedule;
  }

  @Test
  public void testGetCurrentDateByDay() {
    MonthlySchedule schedule = new MonthlySchedule();

    assertNull(schedule.getCurrentDate(new LocalDate(), 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDate() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getCurrentDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateBefore1() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = null;

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateBefore2() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = null;

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = new LocalDate(2012, 9, 5);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateDuring1() {
    LocalDate now = new LocalDate(2012, 9, 10);
    LocalDate expected = new LocalDate(2012, 9, 5);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 19);
    LocalDate expected = new LocalDate(2012, 9, 19);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateDuring2() {
    LocalDate now = new LocalDate(2012, 10, 5);
    LocalDate expected = new LocalDate(2012, 9, 19);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateDuring3() {
    LocalDate now = new LocalDate(2012, 10, 19);
    LocalDate expected = new LocalDate(2012, 9, 19);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateOn3() {
    LocalDate now = new LocalDate(2012, 11, 5);
    LocalDate expected = new LocalDate(2012, 11, 5);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 11, 19);
    LocalDate expected = new LocalDate(2012, 11, 5);

    assertEquals(expected, createMonthlyByDay().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEvery1() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateByDayWithStartDateAndEndDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetNextDate() {
    MonthlySchedule schedule = new MonthlySchedule();

    assertNull(schedule.getNextDate(new LocalDate(), 0));
  }

  @Test
  public void testGetNextDateWithStartDate() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateBefore1() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = new LocalDate(2012, 9, 5);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateBefore2() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 5);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = new LocalDate(2012, 9, 19);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring1() {
    LocalDate now = new LocalDate(2012, 9, 10);
    LocalDate expected = new LocalDate(2012, 9, 19);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 19);
    LocalDate expected = new LocalDate(2012, 11, 5);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring2() {
    LocalDate now = new LocalDate(2012, 10, 5);
    LocalDate expected = new LocalDate(2012, 11, 5);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring3() {
    LocalDate now = new LocalDate(2012, 10, 19);
    LocalDate expected = new LocalDate(2012, 11, 5);

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn3() {
    LocalDate now = new LocalDate(2012, 11, 5);
    LocalDate expected = null;

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 11, 19);
    LocalDate expected = null;

    assertEquals(expected, createMonthlyByDay().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery1() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 10, 2);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 11, 2);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 2), 0));
  }

  @Test
  public void doTest() throws ParseException {
    // Every 2 months on the 1st from 9/2 to 11/1
    LocalDate start = new LocalDate(2012, 9, 2);
    String rdata = "RRULE:FREQ=MONTHLY;INTERVAL=2;BYMONTHDAY=1";
    LocalDateIterator ldi = LocalDateIteratorFactory.createLocalDateIterator(rdata, start, true);

    System.out.println(ldi.next()); // check if valid, skip otherwise
    System.out.println(ldi.next());
    System.out.println(ldi.next());
    System.out.println(ldi.next());
    System.out.println(ldi.next());
  }
}
