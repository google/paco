package com.google.sampling.experiential.server;

import java.io.Serializable;

public abstract class SPResponse  implements Serializable{
  String status;
  String message;
  
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  } 
}
