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

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public Trigger(Key ownerKey, Long id, Integer event, String sourceIdentifier, Long millisecondDelay, Integer timeout) {
    super();
    if (id != null) {
      this.id = KeyFactory.createKey(ownerKey, Trigger.class.getSimpleName(), id);
    }
    this.eventCode = event;
    this.sourceIdentifier = sourceIdentifier;
    this.delay = millisecondDelay;
    this.timeout = timeout;
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
  
  


}
