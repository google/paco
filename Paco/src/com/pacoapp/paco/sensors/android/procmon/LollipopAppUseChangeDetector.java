package com.pacoapp.paco.sensors.android.procmon;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LollipopAppUseChangeDetector {

  private List<String> tasksOfInterestForClosing;
  private List<String> tasksOfInterestForOpening;
  private AppChangeListener listener;
  private HashMap<String, AppUsageEvent> lastOpenedMap;
  private HashMap<String, AppUsageEvent> lastClosedMap;

  public LollipopAppUseChangeDetector(List<String> tasksOfInterestForOpening,
                                      List<String> tasksOfInterestForClosing,
                                      AppChangeListener listener) {
    this.tasksOfInterestForOpening = tasksOfInterestForOpening != null
            ? tasksOfInterestForOpening
            : Lists.<String>newArrayList();
    this.tasksOfInterestForClosing = tasksOfInterestForClosing != null
            ? tasksOfInterestForClosing
            : Lists.<String>newArrayList();
    this.listener = listener;
    this.lastOpenedMap = Maps.newHashMap();
    this.lastClosedMap = Maps.newHashMap();
  }

  public void newEvents(List<AppUsageEvent> events) {
    if (events.size() == 0) {
      return;
    }
    List<AppUsageEvent> newApps = detectNewApps(events);
    List<AppUsageEvent> closedApps = detectClosedApps(events);
  }

  private List<AppUsageEvent> detectClosedApps(List<AppUsageEvent> events) {
    List<AppUsageEvent> results = Lists.newArrayList();
    Lists.reverse(events);
    for (AppUsageEvent appUsageEvent : events) {
      if (appUsageEvent.getType() == AppUsageEvent.MOVE_TO_BACKGROUND_EVENT) {

        AppUsageEvent lastClosedRecord = lastClosedMap.get(appUsageEvent.getAppIdentifier());
        if (lastClosedRecord == null || lastClosedRecord.getTimestamp() < appUsageEvent.getTimestamp()) {
          lastClosedMap.put(appUsageEvent.getAppIdentifier(), appUsageEvent);
          results.add(appUsageEvent);
          listener.appClosed(appUsageEvent, isAppOfInterestForClosing(appUsageEvent));
        }
      }
    }
    return results;
  }

  private List<AppUsageEvent> detectNewApps(List<AppUsageEvent> events) {
    List<AppUsageEvent> results = Lists.newArrayList();
    Lists.reverse(events);
    for (AppUsageEvent appUsageEvent : events) {
      if (appUsageEvent.getType() == AppUsageEvent.MOVE_TO_FOREGROUND_EVENT) {

        AppUsageEvent lastOpenedRecord = lastOpenedMap.get(appUsageEvent.getAppIdentifier());
        if (lastOpenedRecord == null || lastOpenedRecord.getTimestamp() < appUsageEvent.getTimestamp()) {
          lastOpenedMap.put(appUsageEvent.getAppIdentifier(), appUsageEvent);
          results.add(appUsageEvent);
          listener.appOpened(appUsageEvent, isAppOfInterestForOpening(appUsageEvent));
        }

      }
    }
    return results;
  }


  public boolean isAppOfInterestForClosing(AppUsageEvent appUsageEvent) {
    return tasksOfInterestForClosing.contains(appUsageEvent.getAppIdentifier());
  }

  public boolean isAppOfInterestForOpening(AppUsageEvent appUsageEvent) {
    return tasksOfInterestForOpening.contains(appUsageEvent.getAppIdentifier());
  }


}
