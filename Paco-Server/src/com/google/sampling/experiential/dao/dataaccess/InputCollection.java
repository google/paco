package com.google.sampling.experiential.dao.dataaccess;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class InputCollection implements PacoComparator<InputCollection> {
  public static final Logger log = Logger.getLogger(InputCollection.class.getName());

  private Long inputCollectionId;
  Map<String, InputOrderAndChoice> inputOrderAndChoices;

  public Long getInputCollectionId() {
    return inputCollectionId;
  }
  public void setInputCollectionId(Long inputCollectionId) {
    this.inputCollectionId = inputCollectionId;
  }
  public Map<String, InputOrderAndChoice> getInputOrderAndChoices() {
    return inputOrderAndChoices;
  }
  public void setInputOrderAndChoices(Map<String, InputOrderAndChoice> inputOrderAndChoices) {
    this.inputOrderAndChoices = inputOrderAndChoices;
  }
  
  public boolean equals(InputCollection obj){
    if (obj == null) {
      return false;
    } else {
      if (obj.getInputCollectionId() == inputCollectionId) {
        return true;
      } else {
        return false;
      } 
    }
  }
  
  @Override
  public String toString() {
    return "InputCollection [inputCollectionId=" + inputCollectionId + ", inputOrderAndChoices=" + inputOrderAndChoices
           + "]";
  }
  
  @Override
  public boolean hasChanged(InputCollection olderVersion) {
    boolean hasChanged = false;
    if (olderVersion == null) {
      hasChanged = true;
      return hasChanged;
    } else if (this.getInputOrderAndChoices().size() != olderVersion.getInputOrderAndChoices().size()) {
      hasChanged = true;
    }
    // irrespective of the value of has changed, we need to check the ioc changed, in order to update the id's.
    Iterator<String> newInputItr = this.getInputOrderAndChoices().keySet().iterator();
    String currentNewInput = null;
    while (newInputItr.hasNext()) {
      currentNewInput = newInputItr.next();
      if (this.getInputOrderAndChoices() != null && this.getInputOrderAndChoices().get(currentNewInput) != null) {
        if (olderVersion.getInputOrderAndChoices() != null) {
          if (this.getInputOrderAndChoices().get(currentNewInput).hasChanged(olderVersion.getInputOrderAndChoices().get(currentNewInput))) {
            hasChanged = true;
          }
        } else {
          hasChanged = true;
        }
      } 
    }
  
    return hasChanged;
  }
}
