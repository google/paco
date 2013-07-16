package com.google.sampling.experiential.server;

public class ReportJobStatus {
  private String id;
  private String requestor;
  private int status;
  private String errorMessage;
  private String location;
  private String startTime;
  private String endTime;
  public ReportJobStatus(String id, String requestor, int status,  
                   String startTime, String endTime, String location,
                   String errorMessage) {
    this.id = id;
    this.requestor = requestor;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.errorMessage = errorMessage;
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getRequestor() {
    return requestor;
  }
  public void setRequestor(String requestor) {
    this.requestor = requestor;
  }
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }
  public String getStartTime() {
    return startTime;
  }
  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }
  public String getEndTime() {
    return endTime;
  }
  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }
}