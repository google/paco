// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("daily")
public class DailySchedule extends Schedule {
  protected int every = 0;

  /**
   *
   */
  public DailySchedule() {
    this(Schedule.DAILY);
  }

  /**
   * @param type
   */
  protected DailySchedule(String type) {
    super(type);
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
