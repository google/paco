// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("weekly")
public class WeeklySchedule extends DailySchedule {
  public enum Day {
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  }

  private int dayRepeat;

  /**
   *
   */
  public WeeklySchedule() {
    this(Schedule.Type.Weekly);
  }

  /**
   * @param type
   */
  protected WeeklySchedule(Type type) {
    super(type);

    this.dayRepeat = 0;
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
