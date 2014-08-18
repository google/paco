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

package com.google.sampling.experiential.model;

import java.net.URLEncoder;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * Represents a visualization or intervention to the user in response
 * to events gathered in an experiment.
 *
 * @author Bob Evans
 *
 */
@PersistenceCapable
public class Feedback {

  /**
   * @param feedbackText
   */
  public Feedback(String longText) {
    this.longText = new Text(longText);
  }

  /**
   * @param experimentKey
   * @param id2
   * @param feedbackType2
   * @param text2
   */
  public Feedback(Key experimentKey, Long id, String longText) {
    this(longText);
    if (id != null) {
      this.id = KeyFactory.createKey(experimentKey, Feedback.class.getSimpleName(), id);
    }

  }

  public Feedback() {

  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Persistent
  private Experiment experiment;

  /**
   * display text
   * @deprecated
   */
  @Persistent
  @JsonIgnore
  private String text;

  /**
   * Overflow from the text if > 500 chars.
   * Backwards compatibility since a String is not convertible to a Text.
   */
  @Persistent
  @JsonIgnore
  private Text longText;

  public Key getId() {
    return id;
  }

  public void setId(Key id) {
    this.id = id;
  }

  public Experiment getExperiment() {
    return experiment;
  }

  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;
  }

  @JsonIgnore
  public String getText() {
    return text;
  }


  public String getLongText() {
    if (longText == null && text != null) {
      longText = new Text(text);
      text = null;
    }
    if (longText != null) {
      return longText.getValue();
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
    return URLEncoder.encode(baseText);
  }

  public void setLongText(String text) {
    longText = new Text(text);
  }

  @JsonIgnore
  public void setText(String text) {
    this.text = text;
  }



}
