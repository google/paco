// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalScheduleTest {
  @Test
  public void testEquality() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testEqualityWhenSignalSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new FixedSignal());
    signalSchedule2.setSignal(new FixedSignal());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testEqualityWhenScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new DailySchedule());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testInequalityWhenSignalSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new FixedSignal());
    signalSchedule2.setSignal(new RandomSignal());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testInequalityWhenScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new WeeklySchedule());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testSignalIsNullable() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(null);

    assertNull(signalSchedule.getSignal());
  }

  @Test
  public void testScheduleIsNullable() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSchedule(null);

    assertNull(signalSchedule.getSchedule());
  }

  @Test
  public void testHasSignalSchedule() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(new FixedSignal());
    signalSchedule.setSchedule(new DailySchedule());

    assertTrue(signalSchedule.hasSignalSchedule());
  }

  @Test
  public void testHasSignalScheduleWhenSignalNull() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(null);
    signalSchedule.setSchedule(new DailySchedule());

    assertFalse(signalSchedule.hasSignalSchedule());
  }

  @Test
  public void testHasSignalScheduleWhenScheduleNull() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(new FixedSignal());
    signalSchedule.setSchedule(null);

    assertFalse(signalSchedule.hasSignalSchedule());
  }

  @Test
  public void testHasSignalScheduleWhenSignalNullAndScheduleNull() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(null);
    signalSchedule.setSchedule(null);

    assertFalse(signalSchedule.hasSignalSchedule());
  }
}
