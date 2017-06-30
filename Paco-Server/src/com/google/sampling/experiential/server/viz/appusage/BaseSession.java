package com.google.sampling.experiential.server.viz.appusage;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class BaseSession {

  protected DateTime startTime;
  private DateTime endTime;

  public BaseSession(DateTime startTime) {
    super();
    this.startTime = startTime;
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
    return Seconds.secondsBetween(startTime, endTime).getSeconds();
  }

  @Override
  public String toString() {
    return "total time: " + getDurationInSeconds() + ". start: " + getStartTime().toString() + " - " + getEndTime().toString();
  }
}