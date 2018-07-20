package com.google.sampling.experiential.dao.dataaccess;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class ExperimentDetail implements PacoComparator<ExperimentDetail> {
  public static final Logger log = Logger.getLogger(ExperimentDetail.class.getName());
  private PacoId experimentDetailId;
  private String title;
  private String description;
  private User creator;
  private String organization;
  private String contactEmail;
  private InformedConsent informedConsent;
  private boolean deleted;
  private DateTime modifiedDate;
  private boolean published;
  private String ringtoneUri;
  private String postInstallInstructions;
  public PacoId getExperimentDetailId() {
    return experimentDetailId;
  }
  public void setExperimentDetailId(PacoId experimentId) {
    this.experimentDetailId = experimentId;
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
  public User getCreator() {
    return creator;
  }
  public void setCreator(User creator) {
    this.creator = creator;
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
  public InformedConsent getInformedConsent() {
    return informedConsent;
  }
  public void setInformedConsent(InformedConsent informedConsent) {
    this.informedConsent = informedConsent;
  }
  public boolean isDeleted() {
    return deleted;
  }
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
  public DateTime getModifiedDate() {
    return modifiedDate;
  }
  public void setModifiedDate(DateTime modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
  public boolean isPublished() {
    return published;
  }
  public void setPublished(boolean published) {
    this.published = published;
  }
  public String getRingtoneUri() {
    return ringtoneUri;
  }
  public void setRingtoneUri(String ringtoneUri) {
    this.ringtoneUri = ringtoneUri;
  }
  public String getPostInstallInstructions() {
    return postInstallInstructions;
  }
  public void setPostInstallInstructions(String postInstallInstructions) {
    this.postInstallInstructions = postInstallInstructions;
  }
  @Override
  public String toString() {
    return "ExperimentHistory [experimentHistoryId=" + experimentDetailId + ", title=" + title + ", description="
           + description + ", creator=" + creator + ", organization=" + organization + ", contactEmail=" + contactEmail
           + ", informedConsent=" + informedConsent + ", deleted=" + deleted + ", modifiedDate=" + modifiedDate
           + ", published=" + published + ", ringtoneUri=" + ringtoneUri + ", postInstallInstructions="
           + postInstallInstructions + "]";
  }
  
  public Boolean equalsWithoutId(ExperimentDetail other) throws IllegalArgumentException, IllegalAccessException { 
    Field[] fields = this.getClass().getDeclaredFields();

    for (Field field : fields) {
      if (!field.getName().equals("experimentDetailId") && !field.getName().equals("modifiedDate") && !field.getName().equals("informedConsent") && !field.getName().equals("creator")) {
        if(((field.get(this) != null && !field.get(this).equals(field.get(other)))) || (field.get(this) == null && field.get(other) != null)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean hasChanged(ExperimentDetail oldVersion) {
    boolean hasChanged = false;
    try {
      if (oldVersion != null && this.equalsWithoutId(oldVersion)) {
        // experiment property fields matched with older version properties
        hasChanged = false;
      } else {
        hasChanged = true;
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      log.warning("Compare Experiment fields:"+ ErrorMessages.INVALID_ACCESS_OR_ARGUMENT_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));    
    }
    return hasChanged;
  }
}
