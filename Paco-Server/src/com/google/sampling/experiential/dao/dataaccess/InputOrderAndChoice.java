package com.google.sampling.experiential.dao.dataaccess;

import java.util.logging.Logger;

public class InputOrderAndChoice implements PacoComparator<InputOrderAndChoice> {
  public static final Logger log = Logger.getLogger(InputOrderAndChoice.class.getName());
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
  @Override
  public boolean hasChanged(InputOrderAndChoice olderVersion) {
    boolean hasChanged = false;
    if (olderVersion == null) {
      hasChanged = true;
    } else { 
      boolean inputChanged = this.getInput().hasChanged(olderVersion.getInput());
      boolean inputOrderChanged = this.getInputOrder() != olderVersion.getInputOrder();
      boolean choiceCollectionChanged = false;
      if (this.getChoiceCollection() != null) {
        choiceCollectionChanged = this.getChoiceCollection().hasChanged(olderVersion.getChoiceCollection());
      }
      if (inputChanged || inputOrderChanged || choiceCollectionChanged) {
        hasChanged = true;
      }
      if (!inputChanged) {
        this.getInput().setInputId(olderVersion.getInput().getInputId());
      }
      if(this.getChoiceCollection() != null && !choiceCollectionChanged) {
        this.getChoiceCollection().setChoiceCollectionId(olderVersion.getChoiceCollection().getChoiceCollectionId());
      }
    }
    
    return hasChanged;
  }

}
