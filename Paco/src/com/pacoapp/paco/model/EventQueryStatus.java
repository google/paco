package com.pacoapp.paco.model;

import java.util.List;

import com.google.common.collect.Lists;

public class EventQueryStatus {

  private String status;
  private String errorMessage;
  private List<Event> events = Lists.newArrayList();
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
  }
  public List<Event> getEvents() {
    return events;
  }
  public void setEvents(List<Event> events) {
    this.events = events;
  }

}
