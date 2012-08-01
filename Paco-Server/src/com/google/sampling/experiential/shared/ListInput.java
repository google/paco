// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.google.common.collect.Lists;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("list")
public class ListInput extends Input {
  public static final String DEFAULT_QUESTION = "";

  private String question;
  private List<String> choices;
  private boolean multiselect;

  /**
  *
  */
  public ListInput() {
    super(Input.LIST);

    this.question = DEFAULT_QUESTION;
    this.choices = Lists.newArrayList();
    this.multiselect = false;
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
   * @return the choices
   */
  public List<String> getChoices() {
    return choices;
  }

  /**
   * @param choices the choices to set
   */
  public void setChoices(List<String> choices) {
    if (choices == null) {
      this.choices = Lists.newArrayList();
    } else {
      this.choices = choices;
    }
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

    if (getChoices().equals(other.getChoices()) == false) {
      return false;
    }

    return super.equals(obj);
  }
}
