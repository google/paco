package com.pacoapp.paco.os;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.net.ServerCommunicationService;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;
import com.pacoapp.paco.triggering.NotificationCreatorService;

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
      context.startService(new Intent(context, ExperimentExpirationManagerService.class));
      //context.startService(new Intent(context, VersionCheckerService.class));
    }
  }

}

