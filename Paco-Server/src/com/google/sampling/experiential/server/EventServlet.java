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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;

/**
 * Servlet that answers queries for Events.
 *
 * @author Bob Evans
 *
 */
public class EventServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(EventServlet.class.getName());
  private String defaultAdmin = "bobevans@google.com";
  private List<String> adminUsers = Lists.newArrayList(defaultAdmin);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String anonStr = req.getParameter("anon");
      boolean anon = false;
      if (anonStr != null) {
        anon = Boolean.parseBoolean(anonStr);
      }
      if (req.getParameter("mapping") != null) {
        dumpUserIdMapping(req, resp);
      } else if (req.getParameter("json") != null) {
        resp.setContentType("application/json;charset=UTF-8");
        dumpEventsJson(resp, req, anon);
      } else if (req.getParameter("photozip") != null) {
        dumpPhotosZip(resp, req, anon);
      } else if (req.getParameter("csv") != null) {
        dumpEventsCSV(resp, req, anon);
      } else {
        dumpEventsHtml(resp, req, anon);
      }
    }
  }

  private void dumpUserIdMapping(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    List<Event> events = getEventsWithQuery(req, query, 0, 20000);
    EventRetriever.sortEvents(events);
    Set<String> whos = new HashSet<String>();
    for (Event event : events) {
      whos.add(event.getWho());
    }
    StringBuilder mappingOutput = new StringBuilder();
    for (String who : whos) {
      mappingOutput.append(who);
      mappingOutput.append(",");
      mappingOutput.append(Event.getAnonymousId(who));
      mappingOutput.append("\n");
    }
    resp.setContentType("text/csv;charset=UTF-8");
    resp.getWriter().println(mappingOutput.toString());
  }

  private boolean isDevInstance(HttpServletRequest req) {
    return ExperimentServlet.isDevInstance(req);
  }

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    List<Event> events = getEventsWithQuery(req, query, 0, 20000);
    EventRetriever.sortEvents(events);
    String jsonOutput = jsonifyEvents(events, anon, TimeUtil.getTimeZoneForClient(req).getID());
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(List<Event> events, boolean anon, String timezoneId) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : events) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        DateTime responseDateTime = event.getResponseTimeWithTimeZone(timezoneId);
        Date responseTime = null;
        if (responseDateTime != null) {
          responseTime = responseDateTime.toGregorianCalendar().getTime();
        }
        DateTime scheduledDateTime = event.getScheduledTimeWithTimeZone(timezoneId);
        Date scheduledTime = null;
        if (scheduledDateTime != null) {
          scheduledTime = scheduledDateTime.toDate();
        }

        eventDAOs.add(new EventDAO(userId, event.getWhen(), event.getExperimentName(), event.getLat(), event.getLon(),
                                   event.getAppId(), event.getPacoVersion(), event.getWhatMap(), event.isShared(),
                                   responseTime,
                                   scheduledTime,
                                   null, Long.parseLong(event.getExperimentId()),
                                   event.getExperimentVersion(), event.getTimeZone()));
      }
      return mapper.writeValueAsString(eventDAOs);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Error could not retrieve events as json";
  }

  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "csv", req.getParameter("cursor"));
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    resp.sendRedirect("/jobStatus?jobId=" + jobId);

  }


  private void dumpEventsHtml(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "html", req.getParameter("cursor"));
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    resp.sendRedirect("/jobStatus?jobId=" + jobId);
  }

  private void dumpPhotosZip(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "photozip", req.getParameter("cursor"));
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    resp.sendRedirect("/jobStatus?jobId=" + jobId);
  }



  /**
   * Triggers a backend instance call to start the potentially-long-running job
   *
   * @param anon
   * @param loggedInuser
   * @param timeZoneForClient
   * @param req
   * @param reportFormat
   * @param cursor
   * @return the jobId to check in on the status of this background job
   * @throws IOException
   */
  private String runReportJob(boolean anon, String loggedInuser, DateTimeZone timeZoneForClient,
                                 HttpServletRequest req, String reportFormat, String cursor) throws IOException {
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
     try {

      BufferedReader reader = null;
      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor);
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. Trying again...");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor);
      }
      if (reader != null) {
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          buf.append(line);
        }
        reader.close();
        return buf.toString();
      }
    } catch (MalformedURLException e) {
      log.severe("MalformedURLException: " + e.getMessage());
    }
    return null;
  }

  private BufferedReader sendToBackend(DateTimeZone timeZoneForClient, HttpServletRequest req,
                                       String backendAddress, String reportFormat, String cursor) throws MalformedURLException, IOException {
    String httpScheme = "https";
    String localAddr = req.getLocalAddr();
    if (localAddr != null && localAddr.matches("127.0.0.1")) {
      httpScheme = "http";
    }
    URL url = new URL(httpScheme + "://" + backendAddress + "/backendReportJobExecutor?q=" +
            req.getParameter("q") +
            "&who="+AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
            "&anon=" + req.getParameter("anon") +
            "&tz=" + timeZoneForClient +
            "&reportFormat=" + reportFormat +
            "&cursor=" + cursor);
    log.info("URL to backend = " + url.toString());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(false);
    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
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

  private List<Event> getEventsWithQuery(HttpServletRequest req,
                                         List<com.google.sampling.experiential.server.Query> queries, int offset, int limit) {
    User whoFromLogin = AuthUtil.getWhoFromLogin();
    return EventRetriever.getInstance().getEvents(queries, whoFromLogin.getEmail().toLowerCase(), TimeUtil.getTimeZoneForClient(req), offset, limit);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    if (who == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    }

    // TODO(bobevans): Add security check, and length check for DoS
    if (ServletFileUpload.isMultipartContent(req)) {
      processCsvUpload(req, resp);
    } else {
      processJsonUpload(req, resp);
    }
  }

  private void processCsvUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ServletFileUpload fileUploadTool = new ServletFileUpload();
    fileUploadTool.setSizeMax(50000);
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter out = resp.getWriter(); // TODO move all req/resp writing to here.
    try {
      new EventCsvUploadProcessor().processCsvUpload(AuthUtil.getWhoFromLogin(), fileUploadTool.getItemIterator(req), out);
    } catch (FileUploadException e) {
        log.severe("FileUploadException: " + e.getMessage());
        out.println("Error in receiving file.");
    }
  }
  private void processJsonUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String postBodyString;
    try {
      postBodyString = org.apache.commons.io.IOUtils.toString(req.getInputStream(), "UTF-8");
    } catch (IOException e) {
      log.info("IO Exception reading post data stream: " + e.getMessage());
      throw e;
    }
    if (postBodyString.equals("")) {
      throw new IllegalArgumentException("Empty Post body");
    }

    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    log.info("Paco version = " + pacoVersion);
    String results = EventJsonUploadProcessor.create().processJsonEvents(postBodyString, AuthUtil.getWhoFromLogin().getEmail().toLowerCase(), appIdHeader, pacoVersion);
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
