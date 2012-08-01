// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

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

    assertEquals(schedule.getType(), Schedule.DAILY);
  }
}
