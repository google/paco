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
package com.google.android.apps.paco;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {

  DateTimeFormatter df = DateTimeFormat.forPattern("yyyyMMdd:HH:mm:ssZ");
  public static class Creator implements Parcelable.Creator<Event> {

    public Event createFromParcel(Parcel source) {
      Event input = new Event();
      input.id = source.readLong();
      input.experimentId = source.readLong();
      input.experimentServerId = source.readLong();
      input.experimentName = source.readString();
      long scheduledMillis = source.readLong();
      String scheduledMillisTzId = source.readString();
      if (scheduledMillis != -1) {
        input.scheduledTime = new DateTime(scheduledMillis, DateTimeZone.forID(scheduledMillisTzId));
      }
      
      long respondedMillis = source.readLong();
      String respondedMillisTzId = source.readString();
      if (respondedMillis != -1) {
        input.responseTime = new DateTime(respondedMillis, DateTimeZone.forID(respondedMillisTzId));
      }
      input.uploaded = source.readInt() == 1;
      
      int responseSize = source.readInt();
      ClassLoader classLoader = getClass().getClassLoader();

      for (int i = 0; i < responseSize; i++) {
        Output response = source.readParcelable(classLoader);
        input.responses.add(response);
      }
      return input;
    }

    public Event[] newArray(int size) {
      return new Event[size];
    }
  }

  public static final Creator CREATOR = new Creator();

  @JsonIgnore
  private long id = -1;
  
  @JsonIgnore
  private long experimentId = -1;
  
  @JsonProperty("experimentId")
  private long experimentServerId = -1;

  private String experimentName;
  
  @JsonIgnore
  private DateTime scheduledTime;
  
  @JsonIgnore
  private DateTime responseTime;
  
  @JsonIgnore
  private boolean uploaded;
  
  private List<Output> responses = new ArrayList<Output>();

  public Event() {    
  }
  
  @JsonIgnore  
  public long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(long id) {
    this.id = id;
  }

  @JsonIgnore
  public DateTime getScheduledTime() {
    return scheduledTime;
  }

  @JsonIgnore
  public void setScheduledTime(DateTime scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  @JsonIgnore
  public DateTime getResponseTime() {
    return responseTime;
  }

  @JsonIgnore
  public void setResponseTime(DateTime responseTime) {
    this.responseTime = responseTime;
  }

  @JsonProperty("responseTime")
  public String getResponseTimeAsString() {
    if (responseTime == null) {
      return null;
    }
    return df.print(responseTime);
  }

  @JsonProperty("responseTime")
  public void setResponseTimeFromString(String responseTimeStr) {
    this.responseTime = df.parseDateTime(responseTimeStr);
  }

  @JsonProperty("scheduledTime")
  public String getScheduledTimeAsString() {
    if (scheduledTime == null) {
      return null;
    }
    return df.print(scheduledTime);
  }

  @JsonProperty("scheduledTime")
  public void setScheduledTimeFromString(String scheduledTimeStr) {
    this.scheduledTime = df.parseDateTime(scheduledTimeStr);
  }

  @JsonIgnore
  public boolean isUploaded() {
    return uploaded;
  }

  @JsonIgnore
  public void setUploaded(boolean uploaded) {
    this.uploaded = uploaded;
  }

  @JsonIgnore
  public long getExperimentId() {
    return experimentId;
  }

  @JsonIgnore
  public void setExperimentId(long experimentId) {
    this.experimentId = experimentId;
  }

  public String getExperimentName() {
    return experimentName;
  }

  public void setExperimentName(String experimentName) {
    this.experimentName = experimentName;
  }


  @JsonIgnore
  public int describeContents() {
    return 0;
  }

  @JsonIgnore
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeLong(experimentId);
    dest.writeLong(experimentServerId);
    dest.writeString(experimentName);
    long scheduledMillis = -1;
    String scheduledTzId = ""; 
    if (scheduledTime != null) {
      scheduledMillis = scheduledTime.getMillis();
      scheduledTzId = scheduledTime.getZone().getID();
    } 
    dest.writeLong(scheduledMillis);
    dest.writeString(scheduledTzId);

    long respondedMillis = -1;
    String respondedTzId = "";
    if (responseTime != null) {
      respondedMillis = responseTime.getMillis();
      respondedTzId = responseTime.getZone().getID();
    } 
    dest.writeLong(respondedMillis);
    dest.writeString(respondedTzId);
    dest.writeInt(uploaded ? 1 : 0);
    dest.writeInt(responses.size());
    for (Output response : responses) {
      dest.writeParcelable(response, 0);
    }
   
  }

  @JsonIgnore
  public void addResponse(Output response) {
    responses.add(response);
  }

  @JsonProperty("experimentId")
  public void setServerExperimentId(long serverId) {
    this.experimentServerId = serverId;
  }

  @JsonProperty("experimentId")
  public long getExperimentServerId() {
    return experimentServerId;
  }

  public List<Output> getResponses() {
    return responses;
  }

  public void setResponses(List<Output> responses) {
    this.responses = responses;
  }

}
