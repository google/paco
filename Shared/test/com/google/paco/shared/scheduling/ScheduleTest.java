package com.google.paco.shared.scheduling;

import junit.framework.TestCase;

import com.pacoapp.paco.shared.model2.Schedule;


public class ScheduleTest extends TestCase {

  private Schedule schedule;

  protected void setUp() {
    schedule = new Schedule();
    schedule.setScheduleType(Schedule.WEEKLY);
  }

  public void testAddWeekDayToSchedule() {
    schedule.addWeekDayToSchedule(Schedule.SATURDAY);
    assertTrue(schedule.isWeekDayScheduled(Schedule.SATURDAY));
    for (int i = 0; i < 6; ++i) {
      assertFalse(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[i]));
    }
  }

  public void testAddMultipleWeekDaysToSchedule() {
    schedule.addWeekDayToSchedule(Schedule.FRIDAY);
    schedule.addWeekDayToSchedule(Schedule.SATURDAY);
    for (int i = 0; i < 5; ++i) {
      assertFalse(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[i]));
    }
    for (int j = 5; j < 6; ++j) {
      assertTrue(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[j]));
    }

  }

  public void testRemoveAllWeekDaysScheduled() {
    schedule.addWeekDayToSchedule(Schedule.SATURDAY);
    schedule.addWeekDayToSchedule(Schedule.THURSDAY);
    schedule.removeAllWeekDaysScheduled();
    for (int i = 0; i < 7; ++i) {
      assertFalse(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[i]));
    }
  }

  public void testRemoveWeekDayFromSchedule() {
    for (int i = 0; i < 7; ++i) {
      schedule.addWeekDayToSchedule(Schedule.DAYS_OF_WEEK[i]);
    }
    schedule.removeWeekDayFromSchedule(Schedule.SUNDAY);
    assertFalse(schedule.isWeekDayScheduled(Schedule.SUNDAY));
    for (int j = 1; j < 7; ++j) {
      assertTrue(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[j]));
    }
  }

  public void testRemoveMultipleWeekDaysFromSchedule() {
    for (int i = 0; i < 7; ++i) {
      schedule.addWeekDayToSchedule(Schedule.DAYS_OF_WEEK[i]);
    }
    schedule.removeWeekDayFromSchedule(Schedule.SUNDAY);
    schedule.removeWeekDayFromSchedule(Schedule.MONDAY);
    assertFalse(schedule.isWeekDayScheduled(Schedule.SUNDAY));
    assertFalse(schedule.isWeekDayScheduled(Schedule.MONDAY));
    for (int j = 2; j < 7; ++j) {
      assertTrue(schedule.isWeekDayScheduled(Schedule.DAYS_OF_WEEK[j]));
    }
  }

}
