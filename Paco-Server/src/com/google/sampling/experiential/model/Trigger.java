package com.google.sampling.experiential.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Trigger {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Persistent
  private Integer eventCode;

  @Persistent
  private String sourceIdentifier;

  @Persistent
  private Long delay;

  @Persistent
  private Integer timeout;

  @Persistent
  private Integer minimumBuffer;

  @Persistent
  private Integer snoozeCount;

  @Persistent
  private Integer snoozeTime;



  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public Trigger(Key ownerKey, Long id, Integer event, String sourceIdentifier, Long millisecondDelay, Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime) {
    super();
    if (id != null) {
      this.id = KeyFactory.createKey(ownerKey, Trigger.class.getSimpleName(), id);
    }
    this.eventCode = event;
    this.sourceIdentifier = sourceIdentifier;
    this.delay = millisecondDelay;
    this.timeout = timeout;
    this.minimumBuffer = minimumBuffer;
    this.snoozeCount = snoozeCount;
    this.snoozeTime = snoozeTime;
  }

  public Key getId() {
    return id;
  }

  public void setId(Key id) {
    this.id = id;
  }

  public Integer getEventCode() {
    return eventCode;
  }

  public void setEventCode(Integer eventCode) {
    this.eventCode = eventCode;
  }

  public Long getDelay() {
    return delay;
  }

  public void setDelay(Long delay) {
    this.delay = delay;
  }

  public String getSourceIdentifier() {
    return sourceIdentifier;
  }

  public void setSourceIdentifier(String sourceIdentifier) {
    this.sourceIdentifier = sourceIdentifier;
  }

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime;
  }

  @Override
  public String toString() {
    return "Trigger";
  }


}
