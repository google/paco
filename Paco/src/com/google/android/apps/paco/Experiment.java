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

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ActionTrigger;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Input2;
import com.google.paco.shared.model2.InterruptCue;
import com.google.paco.shared.model2.InterruptTrigger;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.google.paco.shared.util.ExperimentHelper;
import com.google.paco.shared.util.TimeUtil;

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
    return joinDate;
  }

  public void setJoinDate(String joinDate) {
    this.joinDate = joinDate;
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



  @JsonIgnore
  public Input2 getInputByName(String inputName) {
    return ExperimentHelper.getInputWithName(experimentDelegate, inputName, null);
  }

  @Override
  public String toString() {
    return experimentDelegate.getTitle();
  }

  @JsonIgnore
  public List<Pair<ExperimentGroup, InterruptTrigger>> shouldTriggerBy(int event, String sourceIdentifier) {
    List<Pair<ExperimentGroup, InterruptTrigger>> groupsThatTrigger = Lists.newArrayList();
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger) actionTrigger;
          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            boolean cueCodeMatches = interruptCue.getCueCode() == event;
            if (!cueCodeMatches) {
              continue;
            }

            boolean usesSourceId = interruptCue.getCueCode() == InterruptCue.PACO_ACTION_EVENT || interruptCue.getCueCode() == InterruptCue.APP_USAGE;
            boolean sourceIdsMatch;
            boolean triggerSourceIdIsEmpty = Strings.isNullOrEmpty(interruptCue.getCueSource());
            if (usesSourceId) {
              boolean paramEmpty = Strings.isNullOrEmpty(sourceIdentifier);
              sourceIdsMatch = (paramEmpty && triggerSourceIdIsEmpty) ||
                interruptCue.getCueSource().equals(sourceIdentifier);
            } else {
              sourceIdsMatch = true;
            }
            if (cueCodeMatches && sourceIdsMatch) {
              groupsThatTrigger.add(new Pair(experimentGroup, trigger));
            }
          }
        }
      }
    }
    return groupsThatTrigger;
  }

  @JsonIgnore
  public boolean shouldWatchProcesses() {
    return hasAppUsageTrigger() || isLogActions();
  }

  boolean isLogActions() {
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (experimentGroup.getLogActions()) {
        return true;
      }
    }
    return false;
  }

  @JsonIgnore
  public boolean hasAppUsageTrigger() {
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger instanceof InterruptTrigger) {
          InterruptTrigger trigger = (InterruptTrigger)actionTrigger;
          List<InterruptCue> cues = trigger.getCues();
          for (InterruptCue interruptCue : cues) {
            if (interruptCue.getCueCode() == InterruptCue.APP_USAGE) {
              return true;
            }
          }

        }
      }
    }
    return false;
  }

  public boolean isRunning(DateTime now) {
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (isRunning(now, experimentGroup)) {
        return true;
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

  private boolean isAnyGroupOngoingDuration() {
    List<ExperimentGroup> groups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration()) {
        return true;
      }
    }
    return false;
  }

  @JsonIgnore
  public boolean declaresLogAppUsageAndBrowserCollection() {
    return experimentDelegate.getExtraDataCollectionDeclarations() != null
            && experimentDelegate.getExtraDataCollectionDeclarations().contains(ExperimentDAO.APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION);
  }

  public void setExperimentDAO(com.google.paco.shared.model2.ExperimentDAO experimentDAO) {
    this.experimentDelegate = experimentDAO;
    this.serverId = experimentDelegate.getId();

  }

  public ExperimentDAO getExperimentDAO() {
    return experimentDelegate;
  }

  public boolean hasUserEditableSchedule() {
    List<ExperimentGroup> experimentGroups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      List<ActionTrigger> triggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : triggers) {
        if (actionTrigger.getUserEditable() && actionTrigger instanceof ScheduleTrigger) {
          return true;
        }
      }
    }
    return false;
  }

  public List<ExperimentGroup> isBackgroundListeningForSourceId(String sourceIdentifier) {
    List<ExperimentGroup> listeningExperimentGroups  = Lists.newArrayList();
    List<ExperimentGroup> experimentGroups = experimentDelegate.getGroups();
    for (ExperimentGroup experimentGroup : experimentGroups) {
      if (experimentGroup.getBackgroundListen() && experimentGroup.getBackgroundListenSourceIdentifier().equals(sourceIdentifier)) {
        listeningExperimentGroups.add(experimentGroup);
      }
    }
    return listeningExperimentGroups;
  }


}
