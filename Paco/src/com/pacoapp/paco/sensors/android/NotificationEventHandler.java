package com.pacoapp.paco.sensors.android;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;
import com.pacoapp.paco.shared.model2.InterruptCue;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

public class NotificationEventHandler {

  private static final String NOTIFICATION_SHADE_ENGLISH = "Notification shade.";
  private static final String CLEAR_ALL_NOTIFICATIONS_ENGLISH = "Clear all notifications.";
  private static final String NOTIFICATION_SWIPE_DISMISSED_ENGLISH = "Notification dismissed.";

  private static final String NOTIFICATION_SHADE_DEUTSCH = "Benachrichtigungsleiste";
  private static final String NOTIFICATION_SWIPE_DISMISSED_DEUTSCH = "Benachrichtigung geschlossen";
  private static final String CLEAR_ALL_NOTIFICATIONS_DEUTSCH = "Clear all notifications."; // "Alle Benachrichtigungen löschen";

  private static final String NOTIFICATION_SHADE_FRANCAIS = "Volet des notifications";
  private static final String NOTIFICATION_SWIPE_DISMISSED_FRANCAIS = "Notification masquée";
  private static final String CLEAR_ALL_NOTIFICATIONS_FRANCAIS = "Clear all notifications."; //"Supprimer toutes les notifications";

  private static final int MAX_BROADCASTS_PER_DAY = 8;
  private static final int MIN_BUFFER_BETWEEN_BROADCASTS = 60;
  private static final String MY_PREFERENCES_NAME = "my_preferences_name";
  private static final String PACO_APP_PKG_NAME = "com.pacoapp.paco";
  private static final String SRC_ID_PREFIX = "com.pacoapp.notificationlistener:";
  private static final String NOTIFICATION_CREATED_SRC_ID = "notification_created";
  private static final String NOTIFICATION_TRAY_OPENED_SRC_ID = "notification_tray_opened";
  private static final String NOTIFICATION_TRAY_CLEAR_ALL_SRC_ID = "notification_tray_clear_all";
  private static final String NOTIFICATION_TRAY_SWIPE_DISMISS_SRC_ID = "notification_tray_swipe_dismiss";
  private static final String NOTIFICATION_TRAY_CANCELLED_SRC_ID = "notification_tray_cancelled";
  private static final String NOTIFICATION_CLICKED_SRC_ID = "notification_clicked";

  static final String TAG = "NotificationEventHandler";



  private boolean trayOpen;
//  private Random random = new Random();
  private Context context;

  private static Map<String, String> languageSpecificEventMappings = Maps.newHashMap();

  static {
    // English mappings
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_SWIPE_DISMISS_SRC_ID + "_English", NOTIFICATION_SWIPE_DISMISSED_ENGLISH);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_CLEAR_ALL_SRC_ID + "_English", CLEAR_ALL_NOTIFICATIONS_ENGLISH);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_OPENED_SRC_ID + "_English", NOTIFICATION_SHADE_ENGLISH);
    // Deutsch mappings
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_SWIPE_DISMISS_SRC_ID + "_Deutsch", NOTIFICATION_SWIPE_DISMISSED_DEUTSCH);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_CLEAR_ALL_SRC_ID + "_Deutsch", CLEAR_ALL_NOTIFICATIONS_DEUTSCH);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_OPENED_SRC_ID + "_Deutsch", NOTIFICATION_SHADE_DEUTSCH);
    // French
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_SWIPE_DISMISS_SRC_ID + "_français", NOTIFICATION_SWIPE_DISMISSED_FRANCAIS);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_CLEAR_ALL_SRC_ID + "_français", CLEAR_ALL_NOTIFICATIONS_FRANCAIS);
    languageSpecificEventMappings.put(NOTIFICATION_TRAY_OPENED_SRC_ID + "_français", NOTIFICATION_SHADE_FRANCAIS);
  };

  public NotificationEventHandler(Context applicationContext) {
    this.context = context;
  }

  private String getEventType(AccessibilityEvent event) {
    return AccessibilityEvent.eventTypeToString(event.getEventType());
  }

  private String getEventText(AccessibilityEvent event) {
    StringBuilder sb = new StringBuilder();
    for (CharSequence s : event.getText()) {
      sb.append(s);
    }
    return sb.toString();
  }

  //@Override
  public Integer handleAccessibilityEvent(AccessibilityEvent event) {
//    Log.v(TAG, String.format("ev:[t] %s [c] %s [p] %s [tm] %s [tx] %s [cd]",
//        getEventType(event), event.getClassName(), event.getPackageName(),
//        event.getEventTime(), getEventText(event), event.getContentDescription()));

    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED &&
        event.getClassName().equals("android.app.Notification")) {

      if (!event.getPackageName().equals(PACO_APP_PKG_NAME)) {
        return fireNotificationCreatedPacoEvent();
      }
    } else if (isNotificationShadeOpenEvent(event)) {
      trayOpen = true;
      return fireNotificationTrayOpenedPacoEvent();
    } else if (trayOpen && event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED && isClearAllNotificationsEvent(event)) {
      trayOpen = false;
      return fireNotificationTrayClearAllPacoEvent();
    } else if (trayOpen && isNotificationDismissedEvent(event)) {
      //trayOpen = false;
      return fireNotificationTraySwipeDismissPacoEvent();
    } else if (trayOpen && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getContentDescription() == null) {
      trayOpen = false;
      return fireNotificationTrayCancelledPacoEvent();
    } else if (trayOpen && event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED && event.getClassName().equals("android.widget.FrameLayout")
        && event.getPackageName().equals("com.android.systemui")) {
      trayOpen = false;
      return fireNotificationClickedPacoEvent();
    }
    return null;
  }

  private boolean isNotificationDismissedEvent(AccessibilityEvent event) {
    return getEventText(event) != null && getEventText(event).equals(getLanguageSpecificNotificationSwipeDismissText());
  }

  private boolean isClearAllNotificationsEvent(AccessibilityEvent event) {
    return (event.getContentDescription() != null && event.getContentDescription().equals(getLanguageSpecificClearAllNotificationsText()))
        || getEventText(event) != null && getEventText(event).equals(getLanguageSpecificClearAllNotificationsText());
  }

  private boolean isNotificationShadeOpenEvent(AccessibilityEvent event) {
    return (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_ANNOUNCEMENT) &&
        (event.getContentDescription() != null && event.getContentDescription().equals(getLanguageSpecificNotificationShadeText()))
        || getEventText(event) != null && getEventText(event).equals(getLanguageSpecificNotificationShadeText());
  }

  private String getLanguageSpecificNotificationSwipeDismissText() {
    return getLanguageSpecificMappingForKey(NOTIFICATION_TRAY_SWIPE_DISMISS_SRC_ID);
  }

  private String getLanguageSpecificClearAllNotificationsText() {
    return getLanguageSpecificMappingForKey(NOTIFICATION_TRAY_CLEAR_ALL_SRC_ID);
  }

  private String getLanguageSpecificNotificationShadeText() {
    return getLanguageSpecificMappingForKey(NOTIFICATION_TRAY_OPENED_SRC_ID);
  }

  private String getLanguageSpecificMappingForKey(String key) {
    return languageSpecificEventMappings.get(key + "_" + Locale.getDefault().getDisplayLanguage());
  }

//  private void logEvent(AccessibilityEvent event, String eventType) {
//    String msg = String.format("%s,%s,%s,\"%s\"", eventType, formatTime(event.getEventTime()), event.getPackageName(), getEventText(event));
//    logMessage(msg);
//  }

//  private void logMessage(String msg) {
//    Log.v(TAG, msg);
//    FileOutputStream out = null;
//    BufferedWriter w = null;
//    try {
//      out = openFileOutput(MainActivity.LOG_FILENAME, MODE_PRIVATE | MODE_APPEND);
//      w = new BufferedWriter(new OutputStreamWriter(out));
//      w.write(msg);
//      w.newLine();
//      w.flush();
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//      if (w != null) {
//        try {
//          w.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//    }
//  }

  private int fireNotificationClickedPacoEvent() {
    return InterruptCue.NOTIFICATION_CLICKED;
  }

  private int fireNotificationTrayCancelledPacoEvent() {
    return InterruptCue.NOTIFICATION_TRAY_CANCELLED;
  }

  private int fireNotificationTraySwipeDismissPacoEvent() {
    return InterruptCue.NOTIFICATION_TRAY_SWIPE_DISMISS;
  }

  private int fireNotificationTrayClearAllPacoEvent() {
    return InterruptCue.NOTIFICATION_TRAY_CLEAR_ALL;
  }

  private int fireNotificationTrayOpenedPacoEvent() {
     return InterruptCue.NOTIFICATION_TRAY_OPENED;
  }

  private int fireNotificationCreatedPacoEvent() {
     return InterruptCue.NOTIFICATION_CREATED;
  }

//  private void notifyPaco(String srcId, AccessibilityEvent event) {
////    if (shouldBroadcastToPaco()) {
//      Log.d(TAG, "sending Paco broadcast");
//      Intent i = new Intent("com.pacoapp.paco.action.PACO_TRIGGER");
//      // don't do per-event srcIds for the time being.
//      i.putExtra("sourceIdentifier", srcId);
//
////      incrementTodaysSignals();
////      updateLastPingTime();
//      context.sendBroadcast(i);
////    } else {
////      Log.d(TAG, "NOT sending Paco broadcast");
////    }
//  }

//  private void incrementTodaysSignals() {
//    SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES_NAME, Context.MODE_PRIVATE);
//    String todaysKey = new DateMidnight().toString(df);
//    int current = prefs.getInt(todaysKey, 0);
//    prefs.edit().clear().putInt(todaysKey, current + 1).commit();
//  }
//
//  private boolean shouldBroadcastToPaco() {
//    return minimumTimePassedSinceLastBroadcast() && !dailyLimitHit() && random.nextBoolean();
//  }
//
//  private void updateLastPingTime() {
//    SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES_NAME, Context.MODE_PRIVATE);
//    prefs.edit().putLong("LAST_PING", new DateTime().getMillis()).commit();
//  }
//
//  private boolean minimumTimePassedSinceLastBroadcast() {
//    SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES_NAME, Context.MODE_PRIVATE);
//    long lastPingLong = prefs.getLong("LAST_PING", 0);
//    return lastPingLong == 0 || Minutes.minutesBetween(new DateTime(lastPingLong), DateTime.now()).getMinutes() > MIN_BUFFER_BETWEEN_BROADCASTS;
//  }
//
//  private boolean dailyLimitHit() {
//    SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES_NAME, Context.MODE_PRIVATE);
//    boolean b = prefs.getInt(new DateMidnight().toString(df), 0) >= MAX_BROADCASTS_PER_DAY;
//    return b;
//  }

  private String formatTime(long l) {
    DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.MEDIUM);
    StringBuilder buf = new StringBuilder();
    buf.append(df.format(new Date()));
    return buf.toString();
  }

//  @Override
//  public void onInterrupt() {
//    Log.v(TAG, "onInterrupt");
//  }
//
//  @Override
//  protected void onServiceConnected() {
//    super.onServiceConnected();
//    Log.v(TAG, "onServiceConnected");
//    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//    // info.flags = AccessibilityServiceInfo.DEFAULT;
//    info.flags = AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT;
//    info.eventTypes =
//    // AccessibilityEvent.TYPE_VIEW_CLICKED
//    // | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//    // | AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
//    // | AccessibilityEvent.TYPE_ANNOUNCEMENT;
//    info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//    info.notificationTimeout = 100;
//    setServiceInfo(info);
//
//  }

}
