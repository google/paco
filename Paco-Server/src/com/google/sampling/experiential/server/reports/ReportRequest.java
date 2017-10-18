package com.google.sampling.experiential.server.reports;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.PacoModule;

public class ReportRequest {
  private String reportId;
  private PacoModule backendModule;
  private ReportJob reportJob;
  private String who;
  private Map<String, String> requestQueryParamMap = Maps.newHashMap();
  private static final String REPORT_ID = "reportId";
  
  public ReportRequest(HttpServletRequest request, String requestorEmail, String backendModuleStr) {
    // identify report name
    reportId = request.getParameter(REPORT_ID);
    // identify back end module
    backendModule = new PacoModule(backendModuleStr, request.getServerName());
    // identify report job
    reportJob =  ReportLookupTable.getReportBackendName(reportId);       
    // identify who
    who = requestorEmail;
    // populate map
    convertQueryString(request);
    // identify output format
    
  }
  
  private  void convertQueryString(HttpServletRequest req) {
    Enumeration<String> enReq = req.getParameterNames();
    while(enReq.hasMoreElements()) {
      String element = enReq.nextElement().toString();
      requestQueryParamMap.put(element, req.getParameter(element));
    }
  }

  public String getReportId() {
    return reportId;
  }

  public void setReportId(String reportId) {
    this.reportId = reportId;
  }

  public PacoModule getBackendModule() {
    return backendModule;
  }

  public void setBackendModule(PacoModule backendModule) {
    this.backendModule = backendModule;
  }

  public ReportJob getReportJob() {
    return reportJob;
  }

  public void setReportJob(ReportJob reportJob) {
    this.reportJob = reportJob;
  }

  public String getWho() {
    return who;
  }

  public void setWho(String who) {
    this.who = who;
  }

  public Map<String, String> getRequestQueryParamMap() {
    return requestQueryParamMap;
  }

  public void setRequestQueryParamMap(Map<String, String> requestQueryParamMap) {
    this.requestQueryParamMap = requestQueryParamMap;
  }
}
