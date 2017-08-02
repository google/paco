package com.google.sampling.experiential.server.viz.appusage;

import org.joda.time.DateTime;

public class AppScreenSession extends BaseSession {

  private String eventAppScreenName;

  public AppScreenSession(String eventAppScreenName, DateTime startTime) {
    super(startTime);
    this.eventAppScreenName = eventAppScreenName;
  }

  @Override
  public String toString() {
    return "      Screen Name: " + eventAppScreenName + ", " + super.toString();
  }

  public String getEventAppScreenName() {
    return eventAppScreenName;
  }

  public void setEventAppScreenName(String eventAppScreenName) {
    this.eventAppScreenName = eventAppScreenName;
  }


}
