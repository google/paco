// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import java.util.Random;

import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("daily")
public class DailySchedule extends Schedule {
  private boolean includeWeekends;

  public DailySchedule() {
    super(Schedule.Type.Daily);

    this.includeWeekends = true;
  }

  public void setIncludeWeekends(boolean includeWeekends) {
    this.includeWeekends = includeWeekends;
  }

  public boolean shouldIncludeWeekends() {
    return includeWeekends;
  }

  @Override
  public ScheduleIterator iterator() {
    if (!isValid()) {
      return null;
    }

    return new DailyScheduleIterator(this);
  }

  @Override
  public ScheduleIterator iterator(Random random) {
    return iterator(); // we don't care about randomness
  }

  protected boolean isValidDate(LocalDate date) {
    if (shouldIncludeWeekends()) {
      return true;
    }

    return (date.getDayOfWeek() != DateTimeConstants.SATURDAY && date.getDayOfWeek() != DateTimeConstants.SUNDAY);
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

  /*
   * (non-Javadoc)
   *
   * @see com.google.paco.shared.model.Schedule#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(super.toString());
    sb.append(" every");
    if (every < 2) {
      sb.append(" day");
    } else {
      sb.append(" ").append(every).append(" days");
    }

    return sb.toString();
  }
}
