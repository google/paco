package com.google.sampling.experiential.dao.dataaccess;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class ChoiceCollection implements PacoComparator<ChoiceCollection> {
  public static final Logger log = Logger.getLogger(ChoiceCollection.class.getName());
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
  @Override
  public boolean hasChanged(ChoiceCollection olderVersion) {
    boolean hasChanged = false;
    if (olderVersion == null || olderVersion.getChoices() == null) {
      hasChanged = true;
    } else if (this.getChoices().size() != olderVersion.getChoices().size()) {
      hasChanged = true;
    } else {
      Iterator<String> newChoicesItr = this.getChoices().keySet().iterator();
      String currentNewChoice = null;
      while (newChoicesItr.hasNext()) {
        currentNewChoice = newChoicesItr.next();
        if (this.getChoices().get(currentNewChoice).hasChanged(olderVersion.getChoices().get(currentNewChoice))) {
          hasChanged = true;
        }
      }
    }
    return hasChanged;
  }
}
