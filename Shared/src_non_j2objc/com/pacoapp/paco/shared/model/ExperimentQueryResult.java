package com.pacoapp.paco.shared.model;

import java.io.Serializable;
import java.util.List;


public class ExperimentQueryResult implements Serializable {



  public ExperimentQueryResult() {
    super();
  }


  public ExperimentQueryResult(String newCursorString, List<ExperimentDAO> experiments) {
    this.cursor = newCursorString;
    this.experiments = experiments;
  }
  String cursor;
  List<ExperimentDAO> experiments;
  public String getCursor() {
    return cursor;
  }
  public void setCursor(String cursor) {
    this.cursor = cursor;
  }
  public List<ExperimentDAO> getExperiments() {
    return experiments;
  }
  public void setExperiments(List<ExperimentDAO> experiments) {
    this.experiments = experiments;
  }


}