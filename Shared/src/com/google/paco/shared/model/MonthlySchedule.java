// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * A schedule that repeats every specified N months. The schedule can be
 * repeated on a specific day of a week of the month, or on a specific day of 
 * the month.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("monthly")
public class MonthlySchedule extends WeeklySchedule {
  public enum Week {
    First, Second, Third, Fourth, Fifth
  }

  private Week weekRepeat;

  /**
   * Default constructor with sane defaults.
   */
  public MonthlySchedule() {
    this(Schedule.Type.Monthly);
  }

  /**
   * @param type the schedule type
   */
  protected MonthlySchedule(Type type) {
    super(type);

    this.weekRepeat = null;
  }

  /**
   * @return whether the schedule repeats on a specific day of the month
   */
  public boolean isByDay() {
    return weekRepeat == null;
  }

  /**
   * @return the week of the month to repeat if not by day
   */
  public Week getWeekRepeat() {
    return weekRepeat;
  }

  /**
   * @param weekRepeat the week of the month to repeat if not by day
   */
  public void setWeekRepeat(Week weekRepeat) {
    this.weekRepeat = weekRepeat;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.WeeklySchedule#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    MonthlySchedule other = (MonthlySchedule) obj;

    if (isByDay() != other.isByDay()) {
      return false;
    }

    if (getWeekRepeat() != other.getWeekRepeat()) {
      return false;
    }

    return true;
  }
}
