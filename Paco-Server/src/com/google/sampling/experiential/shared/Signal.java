// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.io.Serializable;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
@JsonSubTypes({@Type(FixedSignal.class), @Type(RandomSignal.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
public abstract class Signal implements Serializable {
  public static final String FIXED = "fixed";
  public static final String RANDOM = "random";

  protected String type;

  /**
   *
   */
  public Signal(String type) {
    super();

    this.type = type;
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
  protected void setType(String type) {
    this.type = type;
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

    Signal other = (Signal) obj;

    if (getType().equals(other.getType()) == false) {
      return false;
    }

    return true;
  }
}
