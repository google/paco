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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

public class ServerCommunicationService extends Service {

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId); 
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Paco ServerCommunicationService wakelock");
    wl.acquire();

    
    Runnable runnable = new Runnable() {
      public void run() {
        try {
          ServerCommunication.getInstance(ServerCommunicationService.this).checkIn();          
        } finally {
          wl.release();
          stopSelf();
        }
      }
    };
    (new Thread(runnable)).start();
  }


}
