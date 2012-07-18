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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.common.collect.Lists;

import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialized;


/**
 * Represents one data value captured in an experiment.
 *
 * @author Bob Evans
 *
 */
public class Input implements Serializable {
  public static final String QUESTION = "question";
  public static final String LIKERT = "likert";
  public static final String LIKERT_SMILEYS = "likert_smileys";
  public static final String OPEN_TEXT = "open text";
  public static final String LIST = "list";
  public static final String NUMBER = "number";
  public static final String LOCATION = "location";
  public static final String PHOTO = "photo";
  public static final String SOUND = "sound";
  public static final String ACTIVITY = "activity";

  public static String[] RESPONSE_TYPES = {LIKERT_SMILEYS, LIKERT, OPEN_TEXT, LIST, NUMBER,
    LOCATION, PHOTO, SOUND, ACTIVITY};

  public static final Integer DEFAULT_LIKERT_STEPS = 5;

  /**
   * Type of input, (e.g., text question, or sensor input)
   */

  private String questionType = Input.QUESTION;

  private String text = "";


  private Boolean mandatory = true;

  /**
   * for changing questions apps like qotd, the day for which this question is intended,
   */

  private Date scheduleDate = new Date();

  /**
   * Type of response (e.g. Likert, Open Text, List, etc..)
   */
  private String responseType = Input.LIKERT;

  /**
   * For responseType Likert, the number of steps
   */
  private Integer likertSteps = Input.DEFAULT_LIKERT_STEPS;

  /**
   * Variable name for easy searching in responses to this input.
   */
  private String name = "";

  private Boolean conditional = false;

  private String conditionExpression = "";

  private String leftSideLabel = "";

  private String rightSideLabel = "";

  @Serialized
  private List<String> listChoices = Lists.newArrayList();

  private Boolean multiselect;

  public Input(Date parse,  String name, String question) {
    this.scheduleDate = parse;
    this.text = question;
    this.mandatory = true;
    this.name = name;
  }

  public Input(String name, String text, Date scheduleDate,
      String questionType, String responseType, Integer likertSteps, Boolean mandatory,
      Boolean conditional, String conditionalExpression, String leftSideLabel,
      String rightSideLabel, List<String> listChoices, Boolean multiselect) {
    this(scheduleDate, name, text);
    this.questionType = questionType;
    this.responseType = responseType;
    this.likertSteps = likertSteps;
    this.mandatory = mandatory;
    this.conditional = conditional;
    this.conditionExpression = conditionalExpression;
    this.leftSideLabel = leftSideLabel;
    this.rightSideLabel = rightSideLabel;
    this.listChoices = listChoices;
    this.multiselect = multiselect;
  }

  public Input() { }

  public String getQuestionType() {
    return questionType;
  }

  public void setQuestionType(String questionType) {
    this.questionType = questionType;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Boolean getMandatory() {
    return mandatory;
  }

  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public Date getScheduleDate() {
    return scheduleDate;
  }

  public void setScheduleDate(Date scheduleDate) {
    this.scheduleDate = scheduleDate;
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  public String getConditionalExpression() {
    return conditionExpression;
  }

  public void setConditionalExpression(String conditionalExpression) {
    this.conditionExpression = conditionalExpression;
  }

  public Integer getLikertSteps() {
    return likertSteps;
  }

  public void setLikertSteps(Integer likertSteps) {
    this.likertSteps = likertSteps;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getConditional() {
    return conditional;
  }

  public void setConditional(Boolean conditional) {
    this.conditional = conditional;
  }

  public String getConditionExpression() {
    return conditionExpression;
  }

  public void setConditionExpression(String conditionExpression) {
    this.conditionExpression = conditionExpression;
  }

  public String getLeftSideLabel() {
    return leftSideLabel;
  }

  public String getRightSideLabel() {
    return rightSideLabel;
  }

  public void setRightSideLabel(String rightSideLabel) {
    this.rightSideLabel = rightSideLabel;
  }

  public void setLeftSideLabel(String leftSideLabel) {
    this.leftSideLabel = leftSideLabel;
  }

  public List<String> getListChoices() {
    return listChoices;
  }

  public void setListChoices(List<String> listChoices) {
    this.listChoices = listChoices;
  }

  public boolean isInvisibleInput() {
    return getResponseType().equals(Input.LOCATION) || getResponseType().equals(Input.PHOTO);
  }

  public Boolean isMultiselect() {
    return multiselect;
  }

  public void setMultiselect(Boolean multi) {
    this.multiselect = multi;
  }
}
