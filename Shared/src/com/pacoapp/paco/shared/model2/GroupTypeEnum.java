package com.pacoapp.paco.shared.model2;

public enum GroupTypeEnum {
  SYSTEM(1), 
  SURVEY(2),
  APPUSAGE_ANDROID(3), 
  NOTIFICATION(4),
  ACCESSIBILITY(5),
  PHONESTATUS(6),
  APPUSAGE_DESKTOP(7),
  APPUSAGE_SHELL(8),
  IDE_IDEA_USAGE(9);

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
