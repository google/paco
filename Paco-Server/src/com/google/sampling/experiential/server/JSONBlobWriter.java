package com.google.sampling.experiential.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.TimeUtil;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.Views;

public class JSONBlobWriter {

  private static final Logger log = Logger.getLogger(JSONBlobWriter.class.getName());
  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();


  public JSONBlobWriter() {
  }


  public String writeEventsAsJSON(boolean anon, EventQueryResultPair eventQueryResultPair, String jobId,
                                  DateTimeZone timeZoneForClient, boolean includePhotos, Float pacoProtocol) throws IOException {
    EventRetriever.sortEvents(eventQueryResultPair.getEvents());

    String jsonOutput = jsonifyEvents(anon, timeZoneForClient.getID(), includePhotos, eventQueryResultPair, pacoProtocol);

    return writeBlobUsingNewApi(jobId, jsonOutput).getKeyString();
 }

  private String jsonifyEvents(boolean anon, String timezoneId, boolean includePhotos, EventQueryResultPair eventQueryResultPair, Float pacoProtocol) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      for (Event event : eventQueryResultPair.getEvents()) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        DateTime responseDateTime = event.getResponseTimeWithTimeZone(timezoneId);
        DateTime scheduledDateTime = event.getScheduledTimeWithTimeZone(timezoneId);
        List<WhatDAO> whatMap = EventRetriever.convertToWhatDAOs(event.getWhat());
        List<PhotoBlob> photos = event.getBlobs();
        String[] photoBlobs = null;
        if (includePhotos && photos != null && photos.size() > 0) {

          photoBlobs = new String[photos.size()];

          Map<String, PhotoBlob> photoByNames = Maps.newConcurrentMap();
          for (PhotoBlob photoBlob : photos) {
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
      
      EventDAOQueryResultPair eventDaoQueryResultPair = new EventDAOQueryResultPair(eventDAOs, eventQueryResultPair.getCursor());
      String finalRes = null;
      if (pacoProtocol != null && pacoProtocol < 5) { 
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

  public  BlobKey writeBlobUsingNewApi(String jobId, String json) throws IOException,
                                                                     FileNotFoundException {
    GcsService gcsService = GcsServiceFactory.createGcsService();
    String bucketName = System.getProperty("com.pacoapp.reportbucketname");
    String fileName = jobId;
    GcsFilename filename = new GcsFilename(bucketName, fileName);
    GcsFileOptions options = new GcsFileOptions.Builder()
        .mimeType("application/json")
        .acl("project-private")
        .addUserMetadata("jobId", jobId)
        .build();

    GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
    PrintWriter writer = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));

    writer.println(json);
      writer.flush();
      writeChannel.waitForOutstandingWrites();
    writeChannel.close();
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucketName + "/" + fileName);
    return blobKey;
  }
}
