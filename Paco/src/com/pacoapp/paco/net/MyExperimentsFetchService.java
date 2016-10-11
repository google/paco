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

import com.pacoapp.paco.model.Experiment;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

public class MyExperimentsFetchService extends Service {

  private static Logger Log = LoggerFactory.getLogger(MyExperimentsFetchService.class);
  private static MyExperimentsFetcher fetcherInstance;
  private final IBinder mBinder = new LocalBinder();

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   */
  public class LocalBinder extends Binder {
      public MyExperimentsFetchService getService() {
          // Return this instance of LocalService so clients can call public methods
          return MyExperimentsFetchService.this;
      }
  }

  @Override
  public IBinder onBind(Intent intent) {
      return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
      Log.debug("UNBIND");
      return true;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public interface ExperimentFetchListener {
    public void done(List<Experiment> experiments);
  }

  public void getExperiments(ExperimentFetchListener listener) {
    getFetcherInstance(this).update(listener);
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Paco MyExperimentsFetchService wakelock");
    wl.acquire();


    Runnable runnable = new Runnable() {
      public void run() {
          getFetcherInstance(MyExperimentsFetchService.this).update(new ExperimentFetchListener() {

            @Override
            public void done(List<Experiment> experiments) {
              wl.release();
              stopSelf();
            }
          });

      }

    };
    (new Thread(runnable)).start();
  }


  public static synchronized MyExperimentsFetcher getFetcherInstance(Context context) {
    if (fetcherInstance == null) {
      fetcherInstance = MyExperimentsFetcher.getInstance(context);
    }
    return fetcherInstance;
  }

}
