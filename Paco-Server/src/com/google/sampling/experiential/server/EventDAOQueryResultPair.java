package com.google.sampling.experiential.server;

import java.io.Serializable;
import java.util.List;

import com.google.sampling.experiential.shared.EventDAO;

public class EventDAOQueryResultPair implements Serializable{

  private List<EventDAO> events;
  private String cursor;

  public EventDAOQueryResultPair(List<EventDAO> eventDAOs, String cursor) {
    this.events = eventDAOs;
    this.cursor = cursor;
  }

  public List<EventDAO> getEvents() {
    return events;
  }

  public void setEvents(List<EventDAO> events) {
    this.events = events;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }


}
