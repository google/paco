package com.google.sampling.experiential.dao.dataaccess;

public class ExperimentLite {
  Long experimentId;
  Integer experimentVersion;
  String groupName; 
  public ExperimentLite(Long experimentId, Integer experimentVersion, String groupName) { 
    this(experimentId, experimentVersion);
    this.groupName = groupName;
  }
  public ExperimentLite(Long experimentId, Integer experimentVersion) { 
    this.experimentId = experimentId;
    this.experimentVersion = experimentVersion;
  }
  public Long getExperimentId() {
    return experimentId;
  }
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }
  public Integer getExperimentVersion() {
    return experimentVersion;
  }
  public void setExperimentVersion(Integer experimentVersion) {
    this.experimentVersion = experimentVersion;
  }
  public String getExperimentGroupName() {
    return groupName;
  }
  public void setExperimentVersion(String experimentGroupName) {
    this.groupName = experimentGroupName;
  }
  
}
