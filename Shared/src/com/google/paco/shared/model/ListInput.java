// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * A list input displays a question to the user and allows them to select from a
 * set of choices, with (optional) selection of many.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonTypeName("list")
public class ListInput extends Input {
  public static final String DEFAULT_QUESTION = "";

  private String question;
  private List<String> choices;
  private boolean multiselect;

  /**
  * Default constructor with sane defaults.
  */
  public ListInput() {
    super(Input.Type.List);

    this.question = DEFAULT_QUESTION;
    this.choices = Lists.newArrayList();
    this.multiselect = false;
  }

  /**
   * @return the question to show the user
   */
  public String getQuestion() {
    return question;
  }

  /**
   * @param question the question to show the user (or DEFAULT_QUESTION if null)
   */
  public void setQuestion(String question) {
    if (question == null) {
      this.question = DEFAULT_QUESTION;
    } else {
      this.question = question;
    }
  }

  /**
   * @return the choices the user can select from
   */
  public List<String> getChoices() {
    return choices;
  }

  /**
   * @param choices the choices the user can select from
   */
  public void setChoices(List<String> choices) {
    if (choices == null) {
      this.choices = Lists.newArrayList();
    } else {
      this.choices = choices;
    }
  }

  /**
   * @param choice a choice to add to the list of choices
   * @return whether the choice was added
   */
  public boolean addChoice(String choice) {
    if (choices == null) {
      choices = Lists.newArrayList();
    }

    return choices.add(choice);
  }

  /**
   * @return whether the user can select multiple choices
   */
  public boolean isMultiselect() {
    return multiselect;
  }

  /**
   * @param multiselect whether the user can select multiple choices
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
