package com.google.sampling.experiential.server;

public class BlobAcl {

  private String keyString;
  private String experimentIdForBlob;
  private String who;

  public BlobAcl(String keyString, String experimentIdForBlob, String who) {
    this.keyString = keyString;
    this.experimentIdForBlob = experimentIdForBlob;
    this.who = who;          
  }

  public String getKeyString() {
    return keyString;
  }

  public void setKeyString(String keyString) {
    this.keyString = keyString;
  }

  public String getExperimentIdForBlob() {
    return experimentIdForBlob;
  }

  public void setExperimentIdForBlob(String experimentIdForBlob) {
    this.experimentIdForBlob = experimentIdForBlob;
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }
  
  

}
