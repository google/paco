package com.pacoapp.paco.shared.comm;

public class Outcome implements java.io.Serializable {

  private long eventId;
  private boolean status;
  private String errorMessage;

  public Outcome(long eventId) {
    this();
    this.eventId = eventId;
  }

  public Outcome() {
    super();
      this.status = true;
  }

  public Outcome(long eventId, String errorMessage) {
    this(eventId);
    this.status = false;
    this.errorMessage = errorMessage;
  }

  public long getEventId() {
    return eventId;
  }

  public void setEventId(long eventId) {
    this.eventId = eventId;
  }

  public boolean succeeded() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }


  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public void setError(String errorMessage) {
    this.status = false;
    this.errorMessage = errorMessage;

  }

  public boolean getStatus() {
    return status;
  }

}
