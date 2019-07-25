package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.ISO8601DateFormat;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.gcs.GCSFetcher;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.Views;
import com.pacoapp.paco.shared.model2.Views.V4;
import com.pacoapp.paco.shared.model2.Views.V5;

public class EventJsonDownloader {

  public static String jsonifyEvents(boolean anon, String timezoneId, boolean inlineBlobs, 
                                     EventQueryResultPair eventQueryResultPair, Float pacoProtocol, boolean fullBlobAddress) {
    
    List<Event> events = eventQueryResultPair.getEvents();
    String eventCursor = eventQueryResultPair.getCursor();
    return jsonifyEvents(anon, timezoneId, inlineBlobs, pacoProtocol, events, eventCursor, fullBlobAddress);
  }

  public static String jsonifyEvents(boolean anon, String timezoneId, boolean inlineBlobs, Float pacoProtocol,
                                      List<Event> events, String eventCursor, boolean fullBlobAddress) {
    try {
      List<EventDAO> eventDAOs = Lists.newArrayList();
      
      for (Event event : events) {
        String userId = event.getWho();
        if (anon) {
          userId = Event.getAnonymousId(userId);
        }
        DateTime responseDateTime = event.getResponseTimeWithTimeZone(timezoneId);
        DateTime scheduledDateTime = event.getScheduledTimeWithTimeZone(timezoneId);
        List<WhatDAO> whatMap = EventRetriever.convertToWhatDAOs(event.getWhat());
        
        if (inlineBlobs) {
          // legacy GAE DS blob storage
          EventJsonDownloader.fillInResponsesWithEncodedBlobData(event, whatMap);
          // new GCS blob storage
          GCSFetcher.fillInResponsesWithEncodedBlobDataFromGCS(whatMap);
        }
        if (fullBlobAddress) {
          EventJsonDownloader.rewriteBlobUrlsAsFullyQualified(whatMap);
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
      
      
      return writeJsonResponse(pacoProtocol, eventCursor, eventDAOs);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Error could not retrieve events as json";
  }

  public static String jsonifyEventDAOs(boolean anon, boolean inlineBlobs, Float pacoProtocol,
                                     List<EventDAO> events, String eventCursor, boolean fullBlobAddress) {
   try {
     for (EventDAO event : events) {
       if (anon) {
         event.setWho(Event.getAnonymousId(event.getWho()));
       }
       if (inlineBlobs) {
         GCSFetcher.fillInResponsesWithEncodedBlobDataFromGCS(event.getWhat());
       } 
       if (fullBlobAddress) {
         EventJsonDownloader.rewriteBlobUrlsAsFullyQualified(event.getWhat());
       }
     }
     return writeJsonResponse(pacoProtocol, eventCursor, events);
   } catch (JsonGenerationException e) {
     e.printStackTrace();
   } catch (JsonMappingException e) {
     e.printStackTrace();
   } catch (IOException e) {
     e.printStackTrace();
   }
   return "Error could not retrieve events as json";
 }

  
  public static String writeJsonResponse(Float pacoProtocol, String eventCursor,
                                         List<EventDAO> eventDAOs) throws IOException, JsonGenerationException,
                                                                   JsonMappingException {
    EventDAOQueryResultPair eventDaoQueryResultPair = new EventDAOQueryResultPair(eventDAOs, eventCursor);
    String finalRes = null;
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    if (pacoProtocol != null && pacoProtocol < 5) { 
      finalRes = mapper.writerWithView(Views.V4.class).writeValueAsString(eventDaoQueryResultPair);
    } else {
      mapper.setDateFormat(new ISO8601DateFormat());
      finalRes = mapper.writerWithView(Views.V5.class).writeValueAsString(eventDaoQueryResultPair);
    }
    return finalRes;
  }

  static void fillInResponsesWithEncodedBlobData(Event event, List<WhatDAO> whatMap) {
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

  public static void rewriteBlobUrlsAsFullyQualified(List<WhatDAO> whatMap) {
    for(WhatDAO currentWhat : whatMap) {
      String currentWhatValue = currentWhat.getValue();
      if (currentWhatValue != null && currentWhatValue.startsWith("/eventblobs")) {
        currentWhat.setValue("https://" + getHostname() + currentWhatValue);
      }
    }
  }

  public static String getHostname() {
    return HtmlBlobWriter.getHostname();
  }

}
