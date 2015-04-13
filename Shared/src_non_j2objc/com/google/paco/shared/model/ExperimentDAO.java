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
import java.util.ArrayList;
import java.util.List;



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
public class ExperimentDAO extends ExperimentDAOCore implements Serializable {

  public static final int SCHEDULED_SIGNALING = 1;
  public static final int TRIGGERED_SIGNALING = 1;

  private Boolean questionsChange = false;

  private String hash;
  private String modifyDate;
  private InputDAO[] inputs;
  private FeedbackDAO[] feedback;
  private Boolean published;
  private String[] admins;
  private String[] publishedUsers;
  private Boolean deleted = false;
  private Boolean webRecommended = false;
  private Integer version;
  protected SignalingMechanismDAO[] signalingMechanisms;
  protected SignalScheduleDAO schedule;
  protected Boolean customRendering = false;
  protected String customRenderingCode;
  protected Integer feedbackType;

  /**
   * @param id
   * @param title
   * @param description
   * @param informedConsentForm
   * @param email
   * @param fixedDuration
   * @param questionsChange
   * @param hash
   * @param published
   * @param admins
   * @param customRenderingCode
   * @param feedbackType2
   * @param backgroundListen
   * @param backgroundListenSourceIdentifier
   * @param logActions TODO
   * @param recordPhoneDetails TODO
   * @param extraDataCollectionDeclarations TODO
   * @param showFeedback
   * @param hasCustomFeedback
   * @param customHtml
   */
  public ExperimentDAO(Long id, String title, String description, String informedConsentForm,
      String email, SignalingMechanismDAO[] signalingMechanisms, Boolean fixedDuration, Boolean questionsChange,
      String startDate, String endDate, String hash, String joinDate,
      String modifyDate, Boolean published, String[] admins, String[] publishedUsers,
      Boolean deleted, Boolean webRecommended, Integer version, Boolean customRendering, String customRenderingCode,
      Integer feedbackType2, Boolean backgroundListen, String backgroundListenSourceIdentifier, Boolean logActions, Boolean recordPhoneDetails, List<Integer> extraDataCollectionDeclarations) {

    super(id, title, description, informedConsentForm, email, fixedDuration, startDate, endDate, joinDate, backgroundListen,
        backgroundListenSourceIdentifier, logActions, recordPhoneDetails, extraDataCollectionDeclarations);
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
    this.modifyDate = modifyDate;
    this.inputs = new InputDAO[0];
    this.feedback = new FeedbackDAO[0];
    this.published = published;
    this.admins = admins;
    this.publishedUsers = publishedUsers;
    this.deleted = deleted;
    this.webRecommended = webRecommended;
    this.version = version;
    if (customRendering != null) {
      this.customRendering = customRendering;
    }
    this.customRenderingCode = customRenderingCode;
    this.feedbackType = feedbackType2;
  }

  /**
   *
   */
  public ExperimentDAO() {
    super();
    this.inputs = new InputDAO[0];
    this.feedback = new FeedbackDAO[0];
    this.admins = new String[0];
    this.publishedUsers = new String[0];
  }

  public Boolean getQuestionsChange() {
    return questionsChange;
  }

  public void setQuestionsChange(Boolean questionsChange) {
    this.questionsChange = questionsChange;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(String modifyDate) {
    this.modifyDate = modifyDate;
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

  public Boolean isCustomRendering() {
    return customRendering;
  }

  public void setCustomRendering(Boolean isCustom) {
    customRendering = isCustom;
  }

  public String getCustomRenderingCode() {
    return customRenderingCode;
  }

  public void setCustomRenderingCode(String customRenderingCode) {
    this.customRenderingCode = customRenderingCode;
  }

  public Integer getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(Integer i) {
    this.feedbackType = i;

  }
}
