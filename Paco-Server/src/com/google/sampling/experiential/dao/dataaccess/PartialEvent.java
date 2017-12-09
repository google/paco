package com.google.sampling.experiential.dao.dataaccess;

public class PartialEvent {
  Long eventId;
  Integer anonId;
  Integer lookupId;
  public Long getEventId() {
    return eventId;
  }
  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }
  public Integer getAnonId() {
    return anonId;
  }
  public void setAnonId(Integer anonId) {
    this.anonId = anonId;
  }
  public Integer getLookupId() {
    return lookupId;
  }
  public void setLookupId(Integer lookupId) {
    this.lookupId = lookupId;
  }
}
