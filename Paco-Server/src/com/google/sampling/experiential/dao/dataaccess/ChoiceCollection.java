package com.google.sampling.experiential.dao.dataaccess;

import java.util.Map;

public class ChoiceCollection {
  private Long choiceCollectionId;
  private Map<String, Choice> choices;
  
  public Long getChoiceCollectionId() {
    return choiceCollectionId;
  }
  public void setChoiceCollectionId(Long choiceCollectionId) {
    this.choiceCollectionId = choiceCollectionId;
  }
  public Map<String, Choice> getChoices() {
    return choices;
  }
  public void setChoices(Map<String, Choice> choices) {
    this.choices = choices;
  }
  @Override
  public String toString() {
    return "ChoiceCollection [choiceCollectionId=" + choiceCollectionId + ", choices=" + choices + "]";
  }

}
