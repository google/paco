package com.pacoapp.paco.shared.model2;

import java.io.Serializable;

public class ModelBase implements Serializable {

  public String getNameOfClass() {
    return this.getClass().getName();
  }

}
