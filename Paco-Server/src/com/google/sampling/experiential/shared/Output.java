package com.google.sampling.experiential.shared;

import java.io.Serializable;

public class Output implements Serializable {

  private String value;
  private String name;

  public Output(String name, String value) {
    this.value = value;
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  
  
}
