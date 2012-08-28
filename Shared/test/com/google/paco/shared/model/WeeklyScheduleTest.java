// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.WeeklySchedule;
import com.google.paco.shared.model.WeeklySchedule.Day;

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

    schedule1.setOnDay(Day.Monday);
    schedule2.setOnDay(Day.Monday);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenDayRepeatSet() {
    WeeklySchedule schedule1 = new WeeklySchedule();
    WeeklySchedule schedule2 = new WeeklySchedule();

    schedule1.setOnDay(Day.Monday);
    schedule2.setOnDay(Day.Tuesday);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testType() {
    WeeklySchedule schedule = new WeeklySchedule();

    assertEquals(schedule.getType(), Schedule.Type.Weekly);
  }

  @Test
  public void testIsOnDay() {
    WeeklySchedule schedule = new WeeklySchedule();

    assertFalse(schedule.isOnDay(Day.Monday));
    assertFalse(schedule.isOnDay(Day.Tuesday));
    assertFalse(schedule.isOnDay(Day.Wednesday));
    assertFalse(schedule.isOnDay(Day.Thursday));
    assertFalse(schedule.isOnDay(Day.Friday));
    assertFalse(schedule.isOnDay(Day.Saturday));
    assertFalse(schedule.isOnDay(Day.Sunday));
  }

  @Test
  public void testIsOnDayWhenSet() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setOnDay(Day.Thursday);

    assertFalse(schedule.isOnDay(Day.Monday));
    assertFalse(schedule.isOnDay(Day.Tuesday));
    assertFalse(schedule.isOnDay(Day.Wednesday));
    assertTrue(schedule.isOnDay(Day.Thursday));
    assertFalse(schedule.isOnDay(Day.Friday));
    assertFalse(schedule.isOnDay(Day.Saturday));
    assertFalse(schedule.isOnDay(Day.Sunday));
  }
}
