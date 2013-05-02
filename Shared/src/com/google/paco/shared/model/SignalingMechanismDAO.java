package com.google.paco.shared.model;

import java.io.Serializable;


//import org.codehaus.jackson.annotate.JsonSubTypes;
//import org.codehaus.jackson.annotate.JsonTypeInfo;
//import org.codehaus.jackson.annotate.JsonSubTypes.Type;
//
//
//@JsonTypeInfo(  
//              use = JsonTypeInfo.Id.NAME,  
//              include = JsonTypeInfo.As.PROPERTY,  
//              property = "type")  
//          @JsonSubTypes({  
//              @Type(value = SignalScheduleDAO.class, name = "signalSchedule"),  
//              @Type(value = TriggerDAO.class, name = "trigger") })  
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
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
