package com.google.android.apps.paco;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.google.paco.shared.model.SignalingMechanismDAO;

@JsonTypeInfo(
              use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type")
          @JsonSubTypes({
              @Type(value = SignalSchedule.class, name = "signalSchedule"),
              @Type(value = Trigger.class, name = "trigger") })
public class SignalingMechanism {

  private static final int DEFAULT_TIMEOUT_MINUTES = 59;
  private static final Integer DEFAULT_MINIMUM_BUFFER = 59;

  protected Integer timeout;
  protected Integer minimumBuffer;
  protected Integer snoozeCount = SignalingMechanismDAO.SNOOZE_COUNT_DEFAULT;
  protected Integer snoozeTime = SignalingMechanismDAO.SNOOZE_TIME_DEFAULT;

  public Integer getTimeout() {
    if (timeout == null) {
      return DEFAULT_TIMEOUT_MINUTES;
    }
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  @JsonIgnore
  public void setType(String type) {

  }

  @JsonIgnore
  public String getType() {
    return "";
  }

  public Integer getMinimumBuffer() {
    if (minimumBuffer == null) {
      return DEFAULT_MINIMUM_BUFFER;
    }
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime;
  }


}
