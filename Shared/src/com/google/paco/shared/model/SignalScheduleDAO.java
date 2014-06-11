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

package com.google.paco.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * Dumb data object for sending scheduling info back and forth from GWT client.
 *
 * @author Bob Evans
 *
 */
public class SignalScheduleDAO  extends SignalingMechanismDAO implements Serializable {

    public static final int DAILY = 0;
    public static final int WEEKDAY = 1;
    public static final int WEEKLY = 2;
    public static final int MONTHLY = 3;
    public static final int ESM = 4;
    public static final int SELF_REPORT = 5;
    public static final int ADVANCED = 6;
    public static final int[] SCHEDULE_TYPES = new int[]{DAILY, WEEKDAY, WEEKLY, MONTHLY, ESM,
      SELF_REPORT, ADVANCED};

    public static final String[] SCHEDULE_TYPES_NAMES = new String[] {
        "Daily", "Weekdays", "Weekly", "Monthly", "Random sampling (ESM)", "Self report only",
        "Advanced" };

    public static final int ESM_PERIOD_DAY = 0;
    public static final int ESM_PERIOD_WEEK = 1;
    public static final int ESM_PERIOD_MONTH = 2;

    public static final int DEFAULT_ESM_PERIOD = ESM_PERIOD_DAY;
    public static final String[] ESM_PERIODS_NAMES = new String[] { "Day", "Week", "Month"};
    public static final Integer DEFAULT_REPEAT_RATE = 1;
    public static final int[] DAYS_OF_WEEK = new int[] {1,2,4,8,16,32,64};
    public static int[] ESM_PERIODS = new int[] { ESM_PERIOD_DAY, ESM_PERIOD_WEEK,
      ESM_PERIOD_MONTH };

    private Long id;
    private Integer scheduleType = DAILY;
    private Integer esmFrequency = 3;
    private Integer esmPeriodInDays = ESM_PERIOD_DAY;
    private Long esmStartHour = 9 * 60 * 60 * 1000L;
    private Long esmEndHour = 17 * 60 * 60 * 1000L;

    private List<Long> times;
    private List<SignalTimeDAO> signalTimes;
    private Integer repeatRate = 1;
    private Integer weekDaysScheduled = 0;
    private Integer nthOfMonth = 1;
    private Boolean byDayOfMonth = Boolean.TRUE;
    private Integer dayOfMonth = 1;
    private Boolean esmWeekends = false;
    private Boolean userEditable;
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
     * @param nthOfMonth
     * @param repeatRate
     * @param times
     * @param weekDaysScheduled
     * @param esmWeekends TODO
     * @param minimumBuffer
     * @param snoozeCount
     * @param snoozeTime
     * @param onlyEditableOnJoin TODO
     */
    public SignalScheduleDAO(long id, Integer scheduleType, Boolean byDayOfMonth,
        Integer dayOfMonth, Long esmEndHour, Integer esmFrequency, Integer esmPeriodInDays,
        Long esmStartHour, Integer nthOfMonth, Integer repeatRate, List<SignalTimeDAO> times,
        Integer weekDaysScheduled, Boolean esmWeekends, Boolean userEditable, Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime, Boolean onlyEditableOnJoin) {
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
      this.userEditable = userEditable;
      this.timeout = timeout;
      this.minimumBuffer = minimumBuffer;
      this.type = "signalSchedule";
      this.snoozeCount = (snoozeCount != null) ? snoozeCount : SNOOZE_COUNT_DEFAULT;
      this.snoozeTime = (snoozeTime != null) ? snoozeTime : SNOOZE_TIME_DEFAULT;
      this.onlyEditableOnJoin = onlyEditableOnJoin;
    }

    /**
     *
     */
    public SignalScheduleDAO() {
      this.signalTimes = new ArrayList<SignalTimeDAO>();
      this.type = "signalSchedule";
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

    public List<SignalTimeDAO> getSignalTimes() {
        return signalTimes;
    }

    public void setSignalTimes(List<SignalTimeDAO> times) {
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

    public void setId(Long id) {
      this.id = id;
    }

    public Long getId() {
      return id;
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

    public Boolean getOnlyEditableOnJoin() {
      return onlyEditableOnJoin;
    }

    public void setOnlyEditableOnJoin(Boolean value) {
      this.onlyEditableOnJoin = value;

    }

    public List<Long> getTimes() {
      return times;
    }

    public void setTimes(List<Long> times) {
      this.times = times;
    }

}
