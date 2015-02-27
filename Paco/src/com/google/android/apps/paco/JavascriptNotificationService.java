package com.google.android.apps.paco;

import android.content.Context;

public class JavascriptNotificationService {

  private Context context;
  private Experiment experiment;

  /**
   * @param experiment
   * @param experimentExecutorCustomRendering
   */
  public JavascriptNotificationService(Context context, Experiment experiment) {
    this.context = context;
    this.experiment = experiment;
  }

  public void createNotification(String message) {
    createNotification(message, true, true, 1000 * 60 * 60 * 24); // timeout in 24 hours.
  }

  private void createNotification(String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    NotificationCreator.create(context).createNotificationsForCustomGeneratedScript(experiment, message, makeSound, makeVibrate, timeoutMillis);
  }

  public void removeNotification() {
    NotificationCreator.create(context).removeNotificationsForCustomGeneratedScript(experiment);
  }
}