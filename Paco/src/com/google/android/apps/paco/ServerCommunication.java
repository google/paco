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
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.Response;
import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class ServerCommunication {

  private static ServerCommunication instance;
  
  public static synchronized ServerCommunication getInstance(Context context) {
    if (instance == null) {
      instance = new ServerCommunication(context); 
    }
    return instance;    
  }
  
  private static final Uri CONTENT_URI = Uri.parse("content://com.google.android.apps.paco.ServerCommunication/");
  private Context context;
  private UserPreferences userPrefs;

  private ServerCommunication(Context context) {
    this.context = context;
    userPrefs = new UserPreferences(this.context);    
  }

  public synchronized void checkIn() {
    if (userPrefs.isExperimentListStale()) {
      updateExperiments();
    }
    setNextWakeupTime();
  }
  
  private void setNextWakeupTime() {
    DateTime nextServerCommunicationTime = new DateTime(userPrefs.getNextServerCommunicationServiceAlarmTime());
    if (isInFuture(nextServerCommunicationTime)) { 
      return;
    }
    AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
    DateTime tomorrowsCommTime = nextServerCommunicationTime.plusHours(24);
    Intent ultimateIntent = new Intent(context, ServerCommunicationService.class); 
    ultimateIntent.setData(CONTENT_URI);
    PendingIntent intent = PendingIntent.getService(context.getApplicationContext(), 0, ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(intent);
    alarmManager.set(AlarmManager.RTC_WAKEUP, tomorrowsCommTime.getMillis(), intent);
    userPrefs.setNextServerCommunicationServiceAlarmTime(tomorrowsCommTime.getMillis());
    Log.i(PacoConstants.TAG, "Created alarm for ServerCommunicationService. Time: " + tomorrowsCommTime.toString());
  }

  private boolean isInFuture(DateTime time) {
    return time.isAfter(new DateTime().plusSeconds(10));
  }

  public void updateExperiments() {
    // Unify server communication code with duplicate in DownloadTask in FindExperimentActivity.
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    UrlContentManager manager = null;
    try {
      manager = new UrlContentManager(context);
      
      String serverAddress = userPrefs.getServerAddress();
      Response response = manager.createRequest().setUrl(ServerAddressBuilder.createServerUrl(serverAddress, "/experiments")).execute();
      String contentAsString = response.getContentAsString();
      Log.i("FindExperimentsActivity", "data: " + contentAsString);
      ArrayList<Experiment> result = null;
      if (contentAsString != null) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
          result = mapper.readValue(contentAsString,
              new TypeReference<List<Experiment>>() {
              });
        } catch (JsonParseException e) {
          Log.e(PacoConstants.TAG, "Could not parse text: " + contentAsString);
          e.printStackTrace();
        } catch (JsonMappingException e) {
          Log.e(PacoConstants.TAG, "Could not map json: " + contentAsString);
          e.printStackTrace();
        } catch (UnsupportedCharsetException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (result != null) {
        experimentProviderUtil.deleteAllUnJoinedExperiments();
        experimentProviderUtil.updateExistingExperiments(result);
      }
      userPrefs.setExperimentListRefreshTime(new Date().getTime());

    } finally {
      if (manager != null) {
        manager.cleanUp();
      }
    }

  }
}
