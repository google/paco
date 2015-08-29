package com.pacoapp.paco.shared.model2;

import java.io.Serializable;

public class SignalTime extends ModelBase implements Validatable, Serializable {
  public static final Integer FIXED_TIME = 0;
  public static final Integer OFFSET_TIME = 1;

  public static final Integer OFFSET_BASIS_SCHEDULED_TIME = 0;
  public static final Integer OFFSET_BASIS_RESPONSE_TIME = 1;


  public static final Integer MISSED_BEHAVIOR_SKIP = 0;
  public static final Integer MISSED_BEHAVIOR_USE_SCHEDULED_TIME = 1;

  public static final Integer OFFSET_TIME_DEFAULT = 30 * 60 * 1000; // 30 minutes

  private Integer type = FIXED_TIME;
  private Integer fixedTimeMillisFromMidnight;
  private Integer basis; // from previous scheduledTime, from previous responseTime
  private Integer offsetTimeMillis;
  private Integer missedBasisBehavior = MISSED_BEHAVIOR_USE_SCHEDULED_TIME; // skip this time, use previousScheduledTime
  private String label;

  public SignalTime(Integer type, Integer basis, Integer fixedTimeMillisFromMidnight,
                    Integer missedBasisBehavior,
                    Integer offsetTimeMillis, String label) {
    this.type = type;
    this.basis = basis;
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight;
    this.missedBasisBehavior = missedBasisBehavior;
    this.offsetTimeMillis = offsetTimeMillis;
    this.label = label;
  }

  public SignalTime() {
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public Integer getFixedTimeMillisFromMidnight() {
    return fixedTimeMillisFromMidnight;
  }

  public void setFixedTimeMillisFromMidnight(Integer fixedTimeMillisFromMidnight) {
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight;
  }

  public Integer getBasis() {
    return basis;
  }

  public void setBasis(Integer basis) {
    this.basis = basis;
  }

  public Integer getOffsetTimeMillis() {
    return offsetTimeMillis;
  }

  public void setOffsetTimeMillis(Integer offsetTimeMillis) {
    this.offsetTimeMillis = offsetTimeMillis;
  }

  public Integer getMissedBasisBehavior() {
    return missedBasisBehavior;
  }

  public void setMissedBasisBehavior(Integer missedBasisBehavior) {
    this.missedBasisBehavior = missedBasisBehavior;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void validateWith(Validator validator) {
//    System.out.println("VALIDATING SIGNALTIME");
    validator.isNotNull(type, "signal time type is not properly initialized");
    if (type != null && type.equals(FIXED_TIME)) {
      validator.isNotNull(fixedTimeMillisFromMidnight, "fixed type signal times must have fixedTimeMillisFromMidnight");
    } else {
      validator.isNotNull(offsetTimeMillis, "offset type signalTimes must have offsetMillis specified");
      validator.isNotNull(missedBasisBehavior, "offset type signalTimes must have missedBasisBehavior specified");
      validator.isNotNull(basis, "offset type signalTimes must have basis specified");

    }

  }

}
