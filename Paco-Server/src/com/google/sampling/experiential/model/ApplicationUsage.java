package com.google.sampling.experiential.model;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class ApplicationUsage implements Serializable {

  Map<String, Integer> appStartCount = Maps.newHashMap();
  Map<String, Long> appDurationCount = Maps.newHashMap();

  public Map<String, Integer> getAppStartCount() {
    return appStartCount;
  }

  public Map<String, Long> getAppDurationCount() {
    return appDurationCount;
  }

  public void setAppStartCount(Map<String, Integer> appStartCount) {
    this.appStartCount = appStartCount;
  }

  public void setAppDurationCount(Map<String, Long> appDurationCount) {
    this.appDurationCount = appDurationCount;
  }
}
