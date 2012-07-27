// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.googlecode.objectify.annotation.Serialized;

import java.util.Date;
import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class LikertInput extends Input {
  public static final Integer DEFAULT_STEPS = 5;

  private String question;
  @Serialized
  private List<String> labels;
  private boolean smileys;

  /**
   *
   */
  public LikertInput() {
    super();

    this.responseType = Input.LIKERT;
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param specificDate
   * @param question
   * @param labels
   * @param smileys
   */
  public LikertInput(String name,
      boolean required,
      String conditionalExpression,
      Date specificDate,
      String question,
      List<String> labels,
      boolean smileys) {
    super(name, Input.LIKERT, required, conditionalExpression, specificDate);
    this.question = question;
    this.labels = labels;
    this.smileys = smileys;
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
    this.question = question;
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
    this.labels = labels;
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
}
