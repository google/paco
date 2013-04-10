package com.google.android.apps.paco;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.content.Context;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;
import com.google.paco.shared.Outcome;

public class EventUploader {

  private static final int UPLOAD_EVENT_GROUP_SIZE = 50;
  
  private UrlContentManager um;
  private ExperimentProviderUtil experimentProviderUtil;
  private String serverAddress;

  private Context context;

  public EventUploader(UrlContentManager um, String serverAddress, 
                       ExperimentProviderUtil experimentProviderUtil, SyncService syncService) {
    this.um = um;
    this.experimentProviderUtil = experimentProviderUtil;
    this.serverAddress = serverAddress;
    this.context = syncService;
  }
  
  public void uploadEvents(List<Event> allEvents) {
    if (allEvents.size() == 0) {
      Log.d(PacoConstants.TAG, "Nothing to sync");
      return;
    }
    boolean hasErrorOcurred = false;
    Log.d(PacoConstants.TAG, "Tasks found in db");

    int uploadGroupSize = UPLOAD_EVENT_GROUP_SIZE;
    int uploaded = 0;
    while (uploaded < allEvents.size() && !hasErrorOcurred) {
      int groupSize = Math.min(allEvents.size() - uploaded, uploadGroupSize);
      int end = uploaded + groupSize;
      List<Event> events = allEvents.subList(uploaded, end);
      ResponsePair response = sendToPaco(events);
      switch (response.overallCode) {
      case 200:
        for (int i = 0; i < response.outcomes.size(); i++) {
          Outcome current = response.outcomes.get(i);
          if (current.succeeded()) {
            Event correspondingEvent = events.get((int) current.getEventId());
            correspondingEvent.setUploaded(true);
            experimentProviderUtil.updateEvent(correspondingEvent);
          }
        }
        uploaded = end;
        break;
      default:
        hasErrorOcurred = true;
        break;
      }
    }
 
    if (!hasErrorOcurred) {
      Log.d(PacoConstants.TAG, "syncing complete");
    } else {
      Log.d(PacoConstants.TAG, "could not complete upload of events");
    }
  }

  private static class ResponsePair {
    int overallCode;
    List<Outcome> outcomes;
  }
  
  private ResponsePair sendToPaco(List<Event> events) {
    ResponsePair responsePair = new ResponsePair();
    
    String json = toJson(events, responsePair);
    if (responsePair.overallCode == 500) {
      return responsePair;
    }
    
    try {
      Log.i("" + this, "Preparing to post.");      
      Response response = um.createRequest().setUrl(ServerAddressBuilder.createServerUrl(serverAddress, "/events")).
          setPostData(json, Charset.forName("UTF_8").name())
          .addHeader("http.useragent", "Android")
          .addHeader("paco.version", AndroidUtils.getAppVersion(context))
          .execute();
      
      responsePair.overallCode = response.getHttpCode();
      readOutcomesFromJson(responsePair, response.getContentAsString());
      return responsePair;
    } finally {
      if (um != null) {
        um.cleanUp(); 
      }
    }
    
  }

  private void readOutcomesFromJson(ResponsePair responsePair, String contentAsString) {
    if (contentAsString != null) {
      ObjectMapper mapper2 = new ObjectMapper();
      mapper2.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);        
      try {
        responsePair.outcomes = mapper2.readValue(contentAsString, new TypeReference<List<Outcome>>() {});
      } catch (JsonParseException e) {
        Log.e(PacoConstants.TAG, e.getMessage(), e);
        responsePair.overallCode = 500;
      } catch (JsonMappingException e) {
        Log.e(PacoConstants.TAG, e.getMessage(), e);
        responsePair.overallCode = 500;
      } catch (IOException e) {
        Log.e(PacoConstants.TAG, e.getMessage(), e);
        responsePair.overallCode = 500;
      }
    } 
  }

  private String toJson(List<Event> events, ResponsePair responsePair) {
    ObjectMapper mapper = new ObjectMapper();
    StringWriter stringWriter = new StringWriter();
    Log.d(PacoConstants.TAG, "syncing events");
    try {
      mapper.writeValue(stringWriter, events);      
    } catch (JsonGenerationException e) {
      Log.e(PacoConstants.TAG, e.getMessage(), e);
      responsePair.overallCode = 500;
    } catch (JsonMappingException e) {
      Log.e(PacoConstants.TAG, e.getMessage(), e);
      responsePair.overallCode = 500;
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, e.getMessage(), e);
      responsePair.overallCode = 500;
    }
    return stringWriter.toString();
  }

}
