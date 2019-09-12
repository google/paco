package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.nio.channels.Channels;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.gcs.GCSFetcher;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class HtmlBlobWriter {

  private static final Logger log = Logger.getLogger(HtmlBlobWriter.class.getName());
  private static DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();


  public HtmlBlobWriter() {
  }

  public String writeNormalExperimentEventsAsHtml(boolean anon, EventQueryResultPair eventQueryResultPair, String jobId, String experimentId, 
                                                  String timeZone, String originalQuery, String requestorEmail, Float pacoProtocol, boolean inlineBlobs, boolean fullBlobAddress)
          throws IOException {
    log.info("writing normal Experiment events as html");

    ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(Long.parseLong(experimentId));
    String eventPage;
    try {
      eventPage = printEvents(eventQueryResultPair, experiment, anon, originalQuery, requestorEmail, pacoProtocol, inlineBlobs, fullBlobAddress);
    } catch (IOException e) {
      log.severe("Could not run printEvents. " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    BlobKey blobKey = writeBlobUsingNewApi(eventQueryResultPair, jobId, timeZone, experiment, eventPage);
    return blobKey.getKeyString();

  }

  private static BlobKey writeBlobUsingNewApi(EventQueryResultPair eventQueryResultPair, String jobId, String timeZone,
                                       ExperimentDAO experiment, String eventPage) throws IOException,
                                                                               FileNotFoundException {
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucketName = System.getProperty("com.pacoapp.reportbucketname");
    String fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
            .mimeType("text/html")
            .acl("project-private")
            .addUserMetadata("jobId", jobId)
            .build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
    writer.println(printHeader(eventQueryResultPair.getEvents().size(),
                               getExperimentTitle(experiment), timeZone));
    writer.println(eventPage);
    writer.flush();

    writeChannel.waitForOutstandingWrites();

    //writeChannel.write(ByteBuffer.wrap("And miles to go before I sleep.".getBytes("UTF8")));

    writeChannel.close();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + fileName);
    return blobKey;
  }

  //TODO: Has been broken by app engine upgrade, will be fixed in a separate branch
  public String writeEndOfDayExperimentEventsAsHtml(boolean anon, String jobId, String experimentId,
                                                    List<EventDAO> events, String timeZoneForClient, Float pacoProtocol) throws IOException {
    log.info("writing End of Day Experiment events as html");
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile file = fileService.createNewBlobFile("text/html;charset=UTF-8", jobId);

    // Open a channel to write to it
    boolean lock = true;
    FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);

    // Different standard Java ways of writing to the channel
    // are possible. Here we use a PrintWriter:
    PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));


    ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(Long.parseLong(experimentId));
    String eventPage = printEventDAOs(events, experiment, anon, pacoProtocol);

    out.println(printHeader(events.size(), getExperimentTitle(experiment), timeZoneForClient));
    out.println(eventPage);
    out.flush();
    out.close();
    writeChannel.closeFinally();

    BlobKey blobKey = fileService.getBlobKey(file);
    return blobKey.getKeyString();

  }


  private static String getExperimentTitle(ExperimentDAO experiment) {
    String experimentTitle = experiment != null ? experiment.getTitle() : null;
    return escapeText(experimentTitle);
  }

  private static String escapeText(String experimentTitle) {
    return StringEscapeUtils.escapeHtml4(experimentTitle);
  }

  private static String printHeader(int eventCount, String experimentTitle, String clientTimeZone) {
    StringBuilder out = new StringBuilder();
    out.append("<html><head><title>Current Results for "
               + experimentTitle
               + "</title>"
               + "<style type=\"text/css\">"
               + "body {font-family: verdana,arial,sans-serif;color:#333333}"
               + "table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}"
               + "table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}"
               + "table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}"
               + "</style>");
   out.append("</head><body>");
    out.append("<h1>" + experimentTitle + " Results</h1>");
    out.append("<div><span style\"font-weight: bold;\">");
    out.append("Report generated at: ");
    out.append(jodaFormatter.print(new DateTime().withZone(DateTimeZone.forID(clientTimeZone))));
    out.append("</span></div>");
    out.append("<div><span style\"font-weight: bold;\">Number of results:</span> <span>" + eventCount
               + "</span></div>");
    return out.toString();

  }

  private String printEvents(EventQueryResultPair eventQueryResultPair, ExperimentDAO experiment,
                             boolean anon, String originalQuery, String whoFromLogin, 
                             Float pacoProtocol, boolean inlineBlobs, boolean fullBlobAddress) throws IOException {
    if (eventQueryResultPair.getEvents().isEmpty()) {
      return "No events in experiment: " + getExperimentTitle(experiment) + ".";
    } else {
      List<String> inputKeys = Lists.newArrayList();

      if (experiment != null) {
        List<Input2> inputs = getAllInputsForExperiment(experiment);
        for (Input2 item : inputs) {
          inputKeys.add(item.getName());
        }
      }

      StringBuilder out = new StringBuilder();
      out.append("<table class=\"gridtable\">");
      out.append("<tr><th>Experiment Name</th><th>Experiment Version</th><th>Scheduled Time</th><th>Response Time</th><th>Who</th>");

      for (String inputName : inputKeys) {
        out.append("<th>");
        out.append(inputName);
        out.append("</th>");
      }
      out.append("<th>Other Responses</th>");
      out.append("</tr>");
      
      

      for (Event event : eventQueryResultPair.getEvents()) {        
        try {
          out.append("<tr>");
          out.append("<td>").append(event.getExperimentName()).append("</td>");
          out.append("<td>").append(event.getExperimentVersion()).append("</td>");
          out.append("<td>").append(event.getScheduledTimeWithTimeZone(event.getTimeZone())).append("</td>");
          out.append("<td>").append(event.getResponseTimeWithTimeZone(event.getTimeZone())).append("</td>");
          String who = event.getWho();
          if (anon) {
            who = Event.getAnonymousId(who);
          }
          out.append("<td>").append(who).append("</td>");

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
          
          for (String key : inputKeys) {
            out.append("<td>");
            out.append(getValueAsDisplayString(event, photoByNames, key, inlineBlobs));
            out.append("</td>");
          }

          List<String> keysCopy = Lists.newArrayList(event.getWhatKeys());
          keysCopy.removeAll(inputKeys);
          for (String extraKey : keysCopy) {
            out.append("<td>");
            out.append(extraKey);
            out.append(" = ");
            out.append(getValueAsDisplayString(event, photoByNames, extraKey, inlineBlobs));
            out.append("</td>");
          }
          out.append("<tr>");
        } catch (Throwable e) {
          log.log(Level.INFO, "Exception in event processing " + e.getMessage(), e);
        }
      }
      out.append("</table>");
      out.append("</body></html>");
      return out.toString();
    }
  }

  private String printEventDAOs(List<EventDAO> events, ExperimentDAO experiment, boolean anon, Float pacoProtocol) throws IOException {
    if (events.isEmpty()) {
      return "No events in experiment: " + getExperimentTitle(experiment) + ".";
    }
    List<String> inputKeys = Lists.newArrayList();
    List<Input2> inputs = getAllInputsForExperiment(experiment);
    for (Input2 item : inputs) {
      inputKeys.add(item.getName());
    }

    StringBuilder out = new StringBuilder();
    out.append("<table class=\"gridtable\">");
    out.append("<tr><th>Experiment Name</th><th>Experiment Version</th><th>Scheduled Time</th><th>Response Time</th><th>Who</th>");

    for (String inputName : inputKeys) {
      out.append("<th>");
      out.append(escapeText(inputName));
      out.append("</th>");
    }
    out.append("<th>Other Responses</th>");
    out.append("</tr>");

    for (EventDAO event : events) {
      out.append("<tr>");
      out.append("<td>").append(escapeText(event.getExperimentName())).append("</td>");
      out.append("<td>").append(event.getExperimentVersion()).append("</td>");
      try {
        TimeUtil.adjustTimeZone(event);
      } catch (ParseException e) {
        log.warning(ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
      }
      out.append("<td>").append(event.getScheduledTime()).append("</td>");
      out.append("<td>").append(event.getResponseTime()).append("</td>");
      String who = event.getWho();
      if (anon) {
        who = Event.getAnonymousId(who);
      }
      out.append("<td>").append(who).append("</td>");

      for (Input2 input : inputs) {
        out.append("<td>");
        out.append(getValueAsDisplayString(event, input));
        out.append("</td>");
      }

      for (WhatDAO currentWhat : event.getWhat()) {
        if (!inputKeys.contains(currentWhat.getName())) {
          out.append("<td>");
          out.append(currentWhat.getName());
          out.append(" = ");
          out.append(getValueAsDisplayString(currentWhat.getValue()));
          out.append("</td>");
        }
      }
      out.append("<tr>");
    }

    out.append("</table></body></html>");
    return out.toString();

  }

  private List<Input2> getAllInputsForExperiment(ExperimentDAO experiment) {
    List<Input2> inputs = experiment.getGroups().get(0).getInputs();
    return inputs;
  }

  private String getValueAsDisplayString(String value) {
    if (value == null) {
      value = "";
    } else if (value.startsWith("/eventblobs?mt=image")) {
      value = "<img height=\"375\" src=\"" + value + "\">";
    } else if (value.startsWith("===")) {
      byte[] photoData = value.getBytes();
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
    return value;
  }


  private String getValueAsDisplayString(EventDAO event, Input2 input) {
    String value = event.getWhatByKey(input.getName());
    if (value == null) {
      value = "";
    } else if (input.getResponseType().equals(Input2.PHOTO)) {
      if (value.startsWith("/eventblobs?mt=image")) {
        value = "<img height=\"375\" src=\"" + value + "\">";
      } else {
        byte[] photoData = value.getBytes();
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
      }
    } else if (value.indexOf(" ") != -1) {
      value = "\"" + StringEscapeUtils.escapeHtml4(value) + "\"";
    } else {
      value = StringEscapeUtils.escapeHtml4(value);
    }
    return value;
  }

  private String getValueAsDisplayString(Event event, Map<String, PhotoBlob> photoByNames, String key, boolean inlineBlobs) {
    String value = event.getWhatByKey(key);
    if (value == null) {
      value = "";
    } else if (value.startsWith("/eventblobs?mt=image")) {
      if (inlineBlobs) {
        // new GCS blob storage
        String photoData = GCSFetcher.fillInResponseForKeyWithEncodedBlobDataFromGCS(value);
        if (photoData != null && photoData.length() > 0) {
            value = "<img height=\"375\" src=\"data:image/jpg;base64," + photoData + "\">";
        } else {
          value = "";
        }
      } else {
        value = "<img height=\"375\" src=\"https://" + getHostname() + value + "\">";
      }
    } else if (photoByNames.containsKey(key)) { // The old way of doing it
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
    return value;
  }

  public static String getHostname() {
    ModulesService modulesApi = ModulesServiceFactory.getModulesService();
    String currentVersion = modulesApi.getCurrentVersion();
    if (currentVersion.equals(modulesApi.getDefaultVersion("default"))) {
      return "www.pacoapp.com";
    } else {
      return currentVersion + "-dot-" + "default" + "-dot-" + "quantifiedself.appspot.com";
    }
  }
}
