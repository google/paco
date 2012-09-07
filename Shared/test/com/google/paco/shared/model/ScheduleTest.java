// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.Schedule;

import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ScheduleTest {
  private class ScheduleImpl extends Schedule {
    public ScheduleImpl(Type type) {
      super(type);
    }

    @Override
    protected String getRData() {
      return null;
    }

    @Override
    protected boolean isValidDate(LocalDate date) {
      return false;
    }
  }

  @Test
  public void testEquality() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequality() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Weekly);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenStartDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setStartDate(new LocalDate(1, 1, 1));
    schedule2.setStartDate(new LocalDate(1, 1, 1));

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenStartDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setStartDate(null);
    schedule2.setStartDate(null);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenStartDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setStartDate(new LocalDate(1, 1, 1));
    schedule2.setStartDate(new LocalDate(1, 1, 2));

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenStartDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setStartDate(null);
    schedule2.setStartDate(new LocalDate(4));

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenEndDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setEndDate(new LocalDate(3));
    schedule2.setEndDate(new LocalDate(3));

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenEndDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setEndDate(null);
    schedule2.setEndDate(null);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenEndDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setEndDate(new LocalDate(1, 1, 1));
    schedule2.setEndDate(new LocalDate(1, 1, 2));

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenEndDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.Type.Daily);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.Type.Daily);

    schedule1.setEndDate(null);
    schedule2.setEndDate(new LocalDate(4));

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testStartDateIsNullable() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setStartDate(null);

    assertNull(schedule.getStartDate());
  }

  @Test
  public void testEndDateIsNullable() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setEndDate(null);

    assertNull(schedule.getEndDate());
  }

  @Test
  public void testHasStartDate() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setStartDate(new LocalDate(3));

    assertTrue(schedule.hasStartDate());
  }

  @Test
  public void testHasEndDate() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setEndDate(new LocalDate(3));

    assertTrue(schedule.hasEndDate());
  }

  @Test
  public void testHasStartDateWhenNull() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setStartDate(null);

    assertFalse(schedule.hasStartDate());
  }

  @Test
  public void testHasEndDateWhenNull() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setEndDate(null);

    assertFalse(schedule.hasEndDate());
  }

  @Test
  public void testIsFixedDuration() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setStartDate(new LocalDate(0));
    schedule.setEndDate(new LocalDate(1));

    assertTrue(schedule.isFixedDuration());
  }

  @Test
  public void testIsFixedDurationWhenNull() {
    ScheduleImpl schedule = new ScheduleImpl(Schedule.Type.Daily);

    schedule.setStartDate(null);
    schedule.setEndDate(new LocalDate(1));

    assertTrue(schedule.isFixedDuration());
  }
}
