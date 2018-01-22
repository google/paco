package com.google.sampling.experiential.dao.dataaccess;

import java.lang.reflect.Field;

import com.google.sampling.experiential.server.PacoId;

public class Input {
  private PacoId inputId;
  private ExternStringInput name;
  private boolean required;
  private String conditional;
  private DataType responseDataType;
  private ExternStringInput text;
  private Integer likertSteps;
  private String leftLabel;
  private String rightLabel;
  private String channel;
  private PacoId parentId;
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
  public String getChannel() {
    return channel;
  }
  public void setChannel(String channel) {
    this.channel = channel;
  }
  public PacoId getParentId() {
    return parentId;
  }
  public void setParentId(PacoId parentId) {
    this.parentId = parentId;
  }

  public Boolean compareWithoutId(Input other) throws IllegalArgumentException, IllegalAccessException { 
    Field[] fields = this.getClass().getDeclaredFields();

    for(Field field : fields){
      if (!field.getName().equals("inputId") && !field.getName().equals("responseDataType")) {
        if(((field.get(this) != null && !field.get(this).equals(field.get(other)))) || (field.get(this) == null && field.get(other) != null)){
          System.out.println(false);
          return false;
        } else {
          System.out.println(true);
        }
      }
    }
    return true;
  }
  @Override
  public String toString() {
    return "Input [inputId=" + inputId + ", name=" + name + ", required=" + required + ", conditional=" + conditional
           + ", responseDataType=" + responseDataType + ", text=" + text + ", likertSteps=" + likertSteps
           + ", leftLabel=" + leftLabel + ", rightLabel=" + rightLabel + ", channel=" + channel + ", parentId="
           + parentId + "]";
  }
}
