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

package com.google.sampling.experiential.model;

import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.paco.shared.model.SignalScheduleDAO;


/**
 * The Schedule for signalling an experiment response.
 *
 * @author Bob Evans
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class SignalSchedule {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Persistent
  private Integer scheduleType;

  @Persistent
  private Integer esmFrequency = 3;

  @Persistent
  private Integer esmPeriodInDays;

  @Persistent
  private Long esmStartHour;

  @Persistent
  private Long esmEndHour;

  @Persistent
  @Element(dependent = "true")
  private List<SignalTime> signalTimes;

  @Persistent
  private List<Long> times;


  @Persistent
  private Integer repeatRate = 0;

  @Persistent
  private Integer weekDaysScheduled = 0;

  @Persistent
  private Integer nthOfMonth = 0;

  @Persistent
  private Boolean byDayOfMonth = Boolean.TRUE;

  @Persistent
  private Integer dayOfMonth = 0;

  @Persistent
  private Boolean esmWeekends = false;

  @Persistent
  private Boolean userEditable = true;

  @Persistent
  private Integer timeout;

  @Persistent
  private Integer minimumBuffer;

  @Persistent
  private Integer snoozeCount;

  @Persistent
  private Integer snoozeTime;

  @Persistent
  private Boolean onlyEditableOnJoin;

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
   * @param minimumBuffer
   * @param snoozeCount
   * @param snoozeTime
   * @param onlyEditableOnJoin TODO
   */
  public SignalSchedule(Key ownerKey, Long id, Integer scheduleType, Integer esmFrequency,
      Integer esmPeriodInDays, Long esmStartHour, Long esmEndHour, List<SignalTime> times,
      Integer repeatRate, Integer weekDaysScheduled, Integer nthOfMonth, Boolean byDayOfMonth,
      Integer dayOfMonth, Boolean esmWeekends, Boolean userEditable, Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime, Boolean onlyEditableOnJoin) {
    super();
    if (id != null) {
      this.id = KeyFactory.createKey(ownerKey, SignalSchedule.class.getSimpleName(), id);
    }
    this.scheduleType = scheduleType;
    this.esmFrequency = esmFrequency;
    this.esmPeriodInDays = esmPeriodInDays;
    this.esmStartHour = esmStartHour;
    this.esmEndHour = esmEndHour;
    this.esmWeekends = esmWeekends;
    this.signalTimes = times;
    this.repeatRate = repeatRate;
    this.weekDaysScheduled = weekDaysScheduled;
    this.nthOfMonth = nthOfMonth;
    this.byDayOfMonth = byDayOfMonth;
    this.dayOfMonth = dayOfMonth;
    this.userEditable = userEditable;
    this.timeout = timeout;
    this.minimumBuffer = minimumBuffer;
    this.snoozeCount = snoozeCount;
    this.snoozeTime = snoozeTime;
    this.onlyEditableOnJoin = onlyEditableOnJoin;
  }

  public Key getId() {
    return id;
  }

  public void setId(Key id) {
    this.id = id;
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

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {

    StringBuilder buf = new StringBuilder();
    appendKeyValue(buf, "type", SignalScheduleDAO.SCHEDULE_TYPES_NAMES[scheduleType]);
    comma(buf);
    if (scheduleType == SignalScheduleDAO.ESM) {
      appendKeyValue(buf, "frequency", esmFrequency.toString());
      comma(buf);
      appendKeyValue(buf,"esmPeriod", SignalScheduleDAO.ESM_PERIODS_NAMES[esmPeriodInDays]);
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
    if (signalTimes != null) {
      for (SignalTime time : signalTimes) {
        if (firstTime) {
          firstTime = false;
        } else {
          buf.append(",");
        }
        buf.append(getHourOffsetAsTimeString(time));
      }
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
    for (int i= 0; i < SignalScheduleDAO.DAYS_OF_WEEK.length;i++) {
      if ((weekDaysScheduled & SignalScheduleDAO.DAYS_OF_WEEK[i]) == SignalScheduleDAO.DAYS_OF_WEEK[i]) {
        if (first) {
          first = false;
        } else {
          comma(buf);
        }
        buf.append(SignalScheduleDAO.DAYS_OF_WEEK[i]);
      }
    }
    return buf.toString();
  }


  public String getHourOffsetAsTimeString(Long time) {
    DateTime endHour = new DateMidnight().toDateTime().plus(time);
    String endHourString = endHour.getHourOfDay() + ":" + pad(endHour.getMinuteOfHour());
    return endHourString;
  }

  public String getHourOffsetAsTimeString(SignalTime time) {
    DateTime endHour = new DateMidnight().toDateTime().plus(time.getFixedTimeMillisFromMidnight());
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

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime;
  }

  public List<Long> getTimes() {
    return times;
  }

  public Boolean getOnlyEditableOnJoin() {
    return onlyEditableOnJoin;
  }

  public void setOnlyEditableOnJoin(Boolean value) {
    this.onlyEditableOnJoin = value;
  }

}
