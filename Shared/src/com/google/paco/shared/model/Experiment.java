// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class Experiment {
  public static class Summary {
  }
  public static class Viewer extends Summary {
  }
  public static class Observer extends Viewer {
  }

  public static String DEFAULT_TITLE = "";
  public static String DEFAULT_DESCRIPTION = "";
  public static String DEFAULT_CREATOR = "";
  public static String DEFAULT_CONSENT_FORM = "";
  public static String DEFAULT_FEEDBACK = "";

  private Long id;
  private long version;
  @JsonIgnore
  private boolean deleted;

  @JsonView(Summary.class)
  private String title;
  @JsonView(Summary.class)
  private String description;
  @JsonView(Summary.class)
  private String creator;
  @JsonView(Viewer.class)
  private String consentForm;
  @JsonView(Viewer.class)
  private List<Input> inputs;
  @JsonView(Viewer.class)
  private SignalSchedule signalSchedule;
  @JsonView(Viewer.class)
  private String feedback;
  @JsonView(Observer.class)
  private boolean published;
  @JsonView(Observer.class)
  private Set<String> observers; // List of users who can edit this experiment
  @JsonView(Observer.class)
  private Set<String> subjects; // List of users who have joined this experiment
  @JsonView(Observer.class)
  private Set<String> viewers; // List of users who can view this experiment, null if anyone

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
   * @return the feedback
   */
  public String getFeedback() {
    return feedback;
  }

  /**
   * @param feedback the feedback
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
   * @param published whether the experiment is published
   */
  public void setPublished(boolean published) {
    this.published = published;
  }

  /**
   * @return the observers
   */
  public Set<String> getObservers() {
    return observers;
  }

  /**
   * @param observers the observers to List
   */
  public void setObservers(List<String> observers) {
    if (observers == null) {
      this.observers = Sets.newLinkedHashSet();
    } else {
      this.observers = new LinkedHashSet<String>(observers);
    }
  }

  /**
   * @return the subjects
   */
  public Set<String> getSubjects() {
    return subjects;
  }

  /**
   * @param subjects the subjects to List
   */
  public void setSubjects(List<String> subjects) {
    if (subjects == null) {
      this.subjects = Sets.newLinkedHashSet();
    } else {
      this.subjects = new LinkedHashSet<String>(subjects);
    }
  }

  /**
   * @return the viewers
   */
  public Set<String> getViewers() {
    return viewers;
  }

  /**
   * @param viewers the viewers to List
   */
  public void setViewers(List<String> viewers) {
    if (viewers == null) {
      this.viewers = null;
    } else {
      this.viewers = new LinkedHashSet<String>(viewers);
    }
  }

  /**
   * @param user
   */
  public boolean hasObserver(String user) {
    return observers.contains(user);
  }

  /**
   * @param user
   */
  public boolean hasSubject(String user) {
    return subjects.contains(user);
  }

  /**
   * @param user
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
   * @return whether the experiment is public
   */
  @JsonIgnore
  public boolean isPrivate() {
    return (viewers != null);
  }

  /**
   * @param subject
   * @return whether the subject was added
   */
  public boolean addSubject(String subject) {
    return subjects.add(subject);
  }

  /**
   * @param subject
   * @return whether the subject was removed
   */
  public boolean removeSubject(String subject) {
    return subjects.remove(subject);
  }

  /**
   * @param observer
   * @return whether the observer was added
   */
  public boolean addObserver(String observer) {
    return observers.add(observer);
  }

  /**
   * @param observer
   * @return whether the observer was removed
   */
  public boolean removeObserver(String observer) {
    return subjects.remove(observer);
  }

  /**
   * @param viewer
   * @return whether the viewer was added
   */
  public boolean addViewer(String viewer) {
    if (viewers == null) {
      viewers = new LinkedHashSet<String>();
    }

    return viewers.add(viewer);
  }

  /**
   * @param viewer
   * @return whether the viewer was removed
   */
  public boolean removeViewer(String viewer) {
    return subjects.remove(viewer);
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
