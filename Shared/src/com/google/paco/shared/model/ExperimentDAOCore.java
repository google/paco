package com.google.paco.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExperimentDAOCore implements Serializable {

  public static final int APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION = 1;
  public static final int LOCATION_DATA_COLLECTION = 2;
  public static final int PHONE_DETAILS = 3;
  public static final List<Integer> EXTRA_DATA_COLLECTION_DECLS = new ArrayList<Integer>();
  static {
    EXTRA_DATA_COLLECTION_DECLS.add(APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION);
    EXTRA_DATA_COLLECTION_DECLS.add(LOCATION_DATA_COLLECTION);
    EXTRA_DATA_COLLECTION_DECLS.add(PHONE_DETAILS);
   }

  protected String title;
  protected String description;
  protected String informedConsentForm;
  protected String creator;
  protected Boolean fixedDuration = false;
  protected String startDate;
  protected String endDate;
  protected String joinDate;
  protected Long id;
  private Boolean backgroundListen;
  private String backgroundListenSourceIdentifier;
  private Boolean logActions;
  private Boolean recordPhoneDetails;
  protected List<Integer> extraDataCollectionDeclarations;

  public ExperimentDAOCore(Long id, String title, String description, String informedConsentForm,
                           String email, Boolean fixedDuration,
                           String startDate, String endDate, String joinDate, Boolean backgroundListen,
                           String backgroundListenSourceIdentifier, Boolean logActions, Boolean recordPhoneDetails,
                           List<Integer> extraDataCollectionDeclarations) {
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
    this.setBackgroundListen(backgroundListen);
    this.setBackgroundListenSourceIdentifier(backgroundListenSourceIdentifier);
    this.setLogActions(logActions);
    this.setRecordPhoneDetails(recordPhoneDetails);
    if (extraDataCollectionDeclarations == null) {
      this.extraDataCollectionDeclarations = new ArrayList<Integer>();
    } else {
      this.extraDataCollectionDeclarations = extraDataCollectionDeclarations;
    }
  }

  /**
   *
   */
  public ExperimentDAOCore() {
    super();
    this.extraDataCollectionDeclarations = new ArrayList<Integer>();
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

  public Boolean isLogActions() {
    return logActions;
  }

  public void setLogActions(Boolean logActions) {
    this.logActions = logActions;
  }

  public Boolean isBackgroundListen() {
    return backgroundListen;
  }

  public void setBackgroundListen(Boolean backgroundListen) {
    this.backgroundListen = backgroundListen;
  }

  public String getBackgroundListenSourceIdentifier() {
    return backgroundListenSourceIdentifier;
  }

  public void setBackgroundListenSourceIdentifier(String sourceId) {
    this.backgroundListenSourceIdentifier = sourceId;
  }

  public Boolean isRecordPhoneDetails() {
    return recordPhoneDetails;
  }

  public void setRecordPhoneDetails(Boolean recordDetails) {
    this.recordPhoneDetails = recordDetails;
  }

  public List<Integer> getExtraDataCollectionDeclarations() {
    return extraDataCollectionDeclarations;
  }

  public void setExtraDataCollectionDeclarations(List<Integer> extraDataDeclarations) {
    this.extraDataCollectionDeclarations = extraDataDeclarations;
  }



}