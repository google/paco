// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A signal-schedule contains a signal and schedule that is optionally editable
 * by the user when they join an experiment. When a user joins an experiment 
 * with a custom signal-schedule, their name and the corresponding experiment
 * are stored with the signal-schedule as well.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class SignalSchedule {
  @JsonIgnore
  private String subject;
  @JsonIgnore
  private long experimentId;

  private boolean editable;
  private Signal signal;
  private Schedule schedule;

  /**
   * Default construct with sane defaults.
   */
  public SignalSchedule() {
    super();

    this.editable = false;
    this.signal = null;
    this.schedule = null;
  }

  /**
   * @return whether the signal and schedule are editable
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * @param isEditable whether the signal and schedule are editable
   */
  public void setEditable(boolean isEditable) {
    this.editable = isEditable;
  }

  /**
   * @return the subject who edited the signal-schedule
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject who edited the signal-schedule
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the experiment associated with this signal-schedule
   */
  public long getExperimentId() {
    return experimentId;
  }

  /**
   * @param experimentId the experiment associated with this signal-schedule
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
   * @param signal the signal
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
   * @param schedule the schedule
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

    if (isEditable() != other.isEditable()) {
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
