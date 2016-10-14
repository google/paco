package com.pacoapp.paco.sensors.android.procmon;

/**
 * Class used by AccessibilityEventMonitorService to log a permission request. Used for tying requests to their
 * responses.
 */

public class EncounteredPermissionRequest {
  /**
   * The requested permission (we might change this to be one of several constants used in the
   * Android system in the future)
   */
  private CharSequence permissionString;
  /**
   * The time (in milliseconds since epoch) at which the request happened
   */
  private long timestamp;
  /**
   * The name of the application if known (null otherwise)
   */
  private CharSequence appName;

  public EncounteredPermissionRequest(CharSequence permissionString, long timestamp, CharSequence appName) {
    this.permissionString = permissionString;
    this.timestamp = timestamp;
    this.appName = appName;
  }

  public CharSequence getPermissionString() {
    return permissionString;
  }

  public void setPermissionString(CharSequence permissionString) {
    this.permissionString = permissionString;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public CharSequence getAppName() {
    return appName;
  }

  public void setAppName(CharSequence appName) {
    this.appName = appName;
  }
}
