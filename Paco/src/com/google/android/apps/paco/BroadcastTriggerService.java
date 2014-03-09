package com.google.android.apps.paco;

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
    final String timeStr = extras.getString(Experiment.TRIGGERED_TIME);
    final int event = extras.getInt(Experiment.TRIGGER_EVENT);
    final String sourceIdentifier = extras.getString(Experiment.TRIGGER_SOURCE_IDENTIFIER);

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                    "Paco BroadcastTriggerService wakelock");
    wl.acquire();

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          DateTime time = null;
          if (timeStr != null) {
            time = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).parseDateTime(timeStr);
          }
          notifyExperimentsThatCare(time, event, sourceIdentifier);
        } finally {
          wl.release();
          stopSelf();
        }
      }
    };
    (new Thread(runnable)).start();
  }

  protected void notifyExperimentsThatCare(DateTime time, int triggerEvent, String sourceIdentifier) {
    NotificationCreator notificationCreator = NotificationCreator.create(this);
    ExperimentProviderUtil eu = new ExperimentProviderUtil(this);
    DateTime now = new DateTime();
    List<Experiment> joined = eu.getJoinedExperiments();
    for (Experiment experiment : joined) {
      if (experiment.isRunning(now)
          && experiment.shouldTriggerBy(triggerEvent, sourceIdentifier)
          && !recentlyTriggered(experiment.getServerId(),
                                experiment.getSignalingMechanisms().get(0).getMinimumBuffer())) {
        setRecentlyTriggered(now, experiment.getServerId());
        notificationCreator.createNotificationsForTrigger(experiment, time, triggerEvent, sourceIdentifier);
      }
    }
  }

  private void setRecentlyTriggered(DateTime now, long experimentId) {
    UserPreferences prefs = new UserPreferences(getApplicationContext());
    prefs.setRecentlyTriggeredTime(experimentId, now);

  }

  private boolean recentlyTriggered(long experimentId, Integer minimumBufferInMinutes) {
    UserPreferences prefs = new UserPreferences(getApplicationContext());
    DateTime recentlyTriggered = prefs.getRecentlyTriggeredTime(experimentId);
    return recentlyTriggered != null && recentlyTriggered.plusMinutes(minimumBufferInMinutes).isAfterNow();
  }


}
