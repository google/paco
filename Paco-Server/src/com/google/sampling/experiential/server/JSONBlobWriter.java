package com.google.sampling.experiential.server;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
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
  int BUFFER_SIZE = 2 * 1024 * 1024;


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
        
        // blob version of adding base64 encoded photos
        
        if (includePhotos) {
          // legacy GAE DS blob storage
          fillInResponsesWithEncodedBlobData(event, whatMap);
          // new GCS blob storage
          fillInResponsesWithEncodedBlobDataFromGCS(whatMap);
        }

        EventServlet.rewriteBlobUrlsAsFullyQualified(whatMap);
        
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
          log.warning("Blob key for writing blob was null: " + currentWhatValue);
        }
      }
    }
  }

  private String getBlobKey(String value) {
    String[] parts = value.split("&");
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].startsWith("blob-key=")) {
        return parts[i].substring(9);
      }
    }
    return null;
  }


  private GcsFilename getFileName(String bucketName, String objectName) {
    return new GcsFilename(bucketName, objectName);
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
