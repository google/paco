// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.WeeklySchedule;

import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DailyScheduleTest {
  @Test
  public void testEquality() {
    DailySchedule schedule1 = new DailySchedule();
    DailySchedule schedule2 = new DailySchedule();

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequality() {
    DailySchedule schedule1 = new DailySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenEverySet() {
    DailySchedule schedule1 = new DailySchedule();
    DailySchedule schedule2 = new DailySchedule();

    schedule1.setEvery(3);
    schedule2.setEvery(3);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenEverySet() {
    DailySchedule schedule1 = new DailySchedule();
    DailySchedule schedule2 = new DailySchedule();

    schedule1.setEvery(3);
    schedule2.setEvery(4);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testType() {
    DailySchedule schedule = new DailySchedule();

    assertEquals(schedule.getType(), Schedule.Type.Daily);
  }

  private DailySchedule createDaily() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 4));
    schedule.setEvery(2);

    return schedule;
  }

  @Test
  public void testGetCurrentDate() {
    DailySchedule schedule = new DailySchedule();

    assertNull(schedule.getCurrentDate(new LocalDate(), 0));
  }

  @Test
  public void testGetCurrentDateWithStartDate() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getCurrentDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateBefore() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = null;

    assertEquals(expected, createDaily().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, createDaily().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateDuring() {
    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, createDaily().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 4);
    LocalDate expected = new LocalDate(2012, 9, 4);

    assertEquals(expected, createDaily().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = new LocalDate(2012, 9, 4);

    assertEquals(expected, createDaily().getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEvery1() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 1);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetCurrentDateWithStartDateAndEndDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = new LocalDate(2012, 9, 1);

    assertEquals(expected, schedule.getCurrentDate(now, 0));
  }

  @Test
  public void testGetNextDate() {
    DailySchedule schedule = new DailySchedule();

    assertNull(schedule.getNextDate(new LocalDate(), 0));
  }

  @Test
  public void testGetNextDateWithStartDate() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 1), 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateBefore() {
    LocalDate now = new LocalDate(2012, 9, 1);
    LocalDate expected = new LocalDate(2012, 9, 2);

    assertEquals(expected, createDaily().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn1() {
    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 4);

    assertEquals(expected, createDaily().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateDuring() {
    LocalDate now = new LocalDate(2012, 9, 3);
    LocalDate expected = new LocalDate(2012, 9, 4);

    assertEquals(expected, createDaily().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateOn2() {
    LocalDate now = new LocalDate(2012, 9, 4);
    LocalDate expected = null;

    assertEquals(expected, createDaily().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAfter() {
    LocalDate now = new LocalDate(2012, 9, 5);
    LocalDate expected = null;

    assertEquals(expected, createDaily().getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery1() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(1);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(2);

    LocalDate now = new LocalDate(2012, 9, 2);
    LocalDate expected = new LocalDate(2012, 9, 3);

    assertEquals(expected, schedule.getNextDate(now, 0));
  }

  @Test
  public void testGetNextDateWithStartDateAndEndDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setEvery(2);

    assertNull(schedule.getNextDate(new LocalDate(2012, 9, 2), 0));
  }
}
