package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

public class EventQueryStatus {

  private String status;
  private String errorMessage;
  private List<EventDAO> events = Lists.newArrayList();

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

  public List<EventDAO> getEvents() {
    return events;
  }

  public void setEvents(List<EventDAO> events) {
    this.events = events;
  }

}
