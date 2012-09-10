package com.google.paco.shared.model;

import static org.junit.Assert.*;

import org.joda.time.LocalTime;
import org.junit.Test;

import com.google.common.collect.Sets;

public class FixedSignalIteratorTest {
  private SignalIterator createIterator() {
    FixedSignal signal = new FixedSignal();

    signal.setTimes(Sets.newHashSet(new LocalTime(6, 0, 0), new LocalTime(12, 0, 0)));

    return signal.iterator();
  }

  @Test
  public void testIterator() {
    FixedSignal signal = new FixedSignal();

    SignalIterator iterator = signal.iterator();
    iterator.advanceTo(new LocalTime(3, 0, 0));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithTimesBefore() {
    SignalIterator iterator = createIterator();
    iterator.advanceTo(new LocalTime(3, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalTime(6, 0, 0), iterator.next());
  }

  @Test
  public void testIteratorWithTimesOn1() {
    SignalIterator iterator = createIterator();
    iterator.advanceTo(new LocalTime(6, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalTime(12, 0, 0), iterator.next());
  }

  @Test
  public void testIteratorWithTimesDuring() {
    SignalIterator iterator = createIterator();
    iterator.advanceTo(new LocalTime(8, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalTime(12, 0, 0), iterator.next());
  }

  @Test
  public void testIteratorWithTimesOn2() {
    SignalIterator iterator = createIterator();
    iterator.advanceTo(new LocalTime(12, 0, 0));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithTimesAfter() {
    SignalIterator iterator = createIterator();
    iterator.advanceTo(new LocalTime(18, 0, 0));

    assertFalse(iterator.hasNext());
  }
}
