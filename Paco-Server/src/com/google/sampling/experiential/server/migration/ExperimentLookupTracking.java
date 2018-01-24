package com.google.sampling.experiential.server.migration;

public class ExperimentLookupTracking {
  private Integer trackingId;
  private Long experimentId;
  private String experimentName;
  private String groupName;
  private Integer experimentVersion;
  private String who;
  private Character updateEvents;
  public Integer getTrackingId() {
    return trackingId;
  }
  public void setTrackingId(Integer trackingId) {
    this.trackingId = trackingId;
  }
  public Long getExperimentId() {
    return experimentId;
  }
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }
  public String getExperimentName() {
    return experimentName;
  }
  public void setExperimentName(String experimentName) {
    this.experimentName = experimentName;
  }
  public String getGroupName() {
    return groupName;
  }
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }
  public Integer getExperimentVersion() {
    return experimentVersion;
  }
  public void setExperimentVersion(Integer experimentVersion) {
    this.experimentVersion = experimentVersion;
  }
  public String getWho() {
    return who;
  }
  public void setWho(String who) {
    this.who = who;
  }
  public Character getUpdateEvents() {
    return updateEvents;
  }
  public void setUpdateEvents(Character updateEvents) {
    this.updateEvents = updateEvents;
  }
}
