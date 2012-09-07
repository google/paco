// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("monthly")
public class MonthlySchedule extends Schedule {
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

  public enum Week {
    First(1), Second(2), Third(3), Fourth(4), Fifth(5);

    public int ordwk;

    private Week(int ordwk) {
      this.ordwk = ordwk;
    }
  }

  private int every;
  private int dayRepeat; // 1-31 bitmap if weekRepeat is 0
  private int weekRepeat; // if !0, dayRepeat is bitmap of SMTWTFS

  /**
   *
   */
  public MonthlySchedule() {
    this(Schedule.Type.Monthly);
  }

  /**
   * @param type
   */
  protected MonthlySchedule(Type type) {
    super(type);

    this.dayRepeat = 0;
    this.weekRepeat = 0;
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
   * @return the weekRepeat
   */
  public int getWeekRepeat() {
    return weekRepeat;
  }

  public void setOnDay(int day) {
    int bit = 1 << day;
    dayRepeat |= bit;
  }

  public boolean onDay(int day) {
    int bit = 1 << day;
    return ((dayRepeat & bit) == bit);
  }

  public void setOnDay(Day day) {
    setOnDay(day.ordinal());
  }

  public boolean onDay(Day day) {
    return onDay(day.ordinal());
  }

  public void setOnWeek(Week week) {
    int bit = 1 << week.ordinal();
    weekRepeat |= bit;
  }

  public boolean onWeek(Week week) {
    int bit = 1 << week.ordinal();
    return ((weekRepeat & bit) == bit);
  }

  /**
   * @param weekRepeat the weekRepeat to set
   */
  public void setWeekRepeat(int weekRepeat) {
    this.weekRepeat = weekRepeat;
  }

  private String daysString() {
    StringBuffer days = new StringBuffer();

    for (Day day : Day.values()) {
      for (Week week : Week.values()) {
        if (onDay(day) && onWeek(week)) {
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

  private String monthDaysString() {
    StringBuffer monthDays = new StringBuffer();

    for (int i = 1; i < 32; i++) {
      if (onDay(i)) {
        monthDays.append(i);
        monthDays.append(",");
      }
    }

    if (monthDays.length() > 1) {
      monthDays.setLength(monthDays.length() - 1);
    }

    return monthDays.toString();
  }

  @Override
  protected String getRData() {
    String rdata;

    if (weekRepeat == 0) {
      rdata = String.format("BYMONTHDAY=%s", monthDaysString());
    } else {
      rdata = String.format("BYDAY=%s", daysString());
    }

    return String.format("RRULE:FREQ=MONTHLY;WKST=SU;INTERVAL=%d;%s", every, rdata);
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

    MonthlySchedule other = (MonthlySchedule) obj;

    if (getEvery() != other.getEvery()) {
      return false;
    }

    if (getDayRepeat() != other.getDayRepeat()) {
      return false;
    }

    if (getWeekRepeat() != other.getWeekRepeat()) {
      return false;
    }

    return true;
  }
}
