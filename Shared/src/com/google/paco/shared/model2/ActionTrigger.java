package com.google.paco.shared.model2;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;


public abstract class ActionTrigger implements Validatable, Serializable {

  protected String type;
  protected List<PacoAction> actions;
  private Boolean onlyEditableOnJoin = false;
  private Boolean userEditable = true;



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
    validator.isNotNullAndNonEmptyCollection(actions, "ActionTrigger actions should contain at least one action");
    for (PacoAction pacoAction : actions) {
      pacoAction.validateWith(validator);
    }
  }

}
