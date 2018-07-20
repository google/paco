package com.pacoapp.paco.shared.model2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.Days;

import com.pacoapp.paco.shared.util.TimeUtil;


public class ExperimentGroup extends ModelBase implements Validatable, java.io.Serializable {

  private static final int MAX_DURATION_DAYS_FOR_LARGE_DATA_LOGGERS = 14;

  private String name;
  private GroupTypeEnum groupType;

  private Boolean customRendering = false;
  private String customRenderingCode;

  private Boolean fixedDuration = false;
  private String startDate;
  private String endDate;

  private Boolean logActions = false;
  private Boolean logShutdown = false;

  private Boolean backgroundListen = false;
  private String backgroundListenSourceIdentifier;

  private Boolean accessibilityListen = false;

  private List<ActionTrigger> actionTriggers;
  private List<Input2> inputs;
  private Boolean endOfDayGroup = false;
  private String endOfDayReferredGroupName;

  private Feedback feedback;

  // Need to keep this for the interim experiments on staging
  // this will allow us to copy it on to the Feedback object for those
  // experiments
  private Integer feedbackType;

  private Boolean rawDataAccess = true;

  private boolean logNotificationEvents = false;

  public ExperimentGroup() {
    super();
    this.actionTriggers = new ArrayList<ActionTrigger>();
    this.inputs = new ArrayList<Input2>();
    this.feedbackType = Feedback.FEEDBACK_TYPE_STATIC_MESSAGE;
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
    // TODO comment this for now because upon json deserialization it throws and breaks protocol (we should always check later).
//    ExperimentValidator validator = new ExperimentValidator();
//    validateActionTriggers(validator);
//    if (!validator.getResults().isEmpty()) {
//      throw new IllegalArgumentException(validator.stringifyResults());
//    }
  }

  public ActionTrigger getActionTriggerById(Long actionTriggerId) {
    for (ActionTrigger at : actionTriggers) {
      if (at.getId().equals(actionTriggerId)) {
        return at;
      }
    }
    return null;
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
//    ExperimentValidator validator = new ExperimentValidator();
//    validateInputs(validator);
//    if (!validator.getResults().isEmpty()) {
//      throw new IllegalArgumentException(validator.stringifyResults());
//    }
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

  public Boolean getAccessibilityListen() { return accessibilityListen; }

  public void setAccessibilityListen(Boolean accessibilityListen) {
    this.accessibilityListen = accessibilityListen;
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
//    System.out.println("VALIDATING GROUP");
    validator.isNonEmptyString(name, "name is not properly initialized");

    validateActionTriggers(validator);

    validator.isNotNull(backgroundListen, "backgroundListen not initialized");
    validator.isNotNull(accessibilityListen, "accessibilityListen not initialized");
    validator.isNotNull(logActions, "logActions not initialized");
    validator.isNotNull(logNotificationEvents, "logNotificationEvents not initialized");
    validator.isNotNull(logShutdown, "logShutdown not initialized");
    if (backgroundListen != null && backgroundListen) {
      validator.isNonEmptyString(backgroundListenSourceIdentifier,
                                           "background listening requires a source identifier");
    }
    validator.isNotNull(customRendering, "customRendering not initialized properly");
    if (customRendering != null && customRendering) {
      validator.isValidJavascript(customRenderingCode, "custom rendering code is not properly formed");
    }
    validator.isNotNull(fixedDuration, "fixed duration not properly initialized");
    if (fixedDuration != null && fixedDuration) {
      validator.isValidDateString(startDate, "start date must be a valid string");
      validator.isValidDateString(endDate, "end date must be a valid string");
    }
    if (isPresentAndTrue(logActions) || isPresentAndTrue(accessibilityListen) || isPresentAndTrue(logNotificationEvents)) {
      if (fixedDuration == null || !fixedDuration || !isDurationLessThanTwoWeeks()) {
        validator.addError("logActions, logAccessibilityEvents and logNotificationEvents are only "
                + "allowed on Fixed Duration experiments that run less than 2 weeks due to large data volumes.");
      }
    }
    validator.isNotNull(feedbackType, "feedbacktype is not properly initialized");
    validator.isNotNull(feedback, "feedback is not properly initialized");

    validateInputs(validator);

    validator.isNotNull(endOfDayGroup, "endOfDayGroup is not properly initialized");
    if (endOfDayGroup != null && endOfDayGroup) {
      validator.isNonEmptyString(endOfDayReferredGroupName, "endOfDayGroups need to specify the name of the group to which they refer");
    }
    feedback.validateWith(validator);
  }



  private boolean isDurationLessThanTwoWeeks() {
    try {
      if (getStartDate() == null || getEndDate() == null) {
        return false;
      }
      DateMidnight startDateCandidate = TimeUtil.unformatDate(getStartDate()).toDateMidnight();
      DateMidnight endDateCandidate = TimeUtil.unformatDate(getEndDate()).toDateMidnight();
      Days daysDuration = Days.daysBetween(startDateCandidate, endDateCandidate);
      if (daysDuration.getDays() > MAX_DURATION_DAYS_FOR_LARGE_DATA_LOGGERS) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
       // fall through to return false
    }
    return false;
  }



  private boolean isPresentAndTrue(Boolean fieldToValidate) {
    return fieldToValidate != null && logActions;
  }

  public void validateInputs(Validator validator) {
//    System.out.println("VALIDATING INPTUS");
    validator.isNotNullCollection(inputs, "inputs not properly initialized");
    Set<String> inputNames = new HashSet();
    if (inputs == null) {
      return;
    }
    for (Input2 input : inputs) {
      if (!inputNames.add(input.getName())) {
        validator.addError("Input name: " + input.getName() + " is duplicate. All input names within a group must be unique");
      }
      input.validateWith(validator);
    }
  }

  public void validateActionTriggers(Validator validator) {
//    System.out.println("VALIDATING ACTION TRIGGERS");
    validator.isNotNullCollection(actionTriggers, "action triggers not properly initialized");
    Set<Long> ids = new HashSet();
    if (actionTriggers  != null) {
      for (ActionTrigger actionTrigger : actionTriggers) {
        actionTrigger.validateWith(validator);
        if (!ids.add(actionTrigger.getId())) {
          validator.addError("action trigger id: " + actionTrigger.getId() + " is not unique. Ids must be unique and stable across edits.");
        }
      }
    }
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
  
  public boolean getLogShutdown() {
    return this.logShutdown;
  }

  public void setLogShutdown(Boolean logShutdown) {
    this.logShutdown = logShutdown;
  }

  public Boolean getRawDataAccess() {
    return this.rawDataAccess;
  }

  public void setRawDataAccess(Boolean rawDataAccess) {
    this.rawDataAccess = rawDataAccess;
  }
  
  public Boolean getLogNotificationEvents() {
    return this.logNotificationEvents;
  }

  public void setLogNotificationEvents(Boolean shouldLog) {
    this.logNotificationEvents = shouldLog;
  }
  
  public GroupTypeEnum getGroupType() {
    return groupType;
  }
  
  public void setGroupType(GroupTypeEnum groupType) {
    this.groupType = groupType;
  }

}
