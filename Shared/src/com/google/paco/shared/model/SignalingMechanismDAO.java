package com.google.paco.shared.model;

import java.io.Serializable;


public abstract class SignalingMechanismDAO implements Serializable {

  public static final String TRIGGER_SIGNAL_TIMEOUT = "59";
  public static final String ESM_SIGNAL_TIMEOUT = "59";
  public static final String FIXED_SCHEDULE_TIMEOUT = "479";


  protected String type;
  protected Integer timeout;
  protected Integer minimumBuffer;

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
}
