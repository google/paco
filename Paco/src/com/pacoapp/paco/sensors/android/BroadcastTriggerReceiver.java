package com.pacoapp.paco.sensors.android;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.sensors.android.procmon.LollipopProcessMonitorService;
import com.pacoapp.paco.sensors.android.procmon.ProcessService;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

public class BroadcastTriggerReceiver extends BroadcastReceiver {


  private static final String PHONE_ON_EVENT_KEY = "phoneOn";
  public static final String EXPERIMENT_SERVER_ID_EXTRA_KEY = "experimentServerId";
  private static final String FREQUENCY = "Frequency";
  public static final String RUNNING_PROCESS_WATCHER_FLAG = "RUNNING_PROCESS_WATCHER";
  private static final String LOGGING_ACTIONS_FLAG = "LOGGING_ACTIONS";

  public static final String PACO_TRIGGER_INTENT = "com.pacoapp.paco.action.PACO_TRIGGER";
  public static final String PACO_ACTION_PAYLOAD = "paco_action_payload";
  public static final String TRIGGER_TYPE = "triggerType";

  public static final String PACO_EXPERIMENT_JOINED_ACTION =  "com.pacoapp.paco.action.PACO_EXPERIMENT_JOINED_ACTION";
  public static final String PACO_EXPERIMENT_ENDED_ACTION = "com.pacoapp.paco.action.PACO_EXPERIMENT_ENDED_ACTION";
  public static final String PACO_EXPERIMENT_RESPONSE_RECEIVED_ACTION = "com.pacoapp.paco.action.PACO_EXPERIMENT_RESPONSE_RECEIVED_ACTION";

  private static final String ANDROID_PLAY_MUSIC_ACTION = "com.android.music.playstatechanged";

  private static Logger Log = LoggerFactory.getLogger(BroadcastTriggerReceiver.class);


	@Override
  public void onReceive(final Context context, final Intent intent) {
    if (isPhoneRelated(context, intent)) {
      processPhoneStateTriggers(context, intent);
    } else if (isPhoneHangup(intent)) {
      triggerPhoneHangup(context, intent);
    } else if (isUserPresent(intent)) {
      triggerUserPresent(context, intent);
    } else if (intent.getAction().equals(PACO_TRIGGER_INTENT)) {
      triggerPacoTriggerReceived(context, intent);
    } else if (intent.getAction().equals(ANDROID_PLAY_MUSIC_ACTION)) {
      triggerMusicStateAction(context, intent);
    } else if (intent.getAction().equals(PACO_EXPERIMENT_JOINED_ACTION)) {
      triggerPacoExperimentJoinEvent(context ,intent);
    } else if (intent.getAction().equals(PACO_EXPERIMENT_ENDED_ACTION)) {
      triggerPacoExperimentEndedEvent(context ,intent);
    } else if (intent.getAction().equals(PACO_EXPERIMENT_RESPONSE_RECEIVED_ACTION)) {
      triggerPacoExperimentResponseReceivedEvent(context ,intent);
    } else if (isPackageRemoved(context, intent)) {
      triggerPackageRemovedEvent(context, intent);
    } else if (isPackageAdded(context, intent)) {
      triggerPackageAddedEvent(context, intent);
    }

    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                    "Paco BroadcastTriggerService wakelock");
    wl.acquire();

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          // handle polling events for screen on actions (app usage)
          initPollingAndLoggingPreference(context);
          boolean shouldPoll = BroadcastTriggerReceiver.shouldWatchProcesses(context);
          if (isUserPresent(intent) && shouldPoll) {
            createScreenOnPacoEvents(context);
            startProcessService(context);
          } else if (isScreenOn(intent) && !isKeyGuardOn(context) && shouldPoll) {
            createScreenOnPacoEvents(context);
            startProcessService(context);
          } /*else if (isScreenOff(intent)) {
            stopProcessService(context);
            createScreenOffPacoEvents(context);
          }*/ // Android never fires the screen off intent.
          // Instead we detect screen activity in the process monitor
          if (isPhoneShutdown(context, intent)) {
            createPhoneStateLogEvents(context, "false");
          }

        } finally {
          wl.release();
        }
      }
    };
    (new Thread(runnable)).start();
  }

	public static void createPhoneStateLogEvents(Context context, String phoneOnState) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();
    for (Experiment experiment : joined) {
      final ExperimentDAO experimentDAO = experiment.getExperimentDAO();
      List<ExperimentGroup> groups = experimentDAO.getGroups();
      for (ExperimentGroup experimentGroup : groups) {
        if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
          continue;
        } else { 
          if (ActionScheduleGenerator.isExperimentGroupRunning(experimentGroup) && experimentGroup.getLogShutdown()) {
            Event event = createPhoneShutdownPacoEvent(experiment, experimentGroup.getName(), phoneOnState);
            experimentProviderUtil.insertEvent(event);
          }
        }
      }
    }
  }

  protected static Event createPhoneShutdownPacoEvent(Experiment experiment, String experimentGroupName, String phoneOnState) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentGroupName(experimentGroupName);
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer(phoneOnState);
    responseForInput.setName(PHONE_ON_EVENT_KEY);
    event.addResponse(responseForInput);
    return event;
  }

  private boolean isPhoneShutdown(Context context, Intent intent) {
	  return intent.getAction().equals(Intent.ACTION_SHUTDOWN);
  }

  /**
   * Broadcasts an intent destined for the BroadcastTriggerService containing
   * the package name and time of the event as extra data.
   * This method is called by the onReceive() method when it received an
   * ACTION_PACKAGE_REMOVED broadcast.
   * @param context The Android app context
   * @param intent The received broadcast intent
   */
  private void triggerPackageRemovedEvent(Context context, Intent intent) {
    Log.info("App removed trigger");

    triggerPackageEvent(context, intent, InterruptCue.APP_REMOVED);
  }

  /**
   * Broadcasts an intent destined for the BroadcastTriggerService containing
   * the package name and time of the event as extra data.
   * This method is called by the onReceive() method when it received an
   * ACTION_PACKAGE_ADDED broadcast.
   * @param context The Android app context
   * @param intent The received broadcast intent
   */
  private void triggerPackageAddedEvent(Context context, Intent intent) {
    Log.info("App installed trigger");

    triggerPackageEvent(context, intent, InterruptCue.APP_ADDED);
    // Make sure that this new app is in the cache too by caching in the background
    (new AndroidInstalledApplications(context)).cacheApplicationNames();
  }

  private void triggerPackageEvent(Context context, Intent intent, int type) {
    Uri data = intent.getData();
    String packageName = data.getEncodedSchemeSpecificPart();
    AndroidInstalledApplications androidInstalledApplications = new AndroidInstalledApplications(context);
    String appName = androidInstalledApplications.getApplicationName(packageName);
    Bundle payload = new Bundle();
    payload.putString(AndroidInstalledApplications.PACKAGE_NAME, packageName);
    payload.putString(AndroidInstalledApplications.APP_NAME, appName);
    // Cue event names are off by one.
    payload.putString(TRIGGER_TYPE, InterruptCue.CUE_EVENT_NAMES[type-1]);

    if (!packageName.equals("com.pacoapp.paco")) {
      triggerEvent(context, type, packageName, payload);
    }

  }

  /**
   * Helper function for isPackageRemoved and isPackageAdded, checking whether the package removal/
   * installation is actually part of an update (i.e. if a removal will be / was followed by an
   * installation for the same package
   * @param intent The ACTION_PACKAGE_REMOVED or ACTION_PACKAGE_ADDED event
   * @return Whether this event is part of an update
   */
  private boolean isPackageUpdate(Intent intent) {
    // If EXTRA_REPLACING is not present (or if it is present but false), return false.
    return intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
  }

  private boolean isPackageRemoved(Context context, Intent intent) {
    return (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) && !isPackageUpdate(intent));
  }

  /**
   * Checks whether an intent contains information about a *new* app being
   * installed. Updates of existing packages are not considered as new installs.
   * @param context The Android app context
   * @param intent The received broadcast intent
   * @return Whether the intent shows a new package was installed
   */
  private boolean isPackageAdded(Context context, Intent intent) {
    return (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && !isPackageUpdate(intent));
  }

  private void triggerPacoExperimentEndedEvent(Context context, Intent intent) {
    long experimentServerId = intent.getLongExtra(EXPERIMENT_SERVER_ID_EXTRA_KEY, -10l);
    if (experimentServerId == -10l) {
      Log.debug("No experimentServerId specified for PACO_EXPERIMENT_ENDED_ACTION");
      return;
    }
    String experimentServerIdString = Long.toString(experimentServerId);
    triggerEvent(context, InterruptCue.PACO_EXPERIMENT_ENDED_EVENT, experimentServerIdString, intent.getExtras());
  }

  private void triggerPacoExperimentJoinEvent(Context context, Intent intent) {
    long experimentServerId = intent.getLongExtra(EXPERIMENT_SERVER_ID_EXTRA_KEY, -10l);
    if (experimentServerId == -10l) {
      Log.debug("No experimentServerId specified for PACO_EXPERIMENT_JOINED_ACTION");
      return;
    }
    String experimentServerIdString = Long.toString(experimentServerId);
    triggerEvent(context, InterruptCue.PACO_EXPERIMENT_JOINED_EVENT, experimentServerIdString, intent.getExtras());
  }

  private void triggerPacoExperimentResponseReceivedEvent(Context context, Intent intent) {
    long experimentServerId = intent.getLongExtra(EXPERIMENT_SERVER_ID_EXTRA_KEY, -10l);
    if (experimentServerId == -10l) {
      Log.debug("No experimentServerId specified for PACO_EXPERIMENT_RESPONSE_RECEIVED_ACTION");
      return;
    }
    String experimentServerIdString = Long.toString(experimentServerId);
    triggerEvent(context, InterruptCue.PACO_EXPERIMENT_RESPONSE_RECEIVED_EVENT, experimentServerIdString, intent.getExtras());

  }

  /**
	 * This is a modified version of code by Gabe Sechen on StackOverflow:
	 * http://stackoverflow.com/questions/15563921/detecting-an-incoming-call-coming-to-an-android-device
	 *
	 * @param context
	 * @param intent
	 */
  private void processPhoneStateTriggers(Context context, Intent intent) {
    String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
    int state = 0;
    if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
      state = TelephonyManager.CALL_STATE_IDLE;
    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
      state = TelephonyManager.CALL_STATE_OFFHOOK;
    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
      state = TelephonyManager.CALL_STATE_RINGING;
    }

    onCallStateChanged(context, state);
  }

  // Incoming call- goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
  // Outgoing call- goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
  public void onCallStateChanged(Context context, int state) {
    int lastState = BroadcastTriggerReceiver.getLastPhoneState(context);
    if (lastState == state) {
      return;
    }

    final Date callStartTime = new Date();
    switch (state) {
    case TelephonyManager.CALL_STATE_RINGING:
      BroadcastTriggerReceiver.setPhoneCallIncoming(context, true);
      BroadcastTriggerReceiver.setLastPhoneState(context, state);
      BroadcastTriggerReceiver.setPhoneCallStartTime(context, callStartTime);
      break;

    case TelephonyManager.CALL_STATE_OFFHOOK:
      BroadcastTriggerReceiver.setLastPhoneState(context, state);
      BroadcastTriggerReceiver.setPhoneCallStartTime(context, callStartTime);

      if (lastState != TelephonyManager.CALL_STATE_RINGING) {
        triggerOutgoingCallStarted(context, callStartTime);
      } else {
        triggerIncomingCallStarted(context, callStartTime);
      }
      triggerCallStarted(context, callStartTime);
      break;

    case TelephonyManager.CALL_STATE_IDLE:
      if (lastState == TelephonyManager.CALL_STATE_RINGING) {
        triggerMissedCall(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context));
      } else if (BroadcastTriggerReceiver.getPhoneCallIncoming(context)) {
        triggerIncomingCallEnded(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context), callStartTime);
      } else {
        triggerOutgoingCallEnded(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context), callStartTime);
      }
      triggerCallEnded(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context), callStartTime);
      BroadcastTriggerReceiver.unsetLastPhoneState(context);
      BroadcastTriggerReceiver.unsetPhoneCallIncoming(context);
      BroadcastTriggerReceiver.unsetPhoneCallStartTime(context);

      break;
    }
  }




  private void triggerCallEnded(Context context, Date phoneCallStartTime, Date date) {
      triggerPhoneEndEvent(context, InterruptCue.PHONE_CALL_ENDED, callDuration(phoneCallStartTime, date));
  }

  private void triggerCallStarted(Context context, Date callStartTime) {
    triggerEvent(context, InterruptCue.PHONE_CALL_STARTED);
  }

  private void triggerIncomingCallStarted(Context context, Date callStartTime2) {
    triggerEvent(context, InterruptCue.PHONE_INCOMING_CALL_STARTED);
  }

  private void triggerIncomingCallEnded(Context context, Date callStartTime, Date date) {
    triggerPhoneEndEvent(context, InterruptCue.PHONE_INCOMING_CALL_ENDED, callDuration(callStartTime, date));
  }


  private void triggerOutgoingCallStarted(Context context, Date callStartTime) {
    triggerEvent(context, InterruptCue.PHONE_OUTGOING_CALL_STARTED);
  }

  private void triggerOutgoingCallEnded(Context context, Date callStartTime, Date date) {
    triggerPhoneEndEvent(context, InterruptCue.PHONE_OUTGOING_CALL_ENDED, callDuration(callStartTime, date));
  }


  private void triggerMissedCall(Context context, Date callStartTime) {
    triggerEvent(context, InterruptCue.PHONE_MISSED_CALL);
  }

  private long callDuration(Date callStartTime, Date date) {
    if (callStartTime != null && date != null) {
      return date.getTime() - callStartTime.getTime();
    }
    return 0;
  }


  private void triggerMusicStateAction(Context context, Intent intent) {
    if(intent.hasExtra("playing")) {
      boolean playing = intent.getBooleanExtra("playing", false);
      if (playing) {
        triggerEvent(context, InterruptCue.MUSIC_STARTED);
      } else {
        triggerEvent(context, InterruptCue.MUSIC_STOPPED);
      }
    }
  }

  protected void createScreenOnPacoEvents(Context context) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();
    for (Experiment experiment : joined) {
      List<ExperimentGroup> groupsThatCare = ExperimentHelper.getGroupsThatCareAboutActionLogging(experiment.getExperimentDAO());
      for (ExperimentGroup experimentGroup : groupsThatCare) {
        Event event = createScreenOnPacoEvent(experiment, experimentGroup);
        experimentProviderUtil.insertEvent(event);
      }
    }
  }

  protected Event createScreenOnPacoEvent(Experiment experiment, ExperimentGroup experimentGroup) {
      Event event = new Event();
      event.setExperimentId(experiment.getId());
      event.setServerExperimentId(experiment.getServerId());
      event.setExperimentName(experiment.getExperimentDAO().getTitle());
      event.setExperimentGroupName(experimentGroup.getName());
      event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
      event.setResponseTime(new DateTime());

      Output responseForInput = new Output();

      responseForInput.setAnswer(new DateTime().toString());
      responseForInput.setName("userPresent");
      event.addResponse(responseForInput);
      return event;
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


  public static void startProcessService(Context context) {
    Log.info("Starting App Usage poller");
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, true);
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Intent intent = new Intent(context, LollipopProcessMonitorService.class);
      context.startService(intent);
    } else {
      Intent intent = new Intent(context, ProcessService.class);
      context.startService(intent);
    }
  }

  public static void stopProcessService(Context context) {
    Log.info("Stopping App Usage poller");
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, false);
  }

  public static void initPollingAndLoggingPreference(Context context) {
    boolean shouldWatchProcesses = false;
    boolean shouldLogActions = false;

    ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
    DateTime now = new DateTime();
    List<Experiment> joined = eu.getJoinedExperiments();
    for (Experiment experiment : joined) {
      if (!ActionScheduleGenerator.isOver(now, experiment.getExperimentDAO())) {
       if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
        shouldWatchProcesses = true;
       }
       if (ExperimentHelper.isLogActions(experiment.getExperimentDAO())) {
         shouldLogActions = true;
       }
      }
    }
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, shouldWatchProcesses);
    BroadcastTriggerReceiver.toggleLogActions(context, shouldLogActions);
  }

  public static boolean shouldWatchProcesses(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getBoolean(RUNNING_PROCESS_WATCHER_FLAG, false);
  }

  public static void toggleWatchRunningProcesses(Context context, boolean running) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putBoolean(RUNNING_PROCESS_WATCHER_FLAG, running).commit();
  }

  public static boolean shouldLogActions(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getBoolean(LOGGING_ACTIONS_FLAG, false);
  }

  public static void toggleLogActions(Context context, boolean running) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putBoolean(LOGGING_ACTIONS_FLAG, running).commit();
  }

  public static void setFrequency(Context context, int freq) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putInt(FREQUENCY, freq).commit();
  }

  public static int getFrequency(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getInt(FREQUENCY, 4);
  }

  private void triggerPacoTriggerReceived(Context context, Intent intent) {
    String sourceIdentifier = intent.getStringExtra("sourceIdentifier");
    if (sourceIdentifier == null || sourceIdentifier.length() == 0) {
      Log.debug("No source identifier specified for PACO_TRIGGER");
    } else {
      triggerEvent(context, InterruptCue.PACO_ACTION_EVENT, sourceIdentifier, intent.getExtras());
    }
  }

  private boolean isPhoneRelated(Context context, Intent intent) {
    if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
      return true;
    }
    return false;
  }


  private boolean isPhoneHangup(Intent intent) {
    String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    return telephonyExtraState != null && telephonyExtraState.equals(TelephonyManager.EXTRA_STATE_IDLE);
  }

  private void triggerUserPresent(Context context, Intent intent) {
    Log.info("User present trigger");
    triggerEvent(context, InterruptCue.USER_PRESENT);
  }

  private void triggerPhoneHangup(Context context, Intent intent) {
    triggerEvent(context, InterruptCue.PHONE_HANGUP);
  }

  private void triggerEvent(Context context, int triggerEventCode) {
    triggerEvent(context, triggerEventCode, null, null);
  }

  private void triggerEvent(Context context, int triggerEventCode, String sourceIdentifier, Bundle payload) {
    final String extraKey = Experiment.TRIGGER_SOURCE_IDENTIFIER;

    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerEventCode);
    if (payload == null) {
      payload = new Bundle();
    }
    broadcastTriggerServiceIntent.putExtra(PACO_ACTION_PAYLOAD, payload);
    if (sourceIdentifier != null) {

      broadcastTriggerServiceIntent.putExtra(extraKey, sourceIdentifier);
    }
    context.startService(broadcastTriggerServiceIntent);
  }

  private void triggerPhoneEndEvent(Context context, int triggerEventCodeForPhoneState, long callDuration) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerEventCodeForPhoneState);

    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_PHONE_CALL_DURATION, callDuration);
    context.startService(broadcastTriggerServiceIntent);
  }


  public static void setAppToWatchStarted(Context context, String newTask) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putString("ClosedAppTriggerStarted", newTask);
    editor.commit();
  }

  public static String getAppToWatch(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    return prefs.getString("ClosedAppTriggerStarted", null);
  }


  public static void unsetAppToWatchStarted(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.remove("ClosedAppTriggerStarted");
    editor.commit();
  }

  private static int getLastPhoneState(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    return prefs.getInt("LastPhoneState", -1);
  }

  private static void setLastPhoneState(Context context, int state) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putInt("LastPhoneState", state);
    editor.commit();
  }

  private static void unsetLastPhoneState(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.remove("LastPhoneState");
    editor.commit();
  }

  private static void setPhoneCallIncoming(Context context, boolean b) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putBoolean("PhoneCallIncoming", b);
    editor.commit();
  }

  private static boolean getPhoneCallIncoming(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    return prefs.getBoolean("PhoneCallIncoming", false);
  }

  private static void unsetPhoneCallIncoming(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.remove("PhoneCallIncoming");
    editor.commit();
  }

  private static void setPhoneCallStartTime(Context context, Date callStartTime) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.putLong("PhoneCallStartTime", callStartTime.getTime());
    editor.commit();
  }

  private static Date getPhoneCallStartTime(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    long timeMillis = prefs.getLong("PhoneCallStartTime", 0);
    if (timeMillis != 0) {
      return new Date(timeMillis);
    }
    return null;
  }

  private static void unsetPhoneCallStartTime(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    editor.remove("PhoneCallStartTime");
    editor.commit();

  }



}
