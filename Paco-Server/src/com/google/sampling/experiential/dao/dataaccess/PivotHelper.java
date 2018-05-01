package com.google.sampling.experiential.dao.dataaccess;

public class PivotHelper {
  Long expVersionMappingId;
  Integer anonWhoId;
  Long inputId;
  Long eventsPosted;
  Boolean processed;
  
  public PivotHelper(){
    
  }
  public PivotHelper(Long evMapping, Integer anonWhoId, Long inputId, Boolean processed) {
    this.expVersionMappingId = evMapping;
    this.anonWhoId = anonWhoId;
    this.inputId = inputId;
    this.processed = processed;
  }
  public PivotHelper(Long evMapping, Integer anonWhoId, Long inputId, Boolean processed, Long eventsPosted) {
    this(evMapping, anonWhoId, inputId, processed);
    this.eventsPosted = eventsPosted;
  }
  
  public Long getExpVersionMappingId() {
    return expVersionMappingId;
  }
  public void setExpVersionMappingId(Long expVersionMappingId) {
    this.expVersionMappingId = expVersionMappingId;
  }
  public Integer getAnonWhoId() {
    return anonWhoId;
  }
  public void setAnonWhoId(Integer anonWhoId) {
    this.anonWhoId = anonWhoId;
  }
  public Long getInputId() {
    return inputId;
  }
  public void setInputId(Long inputId) {
    this.inputId = inputId;
  }
  public Long getEventsPosted() {
    return eventsPosted;
  }
  public void setEventsPosted(Long eventsPosted) {
    this.eventsPosted = eventsPosted;
  }
  public Boolean getProcessed() {
    return processed;
  }
  public void setProcessed(Boolean processed) {
    this.processed = processed;
  }
  @Override
  public String toString() {
    return "PivotHelper [expVersionMappingId=" + expVersionMappingId + ", anonWhoId=" + anonWhoId + ", inputId="
           + inputId + ", eventsPosted=" + eventsPosted + ", processed=" + processed + "]";
  }
}
