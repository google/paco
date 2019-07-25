package com.google.sampling.experiential.server;

import com.pacoapp.paco.shared.util.Constants;

public class PacoResponse {
  String status;
  String errorMessage;
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    this.status = Constants.FAILURE;
  }

  @Override
  public String toString() {
    return "{\"status\":\"" + status + "\", \"errorMessage\":\"" + errorMessage + "\"}";
  }
  
  
  
}
