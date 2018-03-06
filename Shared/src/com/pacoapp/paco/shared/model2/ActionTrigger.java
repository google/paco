package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class ActionTrigger extends ModelBase implements Validatable, Serializable {

  public static final String INTERRUPT_TRIGGER_TYPE_SPECIFIER = "interruptTrigger";
  public static final String SCHEDULE_TRIGGER_TYPE_SPECIFIER = "scheduleTrigger";
  protected String type;
  protected List<PacoAction> actions;

  // This id should be unique within its group and stable across edits because the client
  // relies on the id to recognize a actionTrigger and action that started a
  // chain of events
  private Long id;



  public ActionTrigger() {
    super();
    actions = new ArrayList();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<PacoAction> getActions() {
    return actions;
  }

  public void setActions(List<PacoAction> triggerActions) {
    this.actions = triggerActions;
    ExperimentValidator validator = new ExperimentValidator();
    validateActions(validator);
    if (validator.getResults().size() > 0 ) {
      throw new IllegalArgumentException(validator.stringifyResults());
    }
  }

  public PacoAction getActionById(Integer id) {
    for (PacoAction at : actions) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }

  public void validateWith(Validator validator) {
//    System.out.println("VALIDATING ACTION TRIGGER");
    validator.isNonEmptyString(getType(), getClass().getSimpleName()
                                         + " type field is not properly initialized");
    validateActions(validator);
  }

  public void validateActions(Validator validator) {
//    System.out.println("VALIDATING Actions");
    validator.isNotNullAndNonEmptyCollection(actions, "ActionTrigger actions should contain at least one action");

    Set<Long> ids = new HashSet();

    boolean hasNotificationToParticipateAction = false;
    boolean hasNotificationMessageAction = false;

    for (PacoAction pacoAction : actions) {
      if (!ids.add(pacoAction.getId())) {
        validator.addError("action id: " + pacoAction.getId() + " is not unique. Each action needs a unique id that is stable across edits.");
      }
      final Integer actionCode = pacoAction.getActionCode();
      validator.isNotNull(actionCode, "actionCode is not properly initialized");
      if (actionCode != null && actionCode.equals(PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE) && hasNotificationToParticipateAction) {
        validator.addError("Should only have one notification to participate action");
      } else {
        hasNotificationToParticipateAction = true;
      }
      if (actionCode != null && actionCode.equals(PacoAction.NOTIFICATION_ACTION_CODE) && hasNotificationMessageAction) {
        validator.addError("Should only have one notification message action");
      } else {
        hasNotificationMessageAction = true;
      }
      pacoAction.validateWith(validator);
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

}
