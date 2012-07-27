// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class TextInput extends Input {
  private String question;
  private boolean multiLine;

  /**
   *
   */
  public TextInput() {
    super();

    this.responseType = Input.TEXT;
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param specificDate
   * @param question
   * @param multiLine
   */
  public TextInput(String name,
      boolean required,
      String conditionalExpression,
      Date specificDate,
      String question,
      boolean multiLine) {
    super(name, Input.TEXT, required, conditionalExpression, specificDate);
    this.question = question;
    this.multiLine = multiLine;
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
   * @return the multiLine
   */
  public boolean isMultiLine() {
    return multiLine;
  }

  /**
   * @param multiLine the multiLine to set
   */
  public void setMultiLine(boolean multiLine) {
    this.multiLine = multiLine;
  }
}
