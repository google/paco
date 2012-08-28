// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Date;
import java.util.Set;

/**
 * A fixed signal prompts the user to respond at fixed times during the schedule.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("fixed")
public class FixedSignal extends Signal {
  private Set<Date> times;

  /**
   * Default constructor with sane defaults.
   */
  public FixedSignal() {
    super(Signal.Type.Fixed);

    this.times = Sets.newLinkedHashSet();
  }

  /**
   * @return the set of times to signal the user
   */
  public Set<Date> getTimes() {
    return times;
  }

  /**
   * @param times the set of times to signal the user
   */
  public void setTimes(Set<Date> times) {
    if (times == null) {
      this.times = Sets.newLinkedHashSet();
    } else {
      this.times = times;
    }
  }

  /**
   * @param time a time
   * @return whether the time was added to the set
   */
  public boolean addTime(Date time) {
    if (times == null) {
      times = Sets.newLinkedHashSet();
    }

    return times.add(time);
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
