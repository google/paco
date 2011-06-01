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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
      boolean noConnectivity =
          intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
      if (!noConnectivity) {
        // Note: both of these services start a separate thread to execute.
        Intent syncService = new Intent(context, SyncService.class);
        context.startService(syncService);
        Intent serverCommService = new Intent(context, ServerCommunicationService.class);
        context.startService(serverCommService);
      }
    }

  }

}

