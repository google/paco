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

package com.google.paco.shared.model2;

import java.io.Serializable;

public class Feedback implements Validatable, Serializable {

  public static final int FEEDBACK_TYPE_STATIC_MESSAGE = 0;
  public static final int FEEDBACK_TYPE_RETROSPECTIVE = 1;
  public static final int FEEDBACK_TYPE_RESPONSIVE = 2;
  public static final int FEEDBACK_TYPE_CUSTOM = 3;
  public static final int FEEDBACK_TYPE_HIDE_FEEDBACK = 4;

  public static final String DEFAULT_FEEDBACK_MSG = "Thanks for Participating!";

  private String text;

  /**
   * @param id
   */
  public Feedback(String text) {
    this.text = text;
  }

  public Feedback() {
    text = DEFAULT_FEEDBACK_MSG;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getFeedbackType() {
    return "display";
  }

  public void setFeedbackType(String feedbackType) {
    // this is the old feedbacktype it is hardwired to "display" until we remove it.

  }

  public void validateWith(Validator validator) {
    validator.isNotNullAndNonEmptyString(text, "feedback text should not be null or empty");
    validator.isValidHtmlOrJavascript(text, "text should be valid javascript");
  }

}
