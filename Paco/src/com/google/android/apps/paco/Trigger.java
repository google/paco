package com.google.android.apps.paco;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Trigger extends SignalingMechanism {

  public static final int HANGUP = 1;
  public static final int USER_PRESENT = 2;
  public static final int PACO_ACTION_EVENT = 3;
  public static final int APP_USAGE = 4;


  public static final Map<Integer, String> EVENT_NAMES;
  static {
    EVENT_NAMES = new HashMap<Integer, String>();
    EVENT_NAMES.put(HANGUP, "Phone Hangup");
    EVENT_NAMES.put(USER_PRESENT, "User Present");
    EVENT_NAMES.put(PACO_ACTION_EVENT, "Paco Action");
    EVENT_NAMES.put(APP_USAGE, "App Usage");
  }


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
    return event == eventCode && (eventCode != PACO_ACTION_EVENT || sourceIdentifier.equals(this.sourceIdentifier));
  }

  public long getDelay() {
    return delay;
  }

  public static String getNameForCode(int code2) {
    return EVENT_NAMES.get(code2);
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
