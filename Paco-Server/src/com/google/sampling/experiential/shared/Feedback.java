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

/**
 * Represents a visualization or intervention to the user in response to responses gathered in an
 * experiment.
 *
 * @author Bob Evans
 *
 */
public class Feedback implements Serializable {
  public static final String DEFAULT_TEXT = "";

  private String text;

  /**
   *
   */
  public Feedback() {
    super();

    this.text = DEFAULT_TEXT;
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    if (text == null) {
      this.text = DEFAULT_TEXT;
    } else {
      this.text = text;
    }
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

    Feedback other = (Feedback) obj;

    if (getText().equals(other.getText()) == false) {
      return false;
    }

    return true;
  }

  public static Feedback create(String text) {
    Feedback feedback = new Feedback();

    feedback.setText(text);

    return feedback;
  }
}
