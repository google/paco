package com.google.paco.shared.model;

import static org.junit.Assert.*;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.paco.shared.model.MonthlySchedule.Day;
import com.google.paco.shared.model.MonthlySchedule.Week;

public class MonthlyScheduleIteratorTest {
  private ScheduleIterator createIteratorByDay() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 11, 14));
    schedule.setEvery(2);
    schedule.setOnDay(5);
    schedule.setOnDay(19);

    return schedule.iterator();
  }

  @Test
  public void testIteratorByDay() {
    MonthlySchedule schedule = new MonthlySchedule();

    ScheduleIterator iterator = schedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayWithStartDate() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateBefore1() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 5), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateBefore2() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 5), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateOn1() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 9, 5));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 19), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateDuring1() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 9, 10));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 9, 19), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateOn2() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 9, 19));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 11, 5), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateDuring2() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 10, 5));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 11, 5), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateDuring3() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 10, 19));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 11, 5), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateOn3() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 11, 5));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateAfter() {
    ScheduleIterator iterator = createIteratorByDay();
    iterator.advanceTo(new LocalDate(2012, 11, 19));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEvery1() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(1);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 10, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertTrue(iterator.hasNext());
    assertEquals(new LocalDate(2012, 11, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayWithStartDateAndEndDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertFalse(iterator.hasNext());
  }

  private ScheduleIterator createIteratorByDayOfWeek() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 2));
    schedule.setEndDate(new LocalDate(2012, 11, 14));
    schedule.setEvery(2);
    schedule.setOnWeek(Week.First);
    schedule.setOnWeek(Week.Third);
    schedule.setOnDay(Day.Saturday);
    schedule.setOnDay(Day.Friday);

    return schedule.iterator();
  }

  @Test
  public void testIteratorByDayOfWeek() {
    MonthlySchedule schedule = new MonthlySchedule();

    ScheduleIterator iterator = schedule.iterator();

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDate() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateBefore1() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 1));

    assertEquals(new LocalDate(2012, 9, 7), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateBefore2() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertEquals(new LocalDate(2012, 9, 7), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateOn1() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 7));

    assertEquals(new LocalDate(2012, 9, 15), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateDuring1() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 14));

    assertEquals(new LocalDate(2012, 9, 15), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateOn2() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 15));

    assertEquals(new LocalDate(2012, 9, 21), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateDuring2() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 18));

    assertEquals(new LocalDate(2012, 9, 21), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateOn3() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 9, 21));

    assertEquals(new LocalDate(2012, 11, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateDuring3() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 10, 5));

    assertEquals(new LocalDate(2012, 11, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateDuring4() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 10, 20));

    assertEquals(new LocalDate(2012, 11, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateOn4() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 11, 2));

    assertEquals(new LocalDate(2012, 11, 3), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateOn5() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 11, 3));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateAfter1() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 11, 7));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateAfter2() {
    ScheduleIterator iterator = createIteratorByDayOfWeek();
    iterator.advanceTo(new LocalDate(2012, 11, 8));

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEvery1() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(1);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertEquals(new LocalDate(2012, 10, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertEquals(new LocalDate(2012, 11, 2), iterator.next());
  }

  @Test
  public void testIteratorByDayOfWeekWithStartDateAndEndDateAndEvery2() {
    MonthlySchedule schedule = new MonthlySchedule();
    schedule.setStartDate(new LocalDate(2012, 9, 1));
    schedule.setEndDate(new LocalDate(2012, 9, 2));
    schedule.setOnDay(2);
    schedule.setEvery(2);

    ScheduleIterator iterator = schedule.iterator();
    iterator.advanceTo(new LocalDate(2012, 9, 2));

    assertFalse(iterator.hasNext());
  }
}
