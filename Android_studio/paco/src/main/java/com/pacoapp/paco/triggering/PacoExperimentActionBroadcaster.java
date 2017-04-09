package com.pacoapp.paco.triggering;

import android.content.Context;
import android.content.Intent;

import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;

public class PacoExperimentActionBroadcaster {

  public static void sendJoinExperiment(Context context, Experiment experiment) {
    sendPacoExperimentBroadcast(context, experiment, BroadcastTriggerReceiver.PACO_EXPERIMENT_JOINED_ACTION);
  }

  public static void sendExperimentResponseReceived(Context context, Experiment experiment) {
    sendPacoExperimentBroadcast(context, experiment, BroadcastTriggerReceiver.PACO_EXPERIMENT_RESPONSE_RECEIVED_ACTION);
  }

  public static void sendExperimentEnded(Context context, Experiment experiment) {
    sendPacoExperimentBroadcast(context, experiment, BroadcastTriggerReceiver.PACO_EXPERIMENT_ENDED_ACTION);
  }

  public static void sendPacoExperimentBroadcast(Context context, Experiment experiment,
                                                 final String pacoExperimentAction) {
    Intent intent = new Intent(pacoExperimentAction);
    intent.putExtra(BroadcastTriggerReceiver.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    context.sendBroadcast(intent);
  }

}
