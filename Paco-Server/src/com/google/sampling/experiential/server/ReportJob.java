package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;

public interface ReportJob {
 
  String runReport(ReportRequest req, String jobId) throws FileNotFoundException, SQLException, JSONException, IOException ;
}
