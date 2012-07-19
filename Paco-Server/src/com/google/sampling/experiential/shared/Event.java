/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Id;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Class that holds a response to an experiment.
 * 
 * @author Bob Evans
 *
 */
public class Event implements Serializable, Cloneable {

  public static final List<String> eventProperties = Lists.newArrayList("who",
      "lat", "lon", "when", "appId", "experimentId", "experimentName", "responseTime",
      "scheduledTime");

  @Id
  private Long id = null;

  private String who = "";

  private String lat = "";

  private String lon = "";

  private Date when = new Date();

  private String appId = "";

  private String pacoVersion = "";

  private String experimentName = "";

  private String experimentId = "";

  private Date scheduledTime = new Date();

  private Date responseTime = new Date();

  @Embedded
  transient private Map<String, String> what = Maps.newHashMap();

  /*
  @Embedded
  private List<String> keysList = Lists.newArrayList();

  @Embedded
  private List<String> valuesList = Lists.newArrayList();
  */

  private Boolean shared = false;

  private List<String> blobs = Lists.newArrayList();

  public boolean isShared() {
    if (shared == null) {
      shared = true;
    }
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  public Event() { }

  public Event(String who, String lat, String lon, Date when, String appId, String pacoVersion,
      Map<String, String> what, boolean shared, String experimentId, String experimentName,
      Date responseTime, Date scheduledTime, List<String> blobs) {
    super();
    if (/*what.size() == 0 || */who == null || when == null || appId == null) {
      throw new IllegalArgumentException("There must be a who, a when, an appId, and "
          + "at least one what to make an event");
    }
    this.who = who;
    this.lat = lat;
    this.lon = lon;
    this.when = when;
    this.what = what;
    this.appId = appId;
    this.pacoVersion = (pacoVersion == null || pacoVersion.isEmpty()) ? "1" : pacoVersion;
    this.shared = shared;
    this.experimentId = experimentId;
    this.experimentName = experimentName;
    this.responseTime = responseTime;
    this.scheduledTime = scheduledTime;
    if (blobs != null) {
      this.blobs = blobs;
    }
  }

  /*
  private void setWhatMap(Set<What> whats) {
    this.keysList = Lists.newArrayList();
    this.valuesList = Lists.newArrayList();
    for (What what : whats) {
      keysList.add(what.getName());
      valuesList.add(what.getValue());
    }
  }
  */


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }

  public String getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public String getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = lon;
  }

  public Date getWhen() {
    return when;
  }

  public void setWhen(Date when) {
    this.when = when;
  }

  public Map<String, String> getWhat() {
    return what;
  }

  public void setWhat(Map<String, String> what) {
    this.what = what;
  }

  public String getExperimentName() {
    return experimentName;
  }

  public void setExperimentName(String experimentName) {
    this.experimentName = experimentName;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public Date getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(Date scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public Date getResponseTime() {
    return responseTime;
  }

  public void setResponseTime(Date responseTime) {
    this.responseTime = responseTime;
  }

  /*
  private String getValueForKey(String key) {
    int index = keysList.indexOf(key);
    if (index == -1) {
      return null;
    }
    return valuesList.get(index);
  }
  */

  public String getWhatByKey(String key) {
    return this.what.get(key);
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getPacoVersion() {
    return pacoVersion;
  }

  public void setPacoVersion(String pacoVersion) {
    this.pacoVersion = pacoVersion;
  }

  public List<String> getBlobs() {
    return blobs;
  }

  public void setBlobs(List<String> blobs) {
    this.blobs = blobs;
  }

  public Map<String, String> getWhatMap() {
    return what;
  }

  public boolean isMissedSignal() {
    return scheduledTime != null && responseTime == null;
  }

  public long responseTime() {
    if (responseTime == null || scheduledTime == null) {
      return 0;
    }
    return responseTime.getTime() - scheduledTime.getTime();
  }

  public boolean isJoinEvent() {
    return getWhatByKey("joined") != null;
  }

  public Event clone() {
    Event newEvent = new Event();

    newEvent.setWho(this.getWho());
    newEvent.setLat(this.getLat());
    newEvent.setLon(this.getLon());
    newEvent.setWhen(this.getWhen());
    newEvent.setAppId(this.getAppId());
    newEvent.setPacoVersion(this.getPacoVersion());
    newEvent.setExperimentName(this.getExperimentName());
    newEvent.setExperimentId(this.getExperimentId());
    newEvent.setScheduledTime((Date)this.getScheduledTime().clone());
    newEvent.setResponseTime((Date)this.getResponseTime().clone());

    return newEvent;
  }

  public String getWhatString() {
    StringBuilder whatStr = new StringBuilder();
    boolean first = true;
    for (String key : what.keySet()) {
      if (first) {
        first = false;
      } else {
        whatStr.append(", ");
      }
      whatStr.append(key);
      whatStr.append("=");
      whatStr.append(what.get(key));
    }
    return whatStr.toString();
  }
}
