// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.LocalDate;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("daily")
public class DailySchedule extends Schedule {
  /**
   *
   */
  public DailySchedule() {
    super(Schedule.Type.Daily);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#getRData()
   */
  @Override
  protected String getRData() {
    return String.format("RRULE:FREQ=DAILY;INTERVAL=%d", getEvery());
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#isValidDate(org.joda.time.LocalDate)
   */
  @Override
  protected boolean isValidDate(LocalDate date) {
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.Schedule#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) == false) {
      return false;
    }

    DailySchedule other = (DailySchedule) obj;

    if (getEvery() != other.getEvery()) {
      return false;
    }

    return true;
  }
}
