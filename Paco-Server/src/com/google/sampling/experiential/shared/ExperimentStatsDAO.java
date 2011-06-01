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

package com.google.sampling.experiential.shared;

import java.io.Serializable;

/**
 * 
 * Dumb data object for passing experiment meta statistics to the GWT client.
 * 
 * @author Bob Evans
 *
 */
public class ExperimentStatsDAO implements Serializable {

  private EventDAO[] joinedEventsList;
  private DateStat[] dailyResponseRate;
  private DateStat[] sevenDayDateStats;
  private String responseRate;
  private String responseTime;

  public ExperimentStatsDAO() {
  }
  
  public EventDAO[] getJoinedEventsList() {
    return joinedEventsList;
  }

  public void setJoinedEventsList(EventDAO[] joinedEventsList) {
    this.joinedEventsList = joinedEventsList;
  }

  /**
   * @param dailyResponseRateFor
   */
  public void setDailyResponseRate(DateStat[] dailyResponseRateFor) {
    dailyResponseRate = dailyResponseRateFor;
  }

  public DateStat[] getDailyResponseRate() {
    return dailyResponseRate;
  }

  public DateStat[] getSevenDayDateStats() {
    return sevenDayDateStats;
  }

  public void setSevenDayDateStats(DateStat[] sevenDayDateStats) {
    this.sevenDayDateStats = sevenDayDateStats;
  }

  /**
   * @return
   */
  public String getResponseRate() {
    return responseRate;
  }

  public void setResponseRate(String responseRate) {
    this.responseRate = responseRate;
  }

  /**
   * @param string
   */
  public void setResponseTime(String string) {
    this.responseTime = string;
  }

  public String getResponseTime() {
    return responseTime;
  }
  
  
  

}
