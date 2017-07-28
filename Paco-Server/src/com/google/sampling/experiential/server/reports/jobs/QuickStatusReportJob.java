package com.google.sampling.experiential.server.reports.jobs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;

import com.google.sampling.experiential.server.reports.CloudSQLReportsDaoImpl;
import com.google.sampling.experiential.server.reports.ReportJob;
import com.google.sampling.experiential.server.reports.ReportRequest;

public class QuickStatusReportJob implements ReportJob {

  @Override
  public String runReport(ReportRequest req, String jobId) throws FileNotFoundException, SQLException, JSONException, IOException {
    CloudSQLReportsDaoImpl  reportsDao = new CloudSQLReportsDaoImpl();
    Long expId = Long.parseLong(req.getRequestQueryParamMap().get(EXP_ID));
    String key = reportsDao.storeQuickStatusInCloudStorage(jobId, expId, req.getWho());
    return key;
  }
}
