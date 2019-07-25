package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.gcs.GCSFetcher;

public class JSONBlobWriter {

  public static final Logger log = Logger.getLogger(JSONBlobWriter.class.getName());
  
  public JSONBlobWriter() {
  }

  public String writeEventsAsJSON(boolean anon, EventQueryResultPair eventQueryResultPair, String jobId,
                                  DateTimeZone timeZoneForClient, boolean inlineBlobs, Float pacoProtocol, boolean fullBlobAddress) throws IOException {
    // TODO see if sorting of events is already being done on the query. This seems like the wrong place to do it and it may be redundant
    EventRetriever.sortEvents(eventQueryResultPair.getEvents());
    String jsonOutput = EventJsonDownloader.jsonifyEvents(anon, timeZoneForClient.getID(), inlineBlobs, eventQueryResultPair, pacoProtocol, fullBlobAddress);
    return GCSFetcher.writeJsonBlobToGCS(jobId, jsonOutput).getKeyString();
 }
}
