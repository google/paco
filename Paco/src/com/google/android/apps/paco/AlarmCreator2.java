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
import android.net.Uri;
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

  /**
   * Static creator method to produce an AlarmCreator with a context.
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
   * given survey type.
   * 
   * @param alarmManager
   * @param contextForPendingIntent An Android Context that provides access to system services.
   */
  AlarmCreator2(AlarmManager alarmManager, Context contextForPendingIntent) {
    this.alarmManager = alarmManager;
    this.pendingIntentContext = contextForPendingIntent;
  }

  /**
   * This method will generate the actual alarm schedule, or regenerate it if the 
   * regenerateAlarms boolean flag is true. This is used in the case of a reboot of
   * the android device, or an upgrade of the application package. This is necessary
   * because alarms are transient in Android.
   * 
   * @param regenerateAlarms
   */
  public void updateAlarm() {

    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(pendingIntentContext);
    List<Experiment> experiments = experimentProviderUtil.getJoinedExperiments();
    if (experiments.isEmpty()) {
      alarmManager.cancel(createNewIntentAndCancelOld(new DateTime(0L), null)); // will the different potential Experiment Id in the uri affect equals? No
      return;
    }
    // TODO (bobevans) stash these in a minheap. Tho there probably will only ever be 2 or 3 experiments running at once.
    // so for now, it is fine this way.
    List<TimeExperiment> experimentTimes = ExperimentAlarms.arrangeExperimentsByNextTime(experiments, pendingIntentContext);
    if (experimentTimes.isEmpty()) {
      return;
    }
    TimeExperiment nextNearestAlarmTime = experimentTimes.get(0);
    createAlarm(new DateTime(nextNearestAlarmTime.time), nextNearestAlarmTime.experiment);
  }

  void createAlarm(DateTime alarmTime, Experiment experiment) {
    Log.i(PacoConstants.TAG, "Creating alarm: " + alarmTime.toString() +" for experiment: " + experiment.getTitle());
    Uri uri = Uri.withAppendedPath(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, experiment.getId().toString());    
    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getMillis(), createNewIntentAndCancelOld(alarmTime, uri));
  }

  /**
   * Nulls are only allowed in the cancel case, because they don't matter and we are matching on the
   * pendingIntent anyway.
   * 
   * The uri data is used in the matching supposedly, so if we create an alarm for a given experiment uri, we 
   * can't cancel it unless we keep track of all alarms created. The goal is to just track one alarm to make
   * it easier to manage. 
   * @param alarmTime
   * @param uri
   * @return
   */
  private PendingIntent createNewIntentAndCancelOld(DateTime alarmTime, Uri uri) {
    Intent ultimateIntent = new Intent(pendingIntentContext, AlarmReceiver.class);
    if (alarmTime != null) {
      ultimateIntent.putExtra(Experiment.SCHEDULED_TIME, alarmTime.getMillis());
    }
    if (uri != null) {
      ultimateIntent.setData(uri);
    }

    PendingIntent intent = PendingIntent.getBroadcast(pendingIntentContext, 1, ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    alarmManager.cancel(intent); 
    return intent;
  }


}
