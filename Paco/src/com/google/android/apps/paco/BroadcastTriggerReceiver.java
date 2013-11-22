package com.google.android.apps.paco;

import java.util.List;

import org.joda.time.DateTime;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BroadcastTriggerReceiver extends BroadcastReceiver {

  private static final String FREQUENCY = "Frequency";
  public static final String RUNNING_PROCESS_WATCHER_FLAG = "RUNNING_PROCESS_WATCHER";

  public static final String PACO_TRIGGER_INTENT = "com.pacoapp.paco.action.PACO_TRIGGER";

	@Override
  public void onReceive(Context context, Intent intent) {
    if (isPhoneHangup(intent)) {
      triggerPhoneHangup(context, intent);
    } else if (isUserPresent(intent)) {
      triggerUserPresent(context, intent);
    } else if (intent.getAction().equals(PACO_TRIGGER_INTENT)) {
      triggerPacoTriggerReceived(context, intent);
    }
    // handle polling events for screen on actions (app usage)
    boolean shouldPoll = shouldPoll(context);
    if (isUserPresent(intent) && shouldPoll) {
      startProcessService(context);
    } else if (isScreenOn(intent) && !isKeyGuardOn(context) && shouldPoll) {
      startProcessService(context);
    } else if (isScreenOff(intent)) {
      stopProcessingService(context);
    }
  }

  private boolean isUserPresent(Intent intent) {
    return intent.getAction().equals(android.content.Intent.ACTION_USER_PRESENT);
  }

  private boolean isKeyGuardOn(Context context) {
    return ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
  }

  private boolean isScreenOn(Intent intent) {
    return intent.getAction().equals(android.content.Intent.ACTION_SCREEN_ON);
  }

  private boolean isScreenOff(Intent intent) {
    return intent.getAction().equals(android.content.Intent.ACTION_SCREEN_OFF);
  }


  private void startProcessService(Context context) {
    Log.i(PacoConstants.TAG, "Starting App Usage poller");
    toggleWatchRunningProcesses(context, true);
    Intent intent = new Intent(context, ProcessService.class);
    context.startService(intent);
  }

  private void stopProcessingService(Context context) {
    Log.i(PacoConstants.TAG, "Stopping App Usage poller");
    toggleWatchRunningProcesses(context, false);
  }

  private static boolean shouldPoll(Context context) {
    boolean shouldPoll = isAtLeastOneExperimentWatchingTasks(context);
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, shouldPoll);
    return shouldPoll;
  }

  private static boolean isAtLeastOneExperimentWatchingTasks(Context context) {
    ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
    DateTime now = new DateTime();
    List<Experiment> joined = eu.getJoinedExperiments();
    for (Experiment experiment : joined) {
      if (!experiment.isOver(now) && experiment.shouldPoll()) {
        return true;
      }
    }
    return false;
  }

  public static boolean shouldWatchProcesses(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getBoolean(RUNNING_PROCESS_WATCHER_FLAG, true);
  }

  public static void toggleWatchRunningProcesses(Context context, boolean running) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putBoolean(RUNNING_PROCESS_WATCHER_FLAG, running).commit();
  }

  public static void setFrequency(Context context, int freq) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putInt(FREQUENCY, freq).commit();
  }

  public static int getFrequency(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getInt(FREQUENCY, 1);
  }

  private void triggerPacoTriggerReceived(Context context, Intent intent) {
    String sourceIdentifier = intent.getStringExtra("sourceIdentifier");
    if (sourceIdentifier == null || sourceIdentifier.length() == 0) {
      Log.d(PacoConstants.TAG, "No source identifier specified for PACO_TRIGGER");
    } else {
      triggerEvent(context, Trigger.PACO_ACTION_EVENT, sourceIdentifier);
    }
  }

  private boolean isPhoneHangup(Intent intent) {
    String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    return telephonyExtraState != null && telephonyExtraState.equals(TelephonyManager.EXTRA_STATE_IDLE);
  }

  private void triggerUserPresent(Context context, Intent intent) {
    Log.i(PacoConstants.TAG, "User present trigger");
    triggerEvent(context, Trigger.USER_PRESENT);
  }

  private void triggerPhoneHangup(Context context, Intent intent) {
    triggerEvent(context, Trigger.HANGUP);
  }

  private void triggerEvent(Context context, int triggerEventCode) {
    triggerEvent(context, triggerEventCode, null);
  }

  private void triggerEvent(Context context, int triggerEventCode, String sourceIdentifier) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerEventCode);
    if (sourceIdentifier != null) {
      broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, sourceIdentifier);
    }
    context.startService(broadcastTriggerServiceIntent);
  }

}
