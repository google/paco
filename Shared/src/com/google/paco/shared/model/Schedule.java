/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import java.text.ParseException;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.joda.time.LocalDate;

import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.compat.jodatime.LocalDateIteratorFactory;

/**
 * The Schedule for signaling an experiment response.
 *
 * @author Bob Evans
 *
 */
@JsonSubTypes({@Type(DailySchedule.class), @Type(WeeklySchedule.class),
    @Type(MonthlySchedule.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class Schedule {
  public enum Type {
    Daily, Weekly, Monthly
  }

  private Type type;
  private LocalDate startDate;
  private LocalDate endDate;
  private int every;

  /**
   *
   */
  public Schedule(Type type) {
    super();

    this.type = type;
    this.startDate = null;
    this.endDate = null;
  }

  /**
   * @return whether the schedule has a start date
   */
  public boolean hasStartDate() {
    return (startDate != null);
  }

  /**
   * @return the startDate
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  /**
   * @return whether the schedule has a end date
   */
  public boolean hasEndDate() {
    return (endDate != null);
  }

  /**
   * @return the endDate
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the type
   */
  @JsonIgnore
  public Type getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  protected void setType(Type type) {
    this.type = type;
  }

  /**
   * @return the repeatEvery
   */
  public int getEvery() {
    return every;
  }

  /**
   * @param every the repeat to set
   */
  public void setEvery(int every) {
    this.every = every;
  }

  /**
   * @return whether the schedule is fixed in duration
   */
  @JsonIgnore
  public boolean isFixedDuration() {
    return hasEndDate();
  }

  @JsonIgnore
  public boolean isDaily() {
    return type.equals(Schedule.Type.Daily);
  }

  @JsonIgnore
  public boolean isWeekly() {
    return type.equals(Schedule.Type.Weekly);
  }

  @JsonIgnore
  public boolean isMonthly() {
    return type.equals(Schedule.Type.Monthly);
  }

  @JsonIgnore
  public DailySchedule asDaily() {
    return (DailySchedule) this;
  }

  @JsonIgnore
  public WeeklySchedule asWeekly() {
    return (WeeklySchedule) this;
  }

  @JsonIgnore
  public MonthlySchedule asMonthly() {
    return (MonthlySchedule) this;
  }

  // public abstract LocalDate getCurrentDate(LocalDate now);
  // public abstract LocalDate getNextDate(LocalDate now);
  protected abstract String getRData();

  public LocalDate getCurrentDate(LocalDate now, long seed) {
    if (hasStartDate() == false || getEvery() < 1) {
      return null;
    }

    LocalDate start = getStartDate();

    if (start == null || now.isBefore(start)) {
      return null;
    }

    LocalDateIterator ldi;

    try {
      ldi = LocalDateIteratorFactory.createLocalDateIterator(getRData(), start, true);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }

    LocalDate last = now;

    switch (getType()) {
      case Daily:
        last = last.minusDays(getEvery());
        break;
      case Weekly:
        last = last.minusWeeks(getEvery());
        break;
      case Monthly:
        last = last.minusMonths(getEvery());
        break;
    }

    ldi.advanceTo(last);

    while (!last.isAfter(now)) {
      LocalDate date = ldi.next();

      System.out.println("now = " + now + "; last = " + last + "; date = " + date);

      if (date.isAfter(now)) {
        break;
      }

      last = date;
    }

    /*
    if (hasEndDate() && last.isAfter(getEndDate())) {
      return null;
    }
    */

    return last;
  }

  public LocalDate getNextDate(LocalDate now, long seed) {
    if (hasStartDate() == false || getEvery() < 1) {
      return null;
    }

    if (hasEndDate() && now.isAfter(getEndDate())) {
      return null;
    }

    LocalDate start = getStartDate();

    if (start == null) {
      return null;
    }

    LocalDateIterator ldi;

    try {
      ldi = LocalDateIteratorFactory.createLocalDateIterator(getRData(), start, true);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }

    ldi.advanceTo(now);

    LocalDate date = ldi.next();

    if (now.isEqual(date)) {
      date = ldi.next();
    }

    if (hasEndDate() && date.isAfter(getEndDate())) {
      return null;
    }

    return date;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (obj.getClass() != getClass()) {
      return false;
    }

    Schedule other = (Schedule) obj;

    if (getType().equals(other.getType()) == false) {
      return false;
    }

    if (hasStartDate()) {
      if (getStartDate().equals(other.getStartDate()) == false) {
        return false;
      }
    } else {
      if (other.hasStartDate()) {
        return false;
      }
    }

    if (hasEndDate()) {
      if (getEndDate().equals(other.getEndDate()) == false) {
        return false;
      }
    } else {
      if (other.hasEndDate()) {
        return false;
      }
    }

    return true;
  }
}
