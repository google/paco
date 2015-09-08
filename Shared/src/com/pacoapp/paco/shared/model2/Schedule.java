/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
// Copyright 2010 Google Inc. All Rights Reserved.

package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Schedule extends ModelBase implements Validatable, MinimumBufferable, Serializable {

  public static final int SATURDAY = 64;
  public static final int FRIDAY = 32;
  public static final int THURSDAY = 16;
  public static final int WEDNESDAY = 8;
  public static final int TUESDAY = 4;
  public static final int MONDAY = 2;
  public static final int SUNDAY = 1;

  public static final int DAILY = 0;
  public static final int WEEKDAY = 1;
  public static final int WEEKLY = 2;
  public static final int MONTHLY = 3;
  public static final int ESM = 4;
  public static final int SELF_REPORT = 5;
  public static final int ADVANCED = 6;
  public static final int[] SCHEDULE_TYPES = new int[] { DAILY, WEEKDAY, WEEKLY, MONTHLY, ESM, SELF_REPORT, ADVANCED };

  public static final String[] SCHEDULE_TYPES_NAMES = new String[] { "Daily", "Weekdays", "Weekly", "Monthly",
                                                                    "Random sampling (ESM)", "Self report only",
                                                                    "Advanced" };

  public static final int ESM_PERIOD_DAY = 0;
  public static final int ESM_PERIOD_WEEK = 1;
  public static final int ESM_PERIOD_MONTH = 2;

  public static final int DEFAULT_ESM_PERIOD = ESM_PERIOD_DAY;
  public static final String[] ESM_PERIODS_NAMES = new String[] { "Day", "Week", "Month" };
  public static final Integer DEFAULT_REPEAT_RATE = 1;
  public static final int[] DAYS_OF_WEEK = new int[] { 1, 2, 4, 8, 16, 32, 64 };
  public static int[] ESM_PERIODS = new int[] { ESM_PERIOD_DAY, ESM_PERIOD_WEEK, ESM_PERIOD_MONTH };

  private Integer scheduleType = DAILY;
  private Integer esmFrequency = 3;
  private Integer esmPeriodInDays = ESM_PERIOD_DAY;
  private Long esmStartHour = 9 * 60 * 60 * 1000L;
  private Long esmEndHour = 17 * 60 * 60 * 1000L;

  private List<SignalTime> signalTimes;
  private Integer repeatRate = 1;
  private Integer weekDaysScheduled = 0;
  private Integer nthOfMonth = 1;
  private Boolean byDayOfMonth = Boolean.TRUE;
  private Integer dayOfMonth = 1;
  private Boolean esmWeekends = false;
  protected Integer minimumBuffer = Integer.parseInt(PacoNotificationAction.ESM_SIGNAL_TIMEOUT);

  private long joinDateMillis;
  private Long beginDate;
  private Long id;
  private Boolean onlyEditableOnJoin = false;
  private Boolean userEditable = true;


  /**
   *
   * @param scheduleType
   * @param byDayOfMonth
   * @param dayOfMonth
   * @param esmEndHour
   * @param esmFrequency
   * @param esmPeriodInDays
   * @param esmStartHour
   * @param nthOfMonth
   * @param repeatRate
   * @param times
   * @param weekDaysScheduled
   * @param esmWeekends
   *          TODO
   * @param minimumBuffer
   * @param snoozeCount
   * @param snoozeTime
   */
  public Schedule(Integer scheduleType, Boolean byDayOfMonth, Integer dayOfMonth, Long esmEndHour,
                  Integer esmFrequency, Integer esmPeriodInDays, Long esmStartHour, Integer nthOfMonth,
                  Integer repeatRate, List<SignalTime> times, Integer weekDaysScheduled, Boolean esmWeekends,
                  Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime) {
    this.scheduleType = scheduleType;
    this.byDayOfMonth = byDayOfMonth;
    this.dayOfMonth = dayOfMonth;
    this.esmEndHour = esmEndHour;
    this.esmFrequency = esmFrequency;
    this.esmPeriodInDays = esmPeriodInDays;
    this.esmStartHour = esmStartHour;
    this.esmWeekends = esmWeekends;
    this.nthOfMonth = nthOfMonth;
    this.repeatRate = repeatRate;
    this.signalTimes = times;
    this.minimumBuffer = minimumBuffer;
    this.weekDaysScheduled = weekDaysScheduled;
  }

  /**
     *
     */
  public Schedule() {
    this.signalTimes = new ArrayList<SignalTime>();
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

  public List<SignalTime> getSignalTimes() {
    return signalTimes;
  }

  public void setSignalTimes(List<SignalTime> times) {
    this.signalTimes = times;
  }

  public Integer getRepeatRate() {
    return repeatRate == null ? DEFAULT_REPEAT_RATE : repeatRate;
  }

  public void setRepeatRate(Integer repeatRate) {
    this.repeatRate = repeatRate;
  }

  public void setWeekDaysScheduled(Integer selected) {
    this.weekDaysScheduled = selected;
  }

  public Integer getWeekDaysScheduled() {
    return weekDaysScheduled;
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

  public Integer getDayOfMonth() {
    return dayOfMonth;
  }

  public void setByDayOfMonth(Boolean byDayOfMonth) {
    this.byDayOfMonth = byDayOfMonth;
  }

  public void setDayOfMonth(Integer dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  public Boolean getEsmWeekends() {
    return esmWeekends;
  }

  public void setEsmWeekends(Boolean esmWeekends) {
    this.esmWeekends = esmWeekends;
  }

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public void validateWith(Validator validator) {
//    System.out.println("VALIDATING SCHEDULE");
    validator.isNotNull(scheduleType, "scheduleType is not properly initialized");
    validator.isNotNull(onlyEditableOnJoin, "onlyEditableOnJoin is not properly initialized");
    validator.isNotNull(userEditable, "userEditable is not properly initialized");

    switch (scheduleType) {
    case DAILY:
    case WEEKDAY:
      break;
    case WEEKLY:
      validator.isNotNull(weekDaysScheduled, "weekdaysSchedule is not properly initialized");
      break;
    case MONTHLY:
      validator.isNotNull(byDayOfMonth, "byDayOfMonth is not properly initialized");
      if (byDayOfMonth) {
        validator.isNotNull(dayOfMonth, "dayOfMonth is not properly initialized");
      } else {
        validator.isNotNull(nthOfMonth, "nthOfMonth is not properly initialized");
        validator.isNotNull(weekDaysScheduled, "weekdaysSchedule is not properly initialized");
      }
      break;
    case ESM:
      validator.isNotNull(esmFrequency, "esm frequency is not properly initialized");
      validator.isNotNull(esmPeriodInDays, "esm period is not properly initialized");
      validator.isNotNull(esmWeekends, "esm weekends is not properly initialized");
      validator.isNotNull(esmStartHour, "esm startHour is not properly initialized");
      validator.isNotNull(esmEndHour, "esm endHour is not properly initialized");
      validator.isNotNull(minimumBuffer, "minimumBuffer for esm signals is not properly initialized");
      break;
    default:
      // do nothing;

    }
    if (scheduleType != null && !scheduleType.equals(ESM) && !scheduleType.equals(SELF_REPORT) && !scheduleType.equals(ADVANCED)) {
      validator.isNotNull(repeatRate, "repeatRate is not properly initialized");
      validator.isNotNullAndNonEmptyCollection(signalTimes,
                                               "For the schedule type, there must be at least one signal Time");
      long lastTime = 0;
      for (SignalTime signalTime : signalTimes) {
        signalTime.validateWith(validator);
        if (signalTime.getBasis() == null || signalTime.getBasis().intValue() == SignalTime.FIXED_TIME) {
          if (signalTime.getFixedTimeMillisFromMidnight() <= lastTime) {
            validator.addError("Signal Times must be in chronological order");
          }
          lastTime = signalTime.getFixedTimeMillisFromMidnight();
        }
      }

    }

  }

  public long getJoinDateMillis() {
    return joinDateMillis;
  }

  public void setJoinDateMillis(long joinDateMillis) {
    this.joinDateMillis = joinDateMillis;
  }

  public int convertEsmPeriodToDays() {
    switch (getEsmPeriodInDays()) {
    case ESM_PERIOD_DAY:
      return 1;
    case ESM_PERIOD_WEEK:
      return 7;
    case ESM_PERIOD_MONTH:
      return 30;
    default:
      return 1;
    }

  }

  public Long getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Long beginDate) {
    this.beginDate = beginDate;
  }

  public void addWeekDayToSchedule(Integer day) {
    weekDaysScheduled |= day;
  }

  public void removeWeekDayFromSchedule(Integer day) {
    weekDaysScheduled &= (~day);
  }

  // Visible for testing
  public void removeAllWeekDaysScheduled() {
    this.weekDaysScheduled = 0;
  }


  public boolean isWeekDayScheduled(Integer day) {
    return (weekDaysScheduled & day) != 0;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Boolean getOnlyEditableOnJoin() {
    return onlyEditableOnJoin;
  }

  public Boolean getUserEditable() {
    return userEditable;
  }

  public void setOnlyEditableOnJoin(Boolean value) {
    this.onlyEditableOnJoin = value;
  }

  public void setUserEditable(Boolean userEditable) {
    this.userEditable = userEditable;
  }


}
