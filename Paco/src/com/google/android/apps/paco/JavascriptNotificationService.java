package com.google.android.apps.paco;

import android.content.Context;

import com.google.paco.shared.model2.ExperimentGroup;

public class JavascriptNotificationService {

  private Context context;
  private Experiment experiment;
  private ExperimentGroup experimentGroup;

  public JavascriptNotificationService(Context context, Experiment experiment, ExperimentGroup experimentGroup) {
    this.context = context;
    this.experiment = experiment;
    this.experimentGroup = experimentGroup;
  }

  public void createNotification(String message) {
    createNotification(message, true, true, 1000 * 60 * 60 * 24); // timeout in 24 hours.
  }

  private void createNotification(String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    NotificationCreator.create(context).createNotificationsForCustomGeneratedScript(experiment, experimentGroup, message, makeSound, makeVibrate, timeoutMillis);
  }

  public void removeNotification(String message) {
    NotificationCreator.create(context).removeNotificationsForCustomGeneratedScript(experiment, experimentGroup, message);
  }
}