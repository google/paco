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

package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/**
 * The Schedule for signaling an experiment response.
 *
 * @author Bob Evans
 *
 */
@JsonSubTypes({
    @Type(DailySchedule.class), @Type(WeeklySchedule.class), @Type(MonthlySchedule.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
public abstract class Schedule implements Serializable {
  public static final String DAILY = "daily";
  public static final String WEEKLY = "weekly";
  public static final String MONTHLY = "monthly";

  protected String type;
  protected Date startDate;
  protected Date endDate;
  protected boolean editable;
  protected Signal signal;

  /**
   *
   */
  public Schedule(String type) {
    super();

    this.type = type;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the type
   */
  @JsonIgnore
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  protected void setType(String type) {
    this.type = type;
  }

  /**
   * @return the editable
   */
  public boolean isEditable() {
    return editable;
  }

  /**
   * @param editable the editable to set
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /**
   * @return the signal
   */
  public Signal getSignal() {
    return signal;
  }

  /**
   * @param signal the signal to set
   */
  public void setSignal(Signal signal) {
    this.signal = signal;
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

    if (getStartDate() == null) {
      if (other.getStartDate() != null) {
        return false;
      }
    } else {
      if (getStartDate().equals(other.getStartDate()) == false) {
        return false;
      }
    }

    if (getEndDate() == null) {
      if (other.getEndDate() != null) {
        return false;
      }
    } else {
      if (getEndDate().equals(other.getEndDate()) == false) {
        return false;
      }
    }

    if (isEditable() != other.isEditable()) {
      return false;
    }

    if (getSignal() == null) {
      if (other.getSignal() != null) {
        return false;
      }
    } else {
      if (getSignal().equals(other.getSignal()) == false) {
        return false;
      }
    }

    return true;
  }
}
