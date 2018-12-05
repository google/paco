package com.pacoapp.paco.js.bridge;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.triggering.NotificationCreator;

public class JavascriptNotificationService {

  private Context context;
  private ExperimentDAO experiment;
  private ExperimentGroup experimentGroup;

  public JavascriptNotificationService(Context context, ExperimentDAO experiment2, ExperimentGroup experimentGroup) {
    this.context = context;
    this.experiment = experiment2;
    this.experimentGroup = experimentGroup;
  }

  @JavascriptInterface
  public void createNotification(String message) {
    createNotification(message, true, true, 1000 * 60 * 60 * 24); // timeout in 24 hours.
  }

  @JavascriptInterface
  public void createNotification(String message, long timeoutMillis) {
    createNotification(message, true, true, timeoutMillis);
  }


  private void createNotification(String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    NotificationCreator.create(context).createNotificationsForCustomGeneratedScript(experiment, experimentGroup, message, makeSound, makeVibrate, timeoutMillis);
  }

  @JavascriptInterface
  public void removeNotification(String message) {
    NotificationCreator.create(context).removeNotificationsForCustomGeneratedScript(experiment, experimentGroup, message);
  }

  @JavascriptInterface
  public void removeAllNotifications() {
    NotificationCreator.create(context).removeAllNotificationsForCustomGeneratedScript(experiment, experimentGroup);
  }
}