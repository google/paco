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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.corp.productivity.specialprojects.android.comm.UrlContentManager;

public class ServerCommunication {

  private static ServerCommunication instance;
  
  public static synchronized ServerCommunication getInstance(Context context) {
    if (instance == null) {
      UserPreferences userPrefs = new UserPreferences(context);    
      AlarmManager alarmManager = (AlarmManager)(context.getSystemService(Context.ALARM_SERVICE));
      instance = new ServerCommunication(context, userPrefs, alarmManager);
    }
    return instance;    
  }
  
  private static final Uri CONTENT_URI = Uri.parse("content://com.google.android.apps.paco.ServerCommunication/");
  private Context context;
  private UserPreferences userPrefs;
  private AlarmManager alarmManager;
  
  // Visible for testing
  public ServerCommunication(Context context, UserPreferences userPrefs, AlarmManager alarmManager) {
    this.context = context;
    this.userPrefs = userPrefs;    
    this.alarmManager = alarmManager;
  }

  public synchronized void checkIn() {
//    if (userPrefs.isJoinedExperimentsListStale()) {
//      updateJoinedExperiments();
//    }
//    
//    setNextWakeupTime();    // PRIYA
    checkIn(false);
  }
  
  // Visible for testing
  public synchronized void checkIn(Boolean forTesting) {
    if (userPrefs.isJoinedExperimentsListStale() && !forTesting) {
      updateJoinedExperiments();
    }
    
    setNextWakeupTime();
  }
  
  private void setNextWakeupTime() {
    DateTime nextServerCommunicationTime = new DateTime(userPrefs.getNextServerCommunicationServiceAlarmTime());
    if (isInFuture(nextServerCommunicationTime)) { 
      return;
    }
    
    DateTime nextCommTime = nextServerCommunicationTime.plusHours(24);
    if (nextCommTime.isBeforeNow() || nextCommTime.isEqualNow()) {
      nextCommTime = new DateTime().plusHours(24);
    }
    Intent ultimateIntent = new Intent(context, ServerCommunicationService.class); 
    ultimateIntent.setData(CONTENT_URI);
    PendingIntent intent = PendingIntent.getService(context.getApplicationContext(), 0, ultimateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(intent);
    alarmManager.set(AlarmManager.RTC_WAKEUP, nextCommTime.getMillis(), intent);
    userPrefs.setNextServerCommunicationServiceAlarmTime(nextCommTime.getMillis());
    Log.i(PacoConstants.TAG, "Created alarm for ServerCommunicationService. Time: " + nextCommTime.toString());
  }

  private boolean isInFuture(DateTime time) {
    return time.isAfter(new DateTime().plusSeconds(10));
  }
  
  private void updateJoinedExperiments() {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    DownloadHelper downloadHelper = new DownloadHelper(context, userPrefs);
    downloadHelper.downloadRunningExperiments(experimentProviderUtil.getJoinedExperimentServerIds());
    saveDownloadedExperiments(experimentProviderUtil, downloadHelper.getContentAsString());
  }

  private void saveDownloadedExperiments(ExperimentProviderUtil experimentProviderUtil, 
                                         String contentAsString) {
    try {
      experimentProviderUtil.updateExistingExperiments(contentAsString);
    } catch (JsonParseException e) {
      // Nothing to be done here.
    } catch (JsonMappingException e) {
      // Nothing to be done here.
    } catch (UnsupportedCharsetException e) {
      // Nothing to be done here.
    } catch (IOException e) {
      // Nothing to be done here.
    }
  }
}
