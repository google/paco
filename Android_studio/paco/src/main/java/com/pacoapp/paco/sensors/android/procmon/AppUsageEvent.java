package com.pacoapp.paco.sensors.android.procmon;

import android.app.usage.UsageEvents;

/**
 * This is a struct over the UsageEvents.Event class in Android Lollipop
 * usage stats monitoring.
 *
 * Since we need to do some complex monitoring and the real class is marked final, it seemed more testable to
 * make our own class that represented the data.
 *
 *
 */
public class AppUsageEvent {

  public static final String CHROME_APP_DOCUMENT_ACTIVITY_CLASS_NAME = "com.google.android.apps.chrome.document.DocumentActivity";
  public static final String CHROME_APP_FIRST_RUN_ACTIVITY_STAGING_CLASS_NAME = "org.chromium.chrome.browser.firstrun.FirstRunActivityStaging";
  public static final String CHROME_APP_MAIN_CLASS_NAME = "com.google.android.apps.chrome.Main";
  public static final String COM_ANDROID_CHROME_PKG_NAME = "com.android.chrome";


  public static final String ANDROID_LOLLIPOP_HOME_APP_PKG = "com.google.android.googlequicksearchbox";
  public static final String ANDROID_LOLLIPOP_HOME_APP_CLASS = "com.google.android.launcher.GEL";

  public static final int NONE_EVENT = 0;
  public static final int MOVE_TO_FOREGROUND_EVENT = 1;
  public static final int MOVE_TO_BACKGROUND_EVENT = 2;
  public static final int CONFIGURATION_EVENT = 5;

  private String className;
  private String pkgName;
  private int type;
  private long timestamp;

  public AppUsageEvent(String pkgName, String className, int type, long timestamp) {
    super();
    this.className = className;
    this.pkgName = pkgName;
    this.type = type;
    this.timestamp = timestamp;
  }

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public String getPkgName() {
    return pkgName;
  }
  public void setPkgName(String pkgName) {
    this.pkgName = pkgName;
  }
  public int getType() {
    return type;
  }
  public void setType(int type) {
    this.type = type;
  }
  public long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  protected String getTypeStr(int eventType) {
    switch (eventType) {
    case UsageEvents.Event.NONE:
      return "none";
    case UsageEvents.Event.MOVE_TO_FOREGROUND:
      return "to foreground";
    case UsageEvents.Event.MOVE_TO_BACKGROUND:
      return "to background";
    case UsageEvents.Event.CONFIGURATION_CHANGE:
      return "config change";
    default:
      return "unknown";
    }
  }

  /**
   * This convention comes from the Android pre-Lollipop process monitor reporting
   * that was in the Recent Tasks List. We use it here to preserve continuity for
   * experiment creators.
   *
   * @return
   */
  public String getAppIdentifier() {
    return getPkgName() + "/" + getClassName();
  }




}