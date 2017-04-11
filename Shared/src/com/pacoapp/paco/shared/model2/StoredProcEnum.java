package com.pacoapp.paco.shared.model2;

public enum StoredProcEnum {
  
  TopNAppUsage ("appusage", "Top15");
  String realDbName;
  String clientRefName;
  
  private StoredProcEnum(String clientRefName, String realDbName){
    this.clientRefName = clientRefName;
    this.realDbName = realDbName;
  }

  public String getRealDbName() {
    return realDbName;
  }

  public void setRealDbName(String realDbName) {
    this.realDbName = realDbName;
  }

  public String getClientRefName() {
    return clientRefName;
  }

  public void setClientRefName(String clientRefName) {
    this.clientRefName = clientRefName;
  }
  
  public static StoredProcEnum getEnum(String clientRefValue) {
    for(StoredProcEnum sp : values()) {
        if(sp.getClientRefName().equalsIgnoreCase(clientRefValue)) { 
          return sp;
        }
    }
    throw new IllegalArgumentException();
  }
  

}
