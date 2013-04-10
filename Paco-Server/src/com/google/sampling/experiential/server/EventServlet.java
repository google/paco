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
import java.net.SocketTimeoutException;
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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
public class EventServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
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
      } else if (req.getParameter("csv") != null) {
        resp.setContentType("text/csv;charset=UTF-8");
        dumpEventsCSV(resp, req, anon);
      } else {
        resp.setContentType("text/html;charset=UTF-8");
        showEvents(req, resp, anon);
      }
    }
  }

  private void dumpUserIdMapping(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(getParam(req, "q")));
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

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(getParam(req, "q")));
    List<Event> events = getEventsWithQuery(req, query, 0, 20000);
    EventRetriever.sortEvents(events);
    String jsonOutput = jsonifyEvents(events, anon);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(List<Event> events, boolean anon) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : events) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        eventDAOs.add(new EventDAO(userId, event.getWhen(), event.getExperimentName(), event.getLat(), event.getLon(),
                                   event.getAppId(), event.getPacoVersion(), event.getWhatMap(), event.isShared(),
                                   event.getResponseTime(), event.getScheduledTime(), null, Long.parseLong(event.getExperimentId()),
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
    String loggedInuser = getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }
    
    DateTimeZone timeZoneForClient = getTimeZoneForClient(req);
    String jobId = runCSVReportJob(anon, loggedInuser, timeZoneForClient, req);
    // Give the backend time to startup and register the job.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
    }
    resp.sendRedirect("/csvJobStatus?jobId=" + jobId);
    
  }

  private String runCSVReportJob(boolean anon, 
                                 String loggedInuser, DateTimeZone timeZoneForClient, HttpServletRequest req) throws IOException {
    // get a backend instance and send it a request. get back the job id
    BackendService backendsApi = BackendServiceFactory.getBackendService();
    String backendAddress = backendsApi.getBackendAddress("reportworker");
    
    try {
      BufferedReader reader = null;
      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress);
      } catch (SocketTimeoutException se) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress);
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

  private BufferedReader sendToBackend(DateTimeZone timeZoneForClient, HttpServletRequest req, String backendAddress)
                                                                                                                     throws MalformedURLException,
                                                                                                                     IOException {
    URL url = new URL("http://" + backendAddress + "/backendReportJobExecutor?q=" + 
            req.getParameter("q") + 
            "&who="+getWhoFromLogin().getEmail().toLowerCase() +
            "&anon=" + req.getParameter("anon") + 
            "&tz="+timeZoneForClient);
    log.info("URL to backend = " + url.toString());
    InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
    BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
  }

  private void showEvents(HttpServletRequest req, HttpServletResponse resp, boolean anon) throws IOException {    
    String q = stripQuotes(getParam(req, "q"));
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(q);
    String offsetStr = getParam(req, "offset");
    int offset = 0;
    int limit = 100;
    if (offsetStr != null && !offsetStr.isEmpty()) {
      offset = Integer.parseInt(offsetStr);
    }
    String limitStr = getParam(req, "limit");
    if (limitStr != null && !limitStr.isEmpty()) {
      limit = Integer.parseInt(limitStr);
    }
    List<Event> events = getEventsWithQuery(req, query, offset, limit);
    if (events.size() > 0) {   
      Experiment experiment = ExperimentRetriever.getInstance().getExperiment(events.get(0).getExperimentId());
      EventRetriever.sortEvents(events);
      printEvents(resp, events, experiment, anon, offset, limit, q);
    }
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
    User whoFromLogin = getWhoFromLogin();
    if (!isDevInstance(req) && whoFromLogin == null) {
      throw new IllegalArgumentException("Must be logged in to retrieve data.");
    }
    String who = null;
    if (whoFromLogin != null) {
      who = whoFromLogin.getEmail().toLowerCase();
    }
    return EventRetriever.getInstance().getEvents(queries, who, getTimeZoneForClient(req), offset, limit);
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

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User who = getWhoFromLogin();
    if (who == null) {
      throw new IllegalArgumentException("Must be logged in!");
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
      new EventCsvUploadProcessor().processCsvUpload(getWhoFromLogin(), fileUploadTool.getItemIterator(req), out);
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
    String results = EventJsonUploadProcessor.create().processJsonEvents(postBodyString, getWhoFromLogin().getEmail().toLowerCase(), appIdHeader);    
    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp) 
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
