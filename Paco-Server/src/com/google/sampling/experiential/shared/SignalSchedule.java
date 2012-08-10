// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SignalSchedule {
  @JsonIgnore
  private String subject;
  @JsonIgnore
  private long experimentId;

  private boolean isUserEditable;
  private Signal signal;
  private Schedule schedule;

  /**
   *
   */
  public SignalSchedule() {
    super();

    this.isUserEditable = false;
    this.signal = null;
    this.schedule = null;
  }

  /**
   * @return the isUserEditable
   */
  public boolean isUserEditable() {
    return isUserEditable;
  }

  /**
   * @param isUserEditable the isUserEditable to set
   */
  public void setUserEditable(boolean isUserEditable) {
    this.isUserEditable = isUserEditable;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the experimentId
   */
  public long getExperimentId() {
    return experimentId;
  }

  /**
   * @param experimentId the experimentId to set
   */
  public void setExperimentId(long experimentId) {
    this.experimentId = experimentId;
  }

  /**
   * @return the signal
   */
  public Signal getSignal() {
    return signal;
  }

  /**
   * @param signal the signal to set
   */
  public void setSignal(Signal signal) {
    this.signal = signal;
  }

  /**
   * @return the schedule
   */
  public Schedule getSchedule() {
    return schedule;
  }

  /**
   * @param schedule the schedule to set
   */
  public void setSchedule(Schedule schedule) {
    this.schedule = schedule;
  }

  /**
   * @return whether the signalSchedule exists
   */
  public boolean hasSignalSchedule() {
    return (signal != null && schedule != null);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (!(obj instanceof SignalSchedule)) {
      return false;
    }

    SignalSchedule other = (SignalSchedule) obj;

    if (isUserEditable() != other.isUserEditable()) {
      return false;
    }

    if (hasSignalSchedule()) {
      if (getSchedule().equals(other.getSchedule()) == false
          || getSignal().equals(other.getSignal()) == false) {
        return false;
      }
    } else {
      if (other.hasSignalSchedule()) {
        return false;
      }
    }

    return true;
  }
}
