package com.google.paco.shared.model;

import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.paco.shared.model.WeeklySchedule.Day;

public class WeeklyScheduleIteratorTest {
  private ScheduleIterator createIterator() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 9, 18));
    schedule.setEvery(2);
    schedule.setOnDay(Day.Monday);
    schedule.setOnDay(Day.Friday);

    return schedule.iterator();
  }

  @Test
  public void testIterator() {
    WeeklySchedule schedule = new WeeklySchedule();

    ScheduleIterator iterator = schedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDate() {
    WeeklySchedule schedule = new WeeklySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateBefore1() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 3), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateBefore2() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 3), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateOn1() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 3));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 7), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateDuring1() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 5));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 7), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateOn2() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 7));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 17), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateDuring2() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 10));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 17), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateDuring3() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 14));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 17), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateOn3() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 17));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateAfter() {
    ScheduleIterator iterator = createIterator();
    iterator.advanceTo(new LocalDate(2012, 9, 18));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorWithStartDateAndEvery1() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(1);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 9), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 9), iterator.next());
  }

  @Test
  public void testIteratorWithStartDateAndEndDateAndEvery2() {
    WeeklySchedule schedule = new WeeklySchedule();

    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(Day.Sunday);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertFalse(iterator.hasNext());
  }
}
