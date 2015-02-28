package com.google.paco.shared.model2;

import java.util.ArrayList;
import java.util.List;

public class ExperimentGroup implements Validatable, java.io.Serializable {

  private String name;

  private Boolean customRendering = false;
  private String customRenderingCode;

  private Boolean fixedDuration = false;
  private String startDate;
  private String endDate;

  private Boolean logActions = false;

  private Boolean backgroundListen = false;
  private String backgroundListenSourceIdentifier;

  private List<ActionTrigger> actionTriggers;
  private List<Input2> inputs;
  private Boolean endOfDayGroup = false;
  private String endOfDayReferredGroupName;

  private Feedback feedback;
  private Integer feedbackType;

  public ExperimentGroup() {
    super();
    this.actionTriggers = new ArrayList<ActionTrigger>();
    this.inputs = new ArrayList<Input2>();
  }

  public ExperimentGroup(String string) {
    this();
    this.name = string;
  }

  public List<ActionTrigger> getActionTriggers() {
    return actionTriggers;
  }

  public void setActionTriggers(List<ActionTrigger> actionTriggers) {
    this.actionTriggers = actionTriggers;
  }

  public Feedback getFeedback() {
    return feedback;
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
  }

  public Boolean getCustomRendering() {
    return customRendering;
  }

  public void setCustomRendering(Boolean customRendering) {
    this.customRendering = customRendering;
  }

  public String getCustomRenderingCode() {
    return customRenderingCode;
  }

  public void setCustomRenderingCode(String customRenderingCode) {
    this.customRenderingCode = customRenderingCode;
  }

  public Integer getFeedbackType() {
    return feedbackType;
  }

  public void setFeedbackType(Integer feedbackType) {
    this.feedbackType = feedbackType;
  }

  public List<Input2> getInputs() {
    return inputs;
  }

  public void setInputs(List<Input2> inputs) {
    this.inputs = inputs;
  }

  public Boolean getFixedDuration() {
    return fixedDuration;
  }

  public void setFixedDuration(Boolean fixedDuration) {
    this.fixedDuration = fixedDuration;
  }

  public Boolean getBackgroundListen() {
    return backgroundListen;
  }

  public void setBackgroundListen(Boolean backgroundListen) {
    this.backgroundListen = backgroundListen;
  }

  public String getBackgroundListenSourceIdentifier() {
    return backgroundListenSourceIdentifier;
  }

  public void setBackgroundListenSourceIdentifier(String backgroundListenSourceIdentifier) {
    this.backgroundListenSourceIdentifier = backgroundListenSourceIdentifier;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public Boolean getLogActions() {
    return logActions;
  }

  public void setLogActions(Boolean logActions) {
    this.logActions = logActions;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void validateWith(Validator validator) {
    validator.isNotNullAndNonEmptyString(name, "name is not properly initialized");
    validator.isNotNullCollection(actionTriggers, "action triggers not properly initialized");
    validator.isNotNullCollection(inputs, "inputs not properly initialized");
    validator.isNotNull(backgroundListen, "backgroundListen not initialized");
    validator.isNotNull(logActions, "backgroundListen not initialized");
    if (backgroundListen) {
      validator.isNotNullAndNonEmptyString(backgroundListenSourceIdentifier,
                                           "background listening requires a source identifier");
    }
    validator.isNotNull(customRendering, "customRendering not initialized properly");
    if (customRendering) {
      validator.isValidJavascript(customRenderingCode, "code is not properly formed");
    }
    validator.isNotNull(fixedDuration, "fixed duration not properly initialized");
    if (fixedDuration) {
      validator.isValidDateString(startDate, "start date must be a valid string");
      validator.isValidDateString(endDate, "end date must be a valid string");
    }
    validator.isNotNull(feedbackType, "feedbacktype is not properly initialized");
    validator.isNotNull(feedback, "feedback is not properly initialized");

    for (ActionTrigger actionTrigger : actionTriggers) {
      actionTrigger.validateWith(validator);
    }

    for (Input2 input : inputs) {
      input.validateWith(validator);
    }

    validator.isNotNull(endOfDayGroup, "endOfDayGroup is not properly initialized");
    if (endOfDayGroup != null && endOfDayGroup) {
      validator.isNotNullAndNonEmptyString(endOfDayReferredGroupName, "endOfDayGroups need to specify the name of the group to which they refer");
    }
    feedback.validateWith(validator);
  }

  public Boolean getEndOfDayGroup() {
    return endOfDayGroup;
  }

  public void setEndOfDayGroup(Boolean endOfDayGroup) {
    this.endOfDayGroup = endOfDayGroup;
  }

  public String getEndOfDayReferredGroupName() {
    return endOfDayReferredGroupName;
  }

  public void setEndOfDayReferredGroupName(String endOfDayReferredGroupName) {
    this.endOfDayReferredGroupName = endOfDayReferredGroupName;
  }



}
