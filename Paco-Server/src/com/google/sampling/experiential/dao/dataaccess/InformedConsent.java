package com.google.sampling.experiential.dao.dataaccess;

import java.util.logging.Logger;

import com.google.sampling.experiential.server.PacoId;

public class InformedConsent implements PacoComparator<InformedConsent> {
  public static final Logger log = Logger.getLogger(InformedConsent.class.getName());
  private Long experimentId;
  private PacoId informedConsentId;
  private String informedConsent;
  public Long getExperimentId() {
    return experimentId;
  }
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }
  public PacoId getInformedConsentId() {
    return informedConsentId;
  }
  public void setInformedConsentId(PacoId informedConsentId) {
    this.informedConsentId = informedConsentId;
  }
  public String getInformedConsent() {
    return informedConsent;
  }
  public void setInformedConsent(String informedConsent) {
    this.informedConsent = informedConsent;
  }
  public Boolean isInformedConsentStringMatched(InformedConsent other) {
    boolean matched = false;
   if (other.getInformedConsent().equalsIgnoreCase(this.getInformedConsent())) {
      matched =  true;
    }
    return matched; 
  }
  @Override
  public String toString() {
    return "InformedConsent [experimentId=" + experimentId + ", informedConsentId="
           + informedConsentId + ", informedConsent=" + informedConsent + "]";
  }
  @Override
  public boolean hasChanged(InformedConsent olderVersion) {
    boolean hasChanged = true;
    if (olderVersion == null || olderVersion.getInformedConsent() == null) {
       hasChanged = true;
    } else if (this.isInformedConsentStringMatched(olderVersion)) {
      hasChanged = false;
    } 
    return hasChanged;
  }
}
