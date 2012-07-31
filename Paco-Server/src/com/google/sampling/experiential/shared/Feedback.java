/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Represents a visualization or intervention to the user in response
 * to responses gathered in an experiment.
 *
 * @author Bob Evans
 *
 */
public class Feedback implements Serializable {

  public static final String DEFAULT_FEEDBACK_MSG = "Thanks for Participating!";
  public static final String DISPLAY_FEEBACK_TYPE = "display";

  /**
   * Currently only one type, display
   */
  private String feedbackType = Feedback.DISPLAY_FEEBACK_TYPE;

  /**
   * Display text
   * @deprecated
   */
  @JsonIgnore
  private String text = Feedback.DEFAULT_FEEDBACK_MSG;

  /**
   * Overflow from the text if > 500 chars.
   * Backwards compatibility since a String is not convertible to a Text.
   */
  @JsonIgnore
  private String longText = "";

  public Feedback() { }

  public Feedback(String feedbackType, String longText) {
    this.feedbackType = feedbackType;
    this.longText = new String(longText);
  }

  public String getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(String feedbackType) {
    this.feedbackType = feedbackType;
  }

  @JsonIgnore
  public String getText() {
    return text;
  }

  public String getLongText() {
    if (longText == null && text != null) {
      longText = new String(text);
      text = null;
    }
    if (longText != null) {
      return longText;
    } else {
      return null;
    }
  }

  @JsonProperty("text")
  public String getJsonSafeHtmlLongText() {
    String baseText = getLongText();
    if (baseText == null) {
      return baseText;
    }
    return baseText;
  }

  public void setLongText(String text) {
    longText = new String(text);
  }

  @JsonIgnore
  public void setText(String text) {
    this.text = text;
  }
}
