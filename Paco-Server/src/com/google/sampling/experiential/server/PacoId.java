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
  
  public PacoId() {
    isCreatedWithThisCall = false;
  }
  public PacoId(Long id, Boolean isCreatedWithThisCall) {
    this.id = id;
    this.isCreatedWithThisCall = isCreatedWithThisCall;
  }
  public PacoId(Integer id, Boolean isCreatedWithThisCall) {
    this.id = new Long(id);
    this.isCreatedWithThisCall = isCreatedWithThisCall;
  }
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
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PacoId other = (PacoId) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  @Override
  public String toString() {
    return "PacoId [id=" + id + ", isCreatedWithThisCall=" + isCreatedWithThisCall + "]";
  }

}
