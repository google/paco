package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class CSVBlobWriter {

  private static final Logger log = Logger.getLogger(CSVBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();


  public CSVBlobWriter() {
  }

  public String writeEndOfDayExperimentEventsAsCSV(boolean anon, List<EventDAO> events,
                                                   String jobId, String clientTimezone) throws IOException {
   sortEventDAOs(events);
   List<String[]> eventsCSV = Lists.newArrayList();

   Set<String> foundColumnNames = Sets.newHashSet();
   for (EventDAO event : events) {
     Map<String, String> whatMap = event.getWhat();
     foundColumnNames.addAll(whatMap.keySet());
   }
   List<String> columns = Lists.newArrayList();
   columns.addAll(foundColumnNames);
   Collections.sort(columns);
   for (EventDAO event : events) {
     eventsCSV.add(toCSV(event, columns, anon, clientTimezone));
   }
   TimeLogger.logTimestamp("T8:");
   // add back in the standard pacot event columns
   columns.add(0, "who");
   columns.add(1, "when");
   columns.add(2, "appId");
   columns.add(3, "pacoVersion");
   columns.add(4, "experimentId");
   columns.add(5, "experimentName");
   columns.add(6, "experimentVersion");
   columns.add(7, "experimentGroupName");
   columns.add(8, "actionTriggerId");
   columns.add(9, "actionId");
   columns.add(10, "actionSpecId");
   columns.add(11, "responseTime");
   columns.add(12, "scheduledTime");
   columns.add(13, "timeZone");

   return writeBlobUsingNewApi(jobId, columns, eventsCSV).getKeyString();


//   FileService fileService = FileServiceFactory.getFileService();
//   AppEngineFile file = fileService.createNewBlobFile("text/csv", jobId);
//
//   // Open a channel to write to it
//   boolean lock = true;
//   FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);
//
//   // Different standard Java ways of writing to the channel
//   // are possible. Here we use a PrintWriter:
//   PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
//
//   CSVWriter csvWriter = null;
//   try {
//     csvWriter = new CSVWriter(out);
//     String[] columnsArray = columns.toArray(new String[0]);
//     csvWriter.writeNext(columnsArray);
//     for (String[] eventCSV : eventsCSV) {
//       csvWriter.writeNext(eventCSV);
//     }
//     csvWriter.flush();
//   } finally {
//     if (csvWriter != null) {
//       csvWriter.close();
//     }
//   }
//   out.close();
//   writeChannel.closeFinally();
//   BlobKey blobKey = fileService.getBlobKey(file);
//   return blobKey.getKeyString();
 }

  private BlobKey writeBlobUsingNewApi(String jobId, List<String> columns, List<String[]> eventsCSV) throws IOException,
                                                                               FileNotFoundException {
    log.info("Writing csv using new api");

    GcsService gcsService = GcsServiceFactory.createGcsService();
    String BUCKETNAME = "reportbucket";
    String FILENAME = jobId;
    GcsFilename filename = new GcsFilename(BUCKETNAME, FILENAME);
    GcsFileOptions options = new GcsFileOptions.Builder().mimeType("text/csv").acl("project-private")
                                                         .addUserMetadata("jobId", jobId).build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
    log.info("got writer");
    CSVWriter csvWriter = null;
    try {
      csvWriter = new CSVWriter(writer);
      log.info("got csv writer");
      String[] columnsArray = columns.toArray(new String[0]);
      csvWriter.writeNext(columnsArray);
      for (String[] eventCSV : eventsCSV) {
        csvWriter.writeNext(eventCSV);
      }

      writer.flush();
      writeChannel.waitForOutstandingWrites();
    } finally {
      if (csvWriter != null) {
        csvWriter.close();
      }
    }

    writeChannel.close();
    log.info("wrote to cloud store");
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey(
        "/gs/" + BUCKETNAME + "/" + FILENAME);
    return blobKey;
  }


 private String[] toCSV(EventDAO event, List<String> columnNames, boolean anon,
                        String clientTimezone) {
   log.info("converting to csv. event: " + getTimeString(event, event.getResponseTime(), clientTimezone));
     int csvIndex = 0;
     String[] parts = new String[14 + columnNames.size()];
     if (anon) {
       parts[csvIndex++] = Event.getAnonymousId(event.getWho() + Event.SALT);
     } else {
       parts[csvIndex++] = event.getWho();
     }
     parts[csvIndex++] = jodaFormatter.print(new DateTime(event.getWhen()));
     parts[csvIndex++] = event.getAppId();
     parts[csvIndex++] = event.getPacoVersion();
     parts[csvIndex++] = event.getExperimentId() != null ? Long.toString(event.getExperimentId()) : null;
     parts[csvIndex++] = event.getExperimentName();
     parts[csvIndex++] = event.getExperimentVersion() != null ? Integer.toString(event.getExperimentVersion()) : "0";
     parts[csvIndex++] = event.getExperimentGroupName();
     parts[csvIndex++] = event.getActionTriggerId() != null ? Long.toString(event.getActionTriggerId()) : "0";
     parts[csvIndex++] = event.getActionId() != null ? Long.toString(event.getActionId()) : "0";
     parts[csvIndex++] = event.getActionTriggerSpecId() != null ? Long.toString(event.getActionTriggerSpecId()) : "0";
     parts[csvIndex++] = getTimeString(event, event.getResponseTime(), clientTimezone);
     parts[csvIndex++] = getTimeString(event, event.getScheduledTime(), clientTimezone);
     parts[csvIndex++] = event.getTimezone();

     Map<String, String> whatMap = event.getWhat();
     for (String key : columnNames) {
       String value = whatMap.get(key);
       parts[csvIndex++] = value;
     }
     return parts;

 }

 String writeNormalExperimentEventsAsCSV(boolean anon, List<EventDAO> eodEventDAOs, String jobId, String clientTimezone) throws IOException {
   List<String[]> eventsCSV = Lists.newArrayList();

   Set<String> foundColumnNames = Sets.newHashSet();
   for (EventDAO event : eodEventDAOs) {
     Map<String, String> whatMap = event.getWhat();
     foundColumnNames.addAll(whatMap.keySet());
   }
   List<String> columns = Lists.newArrayList();
   columns.addAll(foundColumnNames);
   Collections.sort(columns);
   for (EventDAO event : eodEventDAOs) {
     eventsCSV.add(toCSV(event, columns, anon, clientTimezone));
   }
   // add back in the standard pacot event columns
   columns.add(0, "who");
   columns.add(1, "when");
   columns.add(2, "appId");
   columns.add(3, "pacoVersion");
   columns.add(4, "experimentId");
   columns.add(5, "experimentName");
   columns.add(6, "experimentVersion");
   columns.add(7, "experimentGroupName");
   columns.add(8, "actionTriggerId");
   columns.add(9, "actionId");
   columns.add(10, "actionSpecId");
   columns.add(11, "responseTime");
   columns.add(12, "scheduledTime");
   columns.add(13, "timeZone");
   return writeBlobUsingNewApi(jobId, columns, eventsCSV).getKeyString();


 }

 private void sortEventDAOs(List<EventDAO> greetings) {
   Comparator<EventDAO> dateComparator = new Comparator<EventDAO>() {
     @Override
     public int compare(EventDAO o1, EventDAO o2) {
       // TODO really it would be better to sort by responseTime when it exists, or scheduledTime if that does not exist.
       Date when1 = o1.getWhen();
       Date when2 = o2.getWhen();
       if (when1 == null || when2 == null) {
         return 0;
       } else if (when1.after(when2)) {
         return -1;
       } else if (when2.after(when1)) {
         return 1;
       }
       return 0;
     }
   };
   Collections.sort(greetings, dateComparator);
 }

 private String getTimeString(EventDAO event, Date time, String clientTimezone) {
   String scheduledTimeString = "";
   if (time != null) {
     scheduledTimeString = jodaFormatter.print(Event.getTimeZoneAdjustedDate(time, clientTimezone, event.getTimezone()));
   }
   return scheduledTimeString;
 }


}
