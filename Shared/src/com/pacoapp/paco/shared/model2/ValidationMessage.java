package com.pacoapp.paco.shared.model2;

import java.io.Serializable;


public class ValidationMessage implements Serializable {

  private int importance;
  private String msg;

  public ValidationMessage(String msg, int importance) {
    this.msg = msg;
    this.importance = importance;
  }

  public int getImportance() {
    return importance;
  }

  public void setImportance(int importance) {
    this.importance = importance;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  @Override
  public String toString() {
    return importanceString() +": " + msg;
  }

  private String importanceString() {
    switch (importance) {
    case Validator.MANDATORY:
      return "ERROR";
      //break;
    case Validator.OPTIONAL:
      return "WARNING";
    default:
      return "";
      //break;
    }
  }




}
