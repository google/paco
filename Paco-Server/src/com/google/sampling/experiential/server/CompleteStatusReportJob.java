package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;

public class CompleteStatusReportJob implements ReportJob {

  @Override
  public String runReport(ReportRequest req, String jobId) throws FileNotFoundException, SQLException, JSONException, IOException {
    CloudSQLReportsDaoImpl  reportsDao = new CloudSQLReportsDaoImpl();
    Long expId = Long.parseLong(req.getRequestQueryParamMap().get("expId"));
    String key = reportsDao.storeCompleteStatusInCloudStorage(jobId, expId, req.getWho());
    return key;
  }
}
