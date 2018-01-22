package com.google.sampling.experiential.dao.dataaccess;

public class InputOrderAndChoice {
  Input input;
  Integer inputOrder;
  ChoiceCollection choiceCollection;
  public Input getInput() {
    return input;
  }
  public void setInput(Input input) {
    this.input = input;
  }
  public Integer getInputOrder() {
    return inputOrder;
  }
  public void setInputOrder(Integer inputOrder) {
    this.inputOrder = inputOrder;
  }
  public ChoiceCollection getChoiceCollection() {
    return choiceCollection;
  }
  public void setChoiceCollection(ChoiceCollection choiceCollection) {
    this.choiceCollection = choiceCollection;
  }
  @Override
  public String toString() {
    return "InputOrderAndChoice [input=" + input + ", inputOrder=" + inputOrder + ", choiceCollection="
           + choiceCollection + "]";
  }

}
