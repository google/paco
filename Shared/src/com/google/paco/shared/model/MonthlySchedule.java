// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * @author corycornelius@google.com (Cory Cornelius)
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

  public void setOnDay(Day day) {
    setOnDay(day.ordinal());
  }

  public boolean onDay(int day) {
    int bit = 1 << day;
    return ((dayRepeat & bit) == bit);
  }

  public boolean onDay(Day day) {
    return onDay(day.ordinal());
  }

  public void setOnWeek(Week week) {
    int bit = 1 << week.ordinal();
    weekRepeat |= bit;
  }

  public void setOnWeek(int weekOfMonth) {
    for (Week week : Week.values()) {
      if (week.ordwk == weekOfMonth) {
        setOnWeek(week);
        return;
      }
    }
  }

  public boolean onWeek(Week week) {
    int bit = 1 << week.ordinal();
    return ((weekRepeat & bit) == bit);
  }

  public boolean onWeek(int weekOfMonth) {
    for (Week week : Week.values()) {
      if (week.ordwk == weekOfMonth) {
        return onWeek(week);
      }
    }

    return false;
  }

  /**
   * @param weekRepeat the weekRepeat to set
   */
  public void setWeekRepeat(int weekRepeat) {
    this.weekRepeat = weekRepeat;
  }

  /**
   * @return
   */
  public boolean byDay() {
    return (weekRepeat == 0);
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

    return new MonthlyScheduleIterator(this);
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

  protected boolean isValidDate(LocalDate date) {
    // Make sure the date is a valid day of the month
    if (getWeekRepeat() == 0) {
      return onDay(date.getDayOfMonth());
    } else {
      int weekOfMonth = ((date.getDayOfMonth() - 1) / 7) + 1; // ceiling

      // Make sure the date is on a valid week of the month
      if (!onWeek(weekOfMonth)) {
        return false;
      }

      // Make sure the date is on a valid day of the week
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
}
