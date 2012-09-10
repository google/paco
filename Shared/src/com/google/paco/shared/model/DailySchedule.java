// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("daily")
public class DailySchedule extends Schedule {
  /**
   *
   */
  public DailySchedule() {
    super(Schedule.Type.Daily);
  }

  @Override
  public ScheduleIterator iterator() {
    if (!isValid()) {
      return null;
    }

    return new DailyScheduleIterator(this);
  }

  @Override
  public ScheduleIterator iterator(Random random) {
    return iterator(); // we don't care about randomness
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
