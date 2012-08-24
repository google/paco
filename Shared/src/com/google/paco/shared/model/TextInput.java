// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonTypeName("text")
public class TextInput extends Input {
  public static final String DEFAULT_QUESTION = "";

  private String question;
  private boolean multiline;

  /**
   *
   */
  public TextInput() {
    super(Input.Type.Text);

    this.question = DEFAULT_QUESTION;
    this.multiline = false;
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
