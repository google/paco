package com.google.sampling.experiential.dao.dataaccess;

import com.google.sampling.experiential.server.PacoId;

public class ExternStringInput {
  private PacoId externStringInputId;
  private String label;
  public ExternStringInput() { 
    
  }
  public ExternStringInput(String label) {
    Long labelId = null;
    this.label = label;
    this.externStringInputId = new PacoId(labelId, false);
  }
  public ExternStringInput(String label, Long labelId) {
    this.label = label;
    this.externStringInputId = new PacoId(labelId, false);
  }
  public PacoId getExternStringInputId() {
    return externStringInputId;
  }
  public void setExternStringInputId(PacoId externStringInputId) {
    this.externStringInputId = externStringInputId;
  }
  public String getLabel() {
    if (label == null ) {
      label = "";
    }
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  @Override
  public String toString() {
    return "ExternStringInput [externStringInputId=" + externStringInputId + ", label=" + label + "]";
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((label == null) ? 0 : label.hashCode());
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
    ExternStringInput other = (ExternStringInput) obj;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    return true;
  }
}
