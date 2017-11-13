package com.google.sampling.experiential.server;

public class PacoId {
  Long id;
  Boolean isCreatedWithThisCall;
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public Boolean getIsCreatedWithThisCall() {
    return isCreatedWithThisCall;
  }
  public void setIsCreatedWithThisCall(Boolean isCreatedWithThisCall) {
    this.isCreatedWithThisCall = isCreatedWithThisCall;
  }

}
