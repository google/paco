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
package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DateStat implements Comparable<DateStat>, Serializable {

  private Date when;
  private Double average;
  private Double sd;
  private Double med;
  private Double min;
  private Double max;
  private List<Double> values;

  public DateStat() {
    
  }
  
  public DateStat(Date when) {
    this.when = when;
    this.values = Lists.newArrayList();
  }

  public Date getWhen() {
    return when;
  }

  public double getAverage() {
    return average;
  }

  public double getSd() {
    return sd;
  }

  public double getMed() {
    return med;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }

  public void addValue(Double value) {
    values.add(value);
  }

  public void computeStats() {
    Collections.sort(values);
    max = Collections.max(values);
    min = Collections.min(values);
    average = computeAvg();
    med = computeMedian();
    sd = computeStdDev();
  }

  private double computeAvg() {
    Double sum = 0.0;
    for (Double value : values) {
      sum += value;
    }
    return sum / values.size();
  }

  private double computeMedian() {
    if (values.size() % 2 == 0) {
      return (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2.0;
    } else {
      return values.get(values.size() / 2);
    }
  }

  private double computeStdDev() {
    double sumDiffsSqrd = 0.0;
    for (Double value : values) {
      sumDiffsSqrd = (value - average) * (value - average);
    }
    return Math.sqrt((sumDiffsSqrd / values.size()));
  }

  public static List<DateStat> calculateParameterDailyStats(String changingParameterKey,
      List<EventDAO> eventList) {
    Map<String, DateStat> dateStats = Maps.newHashMap();
    for (EventDAO event : eventList) {
      Date date = event.getResponseTime();
      if (date == null) {
        date = event.getScheduledTime();
      }
      // TODO (bobevans): Find a better way to match dates
      // Could use joda, but not in gwt. Could use calendar, but not in gwt.
      String key = date.getYear() + ":" + date.getMonth() + ":" + date.getDate();
      String whatByKey = event.getWhatByKey(changingParameterKey);
      if (whatByKey == null || whatByKey.equals("null")) {
        continue;
      }
      Double value = null;
      try {
        value = Double.parseDouble(whatByKey.trim());
      } catch (NumberFormatException nfe) {
        continue;
      }

      DateStat dateStat = dateStats.get(key);
      if (dateStat == null) {
        dateStat = new DateStat(date);
        dateStats.put(key, dateStat);
      }
      dateStat.addValue(value);
    }

    List<DateStat> dateStatsList = Lists.newArrayList(dateStats.values());
    Collections.sort(dateStatsList);
    for (DateStat dateStat : dateStatsList) {
      dateStat.computeStats();
    }
    return dateStatsList;
  }

  @Override
  public int compareTo(DateStat o) {
    return getWhen().compareTo(o.getWhen());
  }

  public List<Double> getValues() {
    return values;
  }

}
