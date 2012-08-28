// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;

/**
 * A generic mechanism describing at what interval(s) to signal users.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
@JsonSubTypes({@Type(FixedSignal.class), @Type(RandomSignal.class)})
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class Signal {
  public enum Type {
    Fixed, Random
  }

  protected Type type;

  /**
   * @param type the type of signal
   */
  public Signal(Type type) {
    super();

    this.type = type;
  }

  /**
   * @return the type of signal
   */
  @JsonIgnore
  public Type getType() {
    return type;
  }

  /**
   * @param type the type of signal
   */
  protected void setType(Type type) {
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
