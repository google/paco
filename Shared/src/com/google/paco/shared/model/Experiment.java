/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.paco.shared.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An experiment contains all the details necessary for running an experiment,
 * including a title, description, a list of inputs, a signal-schedule, and the
 * feedback. It also maintains an access control list that limits which users
 * can edit, join, and view it. Finally, it contains some meta-data about the
 * state of the experiment.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class Experiment {
  public static String DEFAULT_TITLE = "";
  public static String DEFAULT_DESCRIPTION = "";
  public static String DEFAULT_CREATOR = "";
  public static String DEFAULT_CONSENT_FORM = "";
  public static String DEFAULT_FEEDBACK = "";

  private Long id;
  private long version;
  @JsonIgnore
  private boolean deleted;

  private String title;
  private String description;
  private String creator;
  private String consentForm;
  private List<Input> inputs;
  private SignalSchedule signalSchedule;
  private String feedback;
  private boolean published;
  private Set<String> observers; // List of users who can edit this experiment
  private Set<String> subjects; // List of users who have joined this experiment
  private Set<String> viewers; // List of users who can view this experiment, null if anyone

  /**
   * Default constructor with sane defaults.
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
    this.feedback = DEFAULT_FEEDBACK;

    this.published = false;
    this.observers = Sets.newHashSet();
    this.subjects = Sets.newHashSet();
    this.viewers = null;
  }

  /**
   * @return whether the experiment has an id
   */
  public boolean hasId() {
    return (id != null);
  }

  /**
   * @return the id of the experiment, or null if none.
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id of the experiment (or null if none)
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * This value should be incremented each time the experiment is edited by an
   * observer. It serves as both a lock and a mechanism for keeping track of
   * which set of inputs a user is responding to. Thus, the whole experiment
   * ought to be copied each time it is edited as well.
   *
   * @param version the version of the experiment
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * @return the version of the experiment
   */
  public long getVersion() {
    return version;
  }

  /**
   * @return the title of the experiment
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title of the experiment (if null, sets to DEFAULT_TITLE)
   */
  public void setTitle(String title) {
    if (title == null) {
      this.title = DEFAULT_TITLE;
    } else {
      this.title = title;
    }
  }

  /**
   * @return a description of the experiment
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description a description of the experiment (if null, sets to DEFAULT_DESCRIPTION)
   */
  public void setDescription(String description) {
    if (description == null) {
      this.description = DEFAULT_DESCRIPTION;
    } else {
      this.description = description;
    }
  }

  /**
   * @return the creator of the experiment
   */
  public String getCreator() {
    return creator;
  }

  /**
   * @param creator the creator of the experiment (if null, sets to DEFAULT_CREATOR)
   */
  public void setCreator(String creator) {
    if (creator == null) {
      this.creator = DEFAULT_CREATOR;
    } else {
      this.creator = creator;
    }
  }

  /**
   * @return a consent form used to obtain consent from the user
   */
  public String getConsentForm() {
    return consentForm;
  }

  /**
   * @param consentForm a consent form (if null, sets to DEFAULT_CONSENT_FORM)
   */
  public void setConsentForm(String consentForm) {
    if (consentForm == null) {
      this.consentForm = DEFAULT_CONSENT_FORM;
    } else {
      this.consentForm = consentForm;
    }
  }

  /**
   * @return whether the experiment is (soft) deleted or not
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * @param deleted if the experiment should be (soft) deleted
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * @return a list inputs
   */
  public List<Input> getInputs() {
    return inputs;
  }

  /**
   * @param inputs a list of inputs (if null, defaults to an empty list)
   */
  public void setInputs(List<Input> inputs) {
    if (inputs == null) {
      this.inputs = Lists.newArrayList();
    } else {
      this.inputs = inputs;
    }
  }

  /**
   * @return whether the experiment has a signal-schedule
   */
  public boolean hasSignalSchedule() {
    return (signalSchedule != null && signalSchedule.hasSignalSchedule());
  }

  /**
   * @return the signal-schedule of the experiment
   */
  public SignalSchedule getSignalSchedule() {
    return signalSchedule;
  }

  /**
   * @param signalSchedule the signal-schedule of the experiment
   */
  public void setSignalSchedule(SignalSchedule signalSchedule) {
    this.signalSchedule = signalSchedule;
  }

  /**
   * @return the feedback to display after the user as answered the inputs
   */
  public String getFeedback() {
    return feedback;
  }

  /**
   * @param feedback the feedback to display to the user
   */
  public void setFeedback(String feedback) {
    if (feedback == null) {
      this.feedback = DEFAULT_FEEDBACK;
    } else {
      this.feedback = feedback;
    }
  }

  /**
   * @return whether the experiment is published
   */
  public boolean isPublished() {
    return published;
  }

  /**
   * @param published whether the experiment should be published
   */
  public void setPublished(boolean published) {
    this.published = published;
  }

  /**
   * @return the set of users who can observe this experiment
   */
  public Set<String> getObservers() {
    return observers;
  }

  /**
   * @param observers the set of users who can observer this experiment
   */
  public void setObservers(List<String> observers) {
    if (observers == null) {
      this.observers = Sets.newLinkedHashSet();
    } else {
      this.observers = new LinkedHashSet<String>(observers);
    }
  }

  /**
   * @return the set of subjects who have joined this experiment
   */
  public Set<String> getSubjects() {
    return subjects;
  }

  /**
   * @param subjects the list of subjects who have joined this experiment
   */
  public void setSubjects(List<String> subjects) {
    if (subjects == null) {
      this.subjects = Sets.newLinkedHashSet();
    } else {
      this.subjects = new LinkedHashSet<String>(subjects);
    }
  }

  /**
   * @return the set of users who can view this experiment (or null if anyone can)
   */
  public Set<String> getViewers() {
    return viewers;
  }

  /**
   * @param viewers the set of users who can view this experiment (or null if anyone can)
   */
  public void setViewers(List<String> viewers) {
    if (viewers == null) {
      this.viewers = null;
    } else {
      this.viewers = new LinkedHashSet<String>(viewers);
    }
  }

  /**
   * @param user the user
   * @returns whether the user observers this experiment
   */
  public boolean hasObserver(String user) {
    return observers.contains(user);
  }

  /**
   * @param user the user
   * @returns whether the user has joined this experiment
   */
  public boolean hasSubject(String user) {
    return subjects.contains(user);
  }

  /**
   * @param user the user
   * @returns whether the user can join this experiment when published
   */
  @JsonIgnore
  public boolean hasViewer(String user) {
    if (isPrivate()) {
      return viewers.contains(user);
    } else {
      return true;
    }
  }

  /**
   * @return whether the experiment can only be joined by a limited set of users
   */
  @JsonIgnore
  public boolean isPrivate() {
    return (viewers != null);
  }

  /**
   * @param user the user
   * @return whether the user was added to the set of subjects
   */
  public boolean addSubject(String user) {
    return subjects.add(user);
  }

  /**
   * @param user the user
   * @return whether the user was removed from the set of subjects
   */
  public boolean removeSubject(String user) {
    return subjects.remove(user);
  }

  /**
   * @param user
   * @return whether the user was added to the set of observers
   */
  public boolean addObserver(String user) {
    return observers.add(user);
  }

  /**
   * @param user
   * @return whether the user was removed from the set of observers
   */
  public boolean removeObserver(String user) {
    return subjects.remove(user);
  }

  /**
   * @param user the user
   * @return whether the user was added to the set of viewers, creating it as necessary.
   */
  public boolean addViewer(String user) {
    if (viewers == null) {
      viewers = new LinkedHashSet<String>();
    }

    return viewers.add(user);
  }

  /**
   * @param user the user
   * @return whether the user was removed from the set of viewers, nulling it as necessary.
   */
  public boolean removeViewer(String user) {
    if (viewers == null) {
      return false;
    }

    boolean removed = viewers.remove(user);

    if (viewers.size() == 0) {
      viewers = null;
    }

    return removed;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.Experiment#equals(java.lang.Object)
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

    if (getFeedback().equals(other.getFeedback()) == false) {
      return false;
    }

    if (isPublished() != other.isPublished()) {
      return false;
    }

    if (getObservers().equals(other.getObservers()) == false) {
      return false;
    }

    if (getSubjects().equals(other.getSubjects()) == false) {
      return false;
    }

    if (isPrivate()) {
      if (getViewers().equals(other.getViewers()) == false) {
        return false;
      }
    } else {
      if (other.isPrivate()) {
        return false;
      }
    }

    return true;
  }
}
