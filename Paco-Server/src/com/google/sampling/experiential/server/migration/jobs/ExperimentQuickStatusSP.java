package com.google.sampling.experiential.server.migration.jobs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.JSONException;

import com.google.sampling.experiential.server.CloudSQLReportsDaoImpl;
import com.google.sampling.experiential.server.ReportJob;
import com.google.sampling.experiential.server.ReportRequest;

public class ExperimentQuickStatusSP implements ReportJob {
    public static final Logger log = Logger.getLogger(ExperimentQuickStatusSP.class.getName());

    @Override
    public String runReport(ReportRequest req, String jobId) throws SQLException, JSONException, IOException {
    CloudSQLReportsDaoImpl  reportsDao = new CloudSQLReportsDaoImpl();
    Long expId = Long.parseLong(req.getRequestQueryParamMap().get("expId"));
    String key = reportsDao.storeQuickStatusBySPInCloudStorage(jobId, expId, req.getWho());
    return key;
    }
}
