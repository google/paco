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

import com.google.common.base.Strings;
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
    Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);
    String anonStr = req.getParameter("anon");
    boolean anon = false;
    if (anonStr != null) {
      anon = Boolean.parseBoolean(anonStr);
    }
    
    boolean fullBlobAddress = false;
    String fullBlobAddressParam = req.getParameter("fullBlobAddress");
    if (!Strings.isNullOrEmpty(fullBlobAddressParam)) {
      fullBlobAddress = Boolean.parseBoolean(fullBlobAddressParam);
    }

    String limitStr = req.getParameter("limit");
    int limit = 0;
    if (!Strings.isNullOrEmpty(limitStr)) {
      try {
        limit = Integer.parseInt(limitStr);
      } catch (NumberFormatException e) {
      }
    }

    String reportFormat = req.getParameter("reportFormat");
    String cursor = req.getParameter("cursor");
    boolean includePhotos = getParam(req, "includePhotos") != null;

    if (reportFormat != null && reportFormat.equals("csv2")) {
      log.info("Backend generating csv experimental report");
      dumpEventsCSVExperimental(resp, req, anon, pacoProtocol, /*fullBlobAddress*/true, includePhotos);
    } else if (reportFormat != null && reportFormat.equals("csv")) {
      log.info("Backend generating csv report");
      dumpEventsCSV(resp, req, anon, cursor, limit, pacoProtocol, /*fullBlobAddress*/true, includePhotos);
    } else if (reportFormat != null && reportFormat.equals("json2")) {
      log.info("Backend generating json report");
      dumpEventsJsonExperimental(resp, req, anon, includePhotos, pacoProtocol, /*fullBlobAddress*/true);
    } else if (reportFormat != null && reportFormat.equals("json")) {
      log.info("Backend generating json report");
      dumpEventsJson(resp, req, anon, includePhotos, limit, cursor, pacoProtocol, /*fullBlobAddress*/true);
    } else if (reportFormat != null && reportFormat.equals("photozip")) {
      log.info("Backend generating photo zip file");
      dumpPhotoZip(req, resp, anon, cursor, limit);
    } else if (reportFormat != null && reportFormat.equals("stats")) {
      runStats(req, resp, limit);
    } else if (reportFormat != null && reportFormat.equals("html2")) {
      log.info("Backend generating html2 'experimental' report");
      dumpEventsHtmlExperimental(resp, req, anon, pacoProtocol, /*fullBlobAddress*/true);
    } else {
      log.info("Backend generating html report");
      showEvents(req, resp, anon, cursor, limit, pacoProtocol, fullBlobAddress);
    }
  }

  private void runStats(HttpServletRequest req, HttpServletResponse resp, int limit) throws IOException {
      String requestorEmail = "cron"; // bypass the admins check since it can only be launched by cron or an admin
      DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
      //paco protocol is immaterial, so passing null
      String jobId = ReportJobExecutor.getInstance().runReportJob(requestorEmail, timeZoneForClient, null, false, "stats", null, limit, null, false, null, false);
      resp.setContentType("text/plain;charset=UTF-8");
      resp.getWriter().println(jobId);
  }

  private void dumpPhotoZip(HttpServletRequest req, HttpServletResponse resp, boolean anon, String cursor, int limit) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    //paco protocol is immaterial, so passing null
    String jobId = ReportJobExecutor.getInstance().runReportJob(getRequestorEmail(req), timeZoneForClient, query, anon, "photozip", queryParam, limit, cursor, false, null, true);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private String getParamForQuery(HttpServletRequest req) {
    return getParam(req, "q");
  }


  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon, String cursor, int limit, Float pacoProtocol, boolean fullBlobAddress, boolean includePhotos) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(requestorEmail, timeZoneForClient, query, anon, "csv", queryParam, limit, cursor, includePhotos, pacoProtocol, fullBlobAddress);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }


  private void dumpEventsCSVExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, Float pacoProtocol, boolean fullBlobAddress, boolean includePhotos) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJobExperimental(requestorEmail, timeZoneForClient, query, anon, "csv2", queryParam, includePhotos, pacoProtocol, fullBlobAddress);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private void dumpEventsJsonExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, boolean includePhotos, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJobExperimental(requestorEmail, timeZoneForClient, query, anon, "json2", queryParam, includePhotos, pacoProtocol, fullBlobAddress);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private void dumpEventsHtmlExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJobExperimental(requestorEmail, timeZoneForClient, query, anon, "html2", queryParam, false, pacoProtocol, fullBlobAddress);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }

  private void showEvents(HttpServletRequest req, HttpServletResponse resp, boolean anon, String cursor, int limit, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(getRequestorEmail(req), timeZoneForClient, query, anon, "html", queryParam, limit, cursor, true, pacoProtocol, fullBlobAddress);
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


  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon, boolean includePhotos, int limit, String cursor, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    String queryParam = getParamForQuery(req);
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(queryParam));
    String requestorEmail = getRequestorEmail(req);
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(requestorEmail, timeZoneForClient, query, anon, "json", queryParam, limit, cursor, includePhotos, pacoProtocol, fullBlobAddress);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
  }



}
