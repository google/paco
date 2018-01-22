package com.google.sampling.experiential.dao.dataaccess;

import com.google.sampling.experiential.server.PacoId;

public class InformedConsent {
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
    if (other == null || other.getInformedConsent() == null) {
      matched = false;
    } else if (other.getInformedConsent().equalsIgnoreCase(this.getInformedConsent())) {
      matched =  true;
    }
    return matched; 
  }
  @Override
  public String toString() {
    return "InformedConsent [experimentId=" + experimentId + ", informedConsentId="
           + informedConsentId + ", informedConsent=" + informedConsent + "]";
  }
}
