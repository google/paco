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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Servlet that receives request from frontend to start csv report job.
 * 
 * Runs on backend.
 * 
 * @author Bob Evans
 * 
 */
public class BackendReportJobExecutorServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(BackendReportJobExecutorServlet.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.info("BackendReportJobExecutor servlet was called");
    String anonStr = req.getParameter("anon");
    boolean anon = false;
    if (anonStr != null) {
      anon = Boolean.parseBoolean(anonStr);
    }
    dumpEventsCSV(resp, req, anon);    
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

  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(getParam(req, "q")));

    String whoParam = getParam(req, "who");
    if (whoParam == null) {
      throw new IllegalArgumentException("Must pass the who param");
    }
    String requestorEmail = whoParam.toLowerCase();
    if (requestorEmail != null && adminUsers.contains(requestorEmail)) {
      requestorEmail = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }
    
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = ReportJobExecutor.getInstance().runReportJob(requestorEmail, timeZoneForClient, query, anon);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.getWriter().println(jobId);
    
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
