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

package com.pacoapp.paco.shared.model2;


import java.io.Serializable;
import java.util.List;

public class Input2 extends ModelBase implements Validatable, Serializable {

  public static final String LIKERT = "likert";
  public static final String LIKERT_SMILEYS = "likert_smileys";
  public static final String OPEN_TEXT = "open text";
  public static final String LIST = "list";
  public static final String NUMBER = "number";
  public static final String LOCATION = "location";
  public static final String PHOTO = "photo";
  public static final String SOUND = "sound";
  public static final String ACTIVITY = "activity";
  public static final String AUDIO = "audio";
  public static final String VA_SCALE = "va_scale";

  public static String[] RESPONSE_TYPES = {LIKERT_SMILEYS, LIKERT, OPEN_TEXT, LIST, NUMBER,
    LOCATION, PHOTO, SOUND, ACTIVITY, AUDIO};

  private String name;
  private Boolean required = false;
  private Boolean conditional = false;
  private String conditionExpression;
  private String responseType = LIKERT;

  private String text;

  private Integer likertSteps;
  private String leftSideLabel;
  private String rightSideLabel;


  private List<String> listChoices;
  private Boolean multiselect = false;

  /**
   *
   */
  public static final Integer DEFAULT_LIKERT_STEPS = 5;  

  /**
   *
   * @param name
   * @param responseType
   * @param text
   * @param required
   * @param likertSteps
   * @param conditional
   * @param conditionExpr
   * @param leftSideLabel
   * @param rightSideLabel
   * @param listChoices
   * @param multiselect
   */
  public Input2(String name, String responseType, String text, Boolean required,
      Integer likertSteps, Boolean conditional, String conditionExpr, String leftSideLabel,
      String rightSideLabel, List<String> listChoices, Boolean multiselect) {
    this.text = text;
    this.required = required != null ? required : false;
    this.responseType = responseType;
    this.likertSteps = likertSteps;
    this.name = name;
    this.conditional = conditional;
    this.conditionExpression = conditionExpr;
    this.leftSideLabel = leftSideLabel;
    this.rightSideLabel = rightSideLabel;
    this.listChoices = listChoices;
    this.multiselect = multiselect != null ? multiselect : false;
  }

  // visible for testing
  public Input2(String name, String text) {
    this(name, LIKERT, text, false, null, false, null, null, null, null,
        null);
  }

  public Input2() {
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
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

  public List<String> getListChoices() {
    return listChoices;
  }

  public void setListChoices(List<String> list2) {
    this.listChoices = list2;
  }

  public Boolean getMultiselect() {
    return multiselect;
  }

  public void setMultiselect(Boolean multiselect) {
    this.multiselect = multiselect;
  }

  public void validateWith(Validator validator) {
//    System.out.println("VALIDATING Input");
    validator.isNotNullAndNonEmptyString(name, "input name is not properly initialized");
    //validator.isNotNullAndNonEmptyString(text, "input question text is not properly initialized");
    if (text != null && text.length() > 0) {
      validator.isTrue(text.length() <= 500, "input question text is too long. 500 char limit.");
    }
    validator.isNotNull(responseType, "responseType is not properly initialized");
    validator.isNotNull(required, "required is not properly initialized");
    if (responseType != null) {
      if (responseType.equals(LIKERT)) {
        validator.isNotNull(likertSteps, "scales need a number of steps specified");
        validator.isTrue(likertSteps >= 2, "scales need at least 2 steps");
        validator.isTrue(likertSteps <= 9, "scales need 9 or less steps");
        //validator.isNotNull(leftSideLabel, "no left label is specified for scale");
        //validator.isNotNull(rightSideLabel, "no right label is specified for scale");
      } else if (responseType.equals(LIST)) {
        validator.isNotNullAndNonEmptyCollection(listChoices, "lists must have a non-empty set of choices");
        for (String choice : listChoices) {
          validator.isNotNullAndNonEmptyString(choice, "list choice text must all be non-empty");
          if (choice != null && choice.length() > 0) {
            validator.isTrue(choice.length() <= 500, "list choice text is too long. 500 char limit.");
          }
        }
        validator.isNotNull(multiselect, "multiselect is not initialized properly");
      } else if (responseType.equals(LIKERT_SMILEYS)) {
        //validator.isNotNull(likertSteps, "likert steps is not initialized properly");
//        if (likertSteps != null) {
//          validator.isTrue(likertSteps == 5, "likert smiley only allows 5 steps");
//        }
      }
    }
    validator.isNotNull(conditional, "conditional is not initialized properly");
    if (conditional != null && conditional) {
      validator.isValidConditionalExpression(conditionExpression, "conditionalExpression is not properly specified");
    }
  }

  public boolean isInvisible() {
    return responseType.equals(LOCATION) || responseType.equals(PHOTO);
  }

  public boolean isNumeric() {
    return responseType.equals(LIKERT) ||
            responseType.equals(LIST) || // TODO (bobevans): LIST shoudl be a categorical, not a numeric.
            responseType.equals(NUMBER);
  }



}
