// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Date;

/**
 * A random signal randomly signals the specified amount of times between the
 * specified start and end time.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("random")
public class RandomSignal extends Signal {
  private Date startTime;
  private Date endTime;
  private int frequency;

  /**
   * Default constructor with sane defaults.
   */
  public RandomSignal() {
    super(Signal.Type.Random);
  }

  /**
   * @return the beginning of the time interval when the user can be randomly signaled
   */
  public Date getStartTime() {
    return startTime;
  }

  /**
   * @param startTime the beginning of the time interval when the user can be randomly signaled
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  /**
   * @return the end of the time interval when the user can be randomly signaled
   */
  public Date getEndTime() {
    return endTime;
  }

  /**
   * @param endTime the end of the time interval when the user can be randomly signaled
   */
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  /**
   * @return the number of times to signal the user during the time interval
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * @param frequency the number of times to signal the user during the time interval
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
