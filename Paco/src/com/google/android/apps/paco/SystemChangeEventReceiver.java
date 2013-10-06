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
import android.util.Log;

public class SystemChangeEventReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(PacoConstants.TAG, "SystemChangeEvent received for intent: " + intent.getAction());
    if ((intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && (intent.getDataString().startsWith("package:com.google.android.apps.paco"))) ||
        intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
        intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)/* || 
        intent.getAction().equals(Intent.ACTION_TIME_CHANGED)*/) {
      
      context.startService(new Intent(context, BeeperService.class));
      context.startService(new Intent(context, NotificationCreatorService.class));
      
      //public void setNextServerCommunicationServiceAlarmTime(Long updateTime)
      // is this what is preventing setting an alarm for this service?
      context.startService(new Intent(context, ServerCommunicationService.class));
      //context.startService(new Intent(context, VersionCheckerService.class));
    }
  }

}

