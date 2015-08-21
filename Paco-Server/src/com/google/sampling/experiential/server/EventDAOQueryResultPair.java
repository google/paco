package com.google.sampling.experiential.server;

import java.io.Serializable;
import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;

public class EventDAOQueryResultPair implements Serializable{

  private List<EventDAO> events;
  private String nextCursor;

  public EventDAOQueryResultPair(List<EventDAO> eventDAOs, String nextCursor) {
    this.events = eventDAOs;
    this.nextCursor = nextCursor;
  }

  public List<EventDAO> getEvents() {
    return events;
  }

  public void setEvents(List<EventDAO> events) {
    this.events = events;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
  }


}
