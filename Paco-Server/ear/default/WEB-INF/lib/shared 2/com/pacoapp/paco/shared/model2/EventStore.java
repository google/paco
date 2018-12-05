package com.pacoapp.paco.shared.model2;

import org.joda.time.DateTime;


public interface EventStore {

  public EventInterface getEvent(Long experimentId, DateTime scheduledTime, String groupName, Long actionTriggerId, Long scheduleId);

  public void updateEvent(EventInterface correspondingEvent);

  public void insertEvent(EventInterface event);
}
