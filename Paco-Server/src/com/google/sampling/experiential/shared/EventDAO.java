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
import java.util.Map;

/**
 * 
 * Dumb data object for passing the event data to the
 * GWT client. 
 * 
 * We use this because GWt serialization won't serialize a JDO nucleus object.
 * @author Bob Evans
 *
 */
public class EventDAO implements Serializable {

  private Long id;
  
  private Long experimentId;

  private String who;

  private String lat;

  private String lon;

  private Date when;

  private String appId;

  private String pacoVersion;
  
  private boolean shared;
  
  private String experimentName;


  public String getExperimentName() {
    return experimentName;
  }

  public void setExperimentName(String experimentName) {
    this.experimentName = experimentName;
  }

  private Map<String, String> what;

  private Date responseTime;

  private Date scheduledTime;
  
  private String[] blobs;

  public Map<String, String> getWhat() {
    return what;
  }

  public void setWhat(Map<String, String> what) {
    this.what = what;
  }

  public EventDAO() {

  }

  public EventDAO(String who, Date when, String experimentName, String lat, String lon, 
      String appId, String pacoVersion, Map<String, String> map, boolean shared, Date responseTime, 
      Date scheduledTime, String[] blobs, Long experimentId) {
    super();
    this.who = who;
    this.lat = lat;
    this.lon = lon;
    this.when = when;
    this.what = map;
    this.appId = appId;
    this.pacoVersion = pacoVersion;
    this.shared = shared;
    this.experimentName = experimentName;
    this.responseTime = responseTime;
    this.scheduledTime = scheduledTime;
    this.blobs = blobs;
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

  public String getWhatByKey(String key) {
    return what.get(key);
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

  public void setPaco_version(String pacoVersion) {
    this.pacoVersion = pacoVersion;
  }

  public boolean isShared() {
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  /**
   * @return
   */
  public Date getResponseTime() {
    return responseTime;
  }

  public void setResponseTime(Date responseTime) {
    this.responseTime = responseTime;
  }

  public Date getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(Date scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public boolean isMissedSignal() {
    return scheduledTime != null && responseTime == null;
  }

  /**
   * @return
   */
  public long responseTime() {
    if (responseTime == null || scheduledTime == null) {
      return 0;
    }
    return responseTime.getTime() - scheduledTime.getTime();
  }

  /**
   * @return
   */
  public boolean isJoinEvent() {
    return getWhatByKey("joined") != null;
  }

  public String[] getBlobs() {
    return blobs;
  }

  public void setBlobs(String[] blobs) {
    this.blobs = blobs;
  }

  public String getIdFromTimes() {
    if (getScheduledTime() != null) {
      return Long.toString(getScheduledTime().getTime());
    } else/* if (getResponseTime() != null) */{
      return Long.toString(getResponseTime().getTime());
    } // one of those two has to exist
  }

  public Long getExperimentId() {
    return experimentId;
  }
  
  public void setExperimentId(Long id) {
    this.experimentId = id;
  }
  
  

}
