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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.List;


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
  public SignalSchedule(Key ownerKey, Long id, Integer scheduleType, Integer esmFrequency, 
      Integer esmPeriodInDays, Long esmStartHour, Long esmEndHour, List<Long> times, 
      Integer repeatRate, Integer weekDaysScheduled, Integer nthOfMonth, Boolean byDayOfMonth, 
      Integer dayOfMonth, Boolean esmWeekends) {
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
    this.times = times;
    this.repeatRate = repeatRate;
    this.weekDaysScheduled = weekDaysScheduled;
    this.nthOfMonth = nthOfMonth;
    this.byDayOfMonth = byDayOfMonth;
    this.dayOfMonth = dayOfMonth;
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

  public List<Long> getTimes() {
    return times;
  }

  public void setTimes(List<Long> times) {
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

  public Boolean getEsmWeekends() {
    return esmWeekends;
  }

  public void setEsmWeekends(Boolean esmWeekends) {
    this.esmWeekends = esmWeekends;
  }

  
  
}
