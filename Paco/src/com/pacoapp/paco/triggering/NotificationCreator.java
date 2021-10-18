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

import android.app.*;
import android.os.Build;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.NotificationHolder;
import com.pacoapp.paco.model.NotificationHolderColumns;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.os.AlarmReceiver;
import com.pacoapp.paco.os.RingtoneUtil;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.scheduling.ActionSpecification;
import com.pacoapp.paco.shared.util.ExperimentHelper.Trio;
import com.pacoapp.paco.ui.ExperimentExecutor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationCreator {

  private static Logger Log = LoggerFactory.getLogger(NotificationCreator.class);

  private static final int DEFAULT_SNOOZE_10_MINUTES = 600000;
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
    Log.debug("timeoutNotification: " + notificationId);

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
    Log.debug("recreateActiveNotifications");
    try {
      List<NotificationHolder> allNotifications = experimentProviderUtil.getAllNotifications();
      DateTime now = new DateTime();
      for (NotificationHolder notificationHolder : allNotifications) {
        if (notificationHolder.isActive(now)) {
          Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());

          cancelNotification(context, notificationHolder.getId()); // in case
                                                                   // this
                                                                   // exists on
                                                                   // the status
                                                                   // bar, blow
                                                                   // it away
                                                                   // (this
                                                                   // happens on
                                                                   // package_replace
                                                                   // calls).
          String message = context.getString(R.string.time_to_participate_notification_text);
          if (notificationHolder.isCustomNotification()) {
            message = notificationHolder.getMessage();
          }

          int color = getColorForNotification(experiment, notificationHolder);
          boolean dismissible = getDismissibleForNotification(experiment, notificationHolder);

          fireNotification(context, notificationHolder, experiment.getExperimentDAO().getTitle(), message,
                           experiment.getExperimentDAO().getRingtoneUri(), color, dismissible);

          createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
          if (notificationHolder.getSnoozeCount() != null
              && (notificationHolder.getSnoozeCount() > PacoNotificationAction.SNOOZE_COUNT_DEFAULT)) {
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

  private int getColorForNotification(Experiment experiment, NotificationHolder notificationHolder) {
    String groupName = notificationHolder.getExperimentGroupName();
    Long actionTriggerId = notificationHolder.getActionTriggerId();
    Long actionId = notificationHolder.getActionId();
    ExperimentGroup group = experiment.getExperimentDAO().getGroupByName(groupName);
    if (group != null && actionTriggerId != null && actionId != null) {
      ActionTrigger actionTrigger = group.getActionTriggerById(actionTriggerId);
      if (actionTrigger != null) {
        long actionIdl = actionId;
        if (actionIdl < Integer.MAX_VALUE && actionId > Integer.MIN_VALUE) {
          PacoAction action = actionTrigger.getActionById((int)actionIdl);
          if (action != null && action instanceof PacoNotificationAction) {
            Integer color = ((PacoNotificationAction)action).getColor();
            if (color != null) {
              return color;
            }
          }
        }
      }
    }

    return PacoNotificationAction.DEFAULT_COLOR;
  }

  private boolean getDismissibleForNotification(Experiment experiment, NotificationHolder notificationHolder){
    String groupName = notificationHolder.getExperimentGroupName();
    Long actionTriggerId = notificationHolder.getActionTriggerId();
    Long actionId = notificationHolder.getActionId();
    ExperimentGroup group = experiment.getExperimentDAO().getGroupByName(groupName);
    if (group != null && actionTriggerId != null && actionId != null) {
      ActionTrigger actionTrigger = group.getActionTriggerById(actionTriggerId);
      if (actionTrigger != null) {
        long actionIdl = actionId;
        if (actionIdl < Integer.MAX_VALUE && actionId > Integer.MIN_VALUE) {
          PacoAction action = actionTrigger.getActionById((int)actionIdl);
          if (action != null && action instanceof PacoNotificationAction) {
            Boolean dismissible = ((PacoNotificationAction)action).getDismissible();
            if (dismissible != null) {
              return dismissible;
            }
          }
        }
      }
    }

    return PacoNotificationAction.DEFAULT_DISMISSIBLE;
  }

  public void createSnoozeWakeupNotification(long notificationId) {
    Log.debug("createSnoozeWakeupNotification " + notificationId);
    NotificationHolder notificationHolder = experimentProviderUtil.getNotificationById(notificationId);
    if (notificationHolder == null) {
      return;
    }
    DateTime now = new DateTime();
    if (notificationHolder.isActive(now)) {
      Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
      cancelNotification(context, notificationHolder.getId()); // in case this
                                                               // exists on the
                                                               // status bar,
                                                               // blow it away
                                                               // (this happens
                                                               // on
                                                               // package_replace
                                                               // calls).
      fireNotification(context, notificationHolder, experiment.getExperimentDAO().getTitle(),
                       context.getString(R.string.time_to_participate_notification_text), experiment.getExperimentDAO()
                                                                                                    .getRingtoneUri(),
                       getColorForNotification(experiment, notificationHolder), getDismissibleForNotification(experiment, notificationHolder));
    }
    // TODO
    // Optionally create another snooze alarm if the snoozeCount says it should
    // happen and there is time left
    // calculate snoozecount = time elapsed since original alarmtime %
    // snoozeTime
    // calculate time left = nextSnoozeTime < timeoutTime (absolute times in
    // walltime)
  }

  private void createAllNotificationsForLastMinute(long alarmTime) {
    DateTime alarmAsDateTime = new DateTime(alarmTime);
    Log.info("Creating All notifications for last minute from signaled alarmTime: "
            + alarmAsDateTime.toString());

    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    for (Experiment experiment : experimentProviderUtil.getJoinedExperiments()) {
      experimentDAOs.add(experiment.getExperimentDAO());
    }
    List<ActionSpecification> times = ActionScheduleGenerator.getAllAlarmsWithinOneMinuteofNow(alarmAsDateTime.minusSeconds(59),
                                                                                               experimentDAOs,
                                                                                               new AndroidEsmSignalStore(context),
                                                                                                                         experimentProviderUtil);

    for (ActionSpecification timeExperiment : times) {
      if (timeExperiment.action == null) {
        continue; // not a notification action specification
      }
      // TODO might we be able to timeout all notifications for all experiments
      // instead of doing this for each experiment?
      final Long experimentId = timeExperiment.experiment.getId();
      ExperimentGroup experimentGroup = timeExperiment.experimentGroup;
      if (experimentGroup == null) {
        timeoutNotifications(experimentProviderUtil.getAllNotificationsFor(experimentId));
      } else {
        List<NotificationHolder> notificationsForGroup = experimentProviderUtil.getNotificationsFor(experimentId,
                                                                                                    experimentGroup.getName());
        timeoutNotifications(notificationsForGroup);
      }
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
      Log.info("Timing out notification. Holder = " + notificationHolder.getId() + ", experiment = "
                               + notificationHolder.getExperimentId());
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
    event.setActionTriggerSpecId(notificationHolder.getActionTriggerSpecId());
    event.setActionId(notificationHolder.getActionId());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setScheduledTime(new DateTime(notificationHolder.getAlarmTime()));
    experimentProviderUtil.insertEvent(event);
  }

  private void cancelNotification(Context context, long notificationId) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(new Long(notificationId).intValue());
  }

  private void createNewNotificationForExperiment(Context context, ActionSpecification timeExperiment,
                                                  boolean customGenerated) {
    Log.info("CreateNewNotificationForExperiment start");
    NotificationHolder notificationHolder = createNewNotificationWithDetails(context, timeExperiment);

    createAlarmToCancelNotificationAtTimeout(context, notificationHolder);

    if (timeExperiment.action.getSnoozeCount() > PacoNotificationAction.SNOOZE_COUNT_DEFAULT) {
      createAlarmForSnooze(context, notificationHolder);
    }
    Log.info("CreateNewNotificationForExperiment done");
  }

  private void createNewCustomNotificationForExperiment(Context context, DateTime time, ExperimentDAO experiment,
                                                        String groupName, long expirationTimeInMillis, String message,
                                                        Integer color, Boolean dismissible, Long actionTriggerSpecId, Long actionTriggerId, Long actionId) {
    NotificationHolder notificationHolder = new NotificationHolder(time.getMillis(), experiment.getId(), 0,
                                                                   expirationTimeInMillis, groupName, actionTriggerId, actionId,
                                                                   NotificationHolder.CUSTOM_GENERATED_NOTIFICATION,
                                                                   message, actionTriggerSpecId);

    experimentProviderUtil.insertNotification(notificationHolder);
    fireNotification(context, notificationHolder, experiment.getTitle(), message, experiment.getRingtoneUri(), color, dismissible);
    createAlarmToCancelNotificationAtTimeout(context, notificationHolder);
  }

  private NotificationHolder createNewNotificationWithDetails(Context context, ActionSpecification timeExperiment) {
    final PacoNotificationAction action = timeExperiment.action;
    long expirationTimeInMillis;
    if (action != null) {
      expirationTimeInMillis = action.getTimeout() * 60 * 1000;
    } else {
      expirationTimeInMillis = Integer.parseInt(PacoNotificationAction.ESM_SIGNAL_TIMEOUT);
    }

    String messageText = action.getMsgText() != null ? action.getMsgText() : null;
    if (Strings.isNullOrEmpty(messageText) || (!Strings.isNullOrEmpty(messageText) && messageText.equals(PacoNotificationAction.DEFAULT_NOTIFICATION_MSG))) {
      messageText = context.getString(R.string.time_to_participate_notification_text);
    }

    NotificationHolder notificationHolder = new NotificationHolder(
                                                                   timeExperiment.time.getMillis(),
                                                                   timeExperiment.experiment.getId(),
                                                                   0,
                                                                   expirationTimeInMillis,
                                                                   timeExperiment.experimentGroup.getName(),
                                                                   timeExperiment.actionTrigger.getId(),
                                                                   action.getId(),
                                                                   null,
                                                                   messageText,
                                                                   timeExperiment.actionTriggerSpecId);
    experimentProviderUtil.insertNotification(notificationHolder);
    fireNotification(context, notificationHolder, timeExperiment.experiment.getTitle(), messageText,
                     timeExperiment.experiment.getRingtoneUri(), action.getColor(), action.getDismissible());
    return notificationHolder;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void fireNotification(Context context, NotificationHolder notificationHolder, String experimentTitle,
                                String message, String experimentSpecificRingtone, Integer color, Boolean dismissible) {
    String alarmTimeString = "";
    final Long alarmTime = notificationHolder.getAlarmTime();
    if (alarmTime != null) {
      alarmTimeString = new DateTime(alarmTime).toString();
    }
    Log.info("Creating notification for experiment: " + experimentTitle + ". source: "
                  + notificationHolder.getNotificationSource() + ". alarmTime: "
                  + alarmTimeString + ", holderId = " + notificationHolder.getId());

    Notification notification = createAndroidNotification(context, notificationHolder, experimentTitle, message,
                                                          experimentSpecificRingtone, color, dismissible);
    // NotificationManager notificationManager = (NotificationManager)
    // context.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    notificationManager.notify(notificationHolder.getId().intValue(), notification);

  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private Notification createAndroidNotification(Context context, NotificationHolder notificationHolder,
                                                 String experimentTitle, String message,
                                                 String experimentSpecificRingtone, Integer color, Boolean dismissible) {
    int icon = R.drawable.paco32;

    String tickerText = context.getString(R.string.time_for_notification_title) + experimentTitle;
    if (notificationHolder.isCustomNotification()) {
      tickerText = message;
    }

    Intent surveyIntent = new Intent(context, ExperimentExecutor.class);
    surveyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    surveyIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());

    PendingIntent notificationIntent = PendingIntent.getActivity(context,
                                                                 notificationHolder.getId().intValue(),
                                                                 surveyIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);

    // Adding bigText style to notification enabling larger messages to be read
    // in the notification pane
    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
    bigStyle.setBigContentTitle(experimentTitle);
    bigStyle.bigText(message);

    // Make sure we have a color, or use the default
    if(color == null){
    	color = PacoNotificationAction.DEFAULT_COLOR;
    }

    //Make sure we know whether the notification is dismissible/not, or use default
    if(dismissible == null){
    	dismissible = PacoNotificationAction.DEFAULT_DISMISSIBLE;
    }

    String channelId = makeNotificationChannel();
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).setSmallIcon(icon)
                                                                                            .setContentTitle(experimentTitle)
                                                                                            .setTicker(tickerText)
                                                                                            .setContentText(message)
                                                                                            .setWhen(notificationHolder.getAlarmTime())
                                                                                            .setContentIntent(notificationIntent)
                                                                                            .setOngoing(!dismissible) //whether it's dismissible
                                                                                            .setAutoCancel(dismissible) //whether it should disappear on user click
                                                                                            .setLights(color,PacoNotificationAction.DEFAULT_NOTIFICATION_DELAY,PacoNotificationAction.DEFAULT_NOTIFICATION_DELAY)
                                                                                            .setStyle(bigStyle)
            .setChannelId(channelId);

    int defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
    defaults = getRingtone(context, notificationBuilder, defaults, experimentSpecificRingtone);
    notificationBuilder.setDefaults(defaults);
    return notificationBuilder.build();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public String makeNotificationChannel() {
    NotificationChannel notificationChannel =
            new NotificationChannel("pacoChannel", "Paco Experiment Notifications", NotificationManager.IMPORTANCE_DEFAULT);
    notificationChannel.setDescription("Notices to participate in your Paco Experiment");
    notificationChannel.enableVibration(true);
    notificationChannel.setLockscreenVisibility(0);

    // Adds NotificationChannel to system. Attempting to create an existing notification
    // channel with its original values performs no operation, so it's safe to perform the
    // below sequence.
    NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.createNotificationChannel(notificationChannel);
    return "pacoChannel";
  }

  public int getRingtone(Context context, NotificationCompat.Builder notificationBuilder, int defaults,
                         String experimentRingtone) {
    if (!Strings.isNullOrEmpty(experimentRingtone)) {
      if (experimentRingtone.equals(RingtoneUtil.ALTERNATE_RINGTONE_FILENAME)
          || experimentRingtone.equals(RingtoneUtil.ALTERNATE_RINGTONE_TITLE)
          || experimentRingtone.equals(RingtoneUtil.ALTERNATE_RINGTONE_TITLE_V2)
          || experimentRingtone.equals(RingtoneUtil.ALTERNATE_RINGTONE_TITLE_V2_FULLPATH)) {
        // TODO massive hack for quick study. FIX with proper ringtone
        // customization per experiment
        String ringtoneUri = new UserPreferences(context).getAltRingtoneUri();
        if (ringtoneUri != null) {
          notificationBuilder.setSound(Uri.parse(ringtoneUri));
          return defaults;
        }
      }
    }

    String ringtoneUri = new UserPreferences(context).getRingtoneUri();
    if (ringtoneUri != null) {
      notificationBuilder.setSound(Uri.parse(ringtoneUri));
    } else {
      defaults |= Notification.DEFAULT_SOUND;
    }
    return defaults;
  }

  @SuppressLint("NewApi")
  private void createAlarmToCancelNotificationAtTimeout(Context context, NotificationHolder notificationHolder) {
    DateTime alarmTime = new DateTime(notificationHolder.getAlarmTime());
    int timeoutMinutes = (int) (notificationHolder.getTimeoutMillis() / MILLIS_IN_MINUTE);
    DateTime timeoutTime = new DateTime(alarmTime).plusMinutes(timeoutMinutes);
    long elapsedDurationInMillis = timeoutTime.getMillis();

    Log.info("Creating cancel alarm to timeout notification for holder: " + notificationHolder.getId()
                             + ". experiment = " + notificationHolder.getExperimentId() + ". alarmtime = "
                             + new DateTime(alarmTime).toString() + " timing out in " + timeoutMinutes + " minutes");

    Intent ultimateIntent = new Intent(context, AlarmReceiver.class);
    Uri uri = Uri.withAppendedPath(NotificationHolderColumns.CONTENT_URI, notificationHolder.getId().toString());
    ultimateIntent.setData(uri);
    ultimateIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());

    PendingIntent intent = PendingIntent.getBroadcast(context.getApplicationContext(), 2, ultimateIntent,
                                                      PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(intent);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, elapsedDurationInMillis, intent);
    } else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, elapsedDurationInMillis, intent);
    }

  }

  public void createNotificationsForTrigger(Experiment experiment, Trio<ExperimentGroup, InterruptTrigger, InterruptCue> triggerInfo,
                                            long delayTime, DateTime triggeredDateTime, int triggerEvent,
                                            String sourceIdentifier, ActionSpecification timeExperiment) {
    Log.info("entering createNotificationsForTrigger");
    ExperimentGroup experimentGroup = triggerInfo.first;
    List<NotificationHolder> notificationsForGroup = experimentProviderUtil.getNotificationsFor(experiment.getId(),
                                                                                                experimentGroup.getName());

    Log.info("Got notificationsForGroup: " + notificationsForGroup.size());
    // Approach 1 for triggers, mark old triggers notification as missed, cancel
    // them, and install notification for new trigger.
    // we cannot catch the notification before the user can click it. Thus they
    // will always get triggered twice.
    // timeoutNotifications(notificationsForTrigger);

    // Alternate approach, ignore new trigger if there is already an active
    // notification for this trigger
    if (activeNotificationForTrigger(notificationsForGroup, timeExperiment)) {
      Log.info("ACtive notification for trigger exists. not notifying");
      return;
    }

    Log.info("delaytime in notificaiton action: " + delayTime);
    try {
      if (delayTime >= 5000) { // temporary bug in the default value
        delayTime /= 1000;
      }
      Thread.sleep(delayTime * 1000);
    } catch (InterruptedException e) {
    }
    Log.info("timeout and createNew");
    timeoutNotifications(notificationsForGroup);
    createNewNotificationForExperiment(context, timeExperiment, false);
  }

  public void createNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup,
                                                          String message, boolean makeSound, boolean makeVibrate,
                                                          long timeoutMillis, Long actionTriggerSpecId, Long actionTriggerId, Long actionId) {
    List<NotificationHolder> notifications = experimentProviderUtil.getAllNotificationsFor(experiment.getId());

    if (activeNotificationForCustomGeneratedScript(notifications, message)) {
      return;
    }
    createNewCustomNotificationForExperiment(context, DateTime.now(), experiment, experimentGroup.getName(),
                                             timeoutMillis, message, null, null, actionTriggerSpecId, actionTriggerId, actionId);
  }

  private boolean activeNotificationForTrigger(List<NotificationHolder> notificationsForGroup,
                                               ActionSpecification timeExperiment) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notificationsForGroup) {
      if (notificationHolder.isActive(now) && notificationHolder.matches(timeExperiment)) {
        Log.debug("There is already a live notification for this trigger.");
        return true;
      }
    }
    return false;
  }

  private boolean activeNotificationForCustomGeneratedScript(List<NotificationHolder> notifications, String message) {
    DateTime now = new DateTime();
    for (NotificationHolder notificationHolder : notifications) {
      if (notificationHolder.isCustomNotification() && notificationHolder.getMessage() != null
          && notificationHolder.getMessage().equals(message)
          && notificationHolder.getNotificationSource().equals(NotificationHolder.CUSTOM_GENERATED_NOTIFICATION)) {
        Log.debug("There is already a live custom-generated notification for this experiment.");
        return true;
      }
    }
    return false;
  }

  @SuppressLint("NewApi")
  private void createAlarmForSnooze(Context context, NotificationHolder notificationHolder) {
    DateTime alarmTime = new DateTime(notificationHolder.getAlarmTime());
    Experiment experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
    Integer snoozeTime = notificationHolder.getSnoozeTime();
    if (snoozeTime == null) {
      snoozeTime = DEFAULT_SNOOZE_10_MINUTES;
    }
    int snoozeMinutes = snoozeTime / MILLIS_IN_MINUTE;
    DateTime timeoutMinutes = new DateTime(alarmTime).plusMinutes(snoozeMinutes);
    long snoozeDurationInMillis = timeoutMinutes.getMillis();

    Log.info("Creating snooze alarm to resound notification for holder: " + notificationHolder.getId()
                             + ". experiment = " + notificationHolder.getExperimentId() + ". alarmtime = "
                             + new DateTime(alarmTime).toString() + " waking up from snooze in " + timeoutMinutes
                             + " minutes");

    Intent ultimateIntent = new Intent(context, AlarmReceiver.class);
    Uri uri = Uri.withAppendedPath(NotificationHolderColumns.CONTENT_URI, notificationHolder.getId().toString());
    ultimateIntent.setData(uri);
    ultimateIntent.putExtra(NOTIFICATION_ID, notificationHolder.getId().longValue());
    ultimateIntent.putExtra(SNOOZE_REPEATER_EXTRA_KEY, true);

    PendingIntent intent = PendingIntent.getBroadcast(context.getApplicationContext(), 3, ultimateIntent,
                                                      PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(intent);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeDurationInMillis, intent);
    } else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeDurationInMillis, intent);
    }

  }

  public void removeNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup,
                                                          String message) {
    List<NotificationHolder> notifs = experimentProviderUtil.getAllNotificationsFor(experiment.getId(),
                                                                                    experimentGroup.getName());
    for (NotificationHolder notificationHolder : notifs) {
      if (notificationHolder.isCustomNotification() && notificationHolder.getMessage() != null
          && notificationHolder.getMessage().equals(message)) {
        timeoutNotification(notificationHolder);
        return; // there can only ever be one custom notification per
                // experiment.
      }
    }
  }

  public void removeAllNotificationsForCustomGeneratedScript(ExperimentDAO experiment, ExperimentGroup experimentGroup) {
    List<NotificationHolder> notifs = experimentProviderUtil.getAllNotificationsFor(experiment.getId(),
                                                                                    experimentGroup.getName());
    for (NotificationHolder notificationHolder : notifs) {
      if (notificationHolder.isCustomNotification()) {
        timeoutNotification(notificationHolder);
      }
    }
  }

}
