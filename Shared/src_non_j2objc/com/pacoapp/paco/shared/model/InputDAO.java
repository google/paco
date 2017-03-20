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

package com.pacoapp.paco.shared.model;


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
public class InputDAO implements Serializable {

  /**
   *
   */
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
  public static final String VA_SCALE = "va_scale";

  public static String[] RESPONSE_TYPES = {LIKERT_SMILEYS, LIKERT, OPEN_TEXT, LIST, NUMBER,
    LOCATION, PHOTO, SOUND, ACTIVITY, VA_SCALE};

  private Long id;
  private String questionType;
  private String text;
  private Boolean mandatory;
  private Long scheduleDate;
  private String responseType;
  private Integer likertSteps;
  private String leftSideLabel;
  private String rightSideLabel;
  private String name;
  private Boolean conditional = false;
  private String conditionExpression;
  private String[] listChoices;
  private Boolean multiselect;

  /**
   *
   */
  public static final Integer DEFAULT_LIKERT_STEPS = 5;

  /**
   * @param id
   * @param name TODO
   * @param questionType
   * @param text
   * @param mandatory
   * @param scheduleDate
   * @param likertSteps TODO
   * @param leftSideLabel TODO
   * @param rightSideLabel TODO
   * @param listChoices TODO
   * @param questionType2
   * @param nextInputId
   */
  public InputDAO(Long id, String name, String questionType, String responseType, String text,
      Boolean mandatory, Long scheduleDate, Integer likertSteps, Boolean conditional,
      String conditionExpr, String leftSideLabel, String rightSideLabel, String[] listChoices,
      Boolean multiselect) {
    this.id = id;
    this.questionType = questionType;
    this.text = text;
    this.mandatory = mandatory;
    this.scheduleDate = scheduleDate;
    this.responseType = responseType;
    this.likertSteps = likertSteps;
    this.name = name;
    this.conditional = conditional;
    this.conditionExpression = conditionExpr;
    this.leftSideLabel = leftSideLabel;
    this.rightSideLabel = rightSideLabel;
    this.listChoices = listChoices;
    this.multiselect = multiselect;
  }

  /**
   * @param name TODO
   * @param key
   * @param value
   */
  public InputDAO(Long id, String name, Long scheduledDate, String text) {
    this(id, name, QUESTION, LIKERT, text, false, scheduledDate, null, false, null, null,
        null, null, null);
  }

  public InputDAO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public Long getScheduleDate() {
    return scheduleDate;
  }

  public void setScheduleDate(Long scheduleDate) {
    this.scheduleDate = scheduleDate;
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String type) {
    this.responseType = type;
  }

  public Integer getLikertSteps() {
    return likertSteps;
  }

  public void setLikertSteps(Integer steps) {
    this.likertSteps = steps;
  }

  /**
   * @return
   */
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

  public void setLeftSideLabel(String leftSideLabel) {
    this.leftSideLabel = leftSideLabel;
  }

  public String getRightSideLabel() {
    return rightSideLabel;
  }

  public void setRightSideLabel(String rightSideLabel) {
    this.rightSideLabel = rightSideLabel;
  }

  public String[] getListChoices() {
    return listChoices;
  }

  public void setListChoices(String[] listChoices) {
    this.listChoices = listChoices;
  }

  /**
   * @return
   */
  public boolean isInvisibleInput() {
    return getResponseType().equals(InputDAO.LOCATION);
  }

  public Boolean getMultiselect() {
    return multiselect;
  }

  public void setMultiselect(Boolean multiselect) {
    this.multiselect = multiselect;
  }




}
