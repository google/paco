package com.google.sampling.experiential.server.reports.jobs;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.json.JSONException;

import com.google.sampling.experiential.server.reports.CloudSQLReportsDaoImpl;
import com.google.sampling.experiential.server.reports.ReportJob;
import com.google.sampling.experiential.server.reports.ReportRequest;

public class ExperimentQuickStatusStoredProcedure implements ReportJob {
    public static final Logger log = Logger.getLogger(ExperimentQuickStatusStoredProcedure.class.getName());

    @Override
    public String runReport(ReportRequest req, String jobId) throws SQLException, JSONException, IOException {
    CloudSQLReportsDaoImpl  reportsDao = new CloudSQLReportsDaoImpl();
    Long expId = Long.parseLong(req.getRequestQueryParamMap().get(EXP_ID));
    String key = reportsDao.storeStatusByStoredProcInCloudStorage(QUICK,jobId, expId, req.getWho());
    return key;
    }
}
