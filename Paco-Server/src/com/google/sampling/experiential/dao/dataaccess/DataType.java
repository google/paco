package com.google.sampling.experiential.dao.dataaccess;

import com.google.sampling.experiential.server.PacoId;

public class DataType {
  private PacoId dataTypeId;
  private String name;
  private boolean numeric;
  private boolean multiSelect;
  private boolean responseMappingRequired;
  public DataType() {
    
  }
  public DataType(String name, boolean numeric, boolean multiSelect) {
    this.name = name;
    this.numeric = numeric;
    this.multiSelect = multiSelect;
  }
  public PacoId getDataTypeId() {
    return dataTypeId;
  }
  public void setDataTypeId(PacoId dataTypeId) {
    this.dataTypeId = dataTypeId;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public boolean isNumeric() {
    return numeric;
  }
  public void setNumeric(boolean numeric) {
    this.numeric = numeric;
  }
  public boolean isMultiSelect() {
    return multiSelect;
  }
  public void setMultiSelect(boolean multiSelect) {
    this.multiSelect = multiSelect;
  }
  public boolean isResponseMappingRequired() {
    return responseMappingRequired;
  }
  public void setResponseMappingRequired(boolean responseMappingRequired) {
    this.responseMappingRequired = responseMappingRequired;
  }
  @Override
  public String toString() {
    return "DataType [dataTypeId=" + dataTypeId + ", name=" + name + ", numeric=" + numeric + ", multiSelect="
           + multiSelect + ", responseMappingRequired=" + responseMappingRequired + "]";
  }
}
