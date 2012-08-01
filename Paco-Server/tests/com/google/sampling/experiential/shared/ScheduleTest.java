// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ScheduleTest {
  private class ScheduleImpl extends Schedule {
    public ScheduleImpl(String type) {
      super(type);
    }
  }

  private class SignalImpl extends Signal {
    public SignalImpl(String type) {
      super(type);
    }
  }

  @Test
  public void testEquality() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequality() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.WEEKLY);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenStartDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setStartDate(new Date(3));
    schedule2.setStartDate(new Date(3));

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenStartDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setStartDate(new Date(3));
    schedule2.setStartDate(null);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenEndDateSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setEndDate(new Date(3));
    schedule2.setEndDate(new Date(3));

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenEndDateSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setEndDate(new Date(3));
    schedule2.setEndDate(null);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenEditableSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setEditable(true);
    schedule2.setEditable(true);

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenEditableSetFalse() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setEditable(false);
    schedule1.setEditable(true);

    assertFalse(schedule1.equals(schedule2));
  }

  @Test
  public void testEqualityWhenScheduleSet() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setSignal(new SignalImpl(Signal.RANDOM));
    schedule2.setSignal(new SignalImpl(Signal.RANDOM));

    assertTrue(schedule1.equals(schedule2));
  }

  @Test
  public void testInequalityWhenScheduleSetNull() {
    ScheduleImpl schedule1 = new ScheduleImpl(Schedule.DAILY);
    ScheduleImpl schedule2 = new ScheduleImpl(Schedule.DAILY);

    schedule1.setSignal(new SignalImpl(Signal.RANDOM));
    schedule2.setSignal(null);

    assertFalse(schedule1.equals(schedule2));
  }
}
