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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.io.Serializable;

/**
 * Represents one data value captured in an experiment.
 *
 * @author Bob Evans
 *
 */
@JsonSubTypes({@Type(TextInput.class), @Type(LikertInput.class), @Type(ListInput.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
public abstract class Input implements Serializable {
  public static final String TEXT = "text";
  public static final String LIKERT = "likert";
  public static final String LIST = "list";
  public static final String LOCATION = "location";
  public static final String PHOTO = "photo";
  public static final String SOUND = "sound";
  public static final String ACTIVITY = "activity";

  public static String[] TYPES = {TEXT, LIKERT, LIST, LOCATION, PHOTO, SOUND, ACTIVITY};

  protected String name;
  protected String type;
  protected boolean required;
  protected String conditionalExpression;

  /**
   *
   */
  public Input(String type) {
    super();

    this.type = type;
  }

  /**
   * @param name
   * @param required
   * @param conditionalExpression
   */
  public Input(String name, String type, boolean required, String conditionalExpression) {
    this(type);
    this.name = name;
    this.required = required;
    this.conditionalExpression = conditionalExpression;
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
   * @return the required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * @return the type
   */
  @JsonIgnore
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
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
   * @return whether the input is conditional
   */
  @JsonIgnore
  public boolean isConditional() {
    return conditionalExpression != null;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
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

    Input other = (Input) obj;

    if (getName().equals(other.getName()) == false) {
      return false;
    }

    if (isRequired() != other.isRequired()) {
      return false;
    }

    if (getConditionalExpression() == null) {
      if (other.getConditionalExpression() != null) {
        return false;
      }
    } else {
      if (getConditionalExpression().equals(other.getConditionalExpression()) == false) {
        return false;
      }
    }

    return true;
  }
}
