package com.google.android.apps.paco;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.paco.shared.model.SignalTimeDAO;

public class SignalTime {

  private int type; // fixed time or offset
  private int fixedTimeMillisFromMidnight;
  private int basis; // from previous scheduledTime, from previous responseTime
  private int offsetTimeMillis;
  private int missedBasisBehavior; // skip this time, use previousScheduledTime
  private String label;

  public SignalTime() {

  }

  public SignalTime(int millisOfDay) {
    this.fixedTimeMillisFromMidnight = millisOfDay;
  }

  public SignalTime(int type2, int basis2, int fixedTimeMillisFromMidnight2, int missedBasisBehavior2,
                    int offsetTimeMillis2, String label) {
    this.type = type2;
    this.basis = basis2;
    this.fixedTimeMillisFromMidnight = fixedTimeMillisFromMidnight2;
    this.missedBasisBehavior = missedBasisBehavior2;
    this.offsetTimeMillis = offsetTimeMillis2;
    this.label = label;
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

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("Type: ");
    if (type == SignalTimeDAO.FIXED_TIME) {
      buf.append("Fixed time");
      DateTime time = new DateMidnight().toDateTime().plusMillis(fixedTimeMillisFromMidnight);
      buf.append(time.getHourOfDay() + ":" + time.getMinuteOfHour());
    } else {
      buf.append("Offset Time");
      DateTime time = new DateMidnight().toDateTime().plusMillis(offsetTimeMillis);
      buf.append(time.getMinuteOfDay());
    }
    return buf.toString();
  }


}
