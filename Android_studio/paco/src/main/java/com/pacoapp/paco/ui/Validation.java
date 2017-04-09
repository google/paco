package com.pacoapp.paco.ui;

public class Validation {

  private boolean valid = true;
  private String errorMessages;

  public boolean ok() {
    return valid;
  }

  public String errorMessage() {
    return errorMessages;
  }

  public void addMessage(String errorMessage) {
    valid = false;
    errorMessages = (errorMessages != null) ? errorMessages + errorMessage : errorMessage;    
  }

}
