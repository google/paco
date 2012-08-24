// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("likert")
public class LikertInput extends Input {
  public static final String DEFAULT_QUESTION = "";
  public static final Integer DEFAULT_STEPS = 5;

  private String question;
  private List<String> labels;
  private boolean smileys;

  /**
   *
   */
  public LikertInput() {
    super(Input.Type.Likert);

    this.question = DEFAULT_QUESTION;
    this.labels = Lists.newArrayList();
    this.smileys = false;
  }

  /**
   * @return the question
   */
  public String getQuestion() {
    return question;
  }

  /**
   * @param question the question to set
   */
  public void setQuestion(String question) {
    if (question == null) {
      this.question = DEFAULT_QUESTION;
    } else {
      this.question = question;
    }
  }

  /**
   * @return the labels
   */
  public List<String> getLabels() {
    return labels;
  }

  /**
   * @param labels the labels to set
   */
  public void setLabels(List<String> labels) {
    if (labels == null) {
      this.labels = Lists.newArrayList();
    } else {
      this.labels = labels;
    }
  }

  /**
   * @param label the label
   */
  public boolean addLabel(String label) {
    if (labels == null) {
      labels = Lists.newArrayList();
    }

    return labels.add(label);
  }

  /**
   * @return the useSmileys
   */
  public boolean isSmileys() {
    return smileys;
  }

  /**
   * @param smileys the useSmileys to set
   */
  public void setSmileys(boolean smileys) {
    this.smileys = smileys;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.shared.Input#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (obj.getClass() != getClass()) {
      return false;
    }

    LikertInput other = (LikertInput) obj;

    if (getQuestion().equals(other.getQuestion()) == false) {
      return false;
    }

    if (isSmileys() != other.isSmileys()) {
      return false;
    }

    if (getLabels().equals(other.getLabels()) == false) {
      return false;
    }

    return super.equals(obj);
  }
}
