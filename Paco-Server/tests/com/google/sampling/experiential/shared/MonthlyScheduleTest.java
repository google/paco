// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
  public void testEqualityWhenByDayOfWeekSet() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    MonthlySchedule schedule2 = new MonthlySchedule();

    schedule1.setByDayOfWeek(true);
    schedule2.setByDayOfWeek(true);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenByDayOfWeekSet() {
    MonthlySchedule schedule1 = new MonthlySchedule();
    MonthlySchedule schedule2 = new MonthlySchedule();

    schedule1.setByDayOfWeek(true);
    schedule2.setByDayOfWeek(false);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testType() {
    MonthlySchedule schedule = new MonthlySchedule();

    assertEquals(schedule.getType(), Schedule.Type.Monthly);
  }
}
