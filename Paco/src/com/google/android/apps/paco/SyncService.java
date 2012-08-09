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
package com.google.android.apps.paco;


import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;
import com.google.paco.shared.Outcome;

public class SyncService extends Service {


  private static final int UPLOAD_EVENT_GROUP_SIZE = 50;
  private static final String AUTH_TOKEN_PREFERENCE = null;
  private static final String AUTH_TOKEN_PREFERENCE_NAME_KEY = null;
  private static final String AUTH_TOKEN_PREFERENCE_EXPIRE_KEY = null;
  private static final String DATE_TIME_FORMAT = null;
  private ExperimentProviderUtil experimentProviderUtil;
  private UserPreferences userPrefs;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    userPrefs = new UserPreferences(getApplicationContext());
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Paco SyncService wakelock");
    wl.acquire();

    Runnable syncer = new Runnable() {
      public void run() {
        try {
          syncData();
        } finally {
          wl.release();
        }
      }
    };
    (new Thread(syncer)).start();
  }


  private void syncData() {
    if (!isConnected()) {
      return;
    }
    synchronized (SyncService.class) {
      experimentProviderUtil = new ExperimentProviderUtil(this);
      List<Event> allEvents = experimentProviderUtil.getEventsNeedingUpload();
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
  }

  private boolean isConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }

  private static class ResponsePair {
    int overallCode;
    List<Outcome> outcomes;
  }
  
  private ResponsePair sendToPaco(List<Event> events) {
    ResponsePair responsePair = new ResponsePair();
    
    String json = toJson(events, responsePair);
    if (responsePair.overallCode != 200) {
      return responsePair;
    }
    
    UrlContentManager um = null;
    try {
      um = new UrlContentManager(this, true, userPrefs.getGoogleEmailType());
      Log.i("" + this, "Preparing to post.");
      
      Response response = um.createRequest().setUrl("https://"+userPrefs.getServerAddress()+"/events").
          setPostData(json).addHeader("http.useragent", "PacoDroid2").
          execute();
      
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
    String json = stringWriter.toString();
    return json;
  }

}
