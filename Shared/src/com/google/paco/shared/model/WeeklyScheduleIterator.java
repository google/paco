package com.google.paco.shared.model;

import java.text.ParseException;

import com.google.ical.compat.jodatime.LocalDateIteratorFactory;
import com.google.paco.shared.model.WeeklySchedule.Day;

public class WeeklyScheduleIterator extends ScheduleIterator {
  protected WeeklyScheduleIterator(WeeklySchedule schedule) {
    try {
      this.iterator =
          LocalDateIteratorFactory.createLocalDateIterator(recurrenceString(schedule),
              schedule.getStartDate(), true);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private String recurrenceString(WeeklySchedule schedule) {
    StringBuffer sb = new StringBuffer();

    sb.append("RRULE:");
    sb.append("WKST=").append("SU").append(";");
    sb.append("FREQ=").append("WEEKLY").append(";");
    sb.append("INTERVAL=").append(schedule.getEvery()).append(";");
    sb.append("BYDAY=").append(daysString(schedule)).append(";");

    if (schedule.hasEndDate()) {
      sb.append("UNTIL=").append(schedule.getEndDate().toString("yyyyMMdd")).append(";");
    }

    return sb.toString();
  }

  private String daysString(WeeklySchedule schedule) {
    StringBuffer days = new StringBuffer();

    for (Day day : Day.values()) {
      if (schedule.onDay(day)) {
        days.append(day.abbrev);
        days.append(",");
      }
    }

    if (days.length() > 1) {
      days.setLength(days.length() - 1);
    }

    return days.toString();
  }
}
