// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.FixedSignal;
import com.google.paco.shared.model.RandomSignal;
import com.google.paco.shared.model.SignalSchedule;
import com.google.paco.shared.model.WeeklySchedule;

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
  public void testEqualityWhenUserEditableSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setEditable(true);
    signalSchedule2.setEditable(true);

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testEqualityWhenSignalSetAndScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new FixedSignal());
    signalSchedule2.setSignal(new FixedSignal());
    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new DailySchedule());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testEqualityWhenSignalSetNullAndScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(null);
    signalSchedule2.setSignal(null);
    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new DailySchedule());

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testEqualityWhenSignalSetAndScheduleSetNull() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new FixedSignal());
    signalSchedule2.setSignal(new FixedSignal());
    signalSchedule1.setSchedule(null);
    signalSchedule2.setSchedule(null);

    assertTrue(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testInequalityWhenUserEditableSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setEditable(true);
    signalSchedule2.setEditable(false);

    assertFalse(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testInequalityWhenSignalSetAndScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new FixedSignal());
    signalSchedule2.setSignal(new RandomSignal());
    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new DailySchedule());

    assertFalse(signalSchedule1.equals(signalSchedule2));
  }

  @Test
  public void testInequalityWhenSignalAndScheduleSet() {
    SignalSchedule signalSchedule1 = new SignalSchedule();
    SignalSchedule signalSchedule2 = new SignalSchedule();

    signalSchedule1.setSignal(new RandomSignal());
    signalSchedule2.setSignal(new RandomSignal());
    signalSchedule1.setSchedule(new DailySchedule());
    signalSchedule2.setSchedule(new WeeklySchedule());

    assertFalse(signalSchedule1.equals(signalSchedule2));
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
