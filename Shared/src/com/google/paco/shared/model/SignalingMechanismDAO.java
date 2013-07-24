package com.google.paco.shared.model;

import java.io.Serializable;

public abstract class SignalingMechanismDAO implements Serializable {

  protected String type;
  protected Integer timeout;

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
  

  
  

}
