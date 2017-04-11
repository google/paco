package com.google.sampling.experiential.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class ApplicationUsageProcessor {

  public ApplicationUsage getTotalAppStartsAndDuration(Map<String, List<ApplicationUsageRaw>> usageMap) {
    ApplicationUsage appUsage = new ApplicationUsage();
    Map<String, Integer> appStartCount = Maps.newHashMap();
    Map<String, Long> appDurationCount = Maps.newHashMap();
    // for every user
    for (List<ApplicationUsageRaw> perUserList : usageMap.values()) {
      // for every raw row in the user specific list
      for (ApplicationUsageRaw auRaw : perUserList) {
        // app start
        String appName = auRaw.getAplication();
        Integer ct = appStartCount.get(appName);
        if (ct == null) {
          appStartCount.put(appName, 1);
        } else {
          appStartCount.put(appName, (ct + 1));
        }

        // app duration
        Long duration = appDurationCount.get(appName);
        if (duration == null) {
          appDurationCount.put(appName, auRaw.getDuration());
        } else {
          appDurationCount.put(appName, auRaw.getDuration() + duration);
        }
      }
    }
    appUsage.setAppDurationCount(appDurationCount);
    appUsage.setAppStartCount(appStartCount);
    return appUsage;
  }
}
