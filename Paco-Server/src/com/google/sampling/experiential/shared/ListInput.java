// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("list")
public class ListInput extends Input {
  private String question;
  private List<String> choices;
  private boolean multiselect;

  /**
  *
  */
  public ListInput() {
    super(Input.LIST);
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param question
   * @param choices
   * @param multiselect
   */
  public ListInput(String name,
      boolean required,
      String conditionalExpression,
      String question,
      List<String> choices,
      boolean multiselect) {
    super(name, Input.LIST, required, conditionalExpression);
    this.question = question;
    this.choices = choices;
    this.multiselect = multiselect;
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
   * @return the multiselect
   */
  public boolean isMultiselect() {
    return multiselect;
  }

  /**
   * @param multiselect the multiSelect to set
   */
  public void setMultiselect(boolean multiselect) {
    this.multiselect = multiselect;
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

    ListInput other = (ListInput) obj;

    if (getQuestion().equals(other.getQuestion()) == false) {
      return false;
    }

    if (isMultiselect() != other.isMultiselect()) {
      return false;
    }

    if (getChoices() == null) {
      if (other.getChoices() != null) {
        return false;
      }
    } else {
      if (getChoices().equals(other.getChoices()) == false) {
        return false;
      }
    }

    return super.equals(obj);
  }
}
