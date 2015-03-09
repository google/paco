package com.google.android.apps.paco;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.paco.shared.model.TriggerDAO;

public class Trigger extends SignalingMechanism {



  private int eventCode;
  private long delay;
  private String sourceIdentifier;

  public int getEventCode() {
    return eventCode;
  }

  public void setEventCode(int code) {
    this.eventCode = code;
  }

  public Trigger(int eventCode) {
    this.eventCode = eventCode;
  }

  public Trigger() {
  }

  public boolean match(int event, String sourceIdentifier) {
    return event == eventCode && (eventCode != TriggerDAO.PACO_ACTION_EVENT || sourceIdentifier.equals(this.sourceIdentifier));
  }

  public long getDelay() {
    return delay;
  }

  public static String getNameForCode(int code2) {
    return TriggerDAO.EVENT_NAMES[code2 - 1];
  }

  @Override
  public String toString() {
    return "Trigger: event: " + Trigger.getNameForCode(this.eventCode) + ", delay = " + Long.toString(delay);
  }

  public String getSourceIdentifier() {
    return sourceIdentifier;
  }

  public void setSourceIdentifier(String sourceIdentifier) {
    this.sourceIdentifier = sourceIdentifier;
  }

  @JsonIgnore
  public String getType() {
    return TRIGGER_TYPE;
  }

}
