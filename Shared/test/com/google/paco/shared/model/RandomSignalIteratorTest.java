package com.google.paco.shared.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.joda.time.LocalTime;
import org.junit.Test;

public class RandomSignalIteratorTest {
  @Test
  public void testGetNextTime() {
    RandomSignal signal = new RandomSignal();

    SignalIterator iterator = signal.iterator(createRandom());

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testGetNextTimeWithStartTimeAndEndTime() {
    RandomSignal signal = new RandomSignal();

    signal.setStartTime(new LocalTime(6, 0, 0));
    signal.setEndTime(new LocalTime(18, 0, 0));

    SignalIterator iterator = signal.iterator(createRandom());

    assertFalse(iterator.hasNext());
  }

  private Random createRandom() {
    @SuppressWarnings("serial")
    class LinearIncreaser extends Random {
      private int count = 0;

      public int nextInt(int max) {
        return count++ % max;
      }
    }

    return new LinearIncreaser();
  }

  @Test
  public void testGetNextTimeWithStartTimeAndEndTimeAndFrequency() {
    RandomSignal signal = new RandomSignal();
    signal.setStartTime(new LocalTime(9, 0, 0));
    signal.setEndTime(new LocalTime(17, 0, 0));
    signal.setFrequency(8);

    SignalIterator iterator = signal.iterator(createRandom());
    iterator.advanceTo(new LocalTime(0, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalTime(9, 8, 0), iterator.next());
  }

  @Test
  public void testGetNextTimeWithStartTimeAndEndTimeAndFrequency2() {
    RandomSignal signal = new RandomSignal();
    signal.setStartTime(new LocalTime(17, 0, 0));
    signal.setEndTime(new LocalTime(9, 0, 0));
    signal.setFrequency(9);

    SignalIterator iterator = signal.iterator(createRandom());
    iterator.advanceTo(new LocalTime(0, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalTime(17, 9, 0), iterator.next());
  }
}
