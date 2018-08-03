package com.google.sampling.experiential.dao.dataaccess;

public class GroupTypeInputMapping {
  Integer groupTypeInputMappingId;
  Integer groupTypeId;
  Input input;
  public GroupTypeInputMapping() {
    
  }
  public GroupTypeInputMapping(Integer groupTypeId, Input input) { 
    this.groupTypeId = groupTypeId;
    this.input = input;
  }
  public Integer getPredefinedInputId() {
    return groupTypeInputMappingId;
  }
  public void setPredefinedInputId(Integer predefinedInputId) {
    this.groupTypeInputMappingId = predefinedInputId;
  }
  public Input getInput() {
    return input;
  }
  public void setInput(Input input) {
    this.input = input;
  }
  public Integer getGroupTypeInputMappingId() {
    return groupTypeInputMappingId;
  }
  public void setGroupTypeInputMappingId(Integer groupTypeInputMappingId) {
    this.groupTypeInputMappingId = groupTypeInputMappingId;
  }
  public Integer getGroupTypeId() {
    return groupTypeId;
  }
  public void setGroupTypeId(Integer groupTypeId) {
    this.groupTypeId = groupTypeId;
  }
  @Override
  public String toString() {
    return "GroupTypeInputMapping [groupTypeInputMappingId=" + groupTypeInputMappingId + ", "
           + ", groupTypeId=" + groupTypeId + ", input=" + input + "]";
  }
}
