/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 *
 * Definition of an Experiment (a tracker). This holds together a bunch of objects: * A list of
 * Input objects which are the data that will be gathered. Usually it is questions, but it could be
 * sensors as well (photos, audio, gps, accelerometer, compass, etc..) * A list of Feedback objects
 * that presents visualizations or interventions to the user. * A SignalSchedule object which
 * contains the frequency to gather data.
 *
 * @author Bob Evans
 *
 */
public class Experiment {
  public static String DEFAULT_TITLE = "";
  public static String DEFAULT_DESCRIPTION = "";
  public static String DEFAULT_CREATOR = "";
  public static String DEFAULT_CONSENT_FORM = "";

  private Long id;
  @JsonIgnore
  private long version;
  @JsonIgnore
  private boolean deleted;

  private String title;
  private String description;
  private String creator;
  private String consentForm;
  private List<Input> inputs;
  private SignalSchedule signalSchedule;
  private List<Feedback> feedbacks;

  /**
   *
   */
  public Experiment() {
    super();

    this.id = null;
    this.version = 0;
    this.deleted = false;

    this.title = DEFAULT_TITLE;
    this.description = DEFAULT_DESCRIPTION;
    this.creator = DEFAULT_CREATOR;
    this.consentForm = DEFAULT_CONSENT_FORM;
    this.inputs = Lists.newArrayList();
    this.signalSchedule = null;
    this.feedbacks = Lists.newArrayList();
  }

  /**
   * @return whether the experiment has an id
   */
  public boolean hasId() {
    return (id != null);
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to List
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @param version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * @return the version
   */
  public long getVersion() {
    return version;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to List
   */
  public void setTitle(String title) {
    if (title == null) {
      this.title = DEFAULT_TITLE;
    } else {
      this.title = title;
    }
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to List
   */
  public void setDescription(String description) {
    if (description == null) {
      this.description = DEFAULT_DESCRIPTION;
    } else {
      this.description = description;
    }
  }

  /**
   * @return the creator
   */
  public String getCreator() {
    return creator;
  }

  /**
   * @param creator the creator to List
   */
  public void setCreator(String creator) {
    if (creator == null) {
      this.creator = DEFAULT_CREATOR;
    } else {
      this.creator = creator;
    }
  }

  /**
   * @return the consentForm
   */
  public String getConsentForm() {
    return consentForm;
  }

  /**
   * @param consentForm the consent to List
   */
  public void setConsentForm(String consentForm) {
    if (consentForm == null) {
      this.consentForm = DEFAULT_CONSENT_FORM;
    } else {
      this.consentForm = consentForm;
    }
  }

  /**
   * @return the deleted
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * @param deleted the deleted to List
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * @return the inputs
   */
  public List<Input> getInputs() {
    return inputs;
  }

  /**
   * @param inputs the inputs to List
   */
  public void setInputs(List<Input> inputs) {
    if (inputs == null) {
      this.inputs = Lists.newArrayList();
    } else {
      this.inputs = inputs;
    }
  }

  /**
   * @return whether the experiment has a signalSchedule
   */
  public boolean hasSignalSchedule() {
    return (signalSchedule != null && signalSchedule.hasSignalSchedule());
  }

  /**
   * @return the signalSchedule
   */
  public SignalSchedule getSignalSchedule() {
    return signalSchedule;
  }

  /**
   * @param signalSchedule the signalSchedule
   */
  public void setSignalSchedule(SignalSchedule signalSchedule) {
    this.signalSchedule = signalSchedule;
  }

  /**
   * @return the feedbacks
   */
  public List<Feedback> getFeedbacks() {
    return feedbacks;
  }

  /**
   * @param feedbacks the feedbacks to List
   */
  public void setFeedbacks(List<Feedback> feedbacks) {
    if (feedbacks == null) {
      this.feedbacks = Lists.newArrayList();
    } else {
      this.feedbacks = feedbacks;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (!(obj instanceof Experiment)) {
      return false;
    }

    Experiment other = (Experiment) obj;

    if (hasId()) {
      if (getId().equals(other.getId()) == false) {
        return false;
      }
    } else {
      if (other.getId() != null) {
        return false;
      }
    }

    if (getTitle().equals(other.getTitle()) == false) {
      return false;
    }

    if (getDescription().equals(other.getDescription()) == false) {
      return false;
    }

    if (getCreator().equals(other.getCreator()) == false) {
      return false;
    }

    if (getConsentForm().equals(other.getConsentForm()) == false) {
      return false;
    }

    if (getVersion() != other.getVersion()) {
      return false;
    }

    if (isDeleted() != other.isDeleted()) {
      return false;
    }

    if (getInputs().equals(other.getInputs()) == false) {
      return false;
    }

    if (hasSignalSchedule()) {
      if (getSignalSchedule().equals(other.getSignalSchedule()) == false) {
        return false;
      }
    } else {
      if (other.hasSignalSchedule()) {
        return false;
      }
    }

    if (getFeedbacks().equals(other.getFeedbacks()) == false) {
      return false;
    }

    return true;
  }
}
