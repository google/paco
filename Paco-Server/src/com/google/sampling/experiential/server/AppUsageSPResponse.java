package com.google.sampling.experiential.server;

import java.io.Serializable;

import com.google.sampling.experiential.model.ApplicationUsage;

public class AppUsageSPResponse extends SPResponse implements Serializable {

  private ApplicationUsage appUsage;

  public ApplicationUsage getAppUsage() {
    return appUsage;
  }

  public void setAppUsage(ApplicationUsage appUsage) {
    this.appUsage = appUsage;
  }
}
