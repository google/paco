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
package com.pacoapp.paco.model;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.os.Parcel;
import android.os.Parcelable;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.util.TimeUtil;

public class Experiment implements Parcelable {

  public static class Creator implements Parcelable.Creator<Experiment> {

    public Experiment createFromParcel(Parcel source) {
      ClassLoader classLoader = getClass().getClassLoader();
      String json = source.readString();
      try {
        return ExperimentProviderUtil.getSingleExperimentFromJson(json);
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return new Experiment();
    }

    public Experiment[] newArray(int size) {
      return new Experiment[size];
    }
  }

  public static final Creator CREATOR = new Creator();

  @JsonProperty("localId")
  private Long id;
  @JsonProperty("id")
  private Long serverId;
  private String joinDate;

  @JsonIgnore
  private String json;
  private ExperimentDAO experimentDelegate;

  private List<Event> events;

  public static final String SCHEDULED_TIME = "scheduledTime";
  public static final String URI_AS_EXTRA = "uriAsExtra";
  public static final String TRIGGERED_TIME = "triggeredTime";
  public static final String TRIGGER_EVENT = "trigger_event";
  public static final String TRIGGER_SOURCE_IDENTIFIER = "sourceIdentifier";
  public static final String TRIGGER_PHONE_CALL_DURATION = "phoneCallDurationMillis";

  public static final String EXPERIMENT_GROUP_NAME_EXTRA_KEY = "experimentGroupName";
  public static final String EXPERIMENT_SERVER_ID_EXTRA_KEY = "experimentServerId";

  public static final String ACTION_TRIGGER_ID = "actionTriggerId";

  public static final String ACTION_TRIGGER_SPEC_ID = "actionTriggerSpecId";

  public static final String ACTION_TRIGGER_SPEC = "actionSpecContents";



  public Experiment() {
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }

  public String getJoinDate() {
    if (experimentDelegate != null) {
      return experimentDelegate.getJoinDate();
    }
    return null;
  }

  public void setJoinDate(String joinDate) {
    this.joinDate = joinDate;
    if (experimentDelegate != null) {
      experimentDelegate.setJoinDate(joinDate);
    }
  }

  @JsonProperty("localId")
  public void setId(Long long1) {
    this.id = long1;
  }

  @JsonProperty("localId")
  public Long getId() {
    return id;
  }

  public int describeContents() {
    return 0;
  }


  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(ExperimentProviderUtil.getJson(this));
  }

  public void unsetId() {
    this.id = null;
  }

  public void setEvents(List<Event> list) {
    this.events = list;
  }

  public List<Event> getEvents() {
    return events;
  }

  @Override
  public String toString() {
    return experimentDelegate.getTitle();
  }

  public boolean isRunning(DateTime now) {
    if (experimentDelegate == null) {
      return false;
    }
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        if (isRunning(now, experimentGroup)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isRunning(DateTime now, ExperimentGroup experimentGroup) {
    if (!experimentGroup.getFixedDuration()) {
      return true;
    }
    DateTime startDate = TimeUtil.unformatDate(experimentGroup.getStartDate());
    DateTime endDate = TimeUtil.unformatDate(experimentGroup.getEndDate());
    return !now.isBefore(startDate) && !now.isAfter(endDate);
  }

  public void setExperimentDAO(com.pacoapp.paco.shared.model2.ExperimentDAO experimentDAO) {
    this.experimentDelegate = experimentDAO;
    this.serverId = experimentDelegate.getId();

  }

  public ExperimentDAO getExperimentDAO() {
    return experimentDelegate;
  }



}
