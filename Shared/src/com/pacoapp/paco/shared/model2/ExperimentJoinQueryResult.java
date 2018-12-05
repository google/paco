package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.Date;
import java.util.List;



public class ExperimentJoinQueryResult implements Serializable {

  String cursor;
  List<Pair<Long, Date>> experiments;

  public ExperimentJoinQueryResult() {
    super();
  }

  public ExperimentJoinQueryResult(String newCursorString, List<Pair<Long, Date>> experimentIds) {
    this.cursor = newCursorString;
    this.experiments = experimentIds;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public List<Pair<Long, Date>> getExperiments() {
    return experiments;
  }

  public void setExperiments(List<Pair<Long, Date>> experimentIds) {
    this.experiments = experimentIds;
  }


}