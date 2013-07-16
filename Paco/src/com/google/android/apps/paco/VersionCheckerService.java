package com.google.android.apps.paco;


import org.joda.time.DateTime;

import com.pacoapp.paco.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class VersionCheckerService extends Service {

  private AlarmManager alarmManager;
  private static final String RESULTS_SERVICE_TAG = "ResultsService";

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
  }


  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    // make this do nothing since we are now on the play store.
    stopSelf();
//    Runnable runnable = new Runnable() {
//      public void run() {
//        if (VersionChecker.checkForUpdate(VersionCheckerService.this)) {
//          createUpdateNotification();
//        }
//        createAlarmForNextUpdate();
//        stopSelf();
//      }
//    };
//    (new Thread(runnable)).start();
  }

  //@VisibleForTesting
  void createUpdateNotification() {
    int icon = R.drawable.paco64;
    CharSequence tickerText = "Paco Update - New Version Available";

    Notification notification = new Notification(icon, tickerText, new DateTime().getMillis());

    CharSequence contentTitle = "Paco Update";
    CharSequence contentText = "A new version of Paco is available";

    String url = new UserPreferences(getApplicationContext()).getServerAddress();
    url = "http://" + url + "/paco.apk";
    Intent updateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    PendingIntent notificationIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
    
    notification.setLatestEventInfo(this, contentTitle, contentText, notificationIntent);
    notification.defaults |= Notification.DEFAULT_SOUND;
    notification.defaults |= Notification.DEFAULT_VIBRATE;
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.flags |= Notification.FLAG_NO_CLEAR;

    Log.d(PacoConstants.TAG, "creating notification that an update is available at " + url);
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(0, notification);
  }

  private void createAlarmForNextUpdate() {
    DateTime plusHours = new DateTime().plusHours(24);
    Intent ultimateIntent = new Intent(this, VersionCheckerService.class);
    PendingIntent intent = PendingIntent.getService(getApplicationContext(), 0, ultimateIntent, 0);
    alarmManager.set(AlarmManager.RTC_WAKEUP, plusHours.getMillis(), intent);
    Log.i(RESULTS_SERVICE_TAG, "Created alarm for VersionCheckerService. Time: "
        + plusHours.toString());

  }

}
