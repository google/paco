package com.google.sampling.experiential.server.migration.jobs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;

import com.google.sampling.experiential.server.CloudSQLReportsDaoImpl;
import com.google.sampling.experiential.server.ReportJob;
import com.google.sampling.experiential.server.ReportRequest;

public class ExperimentCompleteStatusSP  implements ReportJob {
    public static final Logger log = Logger.getLogger(ExperimentCompleteStatusSP.class.getName());
    List<String> participantsLst = null;

    @Override
    public String runReport(ReportRequest req, String jobId) throws FileNotFoundException, SQLException, JSONException,
                                                             IOException {
      CloudSQLReportsDaoImpl  reportsDao = new CloudSQLReportsDaoImpl();
      Long expId = Long.parseLong(req.getRequestQueryParamMap().get("expId"));
      String key = reportsDao.storeCompleteStatusBySPInCloudStorage(jobId, expId, req.getWho());
      return key;
    }
}
