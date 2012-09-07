// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.WeeklySchedule;
import com.google.paco.shared.model.WeeklySchedule.Day;

import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class WeeklyScheduleTest {
  @Test
  public void testEquality() {
    WeeklySchedule schedule1 = new WeeklySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequality() {
    WeeklySchedule schedule1 = new WeeklySchedule();
    DailySchedule schedule2 = new DailySchedule();

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenDayRepeatSet() {
    WeeklySchedule schedule1 = new WeeklySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    schedule1.setDayRepeat(3);
    schedule2.setDayRepeat(3);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenDayRepeatSet() {
    WeeklySchedule schedule1 = new WeeklySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    schedule1.setDayRepeat(3);
    schedule2.setDayRepeat(4);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testType() {
    WeeklySchedule schedule = new WeeklySchedule();

    assertEquals(schedule.getType(), Schedule.Type.Weekly);
  }

  private WeeklySchedule createWeekly() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 18));
    schedule.setEvery(2);
    schedule.setOnDay(Day.Monday);
    schedule.setOnDay(Day.Friday);

    return schedule;
  }

  @Test
  public void testGetCurrentDate() {
    WeeklySchedule schedule = new WeeklySchedule();

    assertNull(schedule.getCurrentDate(new LocalDate(), 0));
  }

  @Test
  public void testGetCurrentDateWithStartDate() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getCurrentDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateBefore1() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = null;

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateBefore2() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = null;

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateDuring1() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 7);
    LocalDate expected = new LocalDate(2012, 9, 7);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateDuring2() {
    LocalDate now = new LocalDate(2012, 9, 10);
    LocalDate expected = new LocalDate(2012, 9, 7);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateDuring3() {
    LocalDate now = new LocalDate(2012, 9, 14);
    LocalDate expected = new LocalDate(2012, 9, 7);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateOn3() {
    LocalDate now = new LocalDate(2012, 9, 17);
    LocalDate expected = new LocalDate(2012, 9, 17);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 9, 18);
    LocalDate expected = new LocalDate(2012, 9, 17);

    assertEquals(expected, createWeekly().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEvery1() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetNextDate() {
    WeeklySchedule schedule = new WeeklySchedule();

    assertNull(schedule.getNextDate(new LocalDate(), 0));
  }

  @Test
  public void testGetNextDateWithStartDate() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateBefore1() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateBefore2() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 7);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring1() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = new LocalDate(2012, 9, 7);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 7);
    LocalDate expected = new LocalDate(2012, 9, 17);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring2() {
    LocalDate now = new LocalDate(2012, 9, 10);
    LocalDate expected = new LocalDate(2012, 9, 17);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring3() {
    LocalDate now = new LocalDate(2012, 9, 14);
    LocalDate expected = new LocalDate(2012, 9, 17);

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn3() {
    LocalDate now = new LocalDate(2012, 9, 17);
    LocalDate expected = null;

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 9, 18);
    LocalDate expected = null;

    assertEquals(expected, createWeekly().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery1() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 9);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 16);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 2), 0));
  }
}
