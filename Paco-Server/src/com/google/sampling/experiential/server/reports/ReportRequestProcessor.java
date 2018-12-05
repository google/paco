package com.google.sampling.experiential.server.reports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.server.PacoUrl;

public class ReportRequestProcessor {
  public static final Logger log = Logger.getLogger(ReportRequestProcessor.class.getName());
  private static final String JOB_STATUS_URL_PARTIAL = "/jobStatus?jobId=";
  private static final String HTTP = "http";
  private static final String HTTPS = "https";
  private static final String REPORTS_BACKEND = "reportsBackend";
  
  public String sendReportRequest(ReportRequest req) throws IOException {
    try {
      BufferedReader reader = null;
      try {
        reader = sendRequestToBackend(req);
      } catch (SocketTimeoutException se) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
      }
      if (reader != null) {
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          buf.append(line);
        }
        reader.close();
        return buf.toString();
      }
    } catch (MalformedURLException e) {
      log.severe("MalformedURLException: " + e.getMessage());
    }
    return null;
  }
  
  private BufferedReader sendRequestToBackend(ReportRequest repRequest) throws IOException {
    URL url = null;
    HttpURLConnection connection = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    
    String scheme = HTTPS;
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      scheme = HTTP;
    } 
    String backendAddress = repRequest.getBackendModule().getAddress();
    Map<String, String> qsMap = repRequest.getRequestQueryParamMap();
    qsMap.put(EventServerColumns.WHO, repRequest.getWho());
    PacoUrl.PacoUrlBuilder bldr = new PacoUrl.PacoUrlBuilder(backendAddress).scheme(scheme).qParameters(qsMap).context(REPORTS_BACKEND);
    url = new URL(bldr.build().toString());
    log.info("URL to backend = " + url.toString());
    connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    connection.setReadTimeout(10000);
    connection.setDoInput(true);
    inputStreamReader = new InputStreamReader(connection.getInputStream());
    reader = new BufferedReader(inputStreamReader);
    return reader;
  }
  public static String getUrlwithJobId(String jobId) {
    return JOB_STATUS_URL_PARTIAL + jobId;
  }
}


