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
import java.util.List;

import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * The Schedule for signaling an experiment response.
 *
 * @author Bob Evans
 *
 */
public class SignalSchedule implements Serializable {
  public static final int DAILY = 0;
  public static final int WEEKDAY = 1;
  public static final int WEEKLY = 2;
  public static final int MONTHLY = 3;
  public static final int ESM = 4;
  public static final int SELF_REPORT = 5;
  public static final int ADVANCED = 6;
  public static final int[] SCHEDULE_TYPES =
      new int[] {DAILY, WEEKDAY, WEEKLY, MONTHLY, ESM, SELF_REPORT, ADVANCED};

  public static final String[] SCHEDULE_TYPES_NAMES = new String[] {"Daily",
      "Weekdays",
      "Weekly",
      "Monthly",
      "Random sampling (ESM)",
      "Self report only",
      "Advanced"};

  public static final int ESM_PERIOD_DAY = 0;
  public static final int ESM_PERIOD_WEEK = 1;
  public static final int ESM_PERIOD_MONTH = 2;

  public static final int DEFAULT_ESM_PERIOD = ESM_PERIOD_DAY;
  public static final String[] ESM_PERIODS_NAMES = new String[] {"Day", "Week", "Month"};
  public static final Integer DEFAULT_REPEAT_RATE = 1;
  public static final int[] DAYS_OF_WEEK = new int[] {1, 2, 4, 8, 16, 32, 64};
  public static int[] ESM_PERIODS = new int[] {ESM_PERIOD_DAY, ESM_PERIOD_WEEK, ESM_PERIOD_MONTH};

  private Date startDate;
  private Date endDate;

  private Integer scheduleType = SignalSchedule.DAILY;

  private Integer esmFrequency = 3;

  private Integer esmPeriodInDays = 0;

  private Long esmStartHour = 0l;

  private Long esmEndHour = 0l;

  private List<Date> times = Lists.newArrayList();

  private Integer repeatRate = 0;

  private Integer weekDaysScheduled = 0;

  private Integer nthOfMonth = 0;

  private Boolean byDayOfMonth = Boolean.TRUE;

  private Integer dayOfMonth = 0;

  private Boolean esmWeekends = false;

  private Boolean userEditable = true;

  public SignalSchedule() {}

  /**
   * @param id
   * @param scheduleType
   * @param esmFrequency
   * @param esmPeriodInDays
   * @param esmStartHour
   * @param esmEndHour
   * @param times
   * @param repeatRate
   * @param weekDaysScheduled
   * @param nthOfMonth
   * @param byDayOfMonth
   * @param dayOfMonth
   * @param esmWeekends TODO
   */
  public SignalSchedule(Integer scheduleType,
      Integer esmFrequency,
      Integer esmPeriodInDays,
      Long esmStartHour,
      Long esmEndHour,
      List<Date> times,
      Integer repeatRate,
      Integer weekDaysScheduled,
      Integer nthOfMonth,
      Boolean byDayOfMonth,
      Integer dayOfMonth,
      Boolean esmWeekends,
      Boolean userEditable) {
    super();
    this.scheduleType = scheduleType;
    this.esmFrequency = esmFrequency;
    this.esmPeriodInDays = esmPeriodInDays;
    this.esmStartHour = esmStartHour;
    this.esmEndHour = esmEndHour;
    this.esmWeekends = esmWeekends;
    this.times = times;
    this.repeatRate = repeatRate;
    this.weekDaysScheduled = weekDaysScheduled;
    this.nthOfMonth = nthOfMonth;
    this.byDayOfMonth = byDayOfMonth;
    this.dayOfMonth = dayOfMonth;
    this.userEditable = userEditable;
  }

  public Integer getScheduleType() {
    return scheduleType;
  }

  public void setScheduleType(Integer scheduleType) {
    this.scheduleType = scheduleType;
  }

  public Integer getEsmFrequency() {
    return esmFrequency;
  }

  public void setEsmFrequency(Integer esmFrequency) {
    this.esmFrequency = esmFrequency;
  }

  public Integer getEsmPeriodInDays() {
    return esmPeriodInDays;
  }

  public void setEsmPeriodInDays(Integer esmPeriodInDays) {
    this.esmPeriodInDays = esmPeriodInDays;
  }

  public Long getEsmStartHour() {
    return esmStartHour;
  }

  public void setEsmStartHour(Long esmStartHour) {
    this.esmStartHour = esmStartHour;
  }

  public Long getEsmEndHour() {
    return esmEndHour;
  }

  public void setEsmEndHour(Long esmEndHour) {
    this.esmEndHour = esmEndHour;
  }

  public List<Date> getTimes() {
    return times;
  }

  public void setTimes(List<Date> times) {
    this.times = times;
  }

  public Integer getRepeatRate() {
    return repeatRate;
  }

  public void setRepeatRate(Integer repeatRate) {
    this.repeatRate = repeatRate;
  }

  public Integer getWeekDaysScheduled() {
    return weekDaysScheduled;
  }

  public void setWeekDaysScheduled(Integer weekDaysScheduled) {
    this.weekDaysScheduled = weekDaysScheduled;
  }

  public Integer getNthOfMonth() {
    return nthOfMonth;
  }

  public void setNthOfMonth(Integer nthOfMonth) {
    this.nthOfMonth = nthOfMonth;
  }

  public Boolean getByDayOfMonth() {
    return byDayOfMonth;
  }

  public void setByDayOfMonth(Boolean byDayOfMonth) {
    this.byDayOfMonth = byDayOfMonth;
  }

  public Integer getDayOfMonth() {
    return dayOfMonth;
  }

  public void setDayOfMonth(Integer dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  @JsonIgnore
  public Boolean getByDayOfWeek() {
    return !byDayOfMonth;
  }

  public Boolean getEsmWeekends() {
    return esmWeekends;
  }

  public void setEsmWeekends(Boolean esmWeekends) {
    this.esmWeekends = esmWeekends;
  }

  public Boolean getUserEditable() {
    return userEditable;
  }

  public void setUserEditable(Boolean userEditable) {
    this.userEditable = userEditable;
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

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  @JsonIgnore
  public boolean isFixedDuration() {
    return (startDate != null && endDate != null);
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

    // FIXME: Refactor when refactoring.
    //return super.equals(obj);
    return true;
  }
}
