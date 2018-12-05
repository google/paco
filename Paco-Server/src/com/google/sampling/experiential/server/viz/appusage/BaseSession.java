package com.google.sampling.experiential.server.viz.appusage;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.pacoapp.paco.shared.util.TimeUtil;

public class BaseSession {

  public static final int DEFAULT_SESSION_LENGTH = 1;

  protected DateTime startTime;
  private DateTime endTime;

  public BaseSession(DateTime startTime) {
    super();
    this.startTime = startTime;
  }

  public BaseSession() {
  }

  public void endSession(DateTime responseTime) {
    this.endTime = responseTime;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(DateTime startTime) {
    this.startTime = startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(DateTime endTime) {
    this.endTime = endTime;
  }

  public int getDurationInSeconds() {
    if (startTime != null && endTime != null) {
      return Seconds.secondsBetween(startTime, endTime).getSeconds();
    }

    return 0;
  }

  @Override
  public String toString() {
    return "total time: " + getDurationInSeconds() + ". start-end: " + getStartTime().toString(TimeUtil.dateTimeNoZoneFormatter) + " - " + getEndTime().toString(TimeUtil.dateTimeNoZoneFormatter);
  }

  public DateTime ensureEndSession() {
    if (getEndTime() != null) {
      return getEndTime();
    } else {
      return addArtificialEndTime();
    }
  }

  protected DateTime addArtificialEndTime() {
    DateTime defaultEndTime = getStartTime().plusSeconds(DEFAULT_SESSION_LENGTH);
    setEndTime(defaultEndTime);
    return defaultEndTime;
  }

}