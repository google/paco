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

import java.io.Serializable;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Id;

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
public class Experiment implements Serializable {

  @Id
  private Long id;
  private long version;
  private String title;
  private String description;
  private String creator;
  private String consentForm;
  private boolean published;
  private boolean deleted;
  private List<String> observers;
  private List<String> subjects;
  @Embedded
  private List<Input> inputs;
  @Embedded
  private SignalSchedule schedule;
  @Embedded
  private List<Feedback> feedbacks;

  /**
   *
   */
  public Experiment() {
    super();

    this.subjects = Lists.newArrayList();
    this.observers = Lists.newArrayList();
    this.inputs = Lists.newArrayList();
    this.schedule = new SignalSchedule();
    this.feedbacks = Lists.newArrayList();
  }

  /**
   * @param id
   * @param version
   * @param title
   * @param description
   * @param consent
   * @param published
   * @param deleted
   * @param observers
   * @param subjects
   * @param inputs
   * @param schedule
   * @param feedbacks
   */
  public Experiment(Long id,
      long version,
      String title,
      String description,
      String consent,
      boolean published,
      boolean deleted,
      List<String> observers,
      List<String> subjects,
      List<Input> inputs,
      SignalSchedule schedule,
      List<Feedback> feedbacks) {
    super();
    this.id = id;
    this.version = version;
    this.title = title;
    this.description = description;
    this.consentForm = consent;
    this.published = published;
    this.deleted = deleted;
    this.observers = observers;
    this.subjects = subjects;
    this.inputs = inputs;
    this.schedule = schedule;
    this.feedbacks = feedbacks;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the version
   */
  public long getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the creator
   */
  public String getCreator() {
    return creator;
  }

  /**
   * @param creator the creator to set
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
   * @return the consent
   */
  public String getConsentForm() {
    return consentForm;
  }

  /**
   * @param consent the consent to set
   */
  public void setConsentForm(String consent) {
    this.consentForm = consent;
  }

  /**
   * @return whether the experiment is published
   */
  public boolean isPublished() {
    return published;
  }

  /**
   * @param published whether the experiment is published
   */
  public void setPublished(boolean published) {
    this.published = published;
  }

  /**
   * @return the deleted
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * @param deleted the deleted to set
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * @return the observers
   */
  public List<String> getObservers() {
    return observers;
  }

  /**
   * @param observers the observers to set
   */
  public void setObservers(List<String> observers) {
    this.observers = observers;
  }

  /**
   * @return the subjects
   */
  public List<String> getSubjects() {
    return subjects;
  }

  /**
   * @param subjects the subjects to set
   */
  public void setSubjects(List<String> subjects) {
    this.subjects = subjects;
  }

  /**
   * @return the inputs
   */
  public List<Input> getInputs() {
    return inputs;
  }

  /**
   * @param inputs the inputs to set
   */
  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  /**
   * @return the schedule
   */
  public SignalSchedule getSchedule() {
    return schedule;
  }

  /**
   * @param schedule the schedule to set
   */
  public void setSchedule(SignalSchedule schedule) {
    this.schedule = schedule;
  }

  /**
   * @return the feedbacks
   */
  public List<Feedback> getFeedbacks() {
    return feedbacks;
  }

  /**
   * @param feedbacks the feedbacks to set
   */
  public void setFeedbacks(List<Feedback> feedbacks) {
    this.feedbacks = feedbacks;
  }
}
