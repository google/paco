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
package com.pacoapp.paco.triggering;

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

import com.google.common.collect.Lists;
import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.NotificationHolder;
import com.pacoapp.paco.model.NotificationHolderColumns;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.os.AlarmReceiver;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.scheduling.ActionSpecification;
import com.pacoapp.paco.shared.util.ExperimentHelper.Pair;
import com.pacoapp.paco.ui.ExperimentExecutor;

public class NotificationCreator {

  public static final String SNOOZE_REPEATER_EXTRA_KEY = "SNOOZE REPEATER";
  private static final int MILLIS_IN_MINUTE = 60000;
  public static String NOTIFICATION_ID = "com.google.android.apps.paco.notification_id";
  private Context context;
  private ExperimentProviderUtil experimentProviderUtil;

  public NotificationCreator(Context applicationContext) {
    this.context = applicationContext;
    experimentProviderUtil = new ExperimentProviderUtil(context);
  }

  public static NotificationCreator create(Context context) {
    return new NotificationCreator(context.getApplicationContext());
  }

  public void createNotificationsForAlarmTime(long alarmTime) {
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
    List<NotificationHolder> notifs = experimentProviderUtil.getAllNotificationsFor(experimentId);
    timeoutNotifications(notifs);
  }

  public void recreateActiveNotifications() {
    try {
      List<NotificationHolder> allNotifications = experimentProviderUtil.getAllNotifications();
      DateTime now = new DateTime();
      for (NotificationHolder notificationHolder : allNotifications) {
        if (notificationHolder.isActive(now)) {
          Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());

          cancelNotification(context, notificationHolder.getId());  // in case this exists on the status bar, blow it away (this happens on package_replace calls).
          String message = context.getString(R.string.time_to_participate_notification_text);
          if (notificationHolder.isCustomNotification()) {
            message = notificationHolder.getMessage();
          }
          fireNotification(context, notificationHolder, experiment.getExperimentDAO().getTitle(), message);

          createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
          if (notificationHolder.getSnoozeCount() != null && (notificationHolder.getSnoozeCount() > PacoNotificationAction.SNOOZE_COUNT_DEFAULT)) {
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
        Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
        cancelNotification(context, notificationHolder.getId());  // in case this exists on the status bar, blow it away (this happens on package_replace calls).
        fireNotification(context, notificationHolder, experiment.getExperimentDAO().getTitle(),  context.getString(R.string.time_to_participate_notification_text));
    }
    // TODO
    // Optionally create another snooze alarm if the snoozeCount says it should happen and there is time left
    // calculate snoozecount = time elapsed since original alarmtime % snoozeTime
    // calculate time left = nextSnoozeTime < timeoutTime (absolute times in walltime)
  }

  private void createAllNotificationsForLastMinute(long alarmTime) {
    DateTime alarmAsDateTime = new DateTime(alarmTime);
    Log.i(PacoConstants.TAG, "Creating All notifications for last minute from signaled alarmTime: " + alarmAsDateTime.toString());

    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    for (Experiment experiment : experimentProviderUtil.getJoinedExperiments()) {
      experimentDAOs.add(experiment.getExperimentDAO());
    }
    List<ActionSpecification> times = ActionScheduleGenerator.getAllAlarmsWithinOneMinuteofNow(alarmAsDateTime.minusSeconds(59),
        experimentDAOs, new AndroidEsmSignalStore(context), experimentProviderUtil);

    for (ActionSpecification timeExperiment : times) {
      if (timeExperiment.action == null) {
        continue; // not a notification action specification
      }
      // TODO might we be able to timeout all notifications for all experiments instead of doing this for each experiment?
      timeoutNotifications(experimentProviderUtil.getAllNotificationsFor(timeExperiment.experiment.getId()));
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
    Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentGroupName(notificationHolder.getExperimentGroupName());
    event.setActionTriggerId(notificationHolder.getActionTriggerId());
    event.setActionTriggerSpecId(notificationHolder.getActionId());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setScheduledTime(new DateTime(notificationHolder.getAlarmTime()));
    experimentProviderUtil.insertEvent(event);
  }

  private void cancelNotification(Context context, long notificationId) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(new Long(notificationId).intValue());
  }

  private void createNewNotificationForExperiment(Context context, ActionSpecification timeExperiment, boolean customGenerated) {

    NotificationHolder notificationHolder = createNewNotificationWithDetails(context, timeExperiment);

    createAlarmToCancelNotificationAtTimeout(context, notificationHolder);

    if (timeExperiment.action.getSnoozeCount() > PacoNotificationAction.SNOOZE_COUNT_DEFAULT) {
      createAlarmForSnooze(context, notificationHolder);
    }

  }

  private void createNewCustomNotificationForExperiment(Context context, DateTime time, ExperimentDAO experiment, String groupName, long expirationTimeInMillis, String message) {
    NotificationHolder notificationHolder = new NotificationHolder(time.getMillis(),
                                                                   experiment.getId(),
                                                                   0,
                                                                   expirationTimeInMillis,
                                                                   groupName,
                                                                   null,
                                                                   null,
                                                                   NotificationHolder.CUSTOM_GENERATED_NOTIFICATION,
                                                                   message,
                                                                   null);



    experimentProviderUtil.insertNotification(notificationHolder);
    fireNotification(context, notificationHolder, experiment.getTitle(), message);
    createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
  }



  private NotificationHolder createNewNotificationWithDetails(Context context,ActionSpecification timeExperiment) {
    final PacoNotificationAction action = timeExperiment.action;
    long expirationTimeInMillis;
    if (action != null) {
      expirationTimeInMillis = action.getTimeout() * 60 * 1000;
    } else {
      expirationTimeInMillis = Integer.parseInt(PacoNotificationAction.ESM_SIGNAL_TIMEOUT);
    }

    NotificationHolder notificationHolder = new NotificationHolder(timeExperiment.time.getMillis(),
                                                                   timeExperiment.experiment.getId(),
                                                                   0,
                                                                   expirationTimeInMillis,
                                                                   timeExperiment.experimentGroup.getName(),
                                                                   timeExperiment.actionTrigger.getId(),
                                                                   action.getId(),
                                                                   null,
                                                                   action.getMsgText() != null
                                                                     ? action.getMsgText()
                                                                     : context.getString(R.string.time_to_participate_notification_text),
                                                                     timeExperiment.actionTriggerSpecId);
    experimentProviderUtil.insertNotification(notificationHolder);
    fireNotification(context, notificationHolder, timeExperiment.experiment.getTitle(), action.getMsgText());
    return notificationHolder;
  }

  private void fireNotification(Context context, NotificationHolder notificationHolder, String experimentTitle, String message) {
    Log.i(PacoConstants.TAG, "Creating notification for experiment: " + experimentTitle
            + ". source: " + notificationHolder.getNotificationSource()
            + ". alarmTime: " + notificationHolder.getAlarmTime().toString()
            + ", holderId = " + notificationHolder.getId());

    Notification notification = createNotification(context, notificationHolder, experimentTitle, message);
    //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    notificationManager.notify(notificationHolder.getId().intValue(), notification);

  }

  private Notification createNotification(Context context, NotificationHolder notificationHolder, String experimentTitle, String message) {
    int icon = R.drawable.paco32;


    String tickerText = context.getString(R.string.time_for_notification_title) + experimentTitle;
    if (notificationHolder.isCustomNotification()) {
      tickerText = message;
    }

    Intent surveyIntent = new Intent(context, ExperimentExecutor.class);
    surveyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    surveyIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());

    PendingIntent notificationIntent = PendingIntent.getActivity(context, 1, surveyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(icon)
            .setContentTitle(experimentTitle)
            .setTicker(tickerText)
            .setContentText(message)
            .setWhen(notificationHolder.getAlarmTime())
            .setContentIntent(notificationIntent)
            .setAutoCancel(true);

    int defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
    String ringtoneUri = new UserPreferences(context).getRingtoneUri();
    if (ringtoneUri != null) {
      notificationBuilder.setSound(Uri.parse(ringtoneUri));
    } else {
      defaults |= Notification.DEFAULT_SOUND;
//    notification.sound = Uri.parse(android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
//                                   + "/Android/data/" + context.getPackageName() + "/" +
//                                   "deepbark_trial.mp3");
    }
    notificationBuilder.setDefaults(defaults);
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

  public void createNotificationsForTrigger(Experiment experiment, Pair<ExperimentGroup, InterruptTrigger> triggerInfo, long delayTime, DateTime triggeredDateTime, int triggerEvent, String sourceIdentifier, ActionSpecification timeExperiment) {
    ExperimentGroup experimentGroup = triggerInfo.first;
    List<NotificationHolder> notificationsForGroup = experimentProviderUtil.getNotificationsFor(experiment.getId(), experimentGroup.getName());

    // Approach 1 for triggers, mark old triggers notification as missed, cancel them, and install notification for new trigger.
    // we cannot catch the notification before the user can click it. Thus they will always get triggered twice.
//    timeoutNotifications(notificationsForTrigger);

    //  Alternate approach, ignore new trigger if there is already an active notification for this trigger
    if (activeNotificationForTrigger(notificationsForGroup, timeExperiment)) {
      return;
    }

    try {
      Thread.sleep(delayTime * 1000);
    } catch (InterruptedException e) {
    }

    timeoutNotifications(notificationsForGroup);
    createNewNotificationForExperiment(context, timeExperiment, false);
  }

  public void createNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup, String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    List<NotificationHolder> notifications = experimentProviderUtil.getAllNotificationsFor(experiment.getId());

    if (activeNotificationForCustomGeneratedScript(notifications, message)) {
      return;
    }
    createNewCustomNotificationForExperiment(context, DateTime.now(), experiment, experimentGroup.getName(), timeoutMillis, message);
  }


  private boolean activeNotificationForTrigger(List<NotificationHolder> notificationsForGroup, ActionSpecification timeExperiment) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notificationsForGroup) {
      if (notificationHolder.isActive(now) && notificationHolder.matches(timeExperiment)) {
        Log.d(PacoConstants.TAG, "There is already a live notification for this trigger.");
        return true;
      }
    }
    return false;
  }

  private boolean activeNotificationForCustomGeneratedScript(List<NotificationHolder> notifications, String message) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notifications) {
      if (notificationHolder.isCustomNotification() &&
              notificationHolder.getMessage() != null &&
              notificationHolder.getMessage().equals(message) &&
              notificationHolder.getNotificationSource().equals(NotificationHolder.CUSTOM_GENERATED_NOTIFICATION)) {
        Log.d(PacoConstants.TAG, "There is already a live custom-generated notification for this experiment.");
        return true;
      }
    }
    return false;
  }


  private void createAlarmForSnooze(Context context, NotificationHolder notificationHolder) {
    DateTime alarmTime = new DateTime(notificationHolder.getAlarmTime());
    Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
    Integer snoozeTime = notificationHolder.getSnoozeTime();
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

  public void removeNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup, String message) {
      List<NotificationHolder> notifs = experimentProviderUtil.getAllNotificationsFor(experiment.getId(), experimentGroup.getName());
      for (NotificationHolder notificationHolder : notifs) {
        if (notificationHolder.isCustomNotification() &&
                notificationHolder.getMessage() != null && notificationHolder.getMessage().equals(message)) {
          timeoutNotification(notificationHolder);
          return; // there can only ever be one custom notification per experiment.
        }
      }
  }

  public void removeAllNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup) {
    List<NotificationHolder> notifs = experimentProviderUtil.getAllNotificationsFor(experiment.getId(), experimentGroup.getName());
    for (NotificationHolder notificationHolder : notifs) {
      if (notificationHolder.isCustomNotification()) {
        timeoutNotification(notificationHolder);
      }
    }
}




}
