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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.paco.ExperimentAlarms.TimeExperiment;

public class NotificationCreator {

  static String NOTIFICATION_ID = "notification_id";
  private Context context;

  public NotificationCreator(Context applicationContext) {
    this.context = applicationContext;
  }

  public static NotificationCreator create(Context context) {
    return new NotificationCreator(context.getApplicationContext());
  }

  
  public void updateNotifications(long notificationId, long alarmTime) {
      ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    try {
      if (notificationId != -1) {
        timeoutESMNotification(notificationId, experimentProviderUtil);      
        return;
      } else if (alarmTime != -1) {  
        createAllNotificationsForNextMinute(alarmTime, experimentProviderUtil);            
      } else { // no notificationId && no alarmTime means that we are just recreating the existing notifications
        recreateActiveNotifications(experimentProviderUtil);
      }
    } finally {
      context.startService(new Intent(context, BeeperService.class));
    }
  }

  private void recreateActiveNotifications(ExperimentProviderUtil experimentProviderUtil) {
    List<NotificationHolder> actives = experimentProviderUtil.getNotificationsStillActive(new DateTime());
    for (NotificationHolder notificationHolder : actives) {
      Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
      createNotificationForExperiment(context, notificationHolder.getAlarmTime(), experimentProviderUtil, experiment);      
    }
  }

  private void createAllNotificationsForNextMinute(long alarmTime,
      ExperimentProviderUtil experimentProviderUtil) {
    DateTime alarmAsDateTime = new DateTime(alarmTime);
    Log.i(PacoConstants.TAG, "Creating notifications for received alarm. Alarm Time " + alarmAsDateTime.toString());
 
 
 
    List<TimeExperiment> times = ExperimentAlarms.getAllAlarmsWithinOneMinuteofNow(alarmAsDateTime.minusSeconds(59), experimentProviderUtil.getJoinedExperiments(), context);
    for (TimeExperiment timeExperiment : times) {        
      createNotificationForExperiment(context, timeExperiment.time, experimentProviderUtil, timeExperiment.experiment);
    }
  }

  private void timeoutESMNotification(long notificationId,
      ExperimentProviderUtil experimentProviderUtil) {
    Log.i(PacoConstants.TAG, "Received Notification reminder alarm.");
    NotificationHolder notificationHolder = experimentProviderUtil.getNotificationById(notificationId);
    if (notificationHolder != null) {
      Log.i(PacoConstants.TAG, "Received Notification reminder alarm. Holder = " + notificationHolder.getExperimentId());
      // Responding to the notification would have deleted the holder.
      // So, this notification has not been responded to and should be canceled.        
      cancelNotification(context, notificationId);
      createMissedPacot(context, notificationHolder, experimentProviderUtil);
      experimentProviderUtil.deleteNotificationsForExperiment(notificationHolder.getExperimentId());
      notifySyncService(context);
    }
  }

  // TODO (bobevans): unify these two methods with those in ExperimentExecutor.
  private void notifySyncService(Context context) {
    // run a background OutputProcessorService that tries to upload
    // the recent entries in the events table.
    // (It selects all events that are not marked uploaded, and tries to send them)
    context.startService(new Intent(context, SyncService.class));
  }
  
  private void createMissedPacot(Context context, NotificationHolder notificationHolder, ExperimentProviderUtil experimentProviderUtil) {
    Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    event.setScheduledTime(new DateTime(notificationHolder.getAlarmTime()));
    experimentProviderUtil.insertEvent(event);
  }


  private void cancelNotification(Context context, long notificationId) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(new Long(notificationId).intValue());
  }
  
  private void createNotificationForExperiment(Context context, long alarmTime, 
      ExperimentProviderUtil experimentProviderUtil, Experiment experiment) {
    if (experiment.isQuestionsChange()) {
      experimentProviderUtil.loadInputsForExperiment(experiment);
    }      
//      if (experiment.isQuestionsChange() && !experiment.hasFreshInputs()) {
//        return;
//      }
    int icon = R.drawable.calculator_lb16;

    NotificationHolder notificationHolder = experimentProviderUtil.getNotificationFor(experiment.getId());
    if (notificationHolder == null) {
      notificationHolder = new NotificationHolder(alarmTime, experiment.getId(), 0, experiment.getExpirationTimeInMillis());
      experimentProviderUtil.insertNotification(notificationHolder);
    } else {
      // TODO(bobevans):decide whether to have multiple notifications (1 / experiment)
      // or one notification that launches the app, and an app that shows which ones need response.
      //
      // there is currently no need to do this, because it makes no sense to
      // answer a prior notification when there is a new one, and telling them
      // the number is meaningless. We might use it to consolidate all notifications
      // into one, but not yet.
//        notificationHolder.setNoticeCount(notificationHolder.getNoticeCount());
//        experimentProviderUtil.updateNotification(notificationHolder);
    }
    Notification notification = new Notification(icon, "Time for " + experiment.getTitle(), alarmTime); 
    
    //Intent surveyIntent = new Intent(context, ExperimentExecutor.class);
    Intent surveyIntent = new Intent(context, ExperimentExecutor.class);
    surveyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    Uri uri = Uri.withAppendedPath(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, 
        experiment.getId().toString()); 
    surveyIntent.setData(uri);
    surveyIntent.putExtra(Experiment.SCHEDULED_TIME, alarmTime);
    
    PendingIntent notificationIntent = PendingIntent.getActivity(context, 1,
        surveyIntent, 0);
    notification.setLatestEventInfo(context, experiment.getTitle(),
        "Time to participate!", notificationIntent);
    notification.when = alarmTime;
    
    // This actually seems like a bad idea. If they hit a new notice
    // on the same experiment, we should just refresh the exsiting notice.
    // It is just too late to respond to
    //notification.number = notificationHolder.getNoticeCount();
    
    notification.defaults |= Notification.DEFAULT_SOUND;
    notification.defaults |= Notification.DEFAULT_VIBRATE;
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.ESM)) {
      notification.flags |= Notification.FLAG_NO_CLEAR;
    }
    notification.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    
    Log.i(PacoConstants.TAG, "Creating notification for experiment: " + experiment.getTitle() +". alarmTime: " + new DateTime(alarmTime).toString());
    notificationManager.notify(notificationHolder.getId().intValue(), notification);
    
    if (experiment.isExpiringAlarm()) {
      createCancelAlarm(context, notificationHolder);
    }
  }

  private void createCancelAlarm(Context context, NotificationHolder notificationHolder) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//    Log.i(Constants.TAG, "Creating alarm to timeout notification");
    Intent ultimateIntent = new Intent(context, AlarmReceiver.class);
    ultimateIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().intValue());
    PendingIntent intent = PendingIntent.getBroadcast(context.getApplicationContext(), 1,
            ultimateIntent, 0);

    long timeoutMinutesFromNowInMillis = new DateTime(notificationHolder.getAlarmTime()).plusMinutes(
        (int)(notificationHolder.getTimeoutMillis() / 60000)).getMillis();
    alarmManager.set(AlarmManager.RTC_WAKEUP, timeoutMinutesFromNowInMillis, intent);
  }
  

}
