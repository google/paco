package com.google.paco.shared.model;

import java.io.Serializable;

public class SignalTimeDAO implements Serializable {
  public static final int FIXED_TIME = 0;
  public static final int OFFSET_TIME = 1;

  public static final int OFFSET_BASIS_SCHEDULED_TIME = 0;
  public static final int OFFSET_BASIS_RESPONSE_TIME = 1;
  

  public static final int MISSED_BEHAVIOR_SKIP = 0;
  public static final int MISSED_BEHAVIOR_USE_SCHEDULED_TIME = 1;

  public static final int OFFSET_TIME_DEFAULT = 30 * 60 * 1000; // 30 minutes

  private int type; // fixed time or offset
  private int fixedTimeMillisFromMidnight;
  private int basis; // from previous scheduledTime, from previous responseTime
  private int offsetTimeMillis;
  private int missedBasisBehavior; // skip this time, use previousScheduledTime
  private Long id;
  private String label;

  public SignalTimeDAO(Long id, int type2, int basis2, int fixedTimeMillisFromMidnight2, int missedBasisBehavior2,
                       int offsetTimeMillis2, String label) {
    this.id = id;
    this.type = type2;
    this.basis = basis2;
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight2;
    this.missedBasisBehavior = missedBasisBehavior2;
    this.offsetTimeMillis = offsetTimeMillis2;
    this.label = label;
  }

  public SignalTimeDAO() {
    // TODO Auto-generated constructor stub
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}
