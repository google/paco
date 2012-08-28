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

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;

/**
 * A generic representation of one input to be captured in an experiment.
 * By convention, all inputs has a type, name, whether it is required, and a
 * conditional expression.
 *
 * @author Bob Evans
 * @author corycornelius@google.com (Cory Cornelius)
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
   * Default constructor with sane defaults.
   */
  public Input(Type type) {
    super();

    this.name = DEFAULT_NAME;
    this.type = type;
    this.required = false;
    this.conditionalExpression = null;
  }

  /**
   * @return the name of the input
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name of the input
   */
  public void setName(String name) {
    if (name == null) {
      this.name = DEFAULT_NAME;
    } else {
      this.name = name;
    }
  }

  /**
   * @return the type of input
   */
  @JsonIgnore
  public Type getType() {
    return type;
  }

  /**
   * @param type the type of input
   */
  protected void setType(Type type) {
    this.type = type;
  }

  /**
   * @return whether the input is required to be answered
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * @param required whether the experiment is required
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
   * @return the conditional expression whether to show the input (or null if none)
   */
  public String getConditionalExpression() {
    return conditionalExpression;
  }

  /**
   * @param conditionalExpression the conditional expression of the input (or null if none)
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
