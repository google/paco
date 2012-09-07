// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
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
   *
   */
  public SignalSchedule() {
    super();

    this.editable = false;
    this.signal = null;
    this.schedule = null;
  }

  /**
   * @return the isEditable
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * @param isEditable the isUserEditable to set
   */
  public void setEditable(boolean isEditable) {
    this.editable = isEditable;
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

  public DateTime getNextAlarm(DateTime now, long seed) {
    if (hasSignalSchedule() == false) {
      return null;
    }

    LocalDate date = schedule.getCurrentDate(now.toLocalDate(), seed);
    LocalTime time = signal.getNextTime(now.toLocalTime(), seed);

    // Check if now is before start date (or pre-condition failed)
    if (date == null) {
      return null;
    }

    // Now is after all signal times, roll over to next date (or there are no times)
    if (time == null) {
      date = schedule.getNextDate(now.toLocalDate(), seed);

      // Now is past end date
      if (date == null) {
        return null;
      }

      // Get first time since we rolled over on date
      time = signal.getNextTime(new LocalTime(0, 0, 0), seed);

      // There are no times
      if (time == null) {
        return null;
      }
    }

    return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
        time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
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
