package com.google.sampling.experiential.dao.dataaccess;

public class ExperimentVersionGroupMapping {
  private Long experimentVersionMappingId;
  private ExperimentDetail experimentInfo;
  private GroupDetail groupInfo;
  private InputCollection inputCollection;
  private boolean eventsPosted;
  private Long experimentId;
  private Integer experimentVersion;
  private String source;
  public ExperimentDetail getExperimentInfo() {
    return experimentInfo;
  }
  public void setExperimentInfo(ExperimentDetail experimentInfo) {
    this.experimentInfo = experimentInfo;
  }
  public GroupDetail getGroupInfo() {
    return groupInfo;
  }
  public void setGroupInfo(GroupDetail groupInfo) {
    this.groupInfo = groupInfo;
  }
  public InputCollection getInputCollection() {
    return inputCollection;
  }
  public void setInputCollection(InputCollection inputCollection) {
    this.inputCollection = inputCollection;
  }
  public boolean isEventsPosted() {
    return eventsPosted;
  }
  public void setEventsPosted(boolean eventsPosted) {
    this.eventsPosted = eventsPosted;
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
  public Long getExperimentVersionMappingId() {
    return experimentVersionMappingId;
  }
  public void setExperimentVersionMappingId(Long experimentVersionMappingId) {
    this.experimentVersionMappingId = experimentVersionMappingId;
  }
 
  public String getSource() {
    return source;
  }
  public void setSource(String source) {
    this.source = source;
  }
  @Override
  public String toString() {
    return "ExperimentVersionMapping [experimentVersionMappingId=" + experimentVersionMappingId + ", experimentDetail="
           + experimentInfo + ", groupDetail=" + groupInfo + ", inputCollection=" + inputCollection + ", eventsPosted="
           + eventsPosted + ", experimentId=" + experimentId + ", experimentVersion=" + experimentVersion + ", source="
           + source + "]";
  }

}
