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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.users.User;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.Views;

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
  private static final String REPORT_WORKER = "reportworker";
  int BUFFER_SIZE = 2 * 1024 * 1024;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);
      log.info("pacoProtocol is : " + pacoProtocol);
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

//      if (req.getParameter("mapping") != null) {
//        //only plain user id, so there is no need to check paco protocol
//        dumpUserIdMapping(req, resp, limit, cursor);
//      } else
      if (req.getParameter("json") != null) {
        if (!doJsonOnBackend) {
          resp.setContentType("application/json;charset=UTF-8");
          dumpEventsJson(resp, req, anon, includePhotos, limit, cursor, cmdline, pacoProtocol);
        } else {
          dumpEventsJsonExperimental(resp, req, anon, limit, cursor, cmdline, pacoProtocol);
        }
      } else if (req.getParameter("photozip") != null) {
        dumpPhotosZip(resp, req, anon, limit, cursor, cmdline);
      } else if (req.getParameter("csv") != null) {
        dumpEventsCSVExperimental(resp, req, anon, limit, cursor, cmdline, pacoProtocol);
      } else if (req.getParameter("html2") != null) {
        dumpEventsHtmlExperimental(resp, req, anon, limit, cursor, cmdline, pacoProtocol);
      } else {
        dumpEventsHtml(resp, req, anon, limit, cursor, cmdline, pacoProtocol);
      }
    }
  }

  private void dumpEventsJson(HttpServletResponse resp, HttpServletRequest req, boolean anon, boolean includePhotos, int limit, String cursor, boolean cmdline, Float protocolVersion) throws IOException {
    List<com.google.sampling.experiential.server.Query> query = new QueryParser().parse(stripQuotes(HttpUtil.getParam(req, "q")));
    EventQueryResultPair eventQueryPair = getEventsWithQuery(req, query, limit, cursor);
    List<Event> events = eventQueryPair.getEvents();
    EventRetriever.sortEvents(events);

    String jsonOutput = jsonifyEvents(eventQueryPair, anon, TimeUtil.getTimeZoneForClient(req).getID(), includePhotos, protocolVersion);
    resp.getWriter().println(jsonOutput);
  }

  private String jsonifyEvents(EventQueryResultPair eventQueryPair, boolean anon, String timezoneId, boolean includePhotos, Float protocolVersion) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : eventQueryPair.getEvents()) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        DateTime responseDateTime = event.getResponseTimeWithTimeZone(event.getTimeZone());
        DateTime scheduledDateTime = event.getScheduledTimeWithTimeZone(event.getTimeZone());
        final List<WhatDAO> whatMap = EventRetriever.convertToWhatDAOs(event.getWhat());
        
        if (includePhotos) { 
          // legacy GAE DS blob storage
          fillInResponsesWithEncodedBlobData(event, whatMap);
          // new GCS blob storage
          fillInResponsesWithEncodedBlobDataFromGCS(whatMap);
        }
        rewriteBlobUrlsAsFullyQualified(whatMap); // handles any failed includedPhotos as well.

        eventDAOs.add(new EventDAO(userId,
                                   new DateTime(event.getWhen()),
                                   event.getExperimentName(),
                                   event.getLat(), event.getLon(),
                                   event.getAppId(),
                                   event.getPacoVersion(),
                                   whatMap,
                                   event.isShared(),
                                   responseDateTime,
                                   scheduledDateTime,
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
      String finalRes = null;
      log.info("protocol version: "+ protocolVersion);
      if (protocolVersion != null && protocolVersion < 5) {
        finalRes = mapper.writerWithView(Views.V4.class).writeValueAsString(eventDaoQueryResultPair);
      } else {
        mapper.setDateFormat(new ISO8601DateFormat());
        finalRes = mapper.writerWithView(Views.V5.class).writeValueAsString(eventDaoQueryResultPair);
      }
      return finalRes;
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Error could not retrieve events as json";
  }


  public static void rewriteBlobUrlsAsFullyQualified(List<WhatDAO> whatMap) {
    for(WhatDAO currentWhat : whatMap) {
      String currentWhatValue = currentWhat.getValue();
      if (currentWhatValue.startsWith("/eventblobs")) {
        currentWhat.setValue("https://" + HtmlBlobWriter.getHostname() + currentWhatValue);
      }
    }
  }

  private void fillInResponsesWithEncodedBlobData(Event event, List<WhatDAO> whatMap) {
    List<PhotoBlob> photoBlobs = event.getBlobs();
    if (photoBlobs != null && photoBlobs.size() > 0) {        
      Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
      for (PhotoBlob photoBlob : photoBlobs) {
        photoByNames.put(photoBlob.getName(), photoBlob);
      }
      for(WhatDAO currentWhat : whatMap) {
        String value = null;
        if (photoByNames.containsKey(currentWhat.getName())) {
          byte[] photoData = photoByNames.get(currentWhat.getName()).getValue();
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
          currentWhat.setValue(value);
        }
      }
    }
  }
//
//  private void fillInResponsesWithEncodedBlobDataFromGCSX(List<WhatDAO> whatMap) {
//    for(WhatDAO currentWhat : whatMap) {
//      String currentWhatValue = currentWhat.getValue();
//      if (currentWhatValue.startsWith("/eventblobs")) {
//        final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
//                                                                                 .initialRetryDelayMillis(10)
//                                                                                 .retryMaxAttempts(10)
//                                                                                 .totalRetryPeriodMillis(15000)
//                                                                                 .build());
//        
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        String blobKeyStr = getBlobKey(currentWhatValue);
//        if (blobKeyStr != null) {
//          BlobstoreInputStream bsis;
//          try {
////            bsis = new BlobstoreInputStream(new BlobKey(blobKeyStr));
////            copy(bsis, byteArrayOutputStream);
//            byte[] data = BlobstoreServiceFactory.getBlobstoreService().fetchData(new BlobKey(blobKeyStr), 0, Long.MAX_VALUE);
//            //String photoBlobString = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
//            String photoBlobString = new String(Base64.encodeBase64(data));
//            currentWhat.setValue(photoBlobString);
//          } catch (Exception e) {
//            log.log(Level.WARNING, "failed to copy blob from GCS", e);
//          } finally {
//           if (byteArrayOutputStream != null) {
//             try {
//              byteArrayOutputStream.close();
//            } catch (IOException e) {
//              e.printStackTrace();
//            }
//           }
//          }          
//        } else {
//          log.warning("Blob key for writing blob was null: " + currentWhatValue);
//        }
//      }
//    }
//  }
  
  private void fillInResponsesWithEncodedBlobDataFromGCS(List<WhatDAO> whatMap) {
    final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                                                                     .initialRetryDelayMillis(10)
                                                                     .retryMaxAttempts(10)
                                                                     .totalRetryPeriodMillis(15000)
                                                                     .build());
    BlobAclStore bas = BlobAclStore.getInstance();
    
    for(WhatDAO currentWhat : whatMap) {
      String currentWhatValue = currentWhat.getValue();
      if (currentWhatValue.startsWith("/eventblobs")) {
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String blobKey = getBlobKey(currentWhatValue);
        BlobAcl blobAcl = bas.getAcl(blobKey);
        if (blobAcl != null && blobAcl.getBucketName() != null && blobAcl.getObjectName() != null) {
          System.out.println("Starting blob retrieval from GCS. bucket: " + blobAcl.getBucketName() + ", blobname: " + blobAcl.getObjectName());
          GcsFilename gcsFileName = getFileName(blobAcl.getBucketName(), blobAcl.getObjectName());
          GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(gcsFileName, 0, BUFFER_SIZE);
          try {
            copy(Channels.newInputStream(readChannel), byteArrayOutputStream);
            String photoBlobString = new String(Base64.encodeBase64(byteArrayOutputStream.toByteArray()));
            currentWhat.setValue(photoBlobString);
          } catch (IOException e) {
            log.log(Level.WARNING, "failed to copy blob from GCS", e);
          }          
        } else {
          log.warning("Blob key component for writing blob was null: " + currentWhatValue);
        }
      }
    }
  }

  private GcsFilename getFileName(String bucketName, String objectName) {
    return new GcsFilename(bucketName, objectName);
  }
  private String getBlobKey(String value) {
    String[] parts = value.split("&");
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].startsWith("blob-key=")) {
        String keyValue = parts[i].substring(9).trim();
        log.info("blbo key = " + keyValue);
        return keyValue;
      }
    }
    return null;
  }


  /**
   * Transfer the data from the inputStream to the outputStream. Then close both streams.
   */
  private void copy(InputStream input, OutputStream output) throws IOException {
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
    } finally {
      input.close();
      output.close();
    }
  }
  
  private void dumpEventsCSVExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline, Float pacoProtocol ) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "csv2", limit, cursor, pacoProtocol);
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

  private void dumpEventsJsonExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline, Float pacoProtocol) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "json2", limit, cursor, pacoProtocol);
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

  private void dumpEventsHtmlExperimental(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline, Float pacoProtocol) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "html2", limit, cursor, pacoProtocol);
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



  private void dumpEventsHtml(HttpServletResponse resp, HttpServletRequest req, boolean anon, int limit, String cursor, boolean cmdline, Float pacoProtocol) throws IOException {
    String loggedInuser = AuthUtil.getWhoFromLogin().getEmail().toLowerCase();
    if (loggedInuser != null && adminUsers.contains(loggedInuser)) {
      loggedInuser = defaultAdmin; //TODO this is dumb. It should just be the value, loggedInuser.
    }

    DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "html", limit, cursor, pacoProtocol);
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

    String jobId = runReportJob(anon, loggedInuser, timeZoneForClient, req, "photozip", limit, cursor, null);
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
                                 HttpServletRequest req, String reportFormat, int limit, String cursor, Float pacoProtocol) throws IOException {
    try {
      String serverName = req.getServerName();
      log.info("request servername = " + serverName);
      PacoModule backendModule = new PacoModule(REPORT_WORKER, serverName);
      String backendAddress = backendModule.getAddress();
            
      BufferedReader reader = null;

      try {
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor, limit, pacoProtocol);
      } catch (SocketTimeoutException se) {
        log.info("Timed out sending to backend. Trying again...");
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        reader = sendToBackend(timeZoneForClient, req, backendAddress, reportFormat, cursor, limit, pacoProtocol);
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
                                       String backendAddress, String reportFormat, String cursor, int limit, Float pacoProtocol) throws MalformedURLException, IOException {

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
            "&limit=" + limit +
            "&pacoProtocol=" + pacoProtocol);
    log.info("URL to backend = " + url.toString());
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    // set instance follow redirects should be set to false. Only when it is false, GAE will set the header value to X-Appengine-Inbound-Appid
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
    String postBodyString = RequestProcessorUtil.getBody(req);
    if (postBodyString.equals("")) {
      throw new IllegalArgumentException("Empty Post body");
    }

    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    log.info("Paco version = " + pacoVersion);
    String results = EventJsonUploadProcessor.create().processJsonEvents(postBodyString, AuthUtil.getEmailOfUser(req, AuthUtil.getWhoFromLogin()), appIdHeader, pacoVersion);

    if (req.getHeader("pacoProtocol") != null && req.getHeader("pacoProtocol").indexOf("4") == -1) {
      log.severe("oldProtocol " + req.getHeader("pacoProtocol") + " (iOS) results?");
      log.severe(results);
    }

    resp.setContentType("application/json;charset=UTF-8");
    resp.getWriter().write(results);
  }

  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
      throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}
