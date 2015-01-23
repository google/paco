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
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import android.content.Context;

public class ExperimentAlarms {

  static class TimeExperiment implements Comparable<TimeExperiment> {
    DateTime time;
    Experiment experiment;
    public TimeExperiment(DateTime nextTime, Experiment experiment2) {
      this.time= nextTime;
      this.experiment = experiment2;
    }
    public int compareTo(TimeExperiment arg0) {
      return time.compareTo(arg0.time);
    }
  }

  public static List<TimeExperiment> getAllAlarmsWithinOneMinuteofNow(DateTime now, List<Experiment> experiments, Context context) {
    List<TimeExperiment> times = arrangeExperimentsByNextTimeFrom(experiments, now, context);
    List<TimeExperiment> matchingTimes = new ArrayList<TimeExperiment>();
    for (TimeExperiment time : times) {
      if (new Interval(now, time.time).toDurationMillis() < 60000) {
        matchingTimes.add(time);
      }
    }
    return matchingTimes;
  }




  public static List<TimeExperiment> arrangeExperimentsByNextTime(List<Experiment> experiments, Context context) {
    return arrangeExperimentsByNextTimeFrom(experiments, new DateTime(), context);
  }




  private static List<TimeExperiment> arrangeExperimentsByNextTimeFrom(List<Experiment> experiments, DateTime now, Context context) {
    List<TimeExperiment> times = new ArrayList<TimeExperiment>();
    for (Experiment experiment : experiments) {
      DateTime nextTime = experiment.getNextTime(now, context);
      if (nextTime != null) {
        times.add(new TimeExperiment(nextTime, experiment));
      }
    }
    Collections.sort(times);
    return times;
  }

}
