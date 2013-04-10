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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Servlet that answers queries for Events.
 * 
 * @author Bob Evans
 * 
 */
public class CSVJobStatusServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(CSVJobStatusServlet.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);
  private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    } else {
      String location = getParam(req, "location");
      String jobId = getParam(req, "jobId");
      String who = getWhoFromLogin().getEmail().toLowerCase();
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
          writeJobStatus(resp, jobReport, jobId, who);
        } else {
          resp.getWriter().println("Unknown job ID: " + jobId + ". The report generator may not have started the job yet. Try Refreshing the page once.");
        }
      } else {
        resp.getWriter().println("Must supply a job ID: " + jobId + "");
      }
    }
  }

  private void writeJobStatus(HttpServletResponse resp, ReportJobStatus jobReport, String jobId, String who) throws IOException {
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter printWriter = resp.getWriter();
        
    StringBuilder out = new StringBuilder();
    out.append("<html><head><title>Current Status of " + jobReport.getId() + "</title>" +
        "<style type=\"text/css\">"+
            "body {font-family: verdana,arial,sans-serif;color:#333333}" +
          "table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}" +
          "table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}" +
          "table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}" +
          "</style>" +
               "</head><body>");
    out.append("<h2>Current Status for Job: ");
    out.append(jobReport.getId());
    out.append("</h2><div><p>Refresh to update status</p></div><div><table class=gridtable>");
    out.append("<th>Requestor</th>");
    out.append("<th>Status</th>");
    out.append("<th>Started</th>");
    out.append("<th>Ended</th>");
    out.append("<th>Output File Location</th>");
    out.append("<th>Error</th>");
    out.append("<tr>");
    
    out.append("<td>").append(jobReport.getRequestor()).append("</td>");
    out.append("<td>").append(getNameForStatus(jobReport)).append("</td>");
    out.append("<td>").append(jobReport.getStartTime()).append("</td>");
    out.append("<td>").append(jobReport.getEndTime()).append("</td>");
    out.append("<td>").append(createLinkForLocation(jobReport, jobId, who)).append("</td>");
    out.append("<td>").append(jobReport.getErrorMessage()).append("</td>");
    out.append("</tr></table></div></body></html>");
    
    printWriter.println(out.toString());
  }

  private String createLinkForLocation(ReportJobStatus jobReport, String jobId, String who) {
    String location = jobReport.getLocation();
    if (Strings.isNullOrEmpty(location)) {
      return "";
    }
    return "<a href=\"/csvJobStatus?who=" + who + "&jobId=" + jobId + "&location=" + location + "\">" + location + "</a>";
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

  private boolean isDevInstance(HttpServletRequest req) {
    return ExperimentServlet.isDevInstance(req);
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
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

  private void printEvents(HttpServletResponse resp, List<Event> events, Experiment experiment, boolean anon, int offset, int limit, String q) throws IOException {
    long t1 = System.currentTimeMillis();
    long eventTime = 0;
    long whatTime = 0;
    if (events.isEmpty()) {
      resp.getWriter().println("Nothing to see here.");
    } else {
      StringBuilder out = new StringBuilder();
      out.append("<html><head><title>Current Results for " + experiment.getTitle() + "</title>" +
          "<style type=\"text/css\">"+
              "body {font-family: verdana,arial,sans-serif;color:#333333}" +
            "table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}" +
            "table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}" +
            "table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}" +
            "</style>" +
                 "</head><body>");
      out.append("<h1>" + experiment.getTitle() + " Results</h1>");
      out.append("<div><span style\"font-weight: bold;\">");
      //out.append("<a href=\"/events?q='" + q + "'&offset=" + ((offset == 0) ? offset : offset - limit) + "&limit=" + limit +"\"> < </a>");  
      out.append("Showing results: </span> <span>" + (offset + 1) +" - " + (offset + events.size()));
      //out.append("<a href=\"/events?q='" + q + "'&offset=" + (offset + limit) + "&limit=" + limit +"\"> > </a>");
      out.append("</span></div>" );
      out.append("<div><span style\"font-weight: bold;\">Number of results:</span> <span>" + events.size() +"</span></div>" );
      out.append("<table class=\"gridtable\">");
      out.append("<tr><th>Experiment Name</th><th>Experiment Version</th><th>Scheduled Time</th><th>Response Time</th><th>Who</th><th>Responses</th></tr>");
      for (Event event : events) {
        long e1 = System.currentTimeMillis();
        out.append("<tr>");
        
        out.append("<td>").append(event.getExperimentName()).append("</td>");
        
        out.append("<td>").append(event.getExperimentVersion()).append("</td>");
        
        out.append("<td>").append(getTimeString(event, event.getScheduledTime())).append("</td>");
        
        out.append("<td>").append(getTimeString(event, event.getResponseTime())).append("</td>");
        
        String who = event.getWho();
        if (anon) {
          who = Event.getAnonymousId(who);
        }
        out.append("<td>").append(who).append("</td>");
        eventTime += System.currentTimeMillis() - e1;
        long what1 = System.currentTimeMillis();
        // we want to render photos as photos not as strings.
        // It would be better to do this by getting the experiment for
        // the event and going through the inputs.
        // That was not done because there may be multiple experiments
        // in the data returned for this interface and
        // that is work that is otherwise necessary for now. Go
        // pretotyping!
        // TODO clean all the accesses of what could be tainted data.
        List<PhotoBlob> photos = event.getBlobs();
        Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
        for (PhotoBlob photoBlob : photos) {
          photoByNames.put(photoBlob.getName(), photoBlob);
        }
        Map<String, String> whatMap = event.getWhatMap();
        Set<String> keys = whatMap.keySet();
        if (keys != null) {
          ArrayList<String> keysAsList = Lists.newArrayList(keys);
          Collections.sort(keysAsList);
          Collections.reverse(keysAsList);
          for (String key : keysAsList) {
            String value = whatMap.get(key);
            if (value == null) {
              value = "";
            } else if (photoByNames.containsKey(key)) {
              byte[] photoData = photoByNames.get(key).getValue();
              if (photoData != null && photoData.length > 0) {
                String photoString = new String(Base64.encodeBase64(photoData));
                if (!photoString.equals("==")) {
                  value = "<img height=\"375\" src=\"data:image/jpg;base64," + photoString + "\">";
                } else {
                  value = "";
                }
              } else {
                value = "";
              }
            } else if (value.indexOf(" ") != -1) {
              value = "\"" + StringEscapeUtils.escapeHtml4(value) + "\"";
            } else {
              value = StringEscapeUtils.escapeHtml4(value);
            }
            out.append("<td>");
            out.append(key).append(" = ").append(value);
            out.append("</td>");
          }
        }
        whatTime += System.currentTimeMillis() - what1;
        out.append("<tr>");
      }
      long t2 = System.currentTimeMillis();
      log.info("EventServlet printEvents total: " + (t2 - t1));
      log.info("Event time: " + eventTime);
      log.info("what time: " + whatTime);
      out.append("</table></body></html>");
      resp.getWriter().println(out.toString());
    }
  }

  private String getTimeString(Event event, Date time) {
    String scheduledTimeString = "";
    if (time != null) {
      scheduledTimeString = jodaFormatter.print(new DateTime(time));
    }
    return scheduledTimeString;
  }


  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp) 
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
