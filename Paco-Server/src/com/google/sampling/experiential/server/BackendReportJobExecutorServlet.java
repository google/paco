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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.common.collect.Lists;

/**
 * Servlet that receives request from frontend to start csv report job.
 *
 * Runs on backend.
 *
 * @author Bob Evans
 *
 */
@SuppressWarnings("serial")
public class BackendReportJobExecutorServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(BackendReportJobExecutorServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.info("BackendReportJobExecutor servlet was called");
    log.info(req.getHeader("X-Appengine-Inbound-Appid"));
    String anonStr = req.getParameter("anon");
    boolean anon = false;
    if (anonStr != null) {
      anon = Boolean.parseBoolean(anonStr);
    }
    String reportFormat = req.getParameter("reportFormat");
    String cursor = req.getParameter("cursor");
    if (reportFormat != null && reportFormat.equals("csv")) {
      log.info("Backend generating csv report");
      dumpEventsCSV(resp, req, anon, cursor);
    } else if (reportFormat != null && reportFormat.equals("json")) {
      log.info("Backend generating json report");
      dumpEventsJson(resp, req, anon);
    } else if (reportFormat != null && reportFormat.equals("photozip")) {
        log.info("Backend generating photo zip file");
        dumpPhotoZip(req, resp, anon, cursor);
    } else {
      log.info("Backend generating html report");
      showEvents(req, resp, anon, cursor);
    }
  }

  private void dumpPhotoZip(HttpServletRequest req, HttpServletResponse resp, boolean anon, String cursor) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(getRequestorEmail(req), timeZoneForClient, query, anon, "photozip", cursor, queryParam);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private String getParamForQuery(HttpServletRequest req) {
    return getParam(req, "q");
  }


  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon, String cursor) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(requestorEmail, timeZoneForClient, query, anon, "csv", cursor, queryParam);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private void showEvents(HttpServletRequest req, HttpServletResponse resp, boolean anon, String cursor) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(getRequestorEmail(req), timeZoneForClient, query, anon, "html", cursor, queryParam);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);

  }

  public static DateTimeZone getTimeZoneForClient(HttpServletRequest req) {
    String tzStr = getParam(req, "tz");
    if (tzStr != null && !tzStr.isEmpty()) {
      try {
        DateTimeZone jodaTimeZone = DateTimeZone.forID(tzStr);
        return jodaTimeZone;
      } catch (Exception e) {
        log.warning("Could not get DateTimeZone for string: " + tzStr);
      }
    }
    Locale clientLocale = req.getLocale();
    Calendar calendar = Calendar.getInstance(clientLocale);
    TimeZone clientTimeZone = calendar.getTimeZone();
    DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(clientTimeZone);
    return jodaTimeZone;

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

  private String getRequestorEmail(HttpServletRequest req) {
    String whoParam = getParam(req, "who");
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    String requestorEmail = whoParam.toLowerCase();
    if (requestorEmail != null && adminUsers.contains(requestorEmail)) {
      requestorEmail = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }
    return requestorEmail;
  }

  private String stripQuotes(String parameter) {
    if (parameter == null) {
      return null;
    }
    if (parameter.startsWith("'") || parameter.startsWith("\"")) {
      parameter = parameter.substring(1);
    }
    if (parameter.endsWith("'") || parameter.endsWith("\"")) {
      parameter = parameter.substring(0, parameter.length() - 1);
    }
    return parameter;
  }

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon) {
    throw new RuntimeException("This does not exist on the backend yet!");

  }



}
