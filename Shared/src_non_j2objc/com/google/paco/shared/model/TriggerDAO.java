package com.google.paco.shared.model;

import java.io.Serializable;


public class TriggerDAO extends SignalingMechanismDAO implements Serializable {

  public static final int HANGUP = 1;
  public static final int USER_PRESENT = 2;
  public static final int PACO_ACTION_EVENT = 3;
  public static final int APP_USAGE = 4;
  public static final int APP_CLOSED = 5;
  public static final int MUSIC_STARTED = 6;
  public static final int MUSIC_STOPPED = 7;
  public static final int PHONE_INCOMING_CALL_STARTED = 8;
  public static final int PHONE_INCOMING_CALL_ENDED = 9;
  public static final int PHONE_OUTGOING_CALL_STARTED = 10;
  public static final int PHONE_OUTGOING_CALL_ENDED = 11;
  public static final int PHONE_MISSED_CALL = 12; 
  
  public static final int[] EVENTS = new int[] {HANGUP, USER_PRESENT, PACO_ACTION_EVENT, APP_USAGE, APP_CLOSED, MUSIC_STARTED, MUSIC_STOPPED,
                                                PHONE_INCOMING_CALL_STARTED, PHONE_INCOMING_CALL_ENDED, 
                                                PHONE_OUTGOING_CALL_STARTED, PHONE_OUTGOING_CALL_ENDED, PHONE_MISSED_CALL };
  public static final String[] EVENT_NAMES = new String[] {"HANGUP (deprecated)", "USER_PRESENT", "Paco Action", 
                                                           "App Started", "App Stopped", 
                                                           "Music Started", "Music Stopped",
                                                           "Incoming call started", "Incoming call ended",
                                                           "Outgoing call started", "Outgoing call ended",
                                                           "Missed call" };

  private int eventCode;
  private long delay = 5000;

  private Long id;
  private String sourceIdentifier;


  public TriggerDAO() {
    super();
    this.type = "trigger";
  }

  public TriggerDAO(Long id, Integer eventCode, String sourceIdentifier, Long delay, Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime) {
    super();
    this.id = id;
    this.eventCode = eventCode;
    this.sourceIdentifier = sourceIdentifier;
    this.delay = delay;
    this.type = "trigger";
    this.timeout = timeout;
    this.minimumBuffer = minimumBuffer;
    this.snoozeCount = (snoozeCount != null) ? snoozeCount : SNOOZE_COUNT_DEFAULT;
    this.snoozeTime = (snoozeTime != null) ? snoozeTime : SNOOZE_TIME_DEFAULT;
  }


  public int getEventCode() {
    return eventCode;
  }

  public void setEventCode(int eventCode) {
    this.eventCode = eventCode;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public String getSourceIdentifier() {
    return sourceIdentifier;
  }

  public void setSourceIdentifier(String sourceIdentifier) {
    this.sourceIdentifier = sourceIdentifier;
  }
}
