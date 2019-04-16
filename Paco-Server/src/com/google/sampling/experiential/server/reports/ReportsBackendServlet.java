/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance  with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.google.sampling.experiential.server.reports;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;

import com.google.appengine.api.ThreadManager;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.server.HttpUtil;
import com.google.sampling.experiential.server.ReportJobStatusManager;


/**
 * Servlet that handles report tasks for data
 *
 */
@SuppressWarnings("serial")
public class ReportsBackendServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(ReportsBackendServlet.class.getName());
  private static final String REPORT_WORKER = "reportworker";
  
  @Override
  protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String requestorEmail = getRequestorEmail(req);
    final ReportRequest reportRequestObj = new ReportRequest(req, requestorEmail, REPORT_WORKER);
    final String jobId = getJobId(reportRequestObj);
    final ReportJobStatusManager statusMgr = new ReportJobStatusManager();
    statusMgr.startReport(requestorEmail, jobId);

    final ClassLoader cl = getClass().getClassLoader();
    final Thread thread2 = ThreadManager.createBackgroundThread(new Runnable() {
      @Override
      public void run() {

        log.info("ReportsBackend running");
        Thread.currentThread().setContextClassLoader(cl);
        try {
          String key = runReport(reportRequestObj, jobId);
          if ( key != null) {
            statusMgr.completeReport(requestorEmail, jobId, key);
          } else {
            statusMgr.failReport(requestorEmail, jobId, "Check server logs for stacktrace");
          }
        } catch (Throwable e) {
          final String fullStack = getStackTraceAsString(e);
          final String string = fullStack.length()>700 ? fullStack.substring(0, 700): fullStack;
          statusMgr.failReport(requestorEmail, jobId, e.getClass() + "." + e.getMessage() +"\n" + string);
          log.severe("Could not run migration job: " + e.getMessage());
          log.severe("stacktrace: " + fullStack);
        }
      }
    });
    thread2.start();
    log.info("Leaving reports backend");
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }


  private String runReport(ReportRequest repRequest, String jobId) throws FileNotFoundException, SQLException, JSONException, IOException {
    ReportJob job = ReportLookupTable.getReportBackendName(repRequest.getReportId());
    if (job != null) {
      return job.runReport(repRequest, jobId);
    }
    return null;
  }

  private String getRequestorEmail(HttpServletRequest req) {
    String whoParam = HttpUtil.getParam(req, EventServerColumns.WHO);
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    return whoParam.toLowerCase();
  }
  
  public String getJobId (ReportRequest repRequest) {
    final String jobId = repRequest.getReportId() + "_" +
            DigestUtils.md5Hex(repRequest.getWho() +
            Long.toString(System.currentTimeMillis()));
    log.info("In report backend for job: " + jobId);
    return jobId;
  }


  public String getStackTraceAsString(Throwable e) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(out);
    e.printStackTrace(pw);
    final String string = out.toString();
    return string;
  }
}
