// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("monthly")
public class MonthlySchedule extends WeeklySchedule {
  private boolean byDayOfWeek;
  private int weekRepeat;

  /**
   *
   */
  public MonthlySchedule() {
    this(Schedule.MONTHLY);
  }

  /**
   * @param type
   */
  protected MonthlySchedule(String type) {
    super(type);

    this.byDayOfWeek = false;
    this.weekRepeat = 0;
  }

  /**
   * @return the byDayOfWeek
   */
  public boolean isByDayOfWeek() {
    return byDayOfWeek;
  }

  /**
   * @param byDayOfWeek the byDayOfWeek to set
   */
  public void setByDayOfWeek(boolean byDayOfWeek) {
    this.byDayOfWeek = byDayOfWeek;
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

    if (isByDayOfWeek() != other.isByDayOfWeek()) {
      return false;
    }

    if (getWeekRepeat() != other.getWeekRepeat()) {
      return false;
    }

    return true;
  }
}
