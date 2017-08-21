package com.pacoapp.paco.shared.model2;

import java.util.Date;
import java.util.List;

public class Visualizations {

  private int vizId;
  private Long experimentId;
  private String vizTitle;
  private Date modifyDate;
  private String question;
  private List<String> texts;
  private List<String> participants;
  private String vizType;
  private String vizDesc;
  private Date startDateTime;
  private Date endDateTime;


  public int getVizId() {
    return vizId;
  }

  public void setVizId(int vizId) {
    this.vizId = vizId;
  }
  
  public Long getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  public String getVizTitle() {
    return vizTitle;
  }

  public void setVizTitle(String vizTitle) {
    this.vizTitle = vizTitle;
  }
  
  public Date getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<String> getTexts() {
    return texts;
  }

  public void setTexts(List<String> texts) {
    this.texts = texts;
  }

  public List<String> getParticipants() {
    return participants;
  }

  public void setParticipants(List<String> participants) {
    this.participants = participants;
  }

  public String getVizType() {
    return vizType;
  }

  public void setVizType(String vizType) {
    this.vizType = vizType;
  }

  public String getVizDesc() {
    return vizDesc;
  }

  public void setVizDesc(String vizDesc) {
    this.vizDesc = vizDesc;
  }

  public Date getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(Date startDateTime) {
    this.startDateTime = startDateTime;
  }

  public Date getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(Date endDateTime) {
    this.endDateTime = endDateTime;
  }
  
}
