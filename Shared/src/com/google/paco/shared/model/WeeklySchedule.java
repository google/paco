// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * A schedule that repeats every specified N weeks on the specified days.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("weekly")
public class WeeklySchedule extends DailySchedule {
  public enum Day {
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
  }

  private int dayRepeat;

  /**
   * Default constructor with sane defaults.
   */
  public WeeklySchedule() {
    this(Schedule.Type.Weekly);
  }

  /**
   * @param type the type of schedule
   */
  protected WeeklySchedule(Type type) {
    super(type);

    this.dayRepeat = 0;
  }

  /**
   * @return a bitmap specifying which days of the week to repeat on
   */
  public int getDayRepeat() {
    return dayRepeat;
  }

  /**
   * @param day
   */
  @JsonIgnore
  public void setOnDay(Day day) {
    int bit = 1 << day.ordinal();
    dayRepeat |= bit;
  }

  /**
   * @param day the day
   * @return whether the day is part of the repeated week
   */
  @JsonIgnore
  public boolean isOnDay(Day day) {
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
