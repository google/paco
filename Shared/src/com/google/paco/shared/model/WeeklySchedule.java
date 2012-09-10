// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("weekly")
public class WeeklySchedule extends Schedule {
  public enum Day {
    Monday("MO"), Tuesday("TU"), Wednesday("WE"), Thursday("TH"), Friday("FR"), Saturday("SA"), Sunday(
        "SU");

    public String abbrev;

    private Day(String abbrev) {
      this.abbrev = abbrev;
    }

    public Day next() {
      switch (this) {
        case Monday:
          return Tuesday;
        case Tuesday:
          return Wednesday;
        case Wednesday:
          return Thursday;
        case Thursday:
          return Friday;
        case Friday:
          return Saturday;
        case Saturday:
          return Sunday;
        case Sunday:
          return Monday;
        default:
          return null;
      }
    }
  }

  private int every;
  private int dayRepeat;

  /**
   *
   */
  public WeeklySchedule() {
    super(Schedule.Type.Weekly);

    this.every = 0;
    this.dayRepeat = 0;
  }

  /**
   * @return the repeatEvery
   */
  public int getEvery() {
    return every;
  }

  /**
   * @param every the repeat to set
   */
  public void setEvery(int every) {
    this.every = every;
  }

  /**
   * @return the repeatOnDay
   */
  public int getDayRepeat() {
    return dayRepeat;
  }

  /**
   * @param dayRepeat the repeatOn to set
   */
  public void setDayRepeat(int dayRepeat) {
    this.dayRepeat = dayRepeat;
  }

  /**
   * @param day the day
   * @return whether the day is set
   */
  public boolean onDay(Day day) {
    int bit = 1 << day.ordinal();
    return ((dayRepeat & bit) == bit);
  }

  /**
   * @param day
   */
  public void setOnDay(Day day) {
    int bit = 1 << day.ordinal();
    dayRepeat |= bit;
  }

  @Override
  public LocalDate getStartDate() {
    LocalDate startDate = super.getStartDate();

    while (!isValidDate(startDate)) {
      startDate = startDate.plusDays(1);
    }

    return startDate;
  }

  @Override
  public boolean isValid() {
    return (super.isValid() && getDayRepeat() > 0);
  }

  @Override
  public ScheduleIterator iterator() {
    if (!isValid()) {
      return null;
    }

    return new WeeklyScheduleIterator(this);
  }

  @Override
  public ScheduleIterator iterator(Random random) {
    return iterator(); // we don't care about randomness
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    WeeklySchedule other = (WeeklySchedule) obj;

    if (getEvery() != other.getEvery()) {
      return false;
    }

    if (getDayRepeat() != other.getDayRepeat()) {
      return false;
    }

    return true;
  }

  private boolean isValidDate(LocalDate date) {
    switch (date.getDayOfWeek()) {
      case DateTimeConstants.MONDAY:
        return onDay(Day.Monday);
      case DateTimeConstants.TUESDAY:
        return onDay(Day.Tuesday);
      case DateTimeConstants.WEDNESDAY:
        return onDay(Day.Wednesday);
      case DateTimeConstants.THURSDAY:
        return onDay(Day.Thursday);
      case DateTimeConstants.FRIDAY:
        return onDay(Day.Friday);
      case DateTimeConstants.SATURDAY:
        return onDay(Day.Saturday);
      case DateTimeConstants.SUNDAY:
        return onDay(Day.Sunday);
      default:
        return false;
    }
  }
}
