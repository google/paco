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
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.sampling.experiential.shared.TimeUtil;


/**
 *
 * Definition of an Experiment (a tracker). This holds together a bunch of objects:
 * * A list of Input objects which are the data that will be gathered.
 *    Usually it is questions, but it could be sensors as well (photos, audio, gps, accelerometer,
 *    compass, etc..)
 * * A list of Feedback objects that presents visualizations or interventions to the user.
 * * A SignalSchedule object which contains the frequency to gather data.
 *
 * @author Bob Evans
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Experiment {

  private static final long serialVersionUID = -1407635488794262589l;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String title;

  @Persistent
  private String description;

  @Persistent
  @Deprecated
  @JsonIgnore
  private String informedConsentForm;

  @Persistent
  private User creator;

  @Persistent(defaultFetchGroup="true")
  @Element(dependent = "true")
  private SignalSchedule schedule;

  @Persistent(defaultFetchGroup="true")
  @Element(dependent = "true")
  private Trigger trigger;

  @Persistent
  private Boolean fixedDuration;

  @Persistent
  private Boolean questionsChange;

  @Persistent
  private Date startDate;

  @Persistent
  private Date endDate;

  @Persistent
  private String hash;

  @Persistent
  private Date joinDate;

  @Persistent(mappedBy = "experiment")
  @Element(dependent = "true")
  private List<Input> inputs;

  @Persistent(mappedBy = "experiment")
  @Element(dependent = "true")
  private List<Feedback> feedback;

  @Persistent
  private Date modifyDate;

  @Persistent Boolean deleted = false;
  /**
   * Is this experiment available to anyone
   */
  @Persistent
  private Boolean published;

  @Persistent
  private List<String> admins;

  @Persistent
  private ArrayList<String> publishedUsers;

  @Persistent
  @JsonProperty("informedConsentForm")
  private Text informedConsentFormText;

  @Persistent
  private Integer version;

  @Persistent
  private Boolean isCustomRendering = false;

  @Persistent
  private Text customRenderingCode;

  @Persistent
  private Integer feedbackType;

  @Persistent
  private Boolean backgroundListen = false;

  @Persistent
  private String backgroundListenSourceIdentifier = "";

  @Persistent
  private Boolean logActions = false;

  @Persistent
  private Boolean recordPhoneDetails = false;

  @Persistent
  private List<Integer> extraDataCollectionDeclarations;


  /**
   * @param id
   * @param title2
   * @param description2
   * @param creator2
   * @param informedConsentForm2
   * @param questionsCanChange
   * @param modifyDate
   * @param published TODO
   * @param admins TODO
   */
  public Experiment(Long id, String title, String description, User creator,
      String informedConsentForm, Boolean questionsCanChange, SignalSchedule schedule,
      String modifyDate, Boolean published, List<String> admins) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.creator = creator;
    this.informedConsentForm = informedConsentForm;
    this.schedule = schedule;
    this.questionsChange = questionsCanChange;
    this.modifyDate = getFormattedDate(modifyDate, TimeUtil.DATE_FORMAT);
    this.inputs = Lists.newArrayList();
    feedback = Lists.newArrayList();
    this.published = published;
    this.admins = admins;
    if (this.admins == null) {
      this.admins = Lists.newArrayList(creator.getEmail());
    } else if (admins.size() == 0 || !admins.contains(creator.getEmail())) {
      admins.add(0, creator.getEmail());
    }
  }

  /**
   *
   */
  public Experiment() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @JsonIgnore
  public String getInformedConsentForm() {
    return informedConsentForm;
  }

  @JsonIgnore
  public void setInformedConsentForm(String informedConsentForm) {
    this.informedConsentForm = informedConsentForm;
  }

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public SignalSchedule getSchedule() {
    return schedule;
  }

  public void setSchedule(SignalSchedule schedule) {
    this.schedule = schedule;
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public Boolean getFixedDuration() {
    return fixedDuration;
  }

  public void setFixedDuration(Boolean fixedDuration) {
    this.fixedDuration = fixedDuration;
  }

  public Boolean getQuestionsChange() {
    return questionsChange;
  }

  public void setQuestionsChange(Boolean questionsChange) {
    this.questionsChange = questionsChange;
  }

  public String getStartDate() {
    return getDateAsString(startDate, TimeUtil.DATE_FORMAT);
  }

  public void setStartDate(String startDateStr) {
      setFormattedStartDate(startDateStr);
  }

  private void setFormattedStartDate(String startDateStr) {
    this.startDate = getFormattedDate(startDateStr, TimeUtil.DATE_FORMAT);
  }

  public String getEndDate() {
    return getDateAsString(endDate, TimeUtil.DATE_FORMAT);
  }

  @JsonIgnore
  public Date getEndDateAsDate() {
    return endDate;
  }


  public void setEndDate(String endDateStr) {
      setFormattedEndDate(endDateStr);
  }

  private void setFormattedEndDate(String endDateStr) {
    this.endDate = getFormattedDate(endDateStr, TimeUtil.DATE_FORMAT);
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getJoinDate() {
    return getJoinDateAsString();
  }

  private String getJoinDateAsString() {
    return getDateAsString(joinDate, TimeUtil.DATE_WITH_ZONE_FORMAT);
  }

  public void setJoinDate(String joinDateStr) {
      setFormattedJoinDate(joinDateStr);
  }

  private void setFormattedJoinDate(String joinDateStr) {
    this.joinDate = getFormattedDate(joinDateStr, TimeUtil.DATE_WITH_ZONE_FORMAT);
  }

  public List<Input> getInputs() {
    return inputs;
  }

  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  public List<Feedback> getFeedback() {
    return feedback;
  }

  public void setFeedback(List<Feedback> feedback) {
    this.feedback = feedback;
  }

  public String getModifyDate() {
    return getDateAsString(modifyDate, TimeUtil.DATE_FORMAT);
  }

  public void setModifyDate(String modifyDateStr) {
      setFormattedModifyDate(modifyDateStr);
  }

  private void setFormattedModifyDate(String modifyDateStr) {
    this.modifyDate = getFormattedDate(modifyDateStr, TimeUtil.DATE_FORMAT);
  }
  /**
   * @param published
   */
  public void setPublished(Boolean published) {
    this.published = published;
  }

  public Boolean getPublished() {
    return published;
  }

  public List<String> getAdmins() {
    return admins;
  }

  public void setAdmins(List<String> admins) {
    this.admins = admins;
  }

  /**
   * @param newArrayList
   */
  public void setPublishedUsers(ArrayList<String> newArrayList) {
    this.publishedUsers = newArrayList;
  }

  public ArrayList<String> getPublishedUsers() {
    return publishedUsers;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean delete) {
    this.deleted = delete;
  }

  /**
   * @param inputId
   * @return
   */
  public Input getInputWithId(long inputId) {
    for (Input input : getInputs()) {
      if (input.getId().getId() == inputId) {
        return input;
      }
    }
    return null;
  }

  /**
   * @return
   */
  @JsonProperty("informedConsentForm")
  public String getInformedConsentFormText() {
    if (informedConsentFormText == null && informedConsentForm != null) {
      informedConsentFormText = new Text(informedConsentForm);
      informedConsentForm = null;
    }
    if (informedConsentFormText != null) {
      return informedConsentFormText.getValue();
    } else {
      return null;
    }
  }

  /**
   * @param informedConsentForm2
   */
  @JsonProperty("informedConsentForm")
  public void setInformedConsentFormText(String informedConsentForm2) {
    this.informedConsentFormText = new Text(informedConsentForm2);
  }

  public Input getInputWithName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return null;
    }
    for (Input input : getInputs()) {
      if (name.equals(input.getName())) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public boolean isOver(DateTime now) {
    return getFixedDuration() != null && getFixedDuration() && now.isAfter(getEndDateTime());
  }

  @JsonIgnore
  public boolean isPublic() {
    return (getPublished() != null && getPublished() == true) &&
            (getPublishedUsers() == null || getPublishedUsers().isEmpty());
  }

  @JsonIgnore
  private DateTime getEndDateTime() {
    if (getSchedule() != null && getSchedule().getScheduleType().equals(SignalScheduleDAO.WEEKDAY)) {
      List<SignalTime> signalTimes = schedule.getSignalTimes();
      List<Integer> times = Lists.newArrayList();
      for (SignalTime signalTime : signalTimes) {
        // TODO adjust for offset times and include them
        if (signalTime.getType() == SignalTimeDAO.FIXED_TIME) {
          times.add(signalTime.getFixedTimeMillisFromMidnight());
        }
      }
      // get the latest time
      Collections.sort(times);
      DateTime lastTimeForDay = new DateTime().plus(times.get(times.size() - 1));
      return new DateMidnight(endDate)
          .toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /*if (getScheduleType().equals(SCHEDULE_TYPE_ESM))*/ {
      return new DateMidnight(endDate).plusDays(1).toDateTime();
    }
  }

  private String getDateAsString(Date date, String dateFormat) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    return formatter.format(date);
  }

  private Date getFormattedDate(String inputDateStr, String dateFormat) {
    if (inputDateStr == null) {
      return null;
    }
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    try {
      return formatter.parse(inputDateStr);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Cannot parse date: " + inputDateStr +
                                         ". Format is " + dateFormat);
    }
  }

  @JsonIgnore
  public boolean isWhoAllowedToPostToExperiment(String who) {
    who = who.toLowerCase();
    return isAdmin(who) ||
      (getPublished() && (getPublishedUsers().isEmpty() || getPublishedUsers().contains(who)));
  }

  public boolean isAdmin(String who) {
    return getAdmins().contains(who);
  }

  public Integer getVersion() {
    return this.version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Boolean isCustomRendering() {
    return isCustomRendering;
  }

  public void setCustomRendering(Boolean customHtml) {
    this.isCustomRendering = customHtml;
  }

  public String getCustomRenderingCode() {
    return customRenderingCode != null ? customRenderingCode.getValue() : null;
  }

  public void setCustomRenderingCode(String customRenderingCode) {
    if (!Strings.isNullOrEmpty(customRenderingCode)) {
      this.customRenderingCode = new Text(customRenderingCode);
    } else {
      this.customRenderingCode = null;
    }

  }

  public Integer getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(Integer feedbackType2) {
    this.feedbackType = feedbackType2;

  }

  public Boolean shouldLogActions() {
    return logActions;
  }

  public void setLogActions(Boolean val) {
    this.logActions = val;
  }

  public Boolean isRecordPhoneDetails() {
    return recordPhoneDetails;
  }

  public void setRecordPhoneDetails(Boolean recordPhoneDetails) {
    this.recordPhoneDetails = recordPhoneDetails;
  }

  public Boolean isBackgroundListen() {
    return backgroundListen;
  }

  public void setBackgroundListen(Boolean backgroundListen) {
    this.backgroundListen = backgroundListen;
  }

  public String getBackgroundListenSourceIdentifier() {
    return backgroundListenSourceIdentifier;
  }

  public void setBackgroundListenSourceIdentifier(String sourceId) {
    this.backgroundListenSourceIdentifier = sourceId;
  }

  public List<Integer> getExtraDataCollectionDeclarations() {
    return extraDataCollectionDeclarations;
  }

  public void setExtraDataCollectionDeclarations(List<Integer> dataCollectionDeclarations) {
    this.extraDataCollectionDeclarations = dataCollectionDeclarations;
  }
}
