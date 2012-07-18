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

package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.collect.Lists;

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
public class Experiment implements Serializable {

  @Id
  private Long id = null;

  private String title = "";

  private String description = "";

  @Deprecated
  @JsonIgnore
  private String informedConsentForm = "";

  private String creator = "";

  @Embedded
  private SignalSchedule schedule = new SignalSchedule();

  private Boolean fixedDuration = false;

  private Boolean questionsChange = false;

  private Date startDate = new Date();

  private Date endDate = new Date();

  private String hash = "";

  private Date joinDate = new Date();

  @Embedded
  private List<Input> inputs = Lists.newArrayList();

  @Embedded
  private List<Feedback> feedbacks = Lists.newArrayList();

  private Date modifyDate = new Date();

  Boolean deleted = false;

  /**
   * Is this experiment available to anyone
   */
  private Boolean published = false;

  private List<String> admins = Lists.newArrayList();;

  private List<String> publishedUsers = Lists.newArrayList();

  @JsonProperty("informedConsentForm")
  private String informedConsentFormText;

  public Experiment(Long id, String title, String description, String creator,
      String informedConsentForm, Boolean questionsCanChange, SignalSchedule schedule,
      Date modifyDate, Boolean published, List<String> admins) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.creator = creator;
    this.informedConsentForm = informedConsentForm;
    this.schedule = schedule;
    this.questionsChange = questionsCanChange;
    this.modifyDate = modifyDate;
    this.published = published;
    this.admins = admins;
    if (this.admins == null) {
      this.admins = Lists.newArrayList(creator);
    } else if (admins.size() == 0 || !admins.contains(creator)) {
      admins.add(0, creator);
    }
  }

  public Experiment() { }

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

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public SignalSchedule getSchedule() {
    return schedule;
  }

  public void setSchedule(SignalSchedule schedule) {
    this.schedule = schedule;
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

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public Date getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(Date joinDate) {
    this.joinDate = joinDate;
  }

  public List<Input> getInputs() {
    return this.inputs;
  }

  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  public List<Feedback> getFeedback() {
    return feedbacks;
  }

  public void setFeedback(List<Feedback> feedbacks) {
    this.feedbacks = feedbacks;
  }

  public Date getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
  }

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

  public void setPublishedUsers(List<String> publishedUsers) {
    this.publishedUsers = publishedUsers;
  }

  public List<String> getPublishedUsers() {
    return publishedUsers;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean delete) {
    this.deleted = delete;
  }

  @JsonProperty("informedConsentForm")
  public String getInformedConsentFormText() {
    if (informedConsentFormText == null && informedConsentForm != null) {
      informedConsentFormText = new String(informedConsentForm);
      informedConsentForm = null;
    }

    if (informedConsentFormText != null) {
      return informedConsentFormText;
    } else {
      return null;
    }
  }

  @JsonProperty("informedConsentForm")
  public void setInformedConsentFormText(String informedConsentForm2) {
    this.informedConsentFormText = informedConsentForm2;
  }

  public Input getInputWithName(String name) {
    if (name == null) {
      return null;
    }
    for (Input input : getInputs()) {
      if (name.equals(input.getName())) {
        return input;
      }
    }
    return null;
  }

  /*
  @JsonIgnore
  public boolean isOver(DateTime now) {
    return getFixedDuration() != null && getFixedDuration() && now.isAfter(getEndDateTime());
  }

  private DateTime getEndDateTime() {
    if (getSchedule().getScheduleType().equals(SignalSchedule.WEEKDAY)) {
      List<Long> times = this.getSchedule().getTimes();
      // get the latest time
      Collections.sort(times);

      DateTime lastTimeForDay = new DateTime().plus(times.get(times.size() - 1));
      return new DateMidnight(getEndDate()).toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /*if (getScheduleType().equals(SCHEDULE_TYPE_ESM))*/ /*{
      return new DateMidnight(getEndDate()).plusDays(1).toDateTime();
    }
  }
  */
}
