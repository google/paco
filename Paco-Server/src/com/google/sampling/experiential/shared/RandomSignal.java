// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("random")
public class RandomSignal extends Signal {
  private Date startTime;
  private Date endTime;
  private int frequency;

  /**
   *
   */
  public RandomSignal() {
    super(Signal.Type.Random);
  }

  /**
   * @return the startTime
   */
  public Date getStartTime() {
    return startTime;
  }

  /**
   * @param startTime the startTime to set
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  /**
   * @return the endTime
   */
  public Date getEndTime() {
    return endTime;
  }

  /**
   * @param endTime the endTime to set
   */
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  /**
   * @return the frequency
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(int frequency) {
    this.frequency = frequency;
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

    RandomSignal other = (RandomSignal) obj;

    if (getStartTime() == null) {
      if (other.getStartTime() != null) {
        return false;
      }
    } else {
      if (getStartTime().equals(other.getStartTime()) == false) {
        return false;
      }
    }

    if (getEndTime() == null) {
      if (other.getEndTime() != null) {
        return false;
      }
    } else {
      if (getEndTime().equals(other.getEndTime()) == false) {
        return false;
      }
    }

    if (getFrequency() != other.getFrequency()) {
      return false;
    }

    return true;
  }
}
