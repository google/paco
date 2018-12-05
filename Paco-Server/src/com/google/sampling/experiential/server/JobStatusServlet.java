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
package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class JobStatusServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(JobStatusServlet.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();



  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      boolean cmdline = getParam(req, "cmdline") != null;
      String location = getParam(req, "location");
      String jobId = getParam(req, "jobId");
      String who = user.getEmail().toLowerCase();
      if (!Strings.isNullOrEmpty(jobId) && !Strings.isNullOrEmpty(location)) {
        ReportJobStatus jobReport = getJobReport(who, jobId);
        if (jobReport != null && jobReport.getRequestor().equals(who)) {
          blobstoreService.serve(new BlobKey(location), resp);
        } else {
          resp.getWriter().println("Unknown job ID: " + jobId +".");
        }
      } else if (!Strings.isNullOrEmpty(jobId)) {
        ReportJobStatus jobReport = getJobReport(who, jobId);
        if (jobReport != null && jobReport.getRequestor().equals(who)) {
          if (cmdline) {
            if (!Strings.isNullOrEmpty(jobReport.getLocation())) {
              blobstoreService.serve(new BlobKey(jobReport.getLocation()), resp);
            } else {
              resp.getWriter().println("pending");
            }
          } else {
//            if (!Strings.isNullOrEmpty(jobReport.getLocation())) {
//              resp.sendRedirect(getReportLink(jobId, who, jobReport.getLocation()));
//            } else {
              writeJobStatus(resp, jobReport, jobId, who);
//            }
          }
        } else {
          resp.getWriter().println("Unknown job ID: " + jobId + ". The report generator may not have started the job yet. Try Refreshing the page once.");
        }
      } else {
        resp.getWriter().println("Must supply a report job ID to track status of report generation: " + jobId + "");
      }
    }
  }

  private void writeJobStatus(HttpServletResponse resp, ReportJobStatus jobReport, String jobId, String who) throws IOException {
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter printWriter = resp.getWriter();
    boolean finished = !Strings.isNullOrEmpty(jobReport.getErrorMessage())
                       || !Strings.isNullOrEmpty(jobReport.getLocation());

    StringBuilder out = new StringBuilder();
    out.append("<html><head>");
    if (!finished) {
      out.append("<meta http-equiv=\"refresh\" content=\"5\">");
    }
    out.append("<title>Current Status of Report Generation for job: " + jobReport.getId() + "</title>");
    out.append("<style type=\"text/css\">");
    out.append("body {font-family: verdana,arial,sans-serif;color:#333333}");
    out.append("table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}");
    out.append("table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}");
    out.append("table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}");
    out.append("</style></head><body>");
    out.append("<h1>Hello, " + jobReport.getRequestor() + ".<br>Your report is being generated</h1>");
    out.append("<!-- Report Job ID:  ");
    out.append(jobReport.getId());
    out.append(" -->");

    out.append("<div><p>" + "The page will refresh every 5 seconds until your job is finished. Then it will download your report automatically." + "</p></div>");
    out.append("<div><table class=gridtable>");

    out.append("<tr><th>" + "Status: " + "</th>");
    out.append("<td>").append(getNameForStatus(jobReport)).append("</td></tr>");

    out.append("<tr><th>" + "Started at: " + "</th>");
    out.append("<td>").append(jobReport.getStartTime()).append("</td></tr>");

    String endTime = jobReport.getEndTime();
    if (!Strings.isNullOrEmpty(endTime)) {
      out.append("<tr><th>" + "Ended: " + "</th>");
      out.append("<td>").append(endTime).append("</td></tr>");
    }

    String errorMessage = jobReport.getErrorMessage();
    out.append("<tr>");
    if (!Strings.isNullOrEmpty(errorMessage)) {
      out.append("<th>" + "Error" + "</th>");
      out.append("<td>").append(errorMessage).append("</td>");
    } else if (!Strings.isNullOrEmpty(jobReport.getLocation())) {
      out.append("<th>" + "Report: " + "</th>");
      out.append("<td>").append(createLinkForLocation(jobReport, jobId, who)).append("</td>");
    }
    out.append("</tr></table></div></body></html>");

    printWriter.println(out.toString());
  }

  private String createLinkForLocation(ReportJobStatus jobReport, String jobId, String who) {
    String location = jobReport.getLocation();
    if (Strings.isNullOrEmpty(location)) {
      return "";
    }
    return "<a href=\"" + getReportLink(jobId, who, location) + "\">Your Report</a>";
  }

  private String getReportLink(String jobId, String who, String location) {
    String actualLink = "/jobStatus?who=" + who + "&jobId=" + jobId + "&location=" + location;
    return actualLink;
  }

  private String getNameForStatus(ReportJobStatus jobReport) {
    switch(jobReport.getStatus()) {
    case ReportJobStatusManager.PENDING:
       return "Pending";
    case ReportJobStatusManager.COMPLETE:
      return "Complete";
    case ReportJobStatusManager.FAILED:
      return "Failed";
      default:
        return "Unknown";
    }
  }

  private ReportJobStatus getJobReport(String requestorEmail, String jobId) {
    ReportJobStatusManager mgr = new ReportJobStatusManager();
    return mgr.isItDone(requestorEmail, jobId);
  }

  public static DateTimeZone getTimeZoneForClient(HttpServletRequest req) {
    String tzStr = getParam(req, "tz");
    if (tzStr != null && !tzStr.isEmpty()) {
      DateTimeZone jodaTimeZone = DateTimeZone.forID(tzStr);
      return jodaTimeZone;
    } else {
      Locale clientLocale = req.getLocale();
      Calendar calendar = Calendar.getInstance(clientLocale);
      TimeZone clientTimeZone = calendar.getTimeZone();
      DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(clientTimeZone);
      return jodaTimeZone;
    }
  }

  private static String getParam(HttpServletRequest req, String paramName) {
    try {
      String parameter = req.getParameter(paramName);
      if (parameter == null || parameter.isEmpty()) {
        return null;
      }
      return URLDecoder.decode(parameter, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new IllegalArgumentException("Unspported encoding");
    }
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}
