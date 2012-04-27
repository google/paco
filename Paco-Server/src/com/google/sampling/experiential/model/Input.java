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

import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Represents one data value captured in an experiment.
 * 
 * @author Bob Evans
 *
 */
@PersistenceCapable
public class Input {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Persistent
  private Experiment experiment;

  /**
   * Type of input, (e.g., text question, or sensor input)
   */
  @Persistent
  private String questionType;
  
  @Persistent
  private String text;

  @Persistent
  private Boolean mandatory;
  
  /**
   * for changing questions apps like qotd, the day for which this question is intended, 
   */
  @Persistent
  private Date scheduleDate;

  /**
   * reference to another input ID for ordering input sequence,
   */
  @Persistent
  private Long nextInputId;

  /**
   * Type of response (e.g. Likert, Open Text, List, etc..)
   */
  @Persistent
  private String responseType;

  /**
   * For responseType Likert, the number of steps
   */
  @Persistent
  private Integer likertSteps;

  /**
   * Variable name for easy searching in responses to this input.
   */
  @Persistent
  private String name;

  @Persistent
  private Boolean conditional;

  @Persistent
  private String conditionExpression;

  @Persistent
  private String leftSideLabel;
  
  @Persistent
  private String rightSideLabel;
  
  @Persistent 
  private List<String> listChoices;

  @Persistent
  private Boolean multiselect;
  
  /**
   * @param parse
   * @param question
   */
  public Input(Date parse,  String name, String question) {
    this.scheduleDate = parse;
    this.text = question;
    this.mandatory = true;    
    this.name = name;
  }

  /**
   * @param name TODO
   * @param questionType 
   * @param conditional TODO
   * @param leftSideLabel TODO
   * @param rightSideLabel TODO
   * @param listChoices TODO
   * @param conditionExpression TODO
   * @param id2
   * @param text2
   * @param scheduleDate2
   */
  public Input(Key experimentKey, Long id, String name, String text, Date scheduleDate, 
      String questionType, String responseType, Integer likertSteps, Boolean mandatory, 
      Boolean conditional, String conditionalExpression, String leftSideLabel, 
      String rightSideLabel, List<String> listChoices, Boolean multiselect) {
    this(scheduleDate, name, text);
    if (id != null) {
      this.id = KeyFactory.createKey(experimentKey, Input.class.getSimpleName(), id);
    }
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

  public Input() {
    
  }
  
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

  public Long getNextInputId() {
    return nextInputId;
  }

  public void setNextInputId(Long nextInputId) {
    this.nextInputId = nextInputId;
  }

  /**
   * @return
   */
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

  /**
   * @return name
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

  /**
   * @return
   */
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

  public Boolean isMultiselect() {
    return multiselect;
  }
  
  public void setMultiselect(Boolean multi) {
    this.multiselect = multi;
  }
}
