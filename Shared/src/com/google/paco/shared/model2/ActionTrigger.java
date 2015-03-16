package com.google.paco.shared.model2;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public abstract class ActionTrigger implements Validatable, Serializable {

  protected String type;
  protected List<PacoAction> actions;
  private Boolean onlyEditableOnJoin = false;
  private Boolean userEditable = true;

  // This id should be unique within its group and stable across edits because the client
  // relies on the id to recognize a actionTrigger and action that started a
  // chain of events
  private Long id;



  public ActionTrigger() {
    super();
    actions = Lists.newArrayList();
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

  public Boolean getOnlyEditableOnJoin() {
    return onlyEditableOnJoin;
  }

  public Boolean getUserEditable() {
    return userEditable;
  }

  public void setOnlyEditableOnJoin(Boolean value) {
    this.onlyEditableOnJoin = value;
  }

  public void setUserEditable(Boolean userEditable) {
    this.userEditable = userEditable;
  }

  public void validateWith(Validator validator) {
    validator.isNotNullAndNonEmptyString(getType(), getClass().getSimpleName()
                                         + " type field is not properly initialized");
    validator.isNotNull(onlyEditableOnJoin, "onlyEditableOnJoin is not properly initialized");
    validator.isNotNull(userEditable, "userEditable is not properly initialized");
    validateActions(validator);
  }

  public void validateActions(Validator validator) {
    validator.isNotNullAndNonEmptyCollection(actions, "ActionTrigger actions should contain at least one action");

    Set<Long> ids = Sets.newHashSet();

    boolean hasNotificationToParticipateAction = false;
    boolean hasNotificationMessageAction = false;

    for (PacoAction pacoAction : actions) {
      if (!ids.add(pacoAction.getId())) {
        validator.addError("action id: " + pacoAction.getId() + " is not unique. Each action needs a unique id that is stable across edits.");
      }
      if (pacoAction.getActionCode() == PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE && hasNotificationToParticipateAction) {
        validator.addError("Should only have one notification to participate action");
      } else {
        hasNotificationToParticipateAction = true;
      }
      if (pacoAction.getActionCode() == PacoAction.NOTIFICATION_ACTION_CODE && hasNotificationMessageAction) {
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
