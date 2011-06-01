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
package com.google.sampling.experiential.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.What;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Class that holds a response to an experiment.
 * 
 * @author Bob Evans
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Event {

  public static final List<String> eventProperties = Lists.newArrayList("who", 
      "lat", "lon", "when", "appId", "experimentId", "experimentName", "responseTime", 
      "scheduledTime");
  
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String who;

  @Persistent
  private String lat;

  @Persistent
  private String lon;

  @Persistent
  private Date when;

  @Persistent
  private String appId;

  @Persistent
  private String pacoVersion;

  @Persistent
  private String experimentName;

  @Persistent
  private String experimentId;

  
  @Persistent
  private Date scheduledTime;

  @Persistent
  private Date responseTime;

  @Persistent
  private Set<What> what;

  @Persistent
  private List<String> keysList;
  
  @Persistent
  private List<String> valuesList;
  
  @Persistent
  private Boolean shared = false;

  @Persistent
  private List<PhotoBlob> blobs;

  public boolean isShared() {
    if (shared == null) {
      shared = true;
    }
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  public Event(String who, String lat, String lon, Date when, String appId, String pacoVersion,
      Set<What> what, boolean shared, String experimentId, String experimentName, 
      Date responseTime, Date scheduledTime, List<PhotoBlob> blobs) {
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
    setWhatMap(what);
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

  private void setWhatMap(Set<What> whats) {
    this.keysList = Lists.newArrayList();
    this.valuesList = Lists.newArrayList();
    for (What what : whats) {
      keysList.add(what.getName());
      valuesList.add(what.getValue());
    }
  }


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

  public Set<What> getWhat() {
    return what;
  }

  public void setWhat(Set<What> what) {
    this.what = what;
    setWhatMap(what);
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

  private String getValueForKey(String key) {
    int index = keysList.indexOf(key);
    if (index == -1) {
      return null;
    }
    return valuesList.get(index);
  }

  public String getWhatByKey(String key) {
    return getValueForKey(key);
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

  public List<String> getWhatKeys() {
    return keysList;
  }

  
  public List<PhotoBlob> getBlobs() {
    return blobs;
  }

  public void setBlobs(List<PhotoBlob> blobs) {
    this.blobs = blobs;
  }

  public Map<String, String> getWhatMap() {
    Map<String, String> map = Maps.newHashMap();
    if (keysList == null) {
      keysList = Lists.newArrayList();
    }
    for (int i = 0; i < keysList.size(); i++) {
      String keyName = keysList.get(i);
      if (keyName == null || keyName.length() == 0) {
        keyName = "unknown_"+i;
      }
      map.put(keyName, valuesList.get(i));
    }
    return map;
  }

  public String[] toCSV(List<String> columnNames) {
    java.text.SimpleDateFormat simpleDateFormat =
      new java.text.SimpleDateFormat("yyyyMMdd:HH:mm:ssZ");
    
    int csvIndex = 0;
    String[] parts = new String[10 + columnNames.size()];
    parts[csvIndex++] = who;
    parts[csvIndex++] = simpleDateFormat.format(when);
    parts[csvIndex++] = lat;
    parts[csvIndex++] = lon;
    parts[csvIndex++] = appId;
    parts[csvIndex++] = pacoVersion;
    parts[csvIndex++] = experimentId;
    parts[csvIndex++] = experimentName;
    parts[csvIndex++] = responseTime != null ? simpleDateFormat.format(responseTime) : null;
    parts[csvIndex++] = scheduledTime != null ? simpleDateFormat.format(scheduledTime) : null;
    Map<String, String> whatMap = getWhatMap();
    for (String key : columnNames) {
      String value = whatMap.get(key);
      parts[csvIndex++] = value;
    }
    return parts;
  }

}
