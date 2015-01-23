package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.Date;
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
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.paco.shared.model.InputDAO;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class HtmlBlobWriter {

  private static final Logger log = Logger.getLogger(HtmlBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();


  public HtmlBlobWriter() {
  }

  public String writeNormalExperimentEventsAsHtml(boolean anon, EventQueryResultPair eventQueryResultPair, String jobId, String experimentId, String timeZone, String originalQuery, String requestorEmail)
          throws IOException {
    log.info("writing normal Experiment events as html");

    Experiment experiment = ExperimentRetriever.getInstance().getExperiment(experimentId);
    String eventPage;
    try {
      eventPage = printEvents(eventQueryResultPair, experiment, timeZone, anon, originalQuery, requestorEmail);
    } catch (IOException e) {
      log.severe("Could not run printEvents. " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    BlobKey blobKey = writeBlobUsingOldApi(eventQueryResultPair, jobId, timeZone, experiment, eventPage);
    return blobKey.getKeyString();

  }

//  private BlobKey writeBlobUsingNewApi(EventQueryResultPair eventQueryResultPair, String jobId, String timeZone,
//                                       Experiment experiment, String eventPage) throws IOException,
//                                                                               FileNotFoundException {
//
//    GcsService gcsService = GcsServiceFactory.createGcsService();
//    String BUCKETNAME;
//    String FILENAME;
//    GcsFilename filename = new GcsFilename(BUCKETNAME, FILENAME);
//    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("text/html").acl("public-read")
//                                                         .addUserMetadata("myfield1", "my field value").build();
//
//    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
//    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
//    writer.println("The woods are lovely dark and deep.");
//    writer.println("But I have promises to keep.");
//    writer.flush();
//
//    writeChannel.waitForOutstandingWrites();
//
//    writeChannel.write(ByteBuffer.wrap("And miles to go before I sleep.".getBytes("UTF8")));
//
//    writeChannel.close();
//    return newBlobKey(filename);
//  }

  private BlobKey writeBlobUsingOldApi(EventQueryResultPair eventQueryResultPair, String jobId, String timeZone,
                                       Experiment experiment, String eventPage) throws IOException,
                                                                               FileNotFoundException,
                                                                               FinalizationException, LockException {
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile file;
    try {
      file = fileService.createNewBlobFile("text/html;charset=UTF-8", jobId);
    } catch (IOException e) {
      log.severe("Could not create blob file. " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    // Open a channel to write to it
    boolean lock = true;
    FileWriteChannel writeChannel;
    try {
      writeChannel = fileService.openWriteChannel(file, lock);
    } catch (FileNotFoundException e) {
      log.severe("Could not open write channel. " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (FinalizationException e) {
      log.severe("Could not finalize. " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (LockException e) {
      log.severe("Lock Exception " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (IOException e) {
      log.severe("IOException: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    // Different standard Java ways of writing to the channel
    // are possible. Here we use a PrintWriter:
    PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));



    out.println(printHeader(eventQueryResultPair.getEvents().size(), getExperimentTitle(experiment), timeZone));
    out.println(eventPage);
    out.flush();
    out.close();
    try {
      writeChannel.closeFinally();
    } catch (IllegalStateException e) {
      log.severe("Could not closeFinally on channel. " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (IOException e) {
      log.severe("IOException on closeFinally. " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    BlobKey blobKey = fileService.getBlobKey(file);
    return blobKey;
  }

  public String writeEndOfDayExperimentEventsAsHtml(boolean anon, String jobId, String experimentId,
                                                    List<EventDAO> events, String timeZoneForClient) throws IOException {
    log.info("writing End of Day Experiment events as html");
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile file = fileService.createNewBlobFile("text/html;charset=UTF-8", jobId);

    // Open a channel to write to it
    boolean lock = true;
    FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);

    // Different standard Java ways of writing to the channel
    // are possible. Here we use a PrintWriter:
    PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));


    Experiment experiment = ExperimentRetriever.getInstance().getExperiment(experimentId);
    String eventPage = printEventDAOs(events, experiment, timeZoneForClient, anon);

    out.println(printHeader(events.size(), getExperimentTitle(experiment), timeZoneForClient));
    out.println(eventPage);
    out.flush();
    out.close();
    writeChannel.closeFinally();

    BlobKey blobKey = fileService.getBlobKey(file);
    return blobKey.getKeyString();

  }


  private String getExperimentTitle(Experiment experiment) {
    String experimentTitle = experiment != null ? experiment.getTitle() : null;
    return escapeText(experimentTitle);
  }

  private String escapeText(String experimentTitle) {
    return StringEscapeUtils.escapeHtml4(experimentTitle);
  }

  private String printHeader(int eventCount, String experimentTitle, String clientTimeZone) {
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

  private String printEvents(EventQueryResultPair eventQueryResultPair, Experiment experiment, String clientTimezone, boolean anon, String originalQuery, String whoFromLogin) throws IOException {
    if (eventQueryResultPair.getEvents().isEmpty()) {
      return "No events in experiment: " + getExperimentTitle(experiment) + ".";
    } else {
      List<String> inputKeys = Lists.newArrayList();
      if (experiment != null) {
        for (Input item : experiment.getInputs()) {
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
          out.append("<td>").append(getTimeString(event, event.getScheduledTime(), clientTimezone)).append("</td>");
          out.append("<td>").append(getTimeString(event, event.getResponseTime(), clientTimezone)).append("</td>");

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
            out.append(getValueAsDisplayString(event, photoByNames, key));
            out.append("</td>");
          }

          List<String> keysCopy = Lists.newArrayList(event.getWhatKeys());
          keysCopy.removeAll(inputKeys);
          for (String extraKey : keysCopy) {
            out.append("<td>");
            out.append(extraKey);
            out.append(" = ");
            out.append(getValueAsDisplayString(event, photoByNames, extraKey));
            out.append("</td>");
          }
          out.append("<tr>");
        } catch (Throwable e) {
          log.log(Level.INFO, "Exception in event processing " + e.getMessage(), e);
        }
      }
      out.append("</table>");
      if (eventQueryResultPair.getNextCursor() != null) {
        String nextCursorUrl = "/events?q=" +
                originalQuery +
                "&who=" + whoFromLogin +
                "&anon=" + anon +
                "&tz=" + clientTimezone +
                "&reportFormat=html" +
                "&cursor=" + eventQueryResultPair.getNextCursor();
        out.append("<center><font size=+4><a href=\"" + nextCursorUrl + "\">Load More Results</a></font></center>");

      }
      out.append("</body></html>");
      return out.toString();
    }
  }

  private String printEventDAOs(List<EventDAO> events, Experiment experiment, String clientTimezone, boolean anon) throws IOException {
    if (events.isEmpty()) {
      return "No events in experiment: " + getExperimentTitle(experiment) + ".";
    } else {
      List<String> inputKeys = Lists.newArrayList();
      for (Input item : experiment.getInputs()) {
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
        out.append("<td>").append(getTimeString(event, event.getScheduledTime(), clientTimezone)).append("</td>");
        out.append("<td>").append(getTimeString(event, event.getResponseTime(), clientTimezone)).append("</td>");

        String who = event.getWho();
        if (anon) {
          who = Event.getAnonymousId(who);
        }
        out.append("<td>").append(who).append("</td>");

        for (Input input : experiment.getInputs()) {
          out.append("<td>");
          out.append(getValueAsDisplayString(event, input));
          out.append("</td>");
        }

        List<String> keysCopy = Lists.newArrayList(event.getWhat().keySet());
        keysCopy.removeAll(inputKeys);
        for (String extraKey : keysCopy) {
          out.append("<td>");
          out.append(extraKey);
          out.append(" = ");
          out.append(getValueAsDisplayString(event, extraKey));
          out.append("</td>");
        }
        out.append("<tr>");
      }
      out.append("</table></body></html>");
      return out.toString();
    }
  }

  private String getValueAsDisplayString(EventDAO event, String key) {
    String value = event.getWhatByKey(key);
    if (value == null) {
      value = "";
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


  private String getValueAsDisplayString(EventDAO event, Input input) {
    String value = event.getWhatByKey(input.getName());
    if (value == null) {
      value = "";
    } else if (input.getQuestionType().equals(InputDAO.PHOTO)) {
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

  private String getTimeString(EventDAO event, Date time, String clientTimezone) {
    String timeString = "";
    if (time != null) {
      timeString = jodaFormatter.print(Event.getTimeZoneAdjustedDate(time, clientTimezone, event.getTimezone()));
    }
    return timeString;
  }

  private String getValueAsDisplayString(Event event, Map<String, PhotoBlob> photoByNames, String key) {
    String value = event.getWhatByKey(key);
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
    return value;
  }

 private String getTimeString(Event event, Date time, String defaultTimezone) {
   String scheduledTimeString = "";
   if (time != null) {
     scheduledTimeString = jodaFormatter.print(event.getTimeZoneAdjustedDate(time, defaultTimezone));
   }
   return scheduledTimeString;
 }
}
