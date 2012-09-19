// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.LocalTime;

import java.util.Random;
import java.util.Set;

/**
 * @author corycornelius@google.com (Cory Cornelius)
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
  public SignalIterator iterator() {
    return new FixedSignalIterator(this);
  }

  @Override
  public SignalIterator iterator(Random random) {
    return iterator(); // we don't care about randomness
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

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Signal#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("at ");
    for (LocalTime time : times) {
      sb.append(" ").append(time).append(",");
    }
    sb.delete(sb.length() - 1, sb.length());

    return sb.toString();
  }
}
