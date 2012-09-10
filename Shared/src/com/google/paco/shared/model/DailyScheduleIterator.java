package com.google.paco.shared.model;

import java.text.ParseException;

import com.google.ical.compat.jodatime.LocalDateIteratorFactory;

public class DailyScheduleIterator extends ScheduleIterator {
  protected DailyScheduleIterator(DailySchedule schedule) {
    try {
      this.iterator =
          LocalDateIteratorFactory.createLocalDateIterator(recurrenceString(schedule),
              schedule.getStartDate(), true);
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private String recurrenceString(DailySchedule schedule) {
    StringBuffer sb = new StringBuffer();

    sb.append("RRULE:");
    sb.append("WKST=").append("SU").append(";");
    sb.append("FREQ=").append("DAILY").append(";");
    sb.append("INTERVAL=").append(schedule.getEvery()).append(";");

    if (schedule.hasEndDate()) {
      sb.append("UNTIL=").append(schedule.getEndDate().toString("yyyyMMdd")).append(";");
    }

    return sb.toString();
  }
}
