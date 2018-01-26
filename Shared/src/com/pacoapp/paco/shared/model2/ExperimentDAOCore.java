package com.pacoapp.paco.shared.model2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExperimentDAOCore extends ModelBase implements Validatable, Serializable {

  public static final int APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION = 1;
  public static final int LOCATION_DATA_COLLECTION = 2;
  public static final int PHONE_DETAILS = 3;
  public static final int APP_INSTALL_DATA_COLLECTION = 4;
  public static final int ACCESSIBILITY_LOGGING = 5;
  public static final List<Integer> EXTRA_DATA_COLLECTION_DECLS = new ArrayList<Integer>();
  static {
    EXTRA_DATA_COLLECTION_DECLS.add(APP_USAGE_BROWSER_HISTORY_DATA_COLLECTION);
    EXTRA_DATA_COLLECTION_DECLS.add(LOCATION_DATA_COLLECTION);
    EXTRA_DATA_COLLECTION_DECLS.add(PHONE_DETAILS);
    EXTRA_DATA_COLLECTION_DECLS.add(APP_INSTALL_DATA_COLLECTION);
    EXTRA_DATA_COLLECTION_DECLS.add(ACCESSIBILITY_LOGGING);
   }





  protected String title;
  protected String description;
  protected String creator;
  protected String organization;
  protected String contactEmail;
  protected String contactPhone;
  private String publicKey;
  protected String joinDate;
  protected Long id;
  protected String informedConsentForm;
  private Boolean recordPhoneDetails = false;
  private List<Integer> extraDataCollectionDeclarations;
  protected Boolean deleted = false;
  private Date earliestStartDate;
  private Date latestEndDate;

  public ExperimentDAOCore(Long id, String title, String description, String informedConsentForm,
                           String creatorEmail, String publicKey,
                           String joinDate, Boolean recordPhoneDetails, Boolean deleted2,
                           List<Integer> extraDataCollectionDeclarationsList,
                           String organization, String contactPhone, String contactEmail,
                           Date earliestStartDate, Date latestEndDate) {
    super();
    this.id = id;
    this.title = title;
    this.description = description;
    this.informedConsentForm = informedConsentForm;
    this.creator = creatorEmail;
    this.organization = organization;
    this.contactEmail = contactEmail;
    this.contactPhone = contactPhone;
    this.publicKey = publicKey;
    this.joinDate = joinDate;
    this.setRecordPhoneDetails(recordPhoneDetails);
    this.deleted = deleted != null ? deleted : false;

    this.extraDataCollectionDeclarations = ListMaker.paramOrNewList(extraDataCollectionDeclarationsList, Integer.class);

    this.earliestStartDate = earliestStartDate;
    this.latestEndDate = latestEndDate;
  }




  /**
   *
   */
  public ExperimentDAOCore() {
    super();
    this.extraDataCollectionDeclarations = new java.util.ArrayList();
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

  public void setRecordPhoneDetails(Boolean recordDetails) {
    if (recordDetails != null) {
      this.recordPhoneDetails = recordDetails;
    }
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public List<Integer> getExtraDataCollectionDeclarations() {
    return extraDataCollectionDeclarations;
  }

  public void setExtraDataCollectionDeclarations(List<Integer> extraDataCollectionDeclarations) {
    this.extraDataCollectionDeclarations = extraDataCollectionDeclarations;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public Boolean getRecordPhoneDetails() {
    return recordPhoneDetails;
  }

  @Override
  public void validateWith(Validator validator) {
//    System.out.println("VALIDATING CORE");
    validator.isNonEmptyString(title, "Experiment title cannot be null");
    validator.isValidEmail(creator, "Experiment creator must be a valid email address");
    if (contactEmail != null && contactEmail.length() > 0) {
      validator.isValidEmail(contactEmail, "Experiment contact must be a valid email address");
    }

    validator.isNotNull(deleted, "deleted is not properly initialized");
    validator.isNotNull(recordPhoneDetails, "recordPhoneDetails is not properly initialized");
    validator.isNotNullCollection(extraDataCollectionDeclarations,
                                  "extra data declaration if you use extra data");
    if (joinDate != null) {
      validator.isValidDateString(joinDate, "join date should be a valid date string");
    }
    if (organization != null && organization.length() > 0) {
      validator.isNonEmptyString(organization,
                                           "organization must be non null if it is specified");
    }

  }

  public Date getEarliestStartDate() {
    return earliestStartDate;
  }

  public void setEarliestStartDate(Date earliestStartDate) {
    this.earliestStartDate = earliestStartDate;
  }

  public Date getLatestEndDate() {
    return latestEndDate;
  }

  public void setLatestEndDate(Date latestEndDate) {
    this.latestEndDate = latestEndDate;
  }


  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }
}