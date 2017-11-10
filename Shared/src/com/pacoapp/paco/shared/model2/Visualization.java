package com.pacoapp.paco.shared.model2;

import java.util.List;

import org.joda.time.DateTime;

public class Visualization {

  private int id;
  private Long experimentId;
  private String title;


  private DateTime modifyDate;
  private String question;

  private VizVariable xAxisVariable;
  private List<VizVariable> yAxisVariables;

  private List<String> participants;
  private String type;
  private String description;

  private DateTime startDatetime;

  private DateTime endDatetime;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public Long getExperimentId() {
    return experimentId;
  }
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public DateTime getModifyDate() {
    return modifyDate;
  }
  public void setModifyDate(DateTime modifyDate) {
    this.modifyDate = modifyDate;
  }
  public String getQuestion() {
    return question;
  }
  public void setQuestion(String question) {
    this.question = question;
  }
  public VizVariable getxAxisVariable() {
    return xAxisVariable;
  }
  public void setxAxisVariable(VizVariable xAxisVariable) {
    this.xAxisVariable = xAxisVariable;
  }
  public List<VizVariable> getyAxisVariables() {
    return yAxisVariables;
  }
  public void setyAxisVariables(List<VizVariable> yAxisVariables) {
    this.yAxisVariables = yAxisVariables;
  }
  public List<String> getParticipants() {
    return participants;
  }
  public void setParticipants(List<String> participants) {
    this.participants = participants;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public DateTime getStartDatetime() {
    return startDatetime;
  }
  public void setStartDatetime(DateTime startDatetime) {
    this.startDatetime = startDatetime;
  }
  public DateTime getEndDatetime() {
    return endDatetime;
  }
  public void setEndDatetime(DateTime endDatetime) {
    this.endDatetime = endDatetime;
  }

}
