package com.google.android.apps.paco;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(  
              use = JsonTypeInfo.Id.NAME,  
              include = JsonTypeInfo.As.PROPERTY,  
              property = "type")  
          @JsonSubTypes({  
              @Type(value = SignalSchedule.class, name = "signalSchedule"),  
              @Type(value = Trigger.class, name = "trigger") })  
public class SignalingMechanism {

  private static final int DEFAULT_TIMEOUT_MINUTES = 59;
  protected Integer timeout;

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


}
