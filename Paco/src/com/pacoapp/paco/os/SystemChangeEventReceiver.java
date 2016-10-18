package com.pacoapp.paco.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.net.ServerCommunicationService;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;
import com.pacoapp.paco.triggering.NotificationCreatorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemChangeEventReceiver extends BroadcastReceiver {

  private static Logger Log = LoggerFactory.getLogger(SystemChangeEventReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.info("SystemChangeEvent received for intent: " + intent.getAction());
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
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      context.startService(new Intent(context, BootupService.class));
    }
  }

}

