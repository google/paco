package com.google.sampling.experiential.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class SignalTime {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private int type; // fixed time or offset

  @Persistent
  private int fixedTimeMillisFromMidnight;

  @Persistent
  private int basis; // from previous scheduledTime, from previous responseTime

  @Persistent
  private int offsetTimeMillis;

  @Persistent
  private int missedBasisBehavior; // skip this time, use previousScheduledTime

  @Persistent
  private String label;

  public SignalTime(Key scheduleKey, int type2, int basis2, int fixedTimeMillisFromMidnight2, int missedBasisBehavior2,
                    int offsetTimeMillis2, String label) {
    this.key = scheduleKey;
    this.type = type2;
    this.basis = basis2;
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight2;
    this.missedBasisBehavior = missedBasisBehavior2;
    this.offsetTimeMillis = offsetTimeMillis2;
    this.label = label;
  }

  public Key getKey() {
    return key;
  }

  public void setKey(Key key) {
    this.key = key;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getFixedTimeMillisFromMidnight() {
    return fixedTimeMillisFromMidnight;
  }

  public void setFixedTimeMillisFromMidnight(int fixedTimeMillisFromMidnight) {
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight;
  }

  public int getBasis() {
    return basis;
  }

  public void setBasis(int basis) {
    this.basis = basis;
  }

  public int getOffsetTimeMillis() {
    return offsetTimeMillis;
  }

  public void setOffsetTimeMillis(int offsetTimeMillis) {
    this.offsetTimeMillis = offsetTimeMillis;
  }

  public int getMissedBasisBehavior() {
    return missedBasisBehavior;
  }

  public void setMissedBasisBehavior(int missedBasisBehavior) {
    this.missedBasisBehavior = missedBasisBehavior;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


}
