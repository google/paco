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

package com.google.paco.shared.model;

import java.io.Serializable;


/**
 * 
 * Dumb data object for passing the experiment definition to the
 * GWT client. 
 * 
 * We use this because GWt serialization won't serialize a JDO nucleus object.
 * 
 * @author Bob Evans
 *
 */
public class ExperimentDAO implements Serializable {

  public static final int SCHEDULED_SIGNALING = 1;
  public static final int TRIGGERED_SIGNALING = 1;
  private String title;
  private String description;
  private String informedConsentForm;
  private String creator;
  private Boolean fixedDuration = false;
  private Boolean questionsChange = false;
  private String startDate;
  private String endDate;
  private String hash;
  private Long joinDate;      // change this - PRIYA
  private Long modifyDate;
  private InputDAO[] inputs;
  private Long id;
  private FeedbackDAO[] feedback;
  private Boolean published;
  private String[] admins;
  private String[] publishedUsers;
  private Boolean deleted = false;
  private Boolean webRecommended = false;
  private Integer version;
  private SignalingMechanismDAO[] signalingMechanisms;
  private SignalScheduleDAO schedule;


  /**
   * @param id
   * @param title
   * @param description
   * @param informedConsentForm
   * @param email
   * @param fixedDuration
   * @param questionsChange
   * @param hash
   * @param published TODO
   * @param admins TODO
   */
  public ExperimentDAO(Long id, String title, String description, String informedConsentForm,
      String email, SignalingMechanismDAO[] signalingMechanisms, Boolean fixedDuration, Boolean questionsChange, 
      String startDate, String endDate, String hash, Long joinDate,
      Long modifyDate, Boolean published, String[] admins, String[] publishedUsers, 
      Boolean deleted, Boolean webRecommended, Integer version) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.informedConsentForm = informedConsentForm;
    this.creator = email;
    this.signalingMechanisms = signalingMechanisms;
    setScheduleForBackwardCompatibility();
    this.fixedDuration = fixedDuration;
    this.questionsChange = questionsChange;
    this.startDate = startDate;
    this.endDate = endDate;
    this.hash = hash;
    this.joinDate = joinDate;
    this.modifyDate = modifyDate;
    this.inputs = new InputDAO[0];
    this.feedback = new FeedbackDAO[0];
    this.published = published;
    this.admins = admins;
    this.publishedUsers = publishedUsers;
    this.deleted = deleted;
    this.webRecommended = webRecommended;
    this.version = version;
  }

  /**
   * 
   */
  public ExperimentDAO() {
    super();
    this.inputs = new InputDAO[0];
    this.feedback = new FeedbackDAO[0];
    this.signalingMechanisms = new SignalingMechanismDAO[] { new SignalScheduleDAO()};
    setScheduleForBackwardCompatibility();
    this.admins = new String[0];
    this.publishedUsers = new String[0];
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

  public String getInformedConsentForm() {
    return informedConsentForm;
  }

  public void setInformedConsentForm(String informedConsentForm) {
    this.informedConsentForm = informedConsentForm;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
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
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public Long getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(Long joinDate) {
    this.joinDate = joinDate;
  }

  public Long getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Long modifyDate) {
    this.modifyDate = modifyDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setInputs(InputDAO[] inputDAO) {
    inputs = inputDAO;
  }
  
  public InputDAO[] getInputs() {
    return inputs;
  }
  
  public void setFeedback(FeedbackDAO[] feedbackDAO) {
    feedback = feedbackDAO;
  }
  
  public FeedbackDAO[] getFeedback() {
    return feedback;
  }

  /**
   * @return
   */
  public Boolean getPublished() {
    return published;
  }
  
  public void setPublished(Boolean published) {
    this.published = published;
  }

  /**
   * @return
   */
  public String[] getAdmins() {
    return admins;
  }
  
  public void setAdmins(String[] admins) {
    this.admins = admins;
  }

  /**
   * @return
   */
  public String[] getPublishedUsers() {
    return publishedUsers;
  }

  public void setPublishedUsers(String[] publishedUsers) {
    this.publishedUsers = publishedUsers;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public Boolean getWebRecommended() {
    return webRecommended;
  }

  public void setWebRecommended(Boolean webRecommended) {
    this.webRecommended = webRecommended;
  }

  public Integer getVersion() {
    return version;
  }
  
  public void setVersion(Integer version) {
    this.version = version;
  }

  public SignalingMechanismDAO[] getSignalingMechanisms() {
    return signalingMechanisms;
  }

  public void setSignalingMechanisms(SignalingMechanismDAO[] signalingMechanisms) {
    this.signalingMechanisms = signalingMechanisms;
  }

  
  public void setScheduleForBackwardCompatibility() {
    if (getSignalingMechanisms() != null 
            && getSignalingMechanisms().length > 0 
            && getSignalingMechanisms()[0] instanceof SignalScheduleDAO) {
      schedule = (SignalScheduleDAO) getSignalingMechanisms()[0];
    } else {
      schedule = new SignalScheduleDAO();
      schedule.setScheduleType(SignalScheduleDAO.SELF_REPORT);
    }
  }

  public SignalScheduleDAO getSchedule() {
    return schedule;
  }

  public void setSchedule(SignalScheduleDAO schedule) {
    this.schedule = schedule;
  }
  
  
  
}
