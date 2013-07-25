package com.google.paco.shared.model;

import java.io.Serializable;

public class ExperimentDAOCore implements Serializable {

  protected static final String DEFAULT_STRING = "";
  
  protected String title;
  protected String description;
  protected String informedConsentForm;
  protected String creator;
  protected Boolean fixedDuration = false;
  protected String startDate;
  protected String endDate;
  protected String joinDate;
  protected Long id;

  public ExperimentDAOCore(Long id, String title, String description, String informedConsentForm,
                           String email, Boolean fixedDuration, 
                           String startDate, String endDate, String joinDate) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.informedConsentForm = informedConsentForm;
    this.creator = email;
    this.fixedDuration = fixedDuration;
    this.startDate = startDate;
    this.endDate = endDate;
    this.joinDate = joinDate;
  }
  
  /**
   * 
   */
  public ExperimentDAOCore() {
    super();
    this.title = DEFAULT_STRING;
    this.description = DEFAULT_STRING;
    this.informedConsentForm = DEFAULT_STRING;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInformedConsentForm() {
    return informedConsentForm;
  }

  public void setInformedConsentForm(String informedConsentForm) {
    this.informedConsentForm = informedConsentForm;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public Boolean getFixedDuration() {
    return fixedDuration;
  }

  public void setFixedDuration(Boolean fixedDuration) {
    this.fixedDuration = fixedDuration;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }
  
  public String getJoinDate() {
    return joinDate;
  }

  public void setJoinDate(String joinDate) {
    this.joinDate = joinDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

}