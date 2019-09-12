package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
import com.google.sampling.experiential.shared.EventDateComparator;
import com.google.sampling.experiential.shared.TimeUtil;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.util.ExperimentHelper;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVBlobWriter {

  private static final Logger log = Logger.getLogger(CSVBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();

  public static final List<String> standardColumns = Lists.newArrayList("who", "when","appId","pacoVersion",
                                            "experimentId","experimentName","experimentVersion",
                                            "experimentGroupName","actionTriggerId","actionId",
                                            "actionSpecId","responseTime", "scheduledTime", "timeZone");
  public static final List<String> standardColumnsV5 = Lists.newArrayList("who", "when","appId","pacoVersion",
                                                                        "experimentId","experimentName","experimentVersion",
                                                                        "experimentGroupName","actionTriggerId","actionId",
                                                                        "actionSpecId","responseTime", "scheduledTime");


  public CSVBlobWriter() {
  }

  public String writeEndOfDayExperimentEventsAsCSV(boolean anon, List<EventDAO> events,
                                                   String jobId, Float pacoProtocol) throws IOException {
   Collections.sort(events, new EventDateComparator());
   List<String[]> eventsCSV = Lists.newArrayList();

   List<String> columns = Lists.newArrayList();
   for (EventDAO event : events) {
     List<WhatDAO> whatMap = event.getWhat();
     for (WhatDAO whatDAO : whatMap) {
       if (!columns.contains(whatDAO.getName())) {
         columns.add(whatDAO.getName());
       }
    }

   }
   Collections.sort(columns);
   for (EventDAO event : events) {
     eventsCSV.add(toCSV(event, columns, anon, pacoProtocol));
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
   if (pacoProtocol != null && pacoProtocol <5) { 
     columns.add(13, "timeZone");
   }

   return writeBlobUsingNewApi(jobId, columns, eventsCSV).getKeyString();
 }

  private BlobKey writeBlobUsingNewApi(String jobId, List<String> columns,
                                       List<String[]> eventsCSV) throws IOException,
                                                                     FileNotFoundException {
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucketName = System.getProperty("com.pacoapp.reportbucketname");
    String fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
        .mimeType("text/csv")
        .acl("project-private")
        .addUserMetadata("jobId", jobId)
        .build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
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
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + fileName);
    return blobKey;
  }


  private String[] toCSV(EventDAO event, List<String> whatColumnNames, boolean anon,
                        Float pacoProtocol) {
     int csvIndex = 0;
     int totalColSize = 0;
     if (pacoProtocol != null && pacoProtocol < 5) {
       totalColSize = standardColumns.size() + whatColumnNames.size();
     } else {
       totalColSize = standardColumnsV5.size() + whatColumnNames.size();
     }
     String[] parts = new String[totalColSize];
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
     parts[csvIndex++] = event.getResponseTime() != null ? event.getResponseTime().toString() : "";
     parts[csvIndex++] = event.getScheduledTime() != null ? event.getScheduledTime().toString() : "";
     if (pacoProtocol != null && pacoProtocol < 5) {
       parts[csvIndex++] = event.getTimezone();
     }
     // dealing with possible duplicate var-names, in-order in survey definition (legacy)
     Set<WhatDAO> unhandledWhats = Sets.newHashSet();
     unhandledWhats.addAll(event.getWhat());
     for (String columnName : whatColumnNames) {
       boolean foundWhat = false;
       for (WhatDAO whatDAO : unhandledWhats) {
         if (whatDAO.getName().equals(columnName)) {
           foundWhat = true;
           parts[csvIndex++] = whatDAO.getValue();
           unhandledWhats.remove(whatDAO);
           break;
         }
       }
       if (!foundWhat) {
         parts[csvIndex++] = null;
       }
     }
     return parts;
 }

 String writeNormalExperimentEventsAsCSV(ExperimentDAO experiment, List<EventDAO> eventDAOs, String jobId, boolean anon, Float pacoProtocol) throws IOException {
   List<String> experimentColumnNames = Lists.newArrayList();
   List<Input2> inputs = ExperimentHelper.getInputs(experiment);
   for (Input2 input2 : inputs) {
    experimentColumnNames.add(input2.getName());
  }


   List<String> undeclaredExperimentColumnNames = Lists.newArrayList();
   for (EventDAO event : eventDAOs) {
     List<WhatDAO> whatMap = event.getWhat();
     // no inlining of blobs in csv
     
     EventJsonDownloader.rewriteBlobUrlsAsFullyQualified(whatMap);
     
     for (WhatDAO output : whatMap) {
       String name = output.getName();
       if (!experimentColumnNames.contains(name) && !undeclaredExperimentColumnNames.contains(name)) {
         undeclaredExperimentColumnNames.add(name);
       }
     }
   }
   Collections.sort(undeclaredExperimentColumnNames);

   // add back in the standard pacot event columns
   List<String> columns = Lists.newArrayList();

   columns.addAll(experimentColumnNames);
   columns.addAll(undeclaredExperimentColumnNames);

   List<String[]> eventsCSV = Lists.newArrayList();
   for (EventDAO event : eventDAOs) {
     eventsCSV.add(toCSV(event, columns, anon, pacoProtocol));
   }
   if (pacoProtocol != null && pacoProtocol >=5) {
     columns.addAll(0, standardColumnsV5);
   } else {
     columns.addAll(0, standardColumns);
   }
   return writeBlobUsingNewApi(jobId, columns, eventsCSV).getKeyString();


  }
}
