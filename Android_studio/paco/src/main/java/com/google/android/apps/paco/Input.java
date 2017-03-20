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
package com.google.android.apps.paco;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Input {


  public static final String LIST = "list";
  public static final String LIKERT = "likert";
  public static final String OPEN_TEXT = "open text";
  public static final String LIKERT_SMILEYS = "likert_smileys";
  public static final String NUMBER = "number";
  public static final String LOCATION = "location";
  public static final String PHOTO = "photo";
  public static final String SOUND = "sound";
  public static final String ACTIVITY = "activity";
  public static final String VA_SCALE = "va_scale";


  @JsonIgnore
  private Long id;
  private Long experimentId;
  private Long serverId;
  private String text = "";
  private boolean mandatory = false;
  private String questionType = "";
  private String responseType = "";
  private Boolean conditional = false;
  private String conditionExpression;
  private List<String> listChoices;
  private Integer likertSteps;
  private String leftSideLabel;
  private String rightSideLabel;
  private Long scheduleDate;
  private Date scheduleDateAsDate; // experimenting w/ Jackson json framework
  private String name;
  private Boolean multiselect = false;

  @JsonIgnore
  public Date getScheduleDate() {
    return scheduleDateAsDate;
  }

  @JsonIgnore
  public void setScheduleDate(Date scheduleDate) {
    this.scheduleDateAsDate = scheduleDate;
    this.scheduleDate = scheduleDateAsDate.getTime();
  }

  @JsonProperty("scheduleDate")
  public Long getScheduleDateAsLong() {
    return scheduleDate;
  }

  @JsonProperty("scheduleDate")
  public void setScheduleDateFromLong(Long scheduleDate) {
    this.scheduleDate = scheduleDate;
    this.scheduleDateAsDate = new Date(scheduleDate);
  }



  @JsonIgnore
  public Long getId() {
    return id;
  }

  @JsonIgnore
  public void setId(Long id) {
    this.id = id;
  }

  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  @JsonProperty("id")
  public Long getServerId() {
    return serverId;
  }

  @JsonProperty("id")
  public void setServerId(Long serverId) {
    this.serverId = serverId;
  }

  public String getText() {
    if (responseType.equals(LOCATION)) {
      return "Location";
    }
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public String getQuestionType() {
    return questionType;
  }

  public void setQuestionType(String questionType) {
    this.questionType = questionType;
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

  public void setConditionExpression(String conditionalExpression) {
    this.conditionExpression = conditionalExpression;
  }

  public List<String> getListChoices() {
    return listChoices;
  }

  public void setListChoices(List<String> listOptions) {
    this.listChoices = listOptions;
  }

  public Integer getLikertSteps() {
    return likertSteps;
  }

  public void setLikertSteps(Integer likertSteps) {
    this.likertSteps = likertSteps;
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public boolean isInvisible() {
    return responseType.equals(Input.LOCATION) || responseType.equals(Input.PHOTO);
  }

  @JsonIgnore
  public boolean isNumeric() {
    return
    responseType.equals(Input.LIKERT) ||
    responseType.equals(Input.LIST) || // TODO (bobevans): LIST shoudl be a categorical, not a numeric.
    responseType.equals(Input.NUMBER);
  }

	public Boolean isMultiselect() {
		return multiselect;
	}

	public void setMultiselect(Boolean multiselect) {
		this.multiselect = multiselect;
	}



}
