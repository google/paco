package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.EventDAO;

public class EventQueryStatus {
  public static final String SUCCESS = "Success";
  public static final String FAILURE = "Failure";


  private String status;
  private String errorMessage;
  private List<EventDAO> events = Lists.newArrayList();


  public EventQueryStatus() {
    super();
    setStatus(SUCCESS);
  }

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
    setStatus(FAILURE);
  }

  public List<EventDAO> getEvents() {
    return events;
  }

  public void setEvents(List<EventDAO> events) {
    this.events = events;
  }

}
