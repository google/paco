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
package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.content.Context;

import com.pacoapp.paco.R;

/**
 *
 */
public class SignalSchedule extends SignalingMechanism {

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
  public static final int[] SCHEDULE_TYPES = new int[] { DAILY, WEEKDAY,
      WEEKLY, MONTHLY, ESM, SELF_REPORT, ADVANCED };

  public static final int[] SCHEDULE_TYPES_NAMES = new int[] { R.string.daily_schedule_type,
      R.string.weekdays_schedule_type, R.string.weekly_schedule_type, R.string.monthly_schedule_type, R.string.random_sampling_esm_schedule_type,
      R.string.self_report_only_schedule_type, R.string.advanced_schedule_type };

  public static final String[] SCHEDULE_TYPES_NAMES_DATA_STRINGS = new String[] { "daily", "weekday", "weekly", "monthly", "esm", "self report", "advanced"
                                                                           };
  public static final int ESM_PERIOD_DAY = 0;
  public static final int ESM_PERIOD_WEEK = 1;
  public static final int ESM_PERIOD_MONTH = 2;

  public static final int DEFAULT_ESM_PERIOD = ESM_PERIOD_DAY;
  public static final int[] ESM_PERIODS_NAMES = new int[] { R.string.day_esm_period,
      R.string.week_esm_period, R.string.month_esm_period };
  public static final String[] ESM_PERIODS_NAMES_DATA_STRINGS= new String[] { "day",
                                                            "week", "month"};
  public static final Integer DEFAULT_REPEAT_RATE = 1;
  public static final int[] DAYS_OF_WEEK = new int[] { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY };
  public static final String[] DAYS_SHORT_NAMES = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
  public static int[] ESM_PERIODS = new int[] { ESM_PERIOD_DAY,
      ESM_PERIOD_WEEK, ESM_PERIOD_MONTH };



  @JsonIgnore
  private Long id;

  @JsonProperty("id")
  private Long serverId;

  private Integer scheduleType;
  private Integer esmFrequency = 3;
  private Integer esmPeriodInDays;
  private Long esmStartHour;
  private Long esmEndHour;
  /** timeout in minutes */
  private Integer timeout;

  private List<Long> times;
  private List<SignalTime> signalTimes;
  private Integer repeatRate = 1;
  private Integer weekDaysScheduled = 0;
  private Integer nthOfMonth = 1;
  private Boolean byDayOfMonth = Boolean.TRUE;
  private Integer dayOfMonth = 1;
  @JsonIgnore
  private long beginDate = new Date().getTime();
  private Boolean esmWeekends;
  private Boolean userEditable = Boolean.TRUE;
  private Boolean onlyEditableOnJoin;

  /**
   *
   * @param id
   * @param scheduleType
   * @param byDayOfMonth
   * @param dayOfMonth
   * @param esmEndHour
   * @param esmFrequency
   * @param esmPeriodInDays
   * @param esmStartHour
   * @param esmWeekends TODO
   * @param nthOfMonth
   * @param repeatRate
   * @param times
   * @param weekDaysScheduled
   * @param beginDate TODO
   * @param userEditable TODO
   * @param snoozeCount TODO
   * @param snoozeTime TODO
   * @param onlyEditableOnJoin TODO
   */
  public SignalSchedule(long id, Integer scheduleType, Boolean byDayOfMonth,
      Integer dayOfMonth, Long esmEndHour, Integer esmFrequency,
      Integer esmPeriodInDays, Long esmStartHour, Boolean esmWeekends,
      Integer nthOfMonth, Integer repeatRate, List<SignalTime> times, Integer weekDaysScheduled, Long beginDate, Boolean userEditable, Integer timeout, Integer snoozeCount, Integer snoozeTime, Boolean onlyEditableOnJoin) {
    this.id = id;
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
    this.weekDaysScheduled = weekDaysScheduled;
    if (beginDate != null) {
      this.beginDate = beginDate;
    }
    this.userEditable = userEditable;
    this.timeout = timeout;
    this.snoozeCount = snoozeCount;
    this.snoozeTime = snoozeTime;
    this.onlyEditableOnJoin = onlyEditableOnJoin;
  }

  public SignalSchedule() {
    this.signalTimes = new ArrayList<SignalTime>();
    this.times = new ArrayList<Long>();
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

  @JsonIgnore
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

  public Boolean getByDayOfWeek() {
    return !byDayOfMonth;
  }

  public void setByDayOfWeek(Boolean byDayOfWeek) {
    byDayOfMonth = !byDayOfWeek;
  }

  public void setByDayOfMonth(Boolean byDayOfMonth) {
    this.byDayOfMonth = byDayOfMonth;
  }

  public void setDayOfMonth(Integer dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  @JsonIgnore
  public Long getId() {
    return id;
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }


  public DateTime getNextAlarmTime(DateTime dateTime, Context context, Long experimentServerId) {
    if (!getScheduleType().equals(SignalSchedule.ESM)) {
      return new NonESMSignalGenerator(this, experimentServerId, new ExperimentProviderUtil(context)).getNextAlarmTime(dateTime);
    }
    return null;  // TODO (bobevans) move the esm handling in Experiment to here.
  }

  public Long getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(Long beginDate) {
    this.beginDate = beginDate;
  }

  public Boolean getEsmWeekends() {
    return esmWeekends;
  }

  public void setEsmWeekends(Boolean weekends) {
    this.esmWeekends = weekends;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    appendKeyValue(buf, "type", SCHEDULE_TYPES_NAMES_DATA_STRINGS[scheduleType]);
    comma(buf);
    if (scheduleType == ESM) {
      appendKeyValue(buf, "frequency", esmFrequency.toString());
      comma(buf);
      appendKeyValue(buf,"esmPeriod", ESM_PERIODS_NAMES_DATA_STRINGS[esmPeriodInDays]);
      comma(buf);
      appendKeyValue(buf,"startHour", getHourOffsetAsTimeString(esmStartHour));
      comma(buf);
      appendKeyValue(buf,"endHour", getHourOffsetAsTimeString(esmEndHour));
      comma(buf);
      appendKeyValue(buf,"weekends", esmWeekends.toString());
      comma(buf);
    }
    buf.append("times = [");
    boolean firstTime = true;
    for (SignalTime time : signalTimes) {
      if (firstTime) {
        firstTime = false;
      } else {
        buf.append(",");
      }
      buf.append(time.toString());
    }
    buf.append("]");
    comma(buf);
    appendKeyValue(buf, "repeatRate", repeatRate != null ? repeatRate.toString() : "");
    comma(buf);
    appendKeyValue(buf, "daysOfWeek", weekDaysScheduled != null ? stringNamesOf(weekDaysScheduled) : "");
    comma(buf);
    appendKeyValue(buf, "nthOfMonth", nthOfMonth != null ? nthOfMonth.toString() : "");
    comma(buf);
    appendKeyValue(buf,"byDayOfMonth", byDayOfMonth != null ? byDayOfMonth.toString() : "");
    comma(buf);
    appendKeyValue(buf,"dayOfMonth", dayOfMonth != null ? dayOfMonth.toString() : "");

    return buf.toString();
  }

  private String stringNamesOf(Integer weekDaysScheduled2) {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (int i= 0; i < SignalSchedule.DAYS_OF_WEEK.length;i++) {
      if ((weekDaysScheduled & SignalSchedule.DAYS_OF_WEEK[i]) == SignalSchedule.DAYS_OF_WEEK[i]) {
        if (first) {
          first = false;
        } else {
          comma(buf);
        }
        buf.append(DAYS_SHORT_NAMES[i]);
      }
    }
    return buf.toString();
  }

  public String getHourOffsetAsTimeString(Long esmEndHour2) {
    DateTime endHour = new DateMidnight().toDateTime().plus(esmEndHour2);
    String endHourString = endHour.getHourOfDay() + ":" + pad(endHour.getMinuteOfHour());
    return endHourString;
  }

  private String pad(int minuteOfHour) {
    if (minuteOfHour < 10) {
      return "0" + minuteOfHour;
    } else {
      return Integer.toString(minuteOfHour);
    }
  }

  private void appendKeyValue(StringBuilder buf, String key, String value) {
    buf.append(key);
    buf.append(" = ");
    buf.append(value);
  }

  private void comma(StringBuilder buf) {
    buf.append(",");
  }

  public Boolean getUserEditable() {
    return userEditable;
  }

  public void setUserEditable(Boolean userEditable) {
    this.userEditable = userEditable;
  }

  public Integer getTimeout() {
    if (timeout == null) {
      return getOldDefaultTimeout();
    }
    return timeout;
  }


  private Integer getOldDefaultTimeout() {
    if (getScheduleType().equals(ESM)) {
      setTimeout(59);
      return 59;
    } else {
      setTimeout(479);
      return 479;
    }
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public List<Long> getTimes() {
    return times;
  }

  public void setTimes(List<Long> times) {
    this.times = times;
  }

  public Boolean getOnlyEditableOnJoin() {
    return onlyEditableOnJoin;
  }

  public void setOnlyEditableOnJoin(Boolean value) {
    this.onlyEditableOnJoin = value;
  }

  @JsonIgnore
  public String getType() {
    return SIGNAL_SCHEDULE_TYPE;
  }

}