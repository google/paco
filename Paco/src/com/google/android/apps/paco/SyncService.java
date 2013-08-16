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
    if (!NetworkUtil.isConnected(this)) {
      return;
    }
    synchronized (SyncService.class) {
      experimentProviderUtil = new ExperimentProviderUtil(this);
      List<Event> allEvents = experimentProviderUtil.getEventsNeedingUpload();
      EventUploader eventUploader = new EventUploader(new UrlContentManager(this), 
                        userPrefs.getServerAddress(), 
                        experimentProviderUtil, this);
      eventUploader.uploadEvents(allEvents); 
    }
  }

}
