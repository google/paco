package com.google.android.apps.paco.test;

import com.google.android.apps.paco.SignalSchedule;

import android.test.AndroidTestCase;

public class SignalScheduleTest extends AndroidTestCase {

  private SignalSchedule schedule;

  protected void setUp() {
    schedule = new SignalSchedule();
    schedule.setScheduleType(SignalSchedule.WEEKLY);
  }

  public void testAddWeekDayToSchedule() {
    schedule.addWeekDayToSchedule(SignalSchedule.SATURDAY);
    assertTrue(schedule.isWeekDayScheduled(SignalSchedule.SATURDAY));
    for (int i = 0; i < 6; ++i) {
      assertFalse(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[i]));
    }
  }

  public void testAddMultipleWeekDaysToSchedule() {
    schedule.addWeekDayToSchedule(SignalSchedule.FRIDAY);
    schedule.addWeekDayToSchedule(SignalSchedule.SATURDAY);
    for (int i = 0; i < 5; ++i) {
      assertFalse(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[i]));
    }
    for (int j = 5; j < 6; ++j) {
      assertTrue(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[j]));
    }

  }

  public void testRemoveAllWeekDaysScheduled() {
    schedule.addWeekDayToSchedule(SignalSchedule.SATURDAY);
    schedule.addWeekDayToSchedule(SignalSchedule.THURSDAY);
    schedule.removeAllWeekDaysScheduled();
    for (int i = 0; i < 7; ++i) {
      assertFalse(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[i]));
    }
  }

  public void testRemoveWeekDayFromSchedule() {
    for (int i = 0; i < 7; ++i) {
      schedule.addWeekDayToSchedule(SignalSchedule.DAYS_OF_WEEK[i]);
    }
    schedule.removeWeekDayFromSchedule(SignalSchedule.SUNDAY);
    assertFalse(schedule.isWeekDayScheduled(SignalSchedule.SUNDAY));
    for (int j = 1; j < 7; ++j) {
      assertTrue(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[j]));
    }
  }

  public void testRemoveMultipleWeekDaysFromSchedule() {
    for (int i = 0; i < 7; ++i) {
      schedule.addWeekDayToSchedule(SignalSchedule.DAYS_OF_WEEK[i]);
    }
    schedule.removeWeekDayFromSchedule(SignalSchedule.SUNDAY);
    schedule.removeWeekDayFromSchedule(SignalSchedule.MONDAY);
    assertFalse(schedule.isWeekDayScheduled(SignalSchedule.SUNDAY));
    assertFalse(schedule.isWeekDayScheduled(SignalSchedule.MONDAY));
    for (int j = 2; j < 7; ++j) {
      assertTrue(schedule.isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[j]));
    }
  }

}
