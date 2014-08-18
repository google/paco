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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.apps.paco.ExperimentAlarms.TimeExperiment;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.pacoapp.paco.R;

public class NotificationCreator {

  public static final String SNOOZE_REPEATER_EXTRA_KEY = "SNOOZE REPEATER";
  private static final int MILLIS_IN_MINUTE = 60000;
  static String NOTIFICATION_ID = "com.google.android.apps.paco.notification_id";
  private Context context;
  private ExperimentProviderUtil experimentProviderUtil;

  public NotificationCreator(Context applicationContext) {
    this.context = applicationContext;
    experimentProviderUtil = new ExperimentProviderUtil(context);
  }

  public static NotificationCreator create(Context context) {
    return new NotificationCreator(context.getApplicationContext());
  }

  public void createNotificationsForAlarmTime(long notificationId, long alarmTime) {
    try {
      createAllNotificationsForLastMinute(alarmTime);
    } finally {
      context.startService(new Intent(context, BeeperService.class));
    }
  }

  public void timeoutNotification(long notificationId) {
    try {
      NotificationHolder notificationHolder = experimentProviderUtil.getNotificationById(notificationId);
      timeoutNotification(notificationHolder);
    } finally {
      context.startService(new Intent(context, BeeperService.class));
    }
  }

  public void timeoutNotificationsForExperiment(Long experimentId) {
    List<NotificationHolder> notifs = experimentProviderUtil.getNotificationsFor(experimentId);
    timeoutNotifications(notifs);
  }

  public void recreateActiveNotifications() {
    try {
      List<NotificationHolder> allNotifications = experimentProviderUtil.getAllNotifications();
      DateTime now = new DateTime();
      for (NotificationHolder notificationHolder : allNotifications) {
        if (notificationHolder.isActive(now)) {
          Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
          cancelNotification(context, notificationHolder.getId());  // in case this exists on the status bar, blow it away (this happens on package_replace calls).
          String message = context.getString(R.string.time_to_participate_notification_text);
          if (notificationHolder.isCustomNotification()) {
            message = notificationHolder.getMessage();
          }
          fireNotification(context, notificationHolder, experiment, message);
          if (experiment.isExpiringAlarm()) {
            createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
          }
          if (experiment.getSignalingMechanisms().get(0).getSnoozeCount() > SignalingMechanismDAO.SNOOZE_COUNT_DEFAULT) {
            createAlarmForSnooze(context, notificationHolder);
          }
        } else {
          timeoutNotification(notificationHolder);
        }
      }
    } finally {
      context.startService(new Intent(context, BeeperService.class));
    }
  }

  public void createSnoozeWakeupNotification(long notificationId) {
    NotificationHolder notificationHolder = experimentProviderUtil.getNotificationById(notificationId);
    if (notificationHolder == null) {
      return;
    }
    DateTime now = new DateTime();
    if (notificationHolder.isActive(now)) {
        Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
        cancelNotification(context, notificationHolder.getId());  // in case this exists on the status bar, blow it away (this happens on package_replace calls).
        fireNotification(context, notificationHolder, experiment,  context.getString(R.string.time_to_participate_notification_text));
    }
    // TODO
    // Optionally create another snooze alarm if the snoozeCount says it should happen and there is time left
    // calculate snoozecount = time elapsed since original alarmtime % snoozeTime
    // calculate time left = nextSnoozeTime < timeoutTime (absolute times in walltime)
  }

  private void createAllNotificationsForLastMinute(long alarmTime) {
    DateTime alarmAsDateTime = new DateTime(alarmTime);
    Log.i(PacoConstants.TAG, "Creating All notifications for last minute from signaled alarmTime: " + alarmAsDateTime.toString());

    List<TimeExperiment> times = ExperimentAlarms.getAllAlarmsWithinOneMinuteofNow(alarmAsDateTime.minusSeconds(59),
        experimentProviderUtil.getJoinedExperiments(), context);
    for (TimeExperiment timeExperiment : times) {
      timeoutNotifications(experimentProviderUtil.getNotificationsFor(timeExperiment.experiment.getId()));
      createNewNotificationForExperiment(context, timeExperiment, false);
    }
  }

  private void timeoutNotifications(List<NotificationHolder> notifications) {
    for (NotificationHolder notificationHolder : notifications) {
      timeoutNotification(notificationHolder);
    }
  }

  // Responding to the notification would have deleted the holder.
  // So, this notification has not been responded to and should be canceled.
  public void timeoutNotification(NotificationHolder notificationHolder) {
    if (notificationHolder != null) {
      Log.i(PacoConstants.TAG, "Timing out notification. Holder = " + notificationHolder.getId() + ", experiment = " + notificationHolder.getExperimentId());
      cancelNotification(context, notificationHolder.getId());
      createMissedPacot(context, notificationHolder);
      experimentProviderUtil.deleteNotification(notificationHolder.getId());
      notifySyncService(context);
    }
  }

  // TODO (bobevans): unify these two methods with those in ExperimentExecutor.
  private void notifySyncService(Context context) {
    context.startService(new Intent(context, SyncService.class));
  }

  private void createMissedPacot(Context context, NotificationHolder notificationHolder) {
    Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    event.setExperimentVersion(experiment.getVersion());
    event.setScheduledTime(new DateTime(notificationHolder.getAlarmTime()));
    experimentProviderUtil.insertEvent(event);
  }

  private void cancelNotification(Context context, long notificationId) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(new Long(notificationId).intValue());
  }

  private void createNewNotificationForExperiment(Context context, TimeExperiment timeExperiment, boolean customGenerated) {
    DateTime time = timeExperiment.time;
    Experiment experiment = timeExperiment.experiment;
    long expirationTimeInMillis = experiment.getExpirationTimeInMillis();

    NotificationHolder notificationHolder = createNewNotificationWithDetails(context, time, experiment,
                                                                             experiment.getSignalingMechanisms().get(0).getName(),
                                                                             expirationTimeInMillis,
                                                                             context.getString(R.string.time_to_participate_notification_text));

    if (experiment.isExpiringAlarm()) {
      createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
    }
    if (experiment.getSignalingMechanisms().get(0).getSnoozeCount() > SignalingMechanismDAO.SNOOZE_COUNT_DEFAULT) {
      createAlarmForSnooze(context, notificationHolder);
    }

  }

  private void createNewCustomNotificationForExperiment(Context context, DateTime time, Experiment experiment, long expirationTimeInMillis, String message) {
    NotificationHolder notificationHolder = createNewNotificationWithDetails(context, time, experiment, NotificationHolder.CUSTOM_GENERATED_NOTIFICATION,
                                                                             expirationTimeInMillis,
                                                                             message);

    if (experiment.isExpiringAlarm()) {
      createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
    }
  }



  private NotificationHolder createNewNotificationWithDetails(Context context, DateTime time, Experiment experiment,
                                                              String source, long expirationTimeInMillis, String message) {
    NotificationHolder notificationHolder = new NotificationHolder(time.getMillis(), experiment.getId(), 0, expirationTimeInMillis, source, message);
    experimentProviderUtil.insertNotification(notificationHolder);
    fireNotification(context, notificationHolder, experiment, message);
    return notificationHolder;
  }

  private void fireNotification(Context context, NotificationHolder notificationHolder, Experiment experiment, String message) {
    Log.i(PacoConstants.TAG, "Creating notification for experiment: " + experiment.getTitle() +
        ". alarmTime: " + notificationHolder.getAlarmTime().toString() + " holderId = " + notificationHolder.getId());

    Notification notification = createNotification(context, experiment, notificationHolder, message);
    //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    notificationManager.notify(notificationHolder.getId().intValue(), notification);

  }

  private Notification createNotification(Context context, Experiment experiment, NotificationHolder notificationHolder, String message) {
    int icon = R.drawable.paco32;


    String tickerText = context.getString(R.string.time_for_notification_title) + experiment.getTitle();
    if (notificationHolder.isCustomNotification()) {
      tickerText = message;
    }


    //Notification notification = new Notification(icon, tickerText, notificationHolder.getAlarmTime());

    Intent surveyIntent = new Intent(context, ExperimentExecutor.class);
    surveyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    Uri uri = Uri.withAppendedPath(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, experiment.getId().toString());
    surveyIntent.setData(uri);
    surveyIntent.putExtra(Experiment.SCHEDULED_TIME, notificationHolder.getAlarmTime());
    surveyIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());

    PendingIntent notificationIntent = PendingIntent.getActivity(context, 1, surveyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


    // new wearable compatible way to do it
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(icon)
            .setContentTitle(experiment.getTitle())
            .setTicker(tickerText)
            .setContentText(message)
            .setWhen(notificationHolder.getAlarmTime())
            .setContentIntent(notificationIntent)
            .setAutoCancel(true);

    int defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
    String ringtoneUri = new UserPreferences(context).getRingtone();
    if (ringtoneUri != null) {
      notificationBuilder.setSound(Uri.parse(ringtoneUri));
    } else {
      defaults |= Notification.DEFAULT_SOUND;
//    notification.sound = Uri.parse(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
//                                   + "/Android/data/" + context.getPackageName() + "/" +
//                                   "deepbark_trial.mp3");
    }
    notificationBuilder.setDefaults(defaults);

    //end wearable

    return notificationBuilder.build();
  }

  private void createAlarmToCancelNotificationAtTimeout(Context context, NotificationHolder notificationHolder) {
    DateTime alarmTime = new DateTime(notificationHolder.getAlarmTime());
    int timeoutMinutes = (int)(notificationHolder.getTimeoutMillis() / MILLIS_IN_MINUTE);
    DateTime timeoutTime = new DateTime(alarmTime).plusMinutes(timeoutMinutes);
    long elapsedDurationInMillis = timeoutTime.getMillis();

    Log.i(PacoConstants.TAG, "Creating cancel alarm to timeout notification for holder: "
    		+ notificationHolder.getId()
    		+ ". experiment = " + notificationHolder.getExperimentId()
    		+ ". alarmtime = " + new DateTime(alarmTime).toString()
    		+ " timing out in " + timeoutMinutes + " minutes");

    Intent ultimateIntent = new Intent(context, AlarmReceiver.class);
    Uri uri = Uri.withAppendedPath(NotificationHolderColumns.CONTENT_URI, notificationHolder.getId().toString());
    ultimateIntent.setData(uri);
    ultimateIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());

    PendingIntent intent = PendingIntent.getBroadcast(context.getApplicationContext(), 2,
            ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(intent);
    alarmManager.set(AlarmManager.RTC_WAKEUP, elapsedDurationInMillis, intent);
  }

  public void createNotificationsForTrigger(Experiment experiment, DateTime triggeredDateTime, int triggerEvent, String sourceIdentifier) {
    Trigger trigger = (Trigger) experiment.getSignalingMechanisms().get(0);
    List<NotificationHolder> notificationsForTrigger = experimentProviderUtil.getNotificationsFor(experiment.getId());

    // Approach 1 for triggers, mark old triggers notification as missed, cancel them, and install notification for new trigger.
    // we cannot catch the notification before the user can click it. Thus they will always get triggered twice.
//    timeoutNotifications(notificationsForTrigger);

    //  Alternate approach, ignore new trigger if there is already an active notification for this trigger
    if (activeNotificationForTrigger(notificationsForTrigger)) {
      return;
    }

    try {
      Thread.sleep(trigger.getDelay());
    } catch (InterruptedException e) {
    }

    timeoutNotifications(notificationsForTrigger);
    createNewNotificationForExperiment(context, new TimeExperiment(triggeredDateTime, experiment), false);
  }

  public void createNotificationsForCustomGeneratedScript(Experiment experiment, String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    List<NotificationHolder> notifications = experimentProviderUtil.getNotificationsFor(experiment.getId());

    if (activeNotificationForCustomGeneratedScript(notifications)) {
      return;
    }
    createNewCustomNotificationForExperiment(context, DateTime.now(), experiment, timeoutMillis, message);
  }


  private boolean activeNotificationForTrigger(List<NotificationHolder> notificationsForTrigger) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notificationsForTrigger) {
      if (notificationHolder.isActive(now)) {
        Log.d(PacoConstants.TAG, "There is already a live notification for this trigger.");
        return true;
      }
    }
    return false;
  }

  private boolean activeNotificationForCustomGeneratedScript(List<NotificationHolder> notifications) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notifications) {
      if (notificationHolder.isCustomNotification()) {
        Log.d(PacoConstants.TAG, "There is already a live custom-generated notification for this experiment.");
        return true;
      }
    }
    return false;
  }


  private void createAlarmForSnooze(Context context, NotificationHolder notificationHolder) {
    DateTime alarmTime = new DateTime(notificationHolder.getAlarmTime());
    Experiment experiment = experimentProviderUtil.getExperiment(notificationHolder.getExperimentId());
    Integer snoozeTime = experiment.getSignalingMechanisms().get(0).getSnoozeTime();
    int snoozeMinutes = snoozeTime / MILLIS_IN_MINUTE;
    DateTime timeoutMinutes = new DateTime(alarmTime).plusMinutes(snoozeMinutes);
    long snoozeDurationInMillis = timeoutMinutes.getMillis();

    Log.i(PacoConstants.TAG, "Creating snooze alarm to resound notification for holder: "
        + notificationHolder.getId()
        + ". experiment = " + notificationHolder.getExperimentId()
        + ". alarmtime = " + new DateTime(alarmTime).toString()
        + " waking up from snooze in " + timeoutMinutes + " minutes");

    Intent ultimateIntent = new Intent(context, AlarmReceiver.class);
    Uri uri = Uri.withAppendedPath(NotificationHolderColumns.CONTENT_URI, notificationHolder.getId().toString());
    ultimateIntent.setData(uri);
    ultimateIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());
    ultimateIntent.putExtra(SNOOZE_REPEATER_EXTRA_KEY, true);

    PendingIntent intent = PendingIntent.getBroadcast(context.getApplicationContext(), 3,
            ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(intent);
    alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeDurationInMillis, intent);
  }

  public void removeNotificationsForCustomGeneratedScript(Experiment experiment) {
      List<NotificationHolder> notifs = experimentProviderUtil.getNotificationsFor(experiment.getId());
      for (NotificationHolder notificationHolder : notifs) {
        if (notificationHolder.isCustomNotification()) {
          timeoutNotification(notificationHolder);
          return; // there can only ever be one custom notification per experiment.
        }
      }
  }



}
