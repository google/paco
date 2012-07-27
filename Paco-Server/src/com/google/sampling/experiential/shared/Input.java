/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents one data value captured in an experiment.
 *
 * @author Bob Evans
 *
 */
public abstract class Input implements Serializable {
  public static final String TEXT = "text";
  public static final String LIKERT = "likert";
  public static final String LIST = "list";
  public static final String NUMBER = "number";
  public static final String LOCATION = "location";
  public static final String PHOTO = "photo";
  public static final String SOUND = "sound";
  public static final String ACTIVITY = "activity";

  public static String[] RESPONSE_TYPES =
      {TEXT, LIKERT, LIST, NUMBER, LOCATION, PHOTO, SOUND, ACTIVITY};

  protected String name;
  protected String responseType;
  protected boolean required;
  protected String conditionalExpression;
  protected Date specificDate;

  /**
   *
   */
  public Input() {
    super();
  }

  /**
   * @param name
   * @param responseType
   * @param required
   * @param conditionalExpression
   * @param specificDate
   */
  public Input(String name, String responseType, boolean required, String conditionalExpression,
      Date specificDate) {
    super();
    this.name = name;
    this.responseType = responseType;
    this.required = required;
    this.conditionalExpression = conditionalExpression;
    this.specificDate = specificDate;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the responseType
   */
  public String getResponseType() {
    return responseType;
  }

  /**
   * @return the required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * @param required the required to set
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * @return the conditionalExpression
   */
  public String getConditionalExpression() {
    return conditionalExpression;
  }

  /**
   * @param conditionalExpression the conditionalExpression to set
   */
  public void setConditionalExpression(String conditionalExpression) {
    this.conditionalExpression = conditionalExpression;
  }

  /**
   * @return the specificDate
   */
  public Date getSpecificDate() {
    return specificDate;
  }

  /**
   * @param specificDate the specificDate to set
   */
  public void setSpecificDate(Date specificDate) {
    this.specificDate = specificDate;
  }

  /**
   * @return whether the input is conditional
   */
  public boolean isConditional() {
    return conditionalExpression != null;
  }
}
