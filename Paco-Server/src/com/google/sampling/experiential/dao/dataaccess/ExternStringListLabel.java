package com.google.sampling.experiential.dao.dataaccess;

import com.google.sampling.experiential.server.PacoId;

public class ExternStringListLabel {
  private PacoId externStringListLabelId;
  private String label;
  public PacoId getExternStringListLabelId() {
    return externStringListLabelId;
  }
  public void setExternStringListLabelId(PacoId externStringListLabelId) {
    this.externStringListLabelId = externStringListLabelId;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  @Override
  public String toString() {
    return "ExternStringListLabel [externStringListLabelId=" + externStringListLabelId + ", label=" + label + "]";
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
    ExternStringListLabel other = (ExternStringListLabel) obj;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    return true;
  }
}
