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


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.ExperimentProviderUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Sends locally recorded events to the server.
 *
 */
public class SyncService extends Service {

  private static Logger Log = LoggerFactory.getLogger(SyncService.class);

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
    Log.debug("SyncService onStart");
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
    if (!NetworkUtil.isConnected(this)) {
      Log.debug("No network. Not syncing.");
      return;
    }
    synchronized (SyncService.class) {
      Log.debug("Sync service working");
      experimentProviderUtil = new ExperimentProviderUtil(this);
      List<Event> allEvents = experimentProviderUtil.getEventsNeedingUpload();
      EventUploader eventUploader = new EventUploader(this, userPrefs.getServerAddress(),
                        experimentProviderUtil);
      // For all events, check whether they belong to an experiment that provides a key, and
      // encrypt their answers accordingly
      List<Event> encryptedEvents = new Crypto(experimentProviderUtil).encryptAnswers(allEvents);
      eventUploader.uploadEvents(encryptedEvents);
      Log.debug("SyncService done");
    }
  }
}
