package com.pacoapp.paco.triggering;

import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.os.ExperimentExpirationAlarmReceiver;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

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
public class ExperimentExpirationManagerService extends Service {

  private static Logger Log = LoggerFactory.getLogger(ExperimentExpirationManagerService.class);

  private static final int ALARM_RECEIVER_INTENT_REQUEST_CODE = 1;


  private AlarmManager alarmManager;
  private Context context;


  protected UserPreferences userPreferences;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    if (intent == null) {
      Log.error("Null intent on broadcast trigger!");
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
          ExperimentExpirationManagerService.this.context = getApplicationContext();
          ExperimentExpirationManagerService.this.alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
          ExperimentExpirationManagerService.this.userPreferences = new UserPreferences(ExperimentExpirationManagerService.this.context);
          work();
        } finally {
          wl.release();
          stopSelf();
        }
      }
    };
    (new Thread(runnable)).start();
  }


  public void work() {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> experiments = experimentProviderUtil.getJoinedExperiments();
    if (experiments.isEmpty()) {
      Log.info("No joined experiments. Not creating alarms.");
      return;
    }

    List<Experiment> stillRunningExperiments = checkForEndedExperiments(experiments);
    if (!stillRunningExperiments.isEmpty()) {
      createNextAlarm();
    }
  }

  private List<Experiment> checkForEndedExperiments(List<Experiment> experiments) {
    List<Experiment> stillRunning = Lists.newArrayList();
    DateTime now = DateTime.now();
    for (Experiment experiment : experiments) {

      DateTime lastEndTime = ActionScheduleGenerator.getLastEndTime(experiment.getExperimentDAO());
      if (ActionScheduleGenerator.isOver(now, experiment.getExperimentDAO()) &&
              lastEndTime != null &&
              //Hours.hoursBetween(lastEndTime, now).getHours() < 24
              !alreadyFired(experiment.getId()) // use the local id to allow repeat stop/join resets.
              ) {
        setFired(experiment.getId());
        Log.info("Experiment has ended. Firing event: " + experiment.getExperimentDAO().getTitle());
        PacoExperimentActionBroadcaster.sendExperimentEnded(context.getApplicationContext(), experiment);
        // TODO remove from joined and move to archived.
        // TODO only fire experiment over broadcast once.
        //       For now, use minimumBuffer to prevent this for a long time.
      } else {
        stillRunning.add(experiment);
      }
    }
    return stillRunning;
  }

  private void setFired(Long id) {
    userPreferences.setExperimentEndedFired(id);

  }

  private boolean alreadyFired(Long experimentId) {
    return userPreferences.alreadyFiredExperimentEnd(experimentId);
  }

  private void createNextAlarm() {

    DateTime alarmTime = new DateMidnight().plusDays(1).toDateTime().plusHours(10); // 10am // TODO make this a userpref in settings
    Log.info("Creating wakeup alarm for experiment expiration " + alarmTime.toString());
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
    Intent ultimateIntent = new Intent(context, ExperimentExpirationAlarmReceiver.class);

    return PendingIntent.getBroadcast(context, ALARM_RECEIVER_INTENT_REQUEST_CODE,
        ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
  }


}
