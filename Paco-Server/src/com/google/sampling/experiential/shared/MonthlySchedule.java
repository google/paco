// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("monthly")
public class MonthlySchedule extends WeeklySchedule {
  public enum Week {
    First, Second, Third, Fourth, Fifth
  }

  private boolean byDay;
  private int weekRepeat;

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

    this.byDay = false;
    this.weekRepeat = 0;
  }

  /**
   * @return whether the schedule is by day
   */
  public boolean isByDay() {
    return byDay;
  }

  /**
   * @param byDayOfWeek the byDayOfWeek to set
   */
  public void setByDay(boolean byDayOfWeek) {
    this.byDay = byDayOfWeek;
  }

  /**
   * @return the weekRepeat
   */
  public int getWeekRepeat() {
    return weekRepeat;
  }

  /**
   * @param weekRepeat the weekRepeat to set
   */
  public void setWeekRepeat(int weekRepeat) {
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
