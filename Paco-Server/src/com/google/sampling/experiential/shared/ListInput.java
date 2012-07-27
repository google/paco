// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.googlecode.objectify.annotation.Serialized;

import java.util.Date;
import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ListInput extends Input {
  private String question;
  @Serialized
  private List<String> choices;
  private boolean multiSelect;

  /**
  *
  */
  public ListInput() {
    super();

    this.responseType = Input.LIST;
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param specificDate
   * @param question
   * @param choices
   * @param multiSelect
   */
  public ListInput(String name,
      boolean required,
      String conditionalExpression,
      Date specificDate,
      String question,
      List<String> choices,
      boolean multiSelect) {
    super(name, Input.LIST, required, conditionalExpression, specificDate);
    this.question = question;
    this.choices = choices;
    this.multiSelect = multiSelect;
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
   * @return the choices
   */
  public List<String> getChoices() {
    return choices;
  }

  /**
   * @param choices the choices to set
   */
  public void setChoices(List<String> choices) {
    this.choices = choices;
  }

  /**
   * @return the multiSelect
   */
  public boolean isMultiSelect() {
    return multiSelect;
  }

  /**
   * @param multiSelect the multiSelect to set
   */
  public void setMultiSelect(boolean multiSelect) {
    this.multiSelect = multiSelect;
  }
}
