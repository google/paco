package com.google.sampling.experiential.dao.dataaccess;

import java.util.Map;

public class InputCollection {
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
}
