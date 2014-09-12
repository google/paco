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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.EventServlet;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Class that holds a response to an experiment.
 *
 * @author Bob Evans
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Event {

  private static final long serialVersionUID = -1407635488794262589l;

  public static final String SALT = "zyzzyfoo";

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
  private Integer experimentVersion;

  @Persistent
  private Date scheduledTime;

  @Persistent
  private Date responseTime;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private Set<What> what;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private List<String> keysList;

  @Persistent
  @Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  private List<String> valuesList;

  @Persistent
  private Boolean shared = false;

  @Persistent
  private List<PhotoBlob> blobs;

  @Persistent
  private String timeZone;

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
      Set<What> what, boolean shared, String experimentId, String experimentName, Integer experimentVersion,
      Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String timezone) {
    super();
    if (/*what.size() == 0 || */who == null || when == null) {
      throw new IllegalArgumentException("There must be a who and a when");
    }
    this.who = who;
    this.lat = lat;
    this.lon = lon;
    this.when = when;
    this.what = what;
    setWhatMap(what);
    this.appId = appId;
    this.pacoVersion = Strings.isNullOrEmpty(pacoVersion) ? "1" : pacoVersion;
    this.shared = shared;
    this.experimentId = experimentId;
    this.experimentName = experimentName;
    this.experimentVersion = experimentVersion;
    this.responseTime = responseTime;
    this.scheduledTime = scheduledTime;
    if (blobs != null) {
      this.blobs = blobs;
    }
    this.timeZone = timezone;
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

  public String[] toCSV(List<String> columnNames, boolean anon) {
    DateTimeFormatter jodaTimeFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT);
    int csvIndex = 0;
    String[] parts = new String[10 + columnNames.size()];
    if (anon) {
      parts[csvIndex++] = Event.getAnonymousId(who + SALT);
    } else {
      parts[csvIndex++] = who;
    }
    parts[csvIndex++] = jodaTimeFormatter.print(new DateTime(when.getTime()));
    parts[csvIndex++] = appId;
    parts[csvIndex++] = pacoVersion;
    parts[csvIndex++] = experimentId;
    parts[csvIndex++] = experimentName;
    parts[csvIndex++] = experimentVersion != null ? Integer.toString(experimentVersion) : "0";

    parts[csvIndex++] = responseTime != null ? jodaTimeFormatter.print(getResponseTimeWithTimeZone(null)) : null;
    parts[csvIndex++] = scheduledTime != null ? jodaTimeFormatter.print(getScheduledTimeWithTimeZone(null)) : null;
    parts[csvIndex++] = timeZone;

    Map<String, String> whatMap = getWhatMap();
    for (String key : columnNames) {
      String value = whatMap.get(key);
      parts[csvIndex++] = value;
    }
    return parts;
  }

  public String toString() {
    java.text.SimpleDateFormat simpleDateFormat =
      new java.text.SimpleDateFormat(TimeUtil.DATETIME_FORMAT);

    StringBuilder buf = new StringBuilder();
    buf.append(who).append("\n");
    buf.append(simpleDateFormat.format(when)).append("\n");
    buf.append(experimentId).append("\n");
    buf.append(experimentName).append("\n");
    buf.append(responseTime != null ? simpleDateFormat.format(getResponseTimeWithTimeZone(null)) : null).append("\n");
    buf.append(scheduledTime != null ? simpleDateFormat.format(getScheduledTimeWithTimeZone(null)) : null).append("\n");
    Map<String, String> whatMap = getWhatMap();
    for (String key : whatMap.keySet()) {
      String value = whatMap.get(key);
      buf.append(key).append("=").append(value).append("\n");
    }
    return buf.toString();
  }

  public boolean isJoined() {
    return getWhatByKey("joined") != null;
  }

  public static String getAnonymousId(String who) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      //Log.info("Could not get MD5 algorithm");
      return null;
    }
    messageDigest.reset();
    messageDigest.update(who.getBytes(Charset.forName("UTF8")));
    byte[] resultByte = messageDigest.digest();
    return new String(Hex.encodeHex(resultByte));
  }

  public Integer getExperimentVersion() {
    return experimentVersion;
  }

  public void setExperimentVersion(Integer experimentVersion) {
    this.experimentVersion = experimentVersion;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public DateTime getScheduledTimeWithTimeZone(String defaultTimeZone) {
    DateTime timeZoneAdjustedDate = getTimeZoneAdjustedDate(getScheduledTime(), defaultTimeZone);
    return timeZoneAdjustedDate == null ? null : timeZoneAdjustedDate;
  }

  public DateTime getResponseTimeWithTimeZone(String defaultTimeZone) {
    DateTime timeZoneAdjustedDate = getTimeZoneAdjustedDate(getResponseTime(), defaultTimeZone);
    return timeZoneAdjustedDate == null ? null : timeZoneAdjustedDate;
  }


  public DateTime getTimeZoneAdjustedDate(Date time, String defaultTimeZone) {
    return getTimeZoneAdjustedDate(time, defaultTimeZone, getTimeZone());
  }

  public static DateTime getTimeZoneAdjustedDate(Date time, String defaultTimeZone, String timeZone) {
    if (time == null) {
      return null;
    }

    if (Strings.isNullOrEmpty(timeZone)) {
      if (Strings.isNullOrEmpty(defaultTimeZone)) {
        return new DateTime(time);
      } else {
        DateTimeZone timezoneForOffsetHours = DateTimeZone.forID(defaultTimeZone);
        if (timezoneForOffsetHours == null) {
          return new DateTime(time);
        }
        return new DateTime(time).withZone(timezoneForOffsetHours);
      }
    } else {
      String hours = timeZone.substring(0,3);
      if (hours.startsWith("+")) {
        hours = hours.substring(1);
      }

      int parseInt;
      try {
        parseInt = Integer.parseInt(hours);
      } catch (NumberFormatException e) {
        EventServlet.log.info("Timezone hours are not an integer this event.");
        return new DateTime(time);
      }
      DateTimeZone timezoneForOffsetHours = DateTimeZone.forOffsetHours(parseInt);
      if (timezoneForOffsetHours == null) {
        return new DateTime(time);
      }
      return new DateTime(time).withZone(timezoneForOffsetHours);
    }
  }

}
