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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;

import java.util.Date;

/**
 * A generic schedule for when an experiment should run. Each type of schedule
 * has a start and end date for when the experiment should run.
 *
 * @author Bob Evans
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonSubTypes({
    @Type(DailySchedule.class), @Type(WeeklySchedule.class), @Type(MonthlySchedule.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class Schedule {
  public enum Type {
    Daily, Weekly, Monthly
  }

  protected Type type;
  protected Date startDate;
  protected Date endDate;

  /**
   * Default constructor with sane defaults.
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
   * @return the starting date of the schedule
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the starting date of the schedule, or null if any date.
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return whether the schedule has a end date
   */
  public boolean hasEndDate() {
    return (endDate != null);
  }

  /**
   * @return the end date of the schedule
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate end end date of the schedule, or null if any date.
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the type of schedule
   */
  @JsonIgnore
  public Type getType() {
    return type;
  }

  /**
   * @param type the type of schedule
   */
  protected void setType(Type type) {
    this.type = type;
  }

  /**
   * @return whether the schedule has a fixed duration (i.e., has an end date)
   */
  @JsonIgnore
  public boolean isFixedDuration() {
    return hasEndDate();
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
