// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.paco.shared.model.Signal;

import org.joda.time.LocalTime;
import org.junit.Test;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalTest {
  private class SignalImpl extends Signal {
    public SignalImpl(Type type) {
      super(type);
    }

    @Override
    public LocalTime getNextTime(LocalTime now, long seed) {
      return null;
    }
  }

  @Test
  public void testEquality() {
    SignalImpl signal1 = new SignalImpl(Signal.Type.Fixed);
    SignalImpl signal2 = new SignalImpl(Signal.Type.Fixed);

    assertTrue(signal1.equals(signal2));
  }

  @Test
  public void testInequality() {
    SignalImpl signal1 = new SignalImpl(Signal.Type.Fixed);
    SignalImpl signal2 = new SignalImpl(Signal.Type.Random);

    assertFalse(signal1.equals(signal2));
  }
}
