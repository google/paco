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
    if (userPrefs.isJoinedExperimentsListStale()) {
      updateJoinedExperiments();
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

  public void updateRunningExperiments() {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    DownloadHelper downloadHelper = new DownloadHelper(context, experimentProviderUtil, userPrefs);
    downloadHelper.updateAvailableExperiments();
  }
  
  private void updateJoinedExperiments() {
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    DownloadHelper downloadHelper = new DownloadHelper(context, experimentProviderUtil, userPrefs);
    downloadHelper.updateRunningExperiments(experimentProviderUtil.getJoinedExperiments(), true);
  }
}
