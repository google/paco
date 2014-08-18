package com.google.paco.shared.model;

import java.io.Serializable;


public abstract class SignalingMechanismDAO implements Serializable {

  public static final int SNOOZE_TIME_DEFAULT = 600000;
  public static final int SNOOZE_COUNT_DEFAULT = 0;
  public static final String TRIGGER_SIGNAL_TIMEOUT = "59";
  public static final String ESM_SIGNAL_TIMEOUT = "59";
  public static final String FIXED_SCHEDULE_TIMEOUT = "479";


  protected String type;
  protected Integer timeout;
  protected Integer minimumBuffer;
  protected Integer snoozeCount = SNOOZE_COUNT_DEFAULT;
  protected Integer snoozeTime = SNOOZE_TIME_DEFAULT; // 10 minutes (10min * 60sec * 1000ms)

  public SignalingMechanismDAO() {
    super();
  }

  public abstract void setId(Long object);

  public abstract Long getId();

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;

  }

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount != null ? snoozeCount : SNOOZE_COUNT_DEFAULT;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime != null ? snoozeTime : SNOOZE_TIME_DEFAULT;
  }

  public int getSnoozeTimeInMinutes() {
    return getSnoozeTime() / 1000 / 60;
  }

  public void setSnoozeTimeInMinutes(int minutes) {
    this.snoozeTime = minutes * 60 * 1000;
  }
}
