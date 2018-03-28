package com.google.sampling.experiential.dao.dataaccess;

import java.util.logging.Logger;

import com.google.sampling.experiential.server.PacoId;

public class DataType  {
  public static final Logger log = Logger.getLogger(DataType.class.getName());
  private PacoId dataTypeId;
  private String name;
  private boolean numeric;
  private boolean multiSelect;
  private boolean responseMappingRequired;
  public DataType() {
    
  }
  public DataType(String name, boolean numeric, boolean multiSelect) {
    if ( name == null) {
      name ="";
    }
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
    if (name == null) {
      name = "";
    }
    return name;
  }
  public void setName(String name) {
    if ( name == null) {
      name = "";
    }
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
