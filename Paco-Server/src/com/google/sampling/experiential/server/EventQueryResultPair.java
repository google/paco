package com.google.sampling.experiential.server;

import java.util.List;

import com.google.sampling.experiential.model.Event;

public class EventQueryResultPair {

  private List<Event> events;
  private String nextCursor;

  public EventQueryResultPair(List<Event> newArrayList, String nextCursor) {
    this.events = newArrayList;
    this.nextCursor = nextCursor;
  }

  public List<Event> getEvents() {
    return events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  public void setNextCursor(String nextCursor) {
    this.nextCursor = nextCursor;
  }

}
