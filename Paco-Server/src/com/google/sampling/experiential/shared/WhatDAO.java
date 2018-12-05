package com.google.sampling.experiential.shared;

import org.codehaus.jackson.annotate.JsonProperty;

public class WhatDAO implements Comparable<WhatDAO> {


  private String name;

  @JsonProperty("answer")
  private String value;

  public WhatDAO() {
  }

  public WhatDAO(String name, String value) {
    super();
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("answer")
  public String getValue() {
    return value;
  }

  @JsonProperty("answer")
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int compareTo(WhatDAO o) {
    return getName().compareTo(o.getName());
  }

  @Override
  public String toString() {
    return "WhatDAO [name=" + name + ", value=" + value + "]";
  }

}
