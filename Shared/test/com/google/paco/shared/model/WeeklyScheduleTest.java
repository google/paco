// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Schedule;
import com.google.paco.shared.model.WeeklySchedule;

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
}
