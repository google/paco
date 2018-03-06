package com.google.sampling.experiential.dao.dataaccess;

import java.util.logging.Logger;

public class Choice implements PacoComparator<Choice> {
  public static final Logger log = Logger.getLogger(Choice.class.getName());
  ExternStringListLabel choiceLabel;
  Integer choiceOrder;
  @Override
  public String toString() {
    return "Choice [choiceLabel=" + choiceLabel + ", choiceOrder=" + choiceOrder + "]";
  }
  public ExternStringListLabel getChoiceLabel() {
    return choiceLabel;
  }
  public void setChoiceLabel(ExternStringListLabel choiceLabel) {
    this.choiceLabel = choiceLabel;
  }
  public Integer getChoiceOrder() {
    return choiceOrder;
  }
  public void setChoiceOrder(Integer choiceOrder) {
    this.choiceOrder = choiceOrder;
  }
  @Override
  public boolean hasChanged(Choice olderVersion) {
    boolean hasChanged = true;
    if (olderVersion == null) {
      hasChanged = true;
    } else {
      if (this.getChoiceLabel().equals(olderVersion.getChoiceLabel())) {
        this.setChoiceLabel(olderVersion.getChoiceLabel());
        if ((this.getChoiceOrder() == olderVersion.getChoiceOrder())) {
          hasChanged = false;
        } else {
          hasChanged = true;
        }
      }
    }
    return hasChanged;
  }
}
