// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObservedExperiment extends Experiment {
  private boolean published;
  private Set<String> observers; // List of users who can edit this experiment
  private Set<String> subjects; // List of users who have joined this experiment
  private Set<String> viewers; // List of users who can view this experiment, null if anyone

  /**
   *
   */
  public ObservedExperiment() {
    super();

    this.published = false;
    this.observers = Sets.newHashSet();
    this.subjects = Sets.newHashSet();
    this.viewers = null;
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
    return observers.add(viewer);
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
    if (super.equals(obj) == false) {
      return false;
    }

    ObservedExperiment other = (ObservedExperiment) obj;

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
