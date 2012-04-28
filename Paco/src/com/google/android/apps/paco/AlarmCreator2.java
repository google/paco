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

import java.util.List;

import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.apps.paco.ExperimentAlarms.TimeExperiment;

/**
 * Class that is responsible for keeping the alarm schedule.
 * 
 * Android drops alarms. This class retrieves them from an AlarmStore, and also 
 * uses a generator to generate alarms for a survey according to user preferences.
 * 
 * It's a bit overly decoupled at the moment, that will change as the surveys become 
 * first class objects with different scheduling frequencies.
 * 
 * 
 *
 */
public class AlarmCreator2 {

  private static final int ALARM_RECEIVER_INTENT_REQUEST_CODE = 1;

  /**
   * Produce an AlarmCreator with a context.
   * 
   * @param context Android context from caller.
   * @return AlarmCreator 
   */
  public static AlarmCreator2 createAlarmCreator(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
    Context contextForPendingIntent = context.getApplicationContext();
    return new AlarmCreator2(alarmManager,contextForPendingIntent);
  }

  private AlarmManager alarmManager;
  private Context pendingIntentContext;

  /**
   * An Alarm creator will generate alarms to a given schedule for a 
   * given Experiment.
   * 
   * @param alarmManager
   * @param contextForPendingIntent An Android Context that provides access to system services.
   */
  AlarmCreator2(AlarmManager alarmManager, Context contextForPendingIntent) {
    this.alarmManager = alarmManager;
    this.pendingIntentContext = contextForPendingIntent;
  }

  /**
   * Generate an Android alarm for the next due experiment. 
   * 
   * @param regenerateAlarms
   */
  public void updateAlarm() {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(pendingIntentContext);
    List<Experiment> experiments = experimentProviderUtil.getJoinedExperiments();
    if (experiments.isEmpty()) {
      Log.i(PacoConstants.TAG, "No joined experiments. Not creating alarms.");
      return;
    }

    List<TimeExperiment> experimentTimes = ExperimentAlarms.arrangeExperimentsByNextTime(experiments, pendingIntentContext);
    if (experimentTimes.isEmpty()) {
      Log.i(PacoConstants.TAG, "No experiments with a next time to signal.");
      return;
    }
    TimeExperiment nextNearestAlarmTime = experimentTimes.get(0);
    createAlarm(nextNearestAlarmTime.time, nextNearestAlarmTime.experiment);
  }

  void createAlarm(DateTime alarmTime, Experiment experiment) {
    Log.i(PacoConstants.TAG, "Creating alarm: " + alarmTime.toString() +" for experiment: " + experiment.getTitle());
    PendingIntent intent = createAlarmReceiverIntentForExperiment(alarmTime);
    alarmManager.cancel(intent);
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), intent);
  }

  /**
   * Create an AlarmReceiver PendingIntent for the next experiment.
   *  
   * @param alarmTime Time to trigger notification
   * @return
   */
  private PendingIntent createAlarmReceiverIntentForExperiment(DateTime alarmTime) {
    Intent ultimateIntent = new Intent(pendingIntentContext, AlarmReceiver.class);    
    ultimateIntent.putExtra(Experiment.SCHEDULED_TIME, alarmTime.getMillis());
    return PendingIntent.getBroadcast(pendingIntentContext, ALARM_RECEIVER_INTENT_REQUEST_CODE, 
        ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }


}
