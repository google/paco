package com.google.sampling.experiential.dao.dataaccess;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.google.sampling.experiential.server.PacoId;

public class Input implements PacoComparator<Input> {
  public static final Logger log = Logger.getLogger(Input.class.getName());
  private PacoId inputId;
  private ExternStringInput name;
  private boolean required;
  private String conditional;
  private DataType responseDataType;
  private ExternStringInput text;
  private Integer likertSteps;
  private String leftLabel;
  private String rightLabel;
  private PacoId parentId;
  
  public Input() {
    
  }
  public Input(String name, boolean required, String conditional, DataType dataType, String text, int likertSteps, String leftLabel, String rightLabel, Long parentId) {
    this.name = new ExternStringInput(name);
    this.required = required;
    this.conditional = conditional;
    this.responseDataType = dataType;
    this.text = new ExternStringInput(text);
    this.likertSteps = likertSteps;
    this.leftLabel = leftLabel;
    this.rightLabel = rightLabel;
    this.parentId = new PacoId(parentId, false);
  }
  
  public PacoId getInputId() {
    return inputId;
  }
  public void setInputId(PacoId inputId) {
    this.inputId = inputId;
  }
  public ExternStringInput getName() {
    return name;
  }
  public void setName(ExternStringInput name) {
    this.name = name;
  }
  public boolean isRequired() {
    return required;
  }
  public void setRequired(boolean required) {
    this.required = required;
  }
  public String getConditional() {
    return conditional;
  }
  public void setConditional(String conditional) {
    this.conditional = conditional;
  }
  public DataType getResponseDataType() {
    return responseDataType;
  }
  public void setResponseDataType(DataType responseDataType) {
    this.responseDataType = responseDataType;
  }
  public ExternStringInput getText() {
    return text;
  }
  public void setText(ExternStringInput text) {
    this.text = text;
  }
  public Integer getLikertSteps() {
    return likertSteps;
  }
  public void setLikertSteps(Integer likertSteps) {
    this.likertSteps = likertSteps;
  }
  public String getLeftLabel() {
    return leftLabel;
  }
  public void setLeftLabel(String leftLabel) {
    this.leftLabel = leftLabel;
  }
  public String getRightLabel() {
    return rightLabel;
  }
  public void setRightLabel(String rightLabel) {
    this.rightLabel = rightLabel;
  }
  public PacoId getParentId() {
    return parentId;
  }
  public void setParentId(PacoId parentId) {
    this.parentId = parentId;
  }

  public Boolean equalsWithoutId(Input other) throws IllegalArgumentException, IllegalAccessException { 
    Field[] fields = this.getClass().getDeclaredFields();

    for(Field field : fields){
      if (!field.getName().equals("inputId") && !field.getName().equals("responseDataType")) {
        if(((field.get(this) != null && !field.get(this).equals(field.get(other)))) || (field.get(this) == null && field.get(other) != null)){
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public String toString() {
    return "Input [inputId=" + inputId + ", name=" + name + ", required=" + required + ", conditional=" + conditional
           + ", responseDataType=" + responseDataType + ", text=" + text + ", likertSteps=" + likertSteps
           + ", leftLabel=" + leftLabel + ", rightLabel=" + rightLabel + 
           ", parentId="+ parentId + "]";
  }
  
  @Override
  public boolean hasChanged(Input olderVersion) {
    boolean hasChanged = true;
    try {
      if (this.equalsWithoutId(olderVersion)) {
        if (this.getResponseDataType().isMultiSelect() == olderVersion.getResponseDataType().isMultiSelect() && this.getResponseDataType().getName().equals(olderVersion.getResponseDataType().getName())
                                                          && this.getResponseDataType().isNumeric() == olderVersion.getResponseDataType().isNumeric()) {
          hasChanged = false;  
        }
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return hasChanged;
  }
}
