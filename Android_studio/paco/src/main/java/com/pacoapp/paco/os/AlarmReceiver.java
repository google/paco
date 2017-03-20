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
package com.pacoapp.paco.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.triggering.NotificationCreator;
import com.pacoapp.paco.triggering.NotificationCreatorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

  private static Logger Log = LoggerFactory.getLogger(AlarmReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.info("Receiving alarm");
    Log.info("Intent class = " + intent.getClass());
    Log.info("Data = " + intent.getDataString());

    Intent notificationServiceIntent = new Intent(context, NotificationCreatorService.class);
    Bundle extras = intent.getExtras();
    if (extras != null) {
      Log.info("NotificationId = " + extras.getLong(NotificationCreator.NOTIFICATION_ID, -1L));
      Log.info("AlarmTime = " + extras.getLong(Experiment.SCHEDULED_TIME, -1L));
      notificationServiceIntent.putExtras(intent);
    }
    context.startService(notificationServiceIntent);
  }
}
