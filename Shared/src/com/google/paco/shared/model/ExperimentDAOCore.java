package com.google.paco.shared.model;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class ExperimentDAOCore implements Serializable {

  public static final String DATE_FORMAT = "yyyy/MM/dd";
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
    setTitleWithValidation(title);
  }
  
  private void setTitleWithValidation(String title) {
    if (!isTitleValid(title)) {
      throw new IllegalArgumentException("Title cannot be empty.");
    }
    this.title = title;
  }
  
  public boolean isTitleValid() {
    return isTitleValid(title);
  }
  
  private boolean isTitleValid(String title) {
    return !title.equals("");
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
    setEndDateWithValidation(endDate);
  }
  
  private void setEndDateWithValidation(String endDate) {
    Date startDateAsDate = getFormattedDate(startDate, DATE_FORMAT);
    Date endDateAsDate = getFormattedDate(endDate, DATE_FORMAT);
    if (endDateAsDate.before(startDateAsDate)) {
      throw new IllegalArgumentException("End date cannot be before start date.");
    }
    this.endDate = endDate;
  }
  
  private Date getFormattedDate(String inputDateStr, String dateFormat) {
    if (inputDateStr == null) {
      return null;
    }
    DateTimeFormat formatter = DateTimeFormat.getFormat(DATE_FORMAT);
    return formatter.parse(inputDateStr);
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