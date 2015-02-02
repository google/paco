package com.google.android.apps.paco;

public class JavascriptNotificationService {

  /**
   * 
   */
  private final ExperimentExecutorCustomRendering innerType;

  /**
   * @param experimentExecutorCustomRendering
   */
  JavascriptNotificationService(ExperimentExecutorCustomRendering experimentExecutorCustomRendering) {
    innerType = experimentExecutorCustomRendering;
  }

  public void createNotification(String message) {
    createNotification(message, true, true, 1000 * 60 * 60 * 24); // timeout in 24 hours.
  }

  private void createNotification(String message, boolean makeSound, boolean makeVibrate, long timeoutMillis) {
    NotificationCreator.create(innerType).createNotificationsForCustomGeneratedScript(innerType.experiment, message, makeSound, makeVibrate, timeoutMillis);
  }

  public void removeNotification() {
    NotificationCreator.create(innerType).removeNotificationsForCustomGeneratedScript(innerType.experiment);
  }
}