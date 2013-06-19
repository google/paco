package com.google.android.apps.paco;

import org.joda.time.DateTime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BroadcastTriggerReceiver extends BroadcastReceiver {

  public static final String PACO_TRIGGER_INTENT = "com.pacoapp.paco.action.PACO_TRIGGER";
  
	@Override
	public void onReceive(Context context, Intent intent) {
		if (isPhoneHangup(intent)) {
		  triggerPhoneHangup(context, intent);
		} else if (intent.getAction().equals(android.content.Intent.ACTION_USER_PRESENT)) {
		  triggerUserPresent(context, intent);
		} else if(intent.getAction().equals(PACO_TRIGGER_INTENT)) {
		  triggerPacoTriggerRecieved(context, intent);
		}
	}

  private void triggerPacoTriggerRecieved(Context context, Intent intent) {
    String sourceIdentifier = intent.getStringExtra("sourceIdentifier");
    if (sourceIdentifier == null || sourceIdentifier.length() == 0) {
      Log.d(PacoConstants.TAG, "No source identifier specified for PACO_TRIGGER");
    } else {
      triggerEvent(context, Trigger.PACO_ACTION_EVENT, sourceIdentifier);
    }
  }

  private boolean isPhoneHangup(Intent intent) {
    String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
    return telephonyExtraState != null && telephonyExtraState.equals(TelephonyManager.EXTRA_STATE_IDLE);
  }

  private void triggerUserPresent(Context context, Intent intent) {
    Log.i(PacoConstants.TAG, "User present trigger");
    triggerEvent(context, Trigger.USER_PRESENT);
  }

  private void triggerPhoneHangup(Context context, Intent intent) {
    triggerEvent(context, Trigger.HANGUP);    
  }

  private void triggerEvent(Context context, int triggerEventCode) {
    triggerEvent(context, triggerEventCode, null);
  }
  
  private void triggerEvent(Context context, int triggerEventCode, String sourceIdentifier) {
    Intent broadcastTriggerServiceIntent = new Intent(context, BroadcastTriggerService.class);    
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGERED_TIME, DateTime.now().toString(TimeUtil.DATETIME_FORMAT));           
    broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_EVENT, triggerEventCode);
    if (sourceIdentifier != null) {
      broadcastTriggerServiceIntent.putExtra(Experiment.TRIGGER_SOURCE_IDENTIFIER, sourceIdentifier);
    }
    context.startService(broadcastTriggerServiceIntent);
  }

}
