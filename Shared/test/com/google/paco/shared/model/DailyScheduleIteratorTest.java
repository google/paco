package com.google.paco.shared.model;

import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Test;

public class DailyScheduleIteratorTest {
  private ScheduleIterator createIterator() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 4));
    schedule.setEvery(2);

    return schedule.iterator();
  }

  @Test
  public void testDefaultIterator() {
    DailySchedule schedule = new DailySchedule();

    ScheduleIterator iterator = schedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDate() {
    DailySchedule schedule = new DailySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateBefore() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 2), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateOn1() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 4), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateDuring() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 3));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 4), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateOn2() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 4));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateAfter() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 5));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEvery1() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(1);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 3), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 4), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 3), iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 5), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateAndEvery2() {
    DailySchedule schedule = new DailySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertFalse(iterator.hasNext());
  }
}
