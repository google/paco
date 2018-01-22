package com.google.sampling.experiential.dao.dataaccess;

public class Choice {
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
}
