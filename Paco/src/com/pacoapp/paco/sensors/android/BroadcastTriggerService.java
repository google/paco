package com.pacoapp.paco.sensors.android;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.scheduling.ActionSpecification;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.ExperimentHelper.Pair;
import com.pacoapp.paco.shared.util.TimeUtil;
import com.pacoapp.paco.triggering.AndroidActionExecutor;
import com.pacoapp.paco.triggering.NotificationCreator;

public class BroadcastTriggerService extends Service {

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (intent == null) {
      Log.e(PacoConstants.TAG, "Null intent on broadcast trigger!");
      return;
    }
    final Bundle extras = intent.getExtras();

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                    "Paco BroadcastTriggerService wakelock");
    wl.acquire();

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          propagateToExperimentsThatCare(extras);
        } finally {
          wl.release();
          stopSelf();
        }
      }
    };
    (new Thread(runnable)).start();
  }

  protected synchronized void propagateToExperimentsThatCare(Bundle extras) {

    final int triggerEvent = extras.getInt(Experiment.TRIGGER_EVENT);
    final String sourceIdentifier = extras.getString(Experiment.TRIGGER_SOURCE_IDENTIFIER);
    final String timeStr = extras.getString(Experiment.TRIGGERED_TIME);

    // TODO pass the duration along to the experiment somehow (either log it at the moment it happened, or, pass it in the notification (yuck)?
    final long duration = extras.getLong(Experiment.TRIGGER_PHONE_CALL_DURATION);

    DateTime time = null;
    if (timeStr != null) {
      time = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).parseDateTime(timeStr);
    }

    ExperimentProviderUtil eu = new ExperimentProviderUtil(this);
    DateTime now = new DateTime();
    NotificationCreator notificationCreator = NotificationCreator.create(this);
    List<Experiment> joined = eu.getJoinedExperiments();

    for (Experiment experiment : joined) {
      if (!experiment.isRunning(now)) {
        continue;
      }
      List<ExperimentGroup> groupsListening = ExperimentHelper.isBackgroundListeningForSourceId(experiment.getExperimentDAO(), sourceIdentifier);
      persistBroadcastData(eu, experiment, groupsListening, extras);

      List<Pair<ExperimentGroup, InterruptTrigger>> triggersThatMatch = ExperimentHelper.shouldTriggerBy(experiment.getExperimentDAO(), triggerEvent, sourceIdentifier);
      for (Pair<ExperimentGroup, InterruptTrigger> triggerInfo : triggersThatMatch) {
        List<PacoAction> actions = triggerInfo.second.getActions();
        for (PacoAction pacoAction : actions) {
          if (pacoAction.getActionCode() == pacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE) {
            String uniqueStringForTrigger = createUniqueStringForTrigger(experiment, triggerInfo);
            if (!recentlyTriggered(experiment, uniqueStringForTrigger, triggerInfo.second.getMinimumBuffer())) {
              setRecentlyTriggered(now, uniqueStringForTrigger);
              ActionSpecification timeExperiment = new ActionSpecification(time, experiment.getExperimentDAO(), triggerInfo.first, triggerInfo.second,  (PacoNotificationAction)pacoAction, (Long)null);
              notificationCreator.createNotificationsForTrigger(experiment, triggerInfo, ((PacoNotificationAction)pacoAction).getDelay(), time, triggerEvent, sourceIdentifier, timeExperiment);
            }
          } else if (pacoAction.getActionCode() == PacoAction.EXECUTE_SCRIPT_ACTION_CODE) {
            AndroidActionExecutor.runAction(getApplicationContext(), pacoAction, experiment, experiment.getExperimentDAO(), triggerInfo.first);
          }
        }
      }

    }
  }

  private void setRecentlyTriggered(DateTime now, String uniqueStringForTrigger) {
    UserPreferences prefs = new UserPreferences(getApplicationContext());
    prefs.setRecentlyTriggeredTime(uniqueStringForTrigger, now);

  }

  private boolean recentlyTriggered(Experiment experiment, String uniqueStringForTrigger, int minimumBuffer) {
    UserPreferences prefs = new UserPreferences(getApplicationContext());
    DateTime recentlyTriggered = prefs.getRecentlyTriggeredTime(uniqueStringForTrigger);
    return recentlyTriggered != null && recentlyTriggered.plusMinutes(minimumBuffer).isAfterNow();
  }

  public String createUniqueStringForTrigger(Experiment experiment, Pair<ExperimentGroup, InterruptTrigger> pair) {
    return experiment.getId() + ":"
            + pair.first.getName() + ":"
            + pair.second.getId();
  }

  /*
   * create and persist event containing any payload data sent along in original PACO_INTENT broadcast
   */
  private void persistBroadcastData(ExperimentProviderUtil eu, Experiment experiment,
                                    List<ExperimentGroup> groupsListening, Bundle extras) {
    long nowMillis = new DateTime().getMillis();
    for (ExperimentGroup experimentGroup : groupsListening) {

      Event event = EventUtil.createEvent(experiment, experimentGroup.getName(), nowMillis, null, null, null);
      Bundle payload = extras.getBundle(BroadcastTriggerReceiver.PACO_ACTION_PAYLOAD);
      for (String key : payload.keySet()) {
        if (payload.get(key) == null) {
          continue;
        }
        Output output = new Output();
        output.setEventId(event.getId());
        output.setName(key);
        output.setAnswer(payload.get(key).toString());
        event.addResponse(output);
      }
      eu.insertEvent(event);
    }
    notifySyncService();
  }

  private void notifySyncService() {
    startService(new Intent(this, SyncService.class));
  }

}
