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
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.SignalScheduleDAO;


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
      Date modifyDate, Boolean published, List<String> admins) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.creator = creator;
    this.informedConsentForm = informedConsentForm;
    this.schedule = schedule;
    this.questionsChange = questionsCanChange;
    this.modifyDate = modifyDate;
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

  public Date getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
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
  
  @JsonIgnore
  public boolean isOver(DateTime now) {
    return getFixedDuration() != null && getFixedDuration() && now.isAfter(getEndDateTime());
  }
  
  private DateTime getEndDateTime() {
    if (getSchedule().getScheduleType().equals(SignalScheduleDAO.WEEKDAY)) { 
      List<Long> times = schedule.getTimes();
      // get the latest time
      Collections.sort(times);
      
      DateTime lastTimeForDay = new DateTime().plus(times.get(times.size() - 1));
      return new DateMidnight(getEndDate()).toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /*if (getScheduleType().equals(SCHEDULE_TYPE_ESM))*/ {
      return new DateMidnight(getEndDate()).plusDays(1).toDateTime();
    }
  }
}
