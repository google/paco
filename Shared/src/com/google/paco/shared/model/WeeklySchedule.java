// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;
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

  private String daysString() {
    StringBuffer days = new StringBuffer();

    for (Day day : Day.values()) {
      if (onDay(day)) {
        days.append(day.abbrev);
        days.append(",");
      }
    }

    if (days.length() > 1) {
      days.setLength(days.length() - 1);
    }

    return days.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#getRData()
   */
  @JsonIgnore
  @Override
  protected String getRData() {
    return String.format("RRULE:FREQ=WEEKLY;WKST=SU;INTERVAL=%d;BYDAY=%s", every, daysString());
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#isValidDate(org.joda.time.LocalDate)
   */
  @JsonIgnore
  @Override
  protected boolean isValidDate(LocalDate date) {
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
}
