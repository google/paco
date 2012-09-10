package com.google.paco.shared.model;

import java.text.ParseException;

import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import com.google.paco.shared.model.MonthlySchedule.Day;
import com.google.paco.shared.model.MonthlySchedule.Week;

public class MonthlyScheduleIterator extends ScheduleIterator {
  protected MonthlyScheduleIterator(MonthlySchedule schedule) {
    try {
      this.iterator =
          LocalDateIteratorFactory.createLocalDateIterator(recurrenceString(schedule),
              schedule.getStartDate(), true);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private String recurrenceString(MonthlySchedule schedule) {
    StringBuffer sb = new StringBuffer();

    sb.append("RRULE:");
    sb.append("WKST=").append("SU").append(";");
    sb.append("FREQ=").append("WEEKLY").append(";");
    sb.append("INTERVAL=").append(schedule.getEvery()).append(";");

    if (schedule.byDay()) {
      sb.append("BYMONTHDAY=").append(monthDaysString(schedule)).append(";");
    } else {
      sb.append("BYDAY=").append(daysString(schedule)).append(";");
    }

    if (schedule.hasEndDate()) {
      sb.append("UNTIL=").append(schedule.getEndDate().toString("yyyyMMdd")).append(";");
    }

    return sb.toString();
  }

  private String daysString(MonthlySchedule schedule) {
    StringBuffer days = new StringBuffer();

    for (Day day : Day.values()) {
      for (Week week : Week.values()) {
        if (schedule.onDay(day) && schedule.onWeek(week)) {
          days.append(week.ordwk);
          days.append(day.abbrev);
          days.append(",");
        }
      }
    }

    if (days.length() > 1) {
      days.setLength(days.length() - 1);
    }

    return days.toString();
  }

  private String monthDaysString(MonthlySchedule schedule) {
    StringBuffer monthDays = new StringBuffer();

    for (int i = 1; i < 32; i++) {
      if (schedule.onDay(i)) {
        monthDays.append(i);
        monthDays.append(",");
      }
    }

    if (monthDays.length() > 1) {
      monthDays.setLength(monthDays.length() - 1);
    }

    return monthDays.toString();
  }
}
