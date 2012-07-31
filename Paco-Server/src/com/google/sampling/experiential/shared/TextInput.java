// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("text")
public class TextInput extends Input {
  private String question;
  private boolean multiline;

  /**
   *
   */
  public TextInput() {
    super(Input.TEXT);
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param question
   * @param multiline
   */
  public TextInput(String name, boolean required, String conditionalExpression, String question,
      boolean multiline) {
    super(name, Input.TEXT, required, conditionalExpression);
    this.question = question;
    this.multiline = multiline;
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
   * @return the multiline
   */
  public boolean isMultiline() {
    return multiline;
  }

  /**
   * @param multiline the multiLine to set
   */
  public void setMultiline(boolean multiline) {
    this.multiline = multiline;
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

    TextInput other = (TextInput) obj;

    if (getQuestion().equals(other.getQuestion()) == false) {
      return false;
    }

    if (isMultiline() != other.isMultiline()) {
      return false;
    }

    return super.equals(obj);
  }
}
