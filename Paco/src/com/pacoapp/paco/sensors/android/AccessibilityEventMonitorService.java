package com.pacoapp.paco.sensors.android;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * This class handles monitoring of accessibility events
 */

public class AccessibilityEventMonitorService extends AccessibilityService {

  private static Logger Log = LoggerFactory.getLogger(AccessibilityEventMonitorService.class);

  // Keeps whether the service is connected
  private static boolean running = false;

  /**
   * Called only for accessibility events coming from Android's packageinstaller.
   * {@inheritDoc}
   */
  @Override
  public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    // Assert that we're handling events only for the correct package
    CharSequence packageName = accessibilityEvent.getPackageName();
    //Log.e(PacoConstants.TAG, "Event received: " + accessibilityEvent.toString());
    CharSequence className = accessibilityEvent.getClassName();
    int eventType = accessibilityEvent.getEventType();
    CharSequence contentDescription = accessibilityEvent.getContentDescription();

    String msg = null;
    switch (eventType) {
      case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
        msg = String.format("EVENT: type: %s, p: %s, c: %s, cd: %s", "windowstatechange", packageName, className, contentDescription );
        Log.info(msg);
        break;
      case AccessibilityEvent.TYPE_ANNOUNCEMENT:
        msg = String.format("EVENT: type: %s, p: %s, c: %s, cd: %s", "announcement", packageName, className, contentDescription );
        Log.info(msg);
        break;
      case AccessibilityEvent.TYPE_VIEW_CLICKED:
        msg = String.format("EVENT: type: %s, p: %s, c: %s, cd: %s", "viewclicked", packageName, className, contentDescription );
        Log.info(msg);
        break;
      case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
        msg = String.format("EVENT: type: %s, p: %s, c: %s, cd: %s", "notificationstate", packageName, className, contentDescription );
        Log.info(msg);
        break;
      default:
        //Log.v(PacoConstants.TAG, "accessibility event");
        break;

    }
  }

  /**
   * Returns whether the service is running and connected.
   * @return true if we have accessibility permissions and the service is connected
   */
  public static boolean isRunning() {return running;}

  /**
   * Called by the Android system when it connects the accessibility service. We use this to keep
   * track of whether we have the accessibility permission.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected void onServiceConnected() {
    running = true;
    Log.debug("Connected to the accessibility service");
  }

  /**
   * Called by the Android system when the accessibility service is stopped (e.g. because the user
   * disables accessibility permissions for the app)
   */
  @Override
  public void onDestroy() {
    Log.debug("Accessibility service destroyed");
    running = false;
  }

  /**
   * Called by the Android system when it wants to interrupt feedback
   */
  @Override
  public void onInterrupt() {
    // Ignore, since we are not actually a screen reader.
  }
}
