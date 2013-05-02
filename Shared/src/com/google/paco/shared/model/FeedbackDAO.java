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

package com.google.paco.shared.model;

import java.io.Serializable;

/**
 * 
 * Dumb data object for passing the experiment definition to the
 * GWT client. 
 * 
 * We use this because GWt serialization won't serialize a JDO nucleus object.
 * @author Bob Evans
 *
 */
public class FeedbackDAO implements Serializable {

  public static final String DEFAULT_FEEDBACK_MSG = "Thanks for Participating!";
  public static final String DISPLAY_FEEBACK_TYPE = "display";

  private Long id;
  private String feedbackType;
  private String text;

  /**
   * @param id
   * @param feedbackType
   * @param text
   */
  public FeedbackDAO(Long id, String feedbackType, String text) {
    this.id = id;
    this.feedbackType = feedbackType;
    this.text = text;
  }

  public FeedbackDAO() {}
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(String feedbackType) {
    this.feedbackType = feedbackType;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
