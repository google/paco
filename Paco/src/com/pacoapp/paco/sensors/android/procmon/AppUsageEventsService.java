package com.pacoapp.paco.sensors.android.procmon;

import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import com.google.common.collect.Lists;

/**
 * Abstract away the Android Lollipop UsageStatsManager so that we can test
 * the algorithm.
 *
 */
@SuppressLint("NewApi")
public class AppUsageEventsService {

  private UsageStatsManager usageStatsManager;
  private int lookupBackFrequency;

  public AppUsageEventsService(UsageStatsManager am, int lookupFrequency) {
    this.usageStatsManager = am;
    this.lookupBackFrequency = lookupFrequency;
  }

  public AppUsageEventsService() {
  }

  public List<AppUsageEvent> getUsageEvents() {
    long endTime = Calendar.getInstance().getTimeInMillis();
    long startTime = endTime - (lookupBackFrequency * 1000);
    UsageEvents ls = usageStatsManager.queryEvents(startTime, endTime);
    return convertToFriendlyEvents(ls);
  }

  public boolean canGetStats() {
    Calendar calendar = Calendar.getInstance();
    long endTime = calendar.getTimeInMillis();
    calendar.add(Calendar.YEAR, -1);
    long startTime = calendar.getTimeInMillis();
    List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                                                                        startTime,
                                                                        endTime);
    return !usageStatsList.isEmpty();
  }

  private List<AppUsageEvent> convertToFriendlyEvents(UsageEvents ls) {
    List<AppUsageEvent> usageEventsFriendly = Lists.newArrayList();
    while (ls.hasNextEvent()) {
      android.app.usage.UsageEvents.Event eventOut = new android.app.usage.UsageEvents.Event();
      ls.getNextEvent(eventOut);
      usageEventsFriendly.add(new AppUsageEvent(eventOut.getPackageName(),
                                             eventOut.getClassName(),
                                             eventOut.getEventType(),
                                             eventOut.getTimeStamp()));
    }
    return usageEventsFriendly;
  }


}
