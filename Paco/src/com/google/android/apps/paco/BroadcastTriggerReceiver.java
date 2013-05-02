package com.google.android.apps.paco;

import org.joda.time.DateTime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BroadcastTriggerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
				TelephonyManager.EXTRA_STATE_IDLE)) {
		  triggerPhoneHangup(context, intent);
		}

	}

  private void triggerPhoneHangup(Context context, Intent intent) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);    
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));       
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, Trigger.HANGUP);
    context.startService(broadcastTriggerServiceIntent);
    
  }

}
