package com.pacoapp.paco.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;

public class ExperimentExpirationAlarmReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(PacoConstants.TAG, "Receiving alarm");
    Intent expiryManager = new Intent(context, ExperimentExpirationManagerService.class);
    context.startService(expiryManager);
  }
}
