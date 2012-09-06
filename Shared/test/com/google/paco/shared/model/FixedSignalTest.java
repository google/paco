// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.common.collect.Sets;
import com.google.paco.shared.model.FixedSignal;
import com.google.paco.shared.model.RandomSignal;

import org.joda.time.LocalTime;
import org.junit.Test;


/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class FixedSignalTest {
  @Test
  public void testEquality() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    assertTrue(signal1.equals(signal2));
  }

  @Test
  public void testInequality() {
    FixedSignal signal1 = new FixedSignal();
    RandomSignal signal2 = new RandomSignal();

    assertFalse(signal1.equals(signal2));
  }

  @Test
  public void testEqualityWhenTimesSetNull() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    signal1.setTimes(null);
    signal2.setTimes(null);

    assertTrue(signal1.equals(signal2));
  }

  @Test
  public void testEqualityWhenTimesSetOrdered() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    signal1.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(4)));
    signal2.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(4)));

    assertTrue(signal1.equals(signal2));
  }

  @Test
  public void testEqualityWhenTimesSetUnordered() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    signal1.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(4)));
    signal2.setTimes(Sets.newHashSet(new LocalTime(4), new LocalTime(3), new LocalTime(1)));

    assertTrue(signal1.equals(signal2));
  }

  @Test
  public void testInequalityWhenTimesSet() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    signal1.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(4)));
    signal2.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(5)));

    assertFalse(signal1.equals(signal2));
  }

  @Test
  public void testInequalityWhenTimesSetNull() {
    FixedSignal signal1 = new FixedSignal();
    FixedSignal signal2 = new FixedSignal();

    signal1.setTimes(null);
    signal2.setTimes(Sets.newHashSet(new LocalTime(1), new LocalTime(3), new LocalTime(4)));

    assertFalse(signal1.equals(signal2));
  }

  @Test
  public void testTimesNotNull() {
    FixedSignal signal = new FixedSignal();

    assertNotNull(signal.getTimes());
  }

  @Test
  public void testTimesNotNullWhenTimesSetNull() {
    FixedSignal signal = new FixedSignal();

    signal.setTimes(null);

    assertNotNull(signal.getTimes());
  }
}
