package com.google.sampling.experiential.server;

public class BlobAcl {

  // blobstore id for blob
  private String keyString;
  private String experimentId;
  private String who;
  // gcs bucket name for blob
  private String bucketName;
  // gcs object name for blob
  private String objectName;

  public BlobAcl(String keyString, String experimentIdForBlob, String who, String bucketName, String objectName) {
    this.keyString = keyString;
    this.experimentId = experimentIdForBlob;
    this.who = who;          
    this.setBucketName(bucketName);
    this.setObjectName(objectName);
  }

  public String getKeyString() {
    return keyString;
  }

  public void setKeyString(String keyString) {
    this.keyString = keyString;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  
  

}
