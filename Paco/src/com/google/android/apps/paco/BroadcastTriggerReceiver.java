package com.google.android.apps.paco;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Browser;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.TriggerDAO;

public class BroadcastTriggerReceiver extends BroadcastReceiver {

  private static final String FREQUENCY = "Frequency";
  public static final String RUNNING_PROCESS_WATCHER_FLAG = "RUNNING_PROCESS_WATCHER";
  private static final String LOGGING_ACTIONS_FLAG = "LOGGING_ACTIONS";

  public static final String PACO_TRIGGER_INTENT = "com.pacoapp.paco.action.PACO_TRIGGER";
  public static final String PACO_ACTION_PAYLOAD = "paco_action_payload";
  private static final Object ANDROID_PLAY_MUSIC_ACTION = "com.android.music.playstatechanged";


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
          } else if (isScreenOff(intent)) {
            stopProcessingService(context);
//            if (BroadcastTriggerReceiver.shouldLogActions(context)) {
//              createBrowserHistoryEndSnapshot(context);
//            }

          }

        } finally {
          wl.release();
        }
      }
    };
    (new Thread(runnable)).start();
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
    
    switch (state) {
    case TelephonyManager.CALL_STATE_RINGING:
      BroadcastTriggerReceiver.setPhoneCallIncoming(context, true);
      BroadcastTriggerReceiver.setLastPhoneState(context, state);
      BroadcastTriggerReceiver.setPhoneCallStartTime(context, new Date());
      break;
      
    case TelephonyManager.CALL_STATE_OFFHOOK:
      BroadcastTriggerReceiver.setLastPhoneState(context, state);
      BroadcastTriggerReceiver.setPhoneCallStartTime(context, new Date());

      if (lastState != TelephonyManager.CALL_STATE_RINGING) {
        triggerOutgoingCallStarted(context, new Date());       
      } else {
        triggerIncomingCallStarted(context, new Date());
      }
      break;
      
    case TelephonyManager.CALL_STATE_IDLE:
      if (lastState == TelephonyManager.CALL_STATE_RINGING) {
        triggerMissedCall(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context));
      } else if (BroadcastTriggerReceiver.getPhoneCallIncoming(context)) {
        triggerIncomingCallEnded(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context), new Date());
      } else {
        triggerOutgoingCallEnded(context, BroadcastTriggerReceiver.getPhoneCallStartTime(context), new Date());
      }
      BroadcastTriggerReceiver.unsetLastPhoneState(context);
      BroadcastTriggerReceiver.unsetPhoneCallIncoming(context);
      BroadcastTriggerReceiver.unsetPhoneCallStartTime(context);

      break;
    }    
  }

  


  private void triggerIncomingCallStarted(Context context, Date callStartTime2) {
    triggerEvent(context, TriggerDAO.PHONE_INCOMING_CALL_STARTED);    
  }

  private void triggerIncomingCallEnded(Context context, Date callStartTime, Date date) {
    triggerEvent(context, TriggerDAO.PHONE_INCOMING_CALL_ENDED, callDuration(callStartTime, date));
  }

  
  private void triggerOutgoingCallStarted(Context context, Date callStartTime) {
    triggerEvent(context, TriggerDAO.PHONE_OUTGOING_CALL_STARTED);    
  }

  private void triggerOutgoingCallEnded(Context context, Date callStartTime, Date date) {
    triggerEvent(context, TriggerDAO.PHONE_OUTGOING_CALL_ENDED, callDuration(callStartTime, date));    
  }

  
  private void triggerMissedCall(Context context, Date callStartTime) {
    triggerEvent(context, TriggerDAO.PHONE_MISSED_CALL);   
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
        triggerEvent(context, TriggerDAO.MUSIC_STARTED);
      } else {
        triggerEvent(context, TriggerDAO.MUSIC_STOPPED);
      }
    }    
  }

  protected void createScreenOnPacoEvents(Context context) {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> experimentsNeedingEvent = initializeExperimentsWatchingAppUsage(experimentProviderUtil);

    for (Experiment experiment : experimentsNeedingEvent) {
      Event event = createScreenOnPacoEvent(experiment);
      experimentProviderUtil.insertEvent(event);
    }


  }

  protected Event createScreenOnPacoEvent(Experiment experiment) {
      Event event = new Event();
      event.setExperimentId(experiment.getId());
      event.setServerExperimentId(experiment.getServerId());
      event.setExperimentName(experiment.getTitle());
      event.setExperimentVersion(experiment.getVersion());
      event.setResponseTime(new DateTime());

      Output responseForInput = new Output();

      responseForInput.setAnswer(new DateTime().toString());
      responseForInput.setName("userPresent");
      event.addResponse(responseForInput);
      return event;
  }

  public static void createBrowserHistoryStartSnapshot(Context context) {
//    List<String> searchHistory = getSearchHistory(context);
//    int browserHistoryItemCount = searchHistory.size();
//    String topItemInBrowserHistory = null;
//    if (browserHistoryItemCount > 0) {
//      topItemInBrowserHistory = searchHistory.get(0);
//    }

    setUserPrefsForBrowserAndSession(context, /*browserHistoryItemCount, topItemInBrowserHistory, */System.currentTimeMillis());
  }

  public static void setUserPrefsForBrowserAndSession(Context context, Long currentTimeMillis) {
//    BroadcastTriggerReceiver.setBrowserHistoryCount(context, browserHistoryItemCount);
//    BroadcastTriggerReceiver.setLastBrowserHistoryItem(context, topItemInBrowserHistory);
    BroadcastTriggerReceiver.setSessionStartMillis(context, currentTimeMillis);
  }

  public static void createBrowserHistoryEndSnapshot(Context context) {
    Long sessionStartMillis = BroadcastTriggerReceiver.getSessionStartMillis(context);
    List<String> newSearchHistory = getSearchHistory(context, sessionStartMillis);
//    int browserHistoryItemCount = searchHistory.size();

//    Integer oldItemCount = BroadcastTriggerReceiver.getBrowserHistoryCount(context);
//    String lastTopItem = BroadcastTriggerReceiver.getLastBrowserHistoryItem(context);
//    Long sessionStartMillis = BroadcastTriggerReceiver.getSessionStartMillis(context);


//    List<String> newSearchHistory = Lists.newArrayList();
//    if (browserHistoryItemCount > oldItemCount) {
//      List<String> newItems = searchHistory.subList(0, browserHistoryItemCount - oldItemCount);
//      for (String newHistoryItem : newItems) {
//        newSearchHistory.add(newHistoryItem);
//      }
//    }

    // reset browser prefs
    setUserPrefsForBrowserAndSession(context, /*0, null,*/ null);

    if (newSearchHistory.isEmpty()) {
      return;
    }
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Experiment> experimentsNeedingEvent = initializeExperimentsWatchingAppUsage(experimentProviderUtil);

    String usedAppsString = Joiner.on(",").join(newSearchHistory);
    for (Experiment experiment : experimentsNeedingEvent) {
      Event event = createSitesVisitedPacoEvent(usedAppsString, experiment, sessionStartMillis);
      experimentProviderUtil.insertEvent(event);
    }

  }

  private static List<Experiment> initializeExperimentsWatchingAppUsage(ExperimentProviderUtil experimentProviderUtil) {
    List<Experiment> joined = experimentProviderUtil.getJoinedExperiments();
    List<Experiment> experimentsNeedingEvent = Lists.newArrayList();
    DateTime now = DateTime.now();
    for (Experiment experiment2 : joined) {
      if (!experiment2.isOver(now) && experiment2.isLogActions()) {
        experimentsNeedingEvent.add(experiment2);
      }
    }
    return experimentsNeedingEvent;
  }


  public static List<String> getSearchHistory(Context context, long startTimeMillis) {
    List<String> results = Lists.newArrayList();
    if (startTimeMillis == 0) {
      return results;
    }
    String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE };
    String sel = /*Browser.BookmarkColumns.BOOKMARK + " = 0 & " +*/ Browser.BookmarkColumns.DATE + " > ?"; // 0 = history, 1 = bookmark
    String[] selArgs = new String[] { String.valueOf(startTimeMillis) };
    Cursor mCur = null;
    try {
      mCur = context.getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, selArgs, Browser.BookmarkColumns.DATE + " ASC");
      mCur.moveToFirst();

      String title = "";

      String url = "";

      String ts = "";
      if (mCur.moveToFirst() && mCur.getCount() > 0) {
          boolean cont = true;
          while (mCur.isAfterLast() == false && cont) {
              title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
              url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
              ts = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.DATE));
              if (ts != null) {
                ts = new DateTime(Long.parseLong(ts)).toString();
              }
              results.add( ts + " _ " + title.replaceAll("_",  " ").replaceAll(", ", " ") + " _ " + url );
              mCur.moveToNext();
          }
      }
      return results;
    } finally {
      if (mCur != null) {
        mCur.close();
      }
    }
  }

  public static Event createSitesVisitedPacoEvent(String usedAppsString, Experiment experiment, long startTime) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    event.setExperimentVersion(experiment.getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();

    responseForInput.setAnswer(usedAppsString);
    responseForInput.setName("sites_visited");
    event.addResponse(responseForInput);

    Output responseForInputSessionDuration = new Output();
    long sessionDuration = (System.currentTimeMillis() - startTime) / 1000;
    responseForInputSessionDuration.setAnswer(Long.toString(sessionDuration));
    responseForInputSessionDuration.setName("session_duration");

    event.addResponse(responseForInputSessionDuration);
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
    Log.i(PacoConstants.TAG, "Starting App Usage poller");
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, true);
    Intent intent = new Intent(context, ProcessService.class);
    context.startService(intent);
  }

  public static void stopProcessingService(Context context) {
    Log.i(PacoConstants.TAG, "Stopping App Usage poller");
    BroadcastTriggerReceiver.toggleWatchRunningProcesses(context, false);
  }

  public static void initPollingAndLoggingPreference(Context context) {
    boolean shouldWatchProcesses = false;
    boolean shouldLogActions = false;

    ExperimentProviderUtil eu = new ExperimentProviderUtil(context);
    DateTime now = new DateTime();
    List<Experiment> joined = eu.getJoinedExperiments();
    for (Experiment experiment : joined) {
      if (!experiment.isOver(now)) {
       if (experiment.shouldWatchProcesses()) {
        shouldWatchProcesses = true;
       }
       if (experiment.isLogActions()) {
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

  public static boolean isInBrowser(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getBoolean("inBrowserTask", false);
  }

  public static void toggleInBrowser(Context context, boolean running) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    prefs.edit().putBoolean("inBrowserTask", running).commit();
  }


  public static String getLastBrowserHistoryItem(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getString("LastBrowserHistoryItem", null);
  }

  public static void setLastBrowserHistoryItem(Context context, String lastItem) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    if (lastItem == null) {
      editor.remove("LastBrowserHistoryItem");
    } else {
      editor.putString("LastBrowserHistoryItem", lastItem);
    }
    editor.commit();
  }

  public static Integer getBrowserHistoryCount(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getInt("browserHistorySize", 0);
  }

  public static void setBrowserHistoryCount(Context context, Integer count) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    if (count == null) {
      editor.remove("browserHistorySize");
    } else {
      editor.putInt("browserHistorySize", count);
    }
    editor.commit();
  }

  public static Long getSessionStartMillis(Context context) {
    return context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE).getLong("sessionStartMillis", 0);
  }

  public static void setSessionStartMillis(Context context, Long sessionStartMillis) {
    SharedPreferences prefs = context.getSharedPreferences("PacoProcessWatcher", Context.MODE_PRIVATE);
    Editor editor = prefs.edit();
    if (sessionStartMillis == null) {
      editor.remove("sessionStartMillis");
    } else {
    editor.putLong("sessionStartMillis", sessionStartMillis);
    }
    editor.commit();
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
      triggerEvent(context, TriggerDAO.PACO_ACTION_EVENT, sourceIdentifier, intent.getExtras());
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
    Log.i(PacoConstants.TAG, "User present trigger");
    triggerEvent(context, TriggerDAO.USER_PRESENT);
  }

  private void triggerPhoneHangup(Context context, Intent intent) {
    triggerEvent(context, TriggerDAO.HANGUP);
  }

  private void triggerEvent(Context context, int triggerEventCode) {
    triggerEvent(context, triggerEventCode, null, null);
  }

  private void triggerEvent(Context context, int triggerEventCode, String sourceIdentifier, Bundle payload) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerEventCode);
    if (payload == null) {
      payload = new Bundle();
    }
    broadcastTriggerServiceIntent.putExtra(PACO_ACTION_PAYLOAD, payload);
    if (sourceIdentifier != null) {
      broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, sourceIdentifier);
    }
    context.startService(broadcastTriggerServiceIntent);
  }
  
  private void triggerEvent(Context context, int triggerEventCodeForPhoneState, long callDuration) {
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
