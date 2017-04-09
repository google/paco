package com.pacoapp.paco.os;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExperimentExpirationAlarmReceiver extends BroadcastReceiver {

  private static Logger Log = LoggerFactory.getLogger(ExperimentExpirationAlarmReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.info("Receiving alarm");
    Intent expiryManager = new Intent(context, ExperimentExpirationManagerService.class);
    context.startService(expiryManager);
  }
}
