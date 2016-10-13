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
package com.pacoapp.paco.net;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.ExperimentProviderUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ServerCommunication {

  private static Logger Log = LoggerFactory.getLogger(ServerCommunication.class);

  private static final int DURATION_IN_MINUTES_THAT_IS_LONGER_THAN_TIME_TO_DOWNLOAD_JOINED_EXPERIMENTS = 5;
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
    Log.info("Created alarm for ServerCommunicationService. Time: " + nextCommTime.toString());
  }

  private boolean isInFuture(DateTime time) {
    return time.isAfter(new DateTime().plusMinutes(DURATION_IN_MINUTES_THAT_IS_LONGER_THAN_TIME_TO_DOWNLOAD_JOINED_EXPERIMENTS));
  }

  private void updateJoinedExperiments() {
    final ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(context);
    List<Long> joinedExperimentServerIds = experimentProviderUtil.getJoinedExperimentServerIds();
    if (joinedExperimentServerIds != null && joinedExperimentServerIds.size() > 0) {
      NetworkClient networkClient = new NetworkClient.BackgroundNetworkClient(context) {
        @Override
        public void showAndFinish(String msg) {
          if (msg != null) {
            saveDownloadedExperiments(experimentProviderUtil, msg);
          }
        }

      };
      final Long[] arrayOfIds = joinedExperimentServerIds.toArray(new Long[joinedExperimentServerIds.size()]);
      new PacoBackgroundService(networkClient, ExperimentUrlBuilder.buildUrlForFullExperiment(userPrefs, arrayOfIds));
    }
    // Update even if we don't succeed to prevent endless retries when network is down.
    userPrefs.setJoinedExperimentListRefreshTime(new Date().getTime());
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
