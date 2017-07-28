package com.google.sampling.experiential.server.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;

public interface ReportJob {
  String EXP_ID = "expId";
  String WHO = "who";
  String COMPLETE = "complete";
  String QUICK = "quick";
  String runReport(ReportRequest req, String jobId) throws FileNotFoundException, SQLException, JSONException, IOException ;
}
