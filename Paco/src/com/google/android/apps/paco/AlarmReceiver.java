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
import android.os.Bundle;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(PacoConstants.TAG, "Receiving alarm");
    Log.i(PacoConstants.TAG, "Intent class = " + intent.getClass());
    Log.i(PacoConstants.TAG, "Data = " + intent.getDataString());
    
    Intent notificationServiceIntent = new Intent(context, NotificationCreatorService.class);
    Bundle extras = intent.getExtras();
    if (extras != null) {
      Log.i(PacoConstants.TAG, "NotificationId = " + extras.getLong(NotificationCreator.NOTIFICATION_ID, -1L));
      Log.i(PacoConstants.TAG, "AlarmTime = " + extras.getLong(Experiment.SCHEDULED_TIME, -1L));
      notificationServiceIntent.putExtras(intent);       
    }        
    context.startService(notificationServiceIntent);  
  }
}
