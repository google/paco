package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.List;



public class ExperimentIdQueryResult implements Serializable {

  String cursor;
  List<Long> experiments;

  public ExperimentIdQueryResult() {
    super();
  }

  public ExperimentIdQueryResult(String newCursorString, List<Long> experimentIds) {
    this.cursor = newCursorString;
    this.experiments = experimentIds;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public List<Long> getExperiments() {
    return experiments;
  }

  public void setExperiments(List<Long> experimentIds) {
    this.experiments = experimentIds;
  }


}