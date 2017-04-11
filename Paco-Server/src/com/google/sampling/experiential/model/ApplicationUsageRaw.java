package com.google.sampling.experiential.model;

import java.io.Serializable;

public class ApplicationUsageRaw implements Serializable {
  long duration;
  String aplication;
  String startTime;
  String endTime;

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public String getAplication() {
    return aplication;
  }

  public void setAplication(String aplication) {
    this.aplication = aplication;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

}
