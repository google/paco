// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * A schedule that repeats every specified N days.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("daily")
public class DailySchedule extends Schedule {
  protected int every;

  /**
   * Default constructor with sane defaults.
   */
  public DailySchedule() {
    this(Schedule.Type.Daily);
  }

  /**
   * @param type the type of schedule
   */
  protected DailySchedule(Type type) {
    super(type);

    this.every = 0;
  }

  /**
   * @return how often to repeat the schedule between the time interval
   */
  public int getEvery() {
    return every;
  }

  /**
   * @param every how often to repeat the schedule between the time interval
   */
  public void setEvery(int every) {
    this.every = every;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.Schedule#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    DailySchedule other = (DailySchedule) obj;

    if (getEvery() != other.getEvery()) {
      return false;
    }

    return true;
  }
}
