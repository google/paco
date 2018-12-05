package com.google.sampling.experiential.server;

import java.util.List;

import com.google.sampling.experiential.model.Event;

public class EventQueryResultPair {

  private List<Event> events;
  private String cursor;

  public EventQueryResultPair(List<Event> newArrayList, String cursor) {
    this.events = newArrayList;
    this.cursor = cursor;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

}
