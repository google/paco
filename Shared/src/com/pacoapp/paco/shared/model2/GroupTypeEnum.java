package com.pacoapp.paco.shared.model2;

public enum GroupTypeEnum {
  SYSTEM(1), 
  SURVEY(2),
  APPUSAGE(3), 
  NOTFICATION(4),
  ACCESSIBILITY(5),
  SCRIPTED(6),
  PHONESTATUS(7);

  private int groupTypeId;
  
  private GroupTypeEnum(int groupTypeId) {
      this.groupTypeId = groupTypeId;
  }

  public int getGroupTypeId() {
    return groupTypeId;
  }

  public void setGroupTypeId(int groupTypeId) {
    this.groupTypeId = groupTypeId;
  }
}