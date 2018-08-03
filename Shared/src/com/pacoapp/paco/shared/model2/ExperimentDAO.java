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

package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;




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

  private static final String DEFAULT_POST_INSTALL_INSTRUCTIONS = "<b>You have successfully joined the experiment!</b><br/><br/>"
          + "No need to do anything else for now.<br/><br/>"
          + "Paco will send you a notification when it is time to participate.<br/><br/>"
          + "Be sure your ringer/buzzer is on so you will hear the notification.";
  private String modifyDate;
  private Boolean published;
  private List<String> admins;
  private List<String> publishedUsers;
  private Integer version = 1;
  protected List<ExperimentGroup> groups;
  private String ringtoneUri;
  private String postInstallInstructions;
  private Boolean anonymousPublic;
  private List<Visualization> visualizations;


  // Visible for testing
  public ExperimentDAO(Long id, String title, String description, String informedConsentForm,
      String email, String publicKey,
      String joinDate,
      String modifyDate, Boolean published, List<String> admins, List<String> publishedUsers,
      Boolean deleted, Integer version, Boolean recordPhoneDetails, List<ExperimentGroup> groups,
      List<Integer> extraDataDeclarations, Boolean anonymousPublic, List<Visualization> visualizations) {
    super(id, title, description, informedConsentForm, email, publicKey, joinDate, recordPhoneDetails, deleted, extraDataDeclarations, null, null, null, null, null);
    this.id = id;
    this.title = title;
    this.description = description;
    this.informedConsentForm = informedConsentForm;
    this.creator = email;
    this.modifyDate = modifyDate;
    this.published = published;
    this.admins = ListMaker.paramOrNewList(admins, String.class);
    this.publishedUsers = ListMaker.paramOrNewList(publishedUsers, String.class);
    this.version = version;
    this.groups = ListMaker.paramOrNewList(groups, ExperimentGroup.class);
    this.anonymousPublic = anonymousPublic;
    this.visualizations = visualizations;
  }

  /**
   *
   */
  public ExperimentDAO() {
    super();
    this.admins = new java.util.ArrayList();
    this.publishedUsers = new java.util.ArrayList();
    this.groups = new java.util.ArrayList();
    this.visualizations = new java.util.ArrayList();
  }

  public String getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(String modifyDate) {
    this.modifyDate = modifyDate;
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

  public Boolean getAnonymousPublic() {
    return anonymousPublic;
  }

  public void setAnonymousPublic(Boolean anonymousPublic) {
    this.anonymousPublic = anonymousPublic;
  }


  /**
   * @return
   */
  public List<String> getAdmins() {
    return admins;
  }

  public void setAdmins(List<String> admins) {
    this.admins = admins;
  }

  /**
   * @return
   */
  public List<String> getPublishedUsers() {
    return publishedUsers;
  }

  public void setPublishedUsers(List<String> publishedUsers) {
    this.publishedUsers = publishedUsers;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public List<ExperimentGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<ExperimentGroup> groups) {
    this.groups = groups;
  }

  //@JsonIgnore
  public ExperimentGroup getGroupByName(String groupName) {
    for (ExperimentGroup group : groups) {
      if (group.getName().equals(groupName)) {
        return group;
      }
    }
    return null;
  }

  //@JsonIgnore
  public boolean isWhoAllowedToPostToExperiment(String who) {
    if (getPublished() != null && getPublished() && getAnonymousPublic() != null && getAnonymousPublic()) {
      return true;
    }
    who = who.toLowerCase();
    return isAdmin(who) ||
      (getPublished() && (getPublishedUsers().isEmpty() || getPublishedUsers().contains(who)));
  }

  //@JsonIgnore
  public boolean isAdmin(String who) {
    return getAdmins().contains(who);
  }

  @Override
  public void validateWith(Validator validator) {
    super.validateWith(validator);
    validator.isValidCollectionOfEmailAddresses(admins, "admins should be a valid list of email addresses");
    validator.isNotNullAndNonEmptyCollection(groups, "there should be at least one experiment group");
    if (publishedUsers != null && !publishedUsers.isEmpty()) {
      validator.isValidCollectionOfEmailAddresses(publishedUsers, "published users should contain valid email addresses");
    }
    List<String> groupNames = Lists.newArrayList();
    List<ExperimentGroup> endOfDayGroups = Lists.newArrayList();
    for (ExperimentGroup group : groups) {
      if (groupNames.contains(group.getName())) {
        validator.addError("Group name: " + group.getName() + " is not unique. Group names must be unique");
      }
      groupNames.add(group.getName());
      group.validateWith(validator);
      if (group.getEndOfDayGroup()) {
        endOfDayGroups.add(group);
      }
    }

    if (!endOfDayGroups.isEmpty()) {
      for (ExperimentGroup experimentGroup : endOfDayGroups) {
        String referredGroup = experimentGroup.getEndOfDayReferredGroupName();
        if (referredGroup != null) {
          validator.isTrue(groupNames.contains(referredGroup),
                           "the group to which the end of day group refers does not exist");
        }
      }
    }


    validator.isNotNull(version, "version is unspecified");
    if (version != null && version > 1) {
      validator.isNotNull(id, "editing an existing version, database id should not be null");
    }

  }

  public String getRingtoneUri() {
    return ringtoneUri;
  }

  public void setRingtoneUri(String ringtoneUri) {
    this.ringtoneUri = ringtoneUri;
  }

  public String getPostInstallInstructions() {
    return postInstallInstructions != null ? postInstallInstructions : DEFAULT_POST_INSTALL_INSTRUCTIONS;
  }

  public void setPostInstallInstructions(String instructions) {
    this.postInstallInstructions = instructions;
  }

  public List<Visualization> getVisualizations() {
    return visualizations;
  }

  public void setVisualizations(List<Visualization> visualizations) {
    this.visualizations = visualizations;
  }
}
