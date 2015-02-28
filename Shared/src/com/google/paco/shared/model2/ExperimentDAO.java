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

package com.google.paco.shared.model2;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Strings;
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

  private String modifyDate;
  private Boolean published;
  private List<String> admins;
  private List<String> publishedUsers;
  private Integer version = 1;
  protected List<ExperimentGroup> groups;

  // Visible for testing
  public ExperimentDAO(Long id, String title, String description, String informedConsentForm,
      String email,
      String joinDate,
      String modifyDate, Boolean published, List<String> admins, List<String> publishedUsers,
      Boolean deleted, Integer version, Boolean recordPhoneDetails, List<ExperimentGroup> groups,
      List<Integer> extraDataDeclarations) {
    super(id, title, description, informedConsentForm, email, joinDate, recordPhoneDetails, deleted, extraDataDeclarations, null, null, null);
    this.id = id;
    this.title = title;
    this.description = description;
    this.informedConsentForm = informedConsentForm;
    this.creator = email;
    this.modifyDate = modifyDate;
    this.published = published;
    this.admins = admins;
    this.publishedUsers = publishedUsers;
    this.version = version;
    this.groups = groups;
  }

  /**
   *
   */
  public ExperimentDAO() {
    super();
    this.admins = new java.util.ArrayList();
    this.publishedUsers = new java.util.ArrayList();
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
    who = who.toLowerCase();
    return isAdmin(who) ||
      (getPublished() && (getPublishedUsers().isEmpty() || getPublishedUsers().contains(who)));
  }

  //@JsonIgnore
  public boolean isAdmin(String who) {
    return getAdmins().contains(who);
  }

  public Input2 getInputWithName(String name, String groupName) {
    if (Strings.isNullOrEmpty(name)) {
      return null;
    }
    List<Input2> inputs = null;
    if (groupName == null || groupName.isEmpty()) {
      inputs = getInputs();
    } else {
      ExperimentGroup group = getGroupByName(groupName);
      if (group != null) {
        inputs = group.getInputs();
      }
    }
    if (inputs != null) {
      for (Input2 input : inputs) {
        if (name.equals(input.getName())) {
          return input;
        }
      }
    }
    return null;
  }

  //@JsonIgnore
  private List<Input2> getInputs() {
    List<Input2> inputs = new java.util.ArrayList<Input2>();
    for (ExperimentGroup group : getGroups()) {
      inputs.addAll(group.getInputs());
    }
    return inputs;
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

}
