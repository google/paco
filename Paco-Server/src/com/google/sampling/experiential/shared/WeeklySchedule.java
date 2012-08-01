// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("weekly")
public class WeeklySchedule extends DailySchedule {
  private int dayRepeat;

  /**
   *
   */
  public WeeklySchedule() {
    super(Schedule.WEEKLY);
  }

  /**
   * @param type
   */
  protected WeeklySchedule(String type) {
    super(type);
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

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.DailySchedule#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    WeeklySchedule other = (WeeklySchedule) obj;

    if (getDayRepeat() != other.getDayRepeat()) {
      return false;
    }

    return true;
  }
}
