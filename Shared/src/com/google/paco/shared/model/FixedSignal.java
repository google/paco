// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.LocalTime;

import java.util.Set;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("fixed")
public class FixedSignal extends Signal {
  private Set<LocalTime> times;

  /**
   *
   */
  public FixedSignal() {
    super(Signal.Type.Fixed);

    this.times = Sets.newLinkedHashSet();
  }

  /**
   * @return the times
   */
  public Set<LocalTime> getTimes() {
    return times;
  }

  /**
   * @param times the times to set
   */
  public void setTimes(Set<LocalTime> times) {
    if (times == null) {
      this.times = Sets.newLinkedHashSet();
    } else {
      this.times = times;
    }
  }

  /**
   * @param time the time
   * @return whether the time was added
   */
  public boolean addTime(LocalTime time) {
    if (times == null) {
      times = Sets.newLinkedHashSet();
    }

    return times.add(time);
  }

  @Override
  public LocalTime getNextTime(LocalTime now, long seed) {
    int minDiff = -1;
    LocalTime minDiffTime = null;

    for (LocalTime time : getTimes()) {
      // Skip times that are before now
      if (time.isEqual(now) || time.isBefore(now)) {
        continue;
      }

      // Find time with minimum difference
      int diff = time.getMillisOfDay() - now.getMillisOfDay();

      if (minDiffTime == null || diff < minDiff) {
        minDiffTime = time;
        minDiff = diff;
      }
    }

    return minDiffTime;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.Signal#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    FixedSignal other = (FixedSignal) obj;

    if (getTimes().equals(other.getTimes()) == false) {
      return false;
    }

    return true;
  }
}
