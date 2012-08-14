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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;

/**
 * Represents one data value captured in an experiment.
 *
 * @author Bob Evans
 *
 */
@JsonSubTypes({@Type(TextInput.class), @Type(LikertInput.class), @Type(ListInput.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class Input {
  public enum Type {
    Text, Likert, List
  }

  public static final String DEFAULT_NAME = "";

  protected String name;
  protected Type type;
  protected boolean required;
  protected String conditionalExpression;

  /**
   *
   */
  public Input(Type type) {
    super();

    this.name = DEFAULT_NAME;
    this.type = type;
    this.required = false;
    this.conditionalExpression = null;
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
    if (name == null) {
      this.name = DEFAULT_NAME;
    } else {
      this.name = name;
    }
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
  public Type getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  protected void setType(Type type) {
    this.type = type;
  }

  /**
   * @param required the required to set
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * @return whether the input is conditional
   */
  @JsonIgnore
  public boolean isConditional() {
    return conditionalExpression != null;
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

    if (getType().equals(other.getType()) == false) {
      return false;
    }

    if (isRequired() != other.isRequired()) {
      return false;
    }

    if (isConditional()) {
      if (getConditionalExpression().equals(other.getConditionalExpression()) == false) {
        return false;
      }
    } else {
      if (other.isConditional()) {
        return false;
      }
    }

    return true;
  }
}
