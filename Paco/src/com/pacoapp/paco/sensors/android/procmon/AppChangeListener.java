package com.pacoapp.paco.sensors.android.procmon;

public interface AppChangeListener {
  public void appOpened(AppUsageEvent event);
  public void appClosed(AppUsageEvent event);
}
