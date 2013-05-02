package com.google.paco.shared.model;

import java.io.Serializable;


public class TriggerDAO extends SignalingMechanismDAO implements Serializable {

  public static final int HANGUP = 1;
  
  public static final int[] EVENTS = new int[] {HANGUP};
  public static final String[] EVENT_NAMES = new String[] {"HANGUP"};

  private int eventCode;
  private long delay = 5000;

  private Long id;
  
 
  public TriggerDAO() {
    super();
    this.type = "trigger";
  }
  
  public TriggerDAO(Long id, Integer eventCode, Long delay, Integer timeout) {
    super();
    this.id = id;
    this.eventCode = eventCode;
    this.delay = delay;
    this.type = "trigger";
    this.timeout = timeout;
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
  
  

}
