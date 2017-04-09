package com.pacoapp.paco.sensors.android.procmon;

public interface AppChangeListener {
  public void appOpened(AppUsageEvent event, boolean shouldTrigger);
  public void appClosed(AppUsageEvent event, boolean shouldTrigger);
}
