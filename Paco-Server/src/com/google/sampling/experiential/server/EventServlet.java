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
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

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
      String includePhotosParam = req.getParameter("includePhotos");
      boolean includePhotos = false;
      if (includePhotosParam != null) {
        includePhotos = Boolean.parseBoolean(includePhotosParam);
      }
      boolean cmdline = req.getParameter("cmdline") != null;
      String cursor = req.getParameter("cursor");
      String limitStr = req.getParameter("limit");
      int limit = 0;
      if (!Strings.isNullOrEmpty(limitStr)) {
          try {
            limit = Integer.parseInt(limitStr);
          } catch (NumberFormatException e) {
            e.printStackTrace();
          }
      }
      boolean doJsonOnBackend = req.getParameter("backend") != null;

      if (req.getParameter("mapping") != null) {
        dumpUserIdMapping(req, resp, limit, cursor);
      } else if (req.getParameter("json") != null) {
        if (!doJsonOnBackend) {
          resp.setContentType("application/json;charset=UTF-8");
          dumpEventsJson(resp, req, anon, includePhotos, limit, cursor, cmdline);
        } else {
          dumpEventJsonUsingBackend(resp, req, anon, includePhotos, limit, cursor, cmdline);
        }
      } else if (req.getParameter("photozip") != null) {
        dumpPhotosZip(resp, req, anon, limit, cursor, cmdline);
      } else if (req.getParameter("csv") != null) {
        dumpEventsCSV(resp, req, anon, limit, cursor, cmdline);
      } else if (req.getParameter("csv2") != null) {
        dumpEventsCSVExperimental(resp, req, anon, limit, cursor, cmdline);
      } else if (req.getParameter("json2") != null) {
        dumpEventsJsonExperimental(resp, req, anon, limit, cursor, cmdline);
      } else {
        dumpEventsHtml(resp, req, anon, limit, cursor, cmdline);
      }
    }
  }

  private void dumpEventJsonUsingBackend(HttpServletResponse resp, HttpServletRequest req, boolean anon,
                                         boolean includePhotos, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "json", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }

  }

  // TODO replace this with a call to the joined table to get all the unique users for an experiment.
  private void dumpUserIdMapping(HttpServletRequest req, HttpServletResponse resp, int limit, String cursor) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    EventQueryResultPair eventQueryPair = getEventsWithQuery(req, query, limit, cursor);
    List<Event> events = eventQueryPair.getEvents();
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


  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon, boolean includePhotos, int limit, String cursor, boolean cmdline) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    EventQueryResultPair eventQueryPair = getEventsWithQuery(req, query, limit, cursor);
    List<Event> events = eventQueryPair.getEvents();
    EventRetriever.sortEvents(events);

    String jsonOutput = jsonifyEvents(eventQueryPair, anon, TimeUtil.getTimeZoneForClient(req).getID(), includePhotos);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(EventQueryResultPair eventQueryPair, boolean anon, String timezoneId, boolean includePhotos) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : eventQueryPair.getEvents()) {
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
        final Map<String, String> whatMap = event.getWhatMap();
        List<PhotoBlob> photos = event.getBlobs();
        String[] photoBlobs = null;
        if (includePhotos && photos != null && photos.size() > 0) {

          photoBlobs = new String[photos.size()];

          Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
          for (PhotoBlob photoBlob : photos) {
            photoByNames.put(photoBlob.getName(), photoBlob);
          }
          for(String key : whatMap.keySet()) {
            String value = null;
            if (photoByNames.containsKey(key)) {
              byte[] photoData = photoByNames.get(key).getValue();
              if (photoData != null && photoData.length > 0) {
                String photoString = new String(Base64.encodeBase64(photoData));
                if (!photoString.equals("==")) {
                  value = photoString;
                } else {
                  value = "";
                }
              } else {
                value = "";
              }
              whatMap.put(key, value);
            }
          }
        }

        eventDAOs.add(new EventDAO(userId,
                                   event.getWhen(),
                                   event.getExperimentName(),
                                   event.getLat(), event.getLon(),
                                   event.getAppId(),
                                   event.getPacoVersion(),
                                   whatMap,
                                   event.isShared(),
                                   responseTime,
                                   scheduledTime,
                                   null,
                                   Long.parseLong(event.getExperimentId()),
                                   event.getExperimentVersion(),
                                   event.getTimeZone(),
                                   event.getExperimentGroupName(),
                                   event.getActionTriggerId(),
                                   event.getActionTriggerSpecId(),
                                   event.getActionId()));
      }
      EventDAOQueryResultPair eventDaoQueryResultPair = new EventDAOQueryResultPair(eventDAOs, eventQueryPair.getCursor());
      return mapper.writeValueAsString(eventDaoQueryResultPair);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Error could not retrieve events as json";
  }

  private void dumpEventsCSV(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "csv", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }

  }

  private void dumpEventsCSVExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "csv2", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }

  }

  private void dumpEventsJsonExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "json2", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }

  }




  private void dumpEventsHtml(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "html", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }
  }

  private void dumpPhotosZip(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);

    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "photozip", limit, cursor);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    if (cmdline) {
      resp.getWriter().println(jobId);
    } else {
      resp.sendRedirect("/jobStatus?jobId=" + jobId);
    }
  }



  /**
   * Triggers a backend instance call to start the potentially-long-running job
   *
   * @param anon
   * @param loggedInuser
   * @param timeZoneForClient
   * @param req
   * @param reportFormat
   * @param limit
   * @param cursor
   * @return the jobId to check in on the status of this background job
   * @throws IOException
   */
  private String runReportJob(boolean anon, String loggedInuser, DateTimeZone timeZoneForClient,
                                 HttpServletRequest req, String reportFormat, int limit, String cursor) throws IOException {
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String backendAddress = modulesApi.getVersionHostname("reportworker", modulesApi.getDefaultVersion("reportworker"));
     try {

      BufferedReader reader = null;
      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor, limit);
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. Trying again...");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor, limit);
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
                                       String backendAddress, String reportFormat, String cursor, int limit) throws MalformedURLException, IOException {

    String httpScheme = "https";
    String localAddr = req.getLocalAddr();
    if (localAddr != null && localAddr.matches("127.0.0.1")) {
      httpScheme = "http";
    }
    URL url = new URL(httpScheme + "://" + backendAddress + "/backendReportJobExecutor?q=" +
            req.getParameter("q") +
            "&who="+AuthUtil.getWhoFromLogin().getEmail().toLowerCase() +
            "&anon=" + req.getParameter("anon") +
            "&includePhotos=" +req.getParameter("includePhotos") +
            "&tz=" + timeZoneForClient +
            "&reportFormat=" + reportFormat +
            "&cursor=" + cursor +
            "&limit=" + limit);
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

  private EventQueryResultPair getEventsWithQuery(HttpServletRequest req,
                                         List<com.google.sampling.experiential.server.Query> queries, int limit, String cursor) {
    User whoFromLogin = AuthUtil.getWhoFromLogin();
    return EventRetriever.getInstance().getEventsInBatches(queries, whoFromLogin.getEmail().toLowerCase(), TimeUtil.getTimeZoneForClient(req), limit, cursor);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    setCharacterEncoding(req, resp);
    User who = AuthUtil.getWhoFromLogin();
    if (who == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      if (ServletFileUpload.isMultipartContent(req)) {
        processCsvUpload(req, resp);
      } else {
        processJsonUpload(req, resp);
      }
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
    String results = EventJsonUploadProcessor.create().processJsonEvents(postBodyString, AuthUtil.getEmailOfUser(req, AuthUtil.getWhoFromLogin()), appIdHeader, pacoVersion);
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
