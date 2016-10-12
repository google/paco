package com.pacoapp.paco.sensors.android;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

/**
 *
 * This service is enabled when Paco is granted the Accessibility permission. The
 * BroadcastTriggerService will make sure that only experiments enabling accessibility logging will
 * receive accessibility events.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // TODO: update to Marshmallow when project SDK changes
public class AccessibilityEventMonitorService extends AccessibilityService {
  private static Logger Log = LoggerFactory.getLogger(AccessibilityEventMonitorService.class);

    // Keeps whether the service is connected
  private static boolean running = false;
  private RuntimePermissionsAccessibilityEventHandler runtimePermissionsEventHandler;

  /**
   * Called to allow experiments that trigger on accessibility events.
   * {@inheritDoc}
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    CharSequence packageName = accessibilityEvent.getPackageName();
    if (RuntimePermissionsAccessibilityEventHandler.isPackageInstallerEvent(packageName)) {
      runtimePermissionsEventHandler.handleRuntimePermissionEvents(accessibilityEvent);
      return;
    } else {
      inspectEvent(accessibilityEvent);
    }
  }

  private void inspectEvent(AccessibilityEvent accessibilityEvent) {
    CharSequence packageName = accessibilityEvent.getPackageName();
    CharSequence className = accessibilityEvent.getClassName();
    int eventType = accessibilityEvent.getEventType();
    CharSequence contentDescription = accessibilityEvent.getContentDescription();

    Log.debug(eventToString(accessibilityEvent));
  }

  private String getStringForEventType(int eventType) {
    switch (eventType) {
    case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
      return "windowstatechange";
    case AccessibilityEvent.TYPE_ANNOUNCEMENT:
      return "announcement";
    case AccessibilityEvent.TYPE_VIEW_CLICKED:
      return "viewclicked";
    case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
      return "notificationstate";
    default:
      return "unknown or uninteresting accessibility event type: " + eventType;
    }
  }

  private String eventToString(AccessibilityEvent accessibilityEvent) {
    int eventType = accessibilityEvent.getEventType();
    return String.format("EVENT: type: %s, p: %s, c: %s, cd: %s",
                         getStringForEventType(eventType),
                         accessibilityEvent.getPackageName(),
                         accessibilityEvent.getClassName(),
                         accessibilityEvent.getContentDescription());
  }

  /**
   * Returns whether the service is running and connected.
   * @return true if we have accessibility permissions and the service is connected
   */
  public static boolean isRunning() {
    return running;
  }

  /**
   * Called by the Android system when it connects the accessibility service. We use this to keep
   * track of whether we have the accessibility permission.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected void onServiceConnected() {
    running = true;
    Log.debug("Connected to the accessibility service");
    initializeRuntimePermissionsMonitoringState();
  }

  private void initializeRuntimePermissionsMonitoringState() {
    runtimePermissionsEventHandler = new RuntimePermissionsAccessibilityEventHandler(getApplicationContext());
  }

  /**
   * Called by the Android system when the accessibility service is stopped (e.g. because the user
   * disables accessibility permissions for the app)
   */
  @Override
  public void onDestroy() {
    Log.debug("Accessibility service destroyed");
    running = false;
    runtimePermissionsEventHandler = null;
  }

  /**
   * Called by the Android system when it wants to interrupt feedback
   */
  @Override
  public void onInterrupt() {
    // Ignore, since we are not actually a screen reader.
  }
}
