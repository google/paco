package com.google.paco.shared.model;

import java.util.Iterator;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class SignalScheduleIterator implements Iterator<DateTime> {
  private SignalIterator signal;
  private ScheduleIterator schedule;
  private LocalDate date;
  private LocalTime time;

  public SignalScheduleIterator(SignalSchedule signalSchedule, Random random) {
    if (!signalSchedule.hasSignalSchedule()) {
      return;
    }

    this.signal = signalSchedule.getSignal().iterator(random);
    this.schedule = signalSchedule.getSchedule().iterator(random);
  }

  public void advanceTo(DateTime newStart) {
    if (signal == null || schedule == null) {
      return;
    }

    schedule.advanceTo(newStart.toLocalDate());
    signal.advanceTo(newStart.toLocalTime());

    if (!signal.hasNext() && schedule.hasNext()) {
      date = schedule.next();

      // If the start we desire isn't before the current date, then we want the next date
      if (!newStart.toLocalDate().isBefore(date)) {
        date = null;
      }

      signal.advanceTo(new LocalTime(0, 0, 0));
    }
  }

  @Override
  public boolean hasNext() {
    if (signal == null || schedule == null) {
      return false;
    }

    return (signal.hasNext() || schedule.hasNext());
  }

  @Override
  public DateTime next() {
    if (!hasNext()) {
      return null;
    }

    if (date == null) {
      date = schedule.next();
    }

    if (!signal.hasNext()) {
      signal.advanceTo(new LocalTime(0, 0, 0));

      if (schedule.hasNext()) {
        date = schedule.next();
      }
    }

    time = signal.next();

    if (time == null) {
      return null;
    }

    return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
        time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
