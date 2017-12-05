package com.google.sampling.experiential.server;
/**
 * Class to capture the auto id generated /retrieved in cloud sql, with additional information if the id was created in this call.
 * 
 * @author imeyyappan
 *
 */
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
