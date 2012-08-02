// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Date;
import java.util.Set;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("fixed")
public class FixedSignal extends Signal {
  private Set<Date> times;

  /**
   *
   */
  public FixedSignal() {
    super(Signal.FIXED);

    this.times = Sets.newLinkedHashSet();
  }

  /**
   * @return the times
   */
  public Set<Date> getTimes() {
    return times;
  }

  /**
   * @param times the times to set
   */
  public void setTimes(Set<Date> times) {
    if (times == null) {
      this.times = Sets.newLinkedHashSet();
    } else {
      this.times = times;
    }
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
