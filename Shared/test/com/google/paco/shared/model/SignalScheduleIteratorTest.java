package com.google.paco.shared.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

public class SignalScheduleIteratorTest {
  @Test
  public void testIterator() {
    SignalSchedule signalSchedule = new SignalSchedule();

    SignalScheduleIterator iterator = signalSchedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithSignal() {
    SignalSchedule signalSchedule = new SignalSchedule();
    signalSchedule.setSignal(new FixedSignal());

    SignalScheduleIterator iterator = signalSchedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithSchedule() {
    SignalSchedule signalSchedule = new SignalSchedule();
    signalSchedule.setSchedule(new DailySchedule());

    SignalScheduleIterator iterator = signalSchedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithSignalSchedule() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(new FixedSignal());
    signalSchedule.setSchedule(new DailySchedule());

    SignalScheduleIterator iterator = signalSchedule.iterator();

    assertFalse(iterator.hasNext());
  }

  private SignalScheduleIterator createFixedDailyIterator() {
    SignalSchedule signalSchedule = new SignalSchedule();

    FixedSignal signal = new FixedSignal();
    signal.addTime(new LocalTime(12, 0, 0));

    DailySchedule schedule = new DailySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 3));
    schedule.setEvery(1);

    signalSchedule.setSignal(signal);
    signalSchedule.setSchedule(schedule);

    return signalSchedule.iterator();
  }

  @Test
  public void testFixedDailyIteratorBefore() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 6, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 2, 12, 0, 0), iterator.next());
  }

  @Test
  public void testFixedDailyIteratorOn1() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 12, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 12, 0, 0), iterator.next());
  }

  @Test
  public void testFixedDailyIteratorDuring1() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 18, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 12, 0, 0), iterator.next());
  }

  @Test
  public void testFixedDailyIteratorDuring2() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 6, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 12, 0, 0), iterator.next());
  }

  @Test
  public void testFixedDailyIteratorOn2() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 12, 0, 0));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testFixedDailyIteratorAfter() {
    SignalScheduleIterator iterator = createFixedDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 18, 0, 0));

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

  private SignalScheduleIterator createRandomDailyIterator() {
    SignalSchedule signalSchedule = new SignalSchedule();

    RandomSignal signal = new RandomSignal();
    signal.setStartTime(new LocalTime(9, 0, 0));
    signal.setEndTime(new LocalTime(17, 0, 0));
    signal.setFrequency(8);

    DailySchedule schedule = new DailySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 3));
    schedule.setEvery(1);

    signalSchedule.setSignal(signal);
    signalSchedule.setSchedule(schedule);

    return signalSchedule.iterator(createRandom());
  }

  @Test
  public void testRandomDailyIteratorBefore() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 6, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 2, 9, 8, 0), iterator.next());
  }

  @Test
  public void testRandomDailyIteratorOn1() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 12, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 9, 8, 0), iterator.next());
  }

  @Test
  public void testRandomDailyIteratorDuring1() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 2, 18, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 9, 8, 0), iterator.next());
  }

  @Test
  public void testRandomDailyIteratorDuring2() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 6, 0, 0));

    assertTrue(iterator.hasNext());
    assertEquals(new DateTime(2012, 9, 3, 9, 8, 0), iterator.next());
  }

  @Test
  public void testRandomDailyIteratorOn2() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 12, 0, 0));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testRandomDailyIteratorAfter() {
    SignalScheduleIterator iterator = createRandomDailyIterator();
    iterator.advanceTo(new DateTime(2012, 9, 3, 18, 0, 0));

    assertFalse(iterator.hasNext());
  }
}
