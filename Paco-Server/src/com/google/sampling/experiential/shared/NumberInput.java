// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import java.util.Date;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class NumberInput extends Input {
  private String question;
  private Number number;

  /**
  *
  */
  public NumberInput() {
    super();

    this.responseType = Input.NUMBER;
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   * @param specificDate
   * @param question
   * @param number
   */
  public NumberInput(String name,
      boolean required,
      String conditionalExpression,
      Date specificDate,
      String question,
      Number number) {
    super(name, Input.NUMBER, required, conditionalExpression, specificDate);
    this.question = question;
    this.number = number;
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
   * @return the number
   */
  public Number getNumber() {
    return number;
  }

  /**
   * @param number the number to set
   */
  public void setNumber(Number number) {
    this.number = number;
  }
}
