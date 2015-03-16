package com.google.android.apps.paco.utils;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentLoadingActivity;
import com.google.android.apps.paco.ExperimentProviderUtil;

public class IntentExtraHelper {

  public static void loadExperimentInfoFromIntent(ExperimentLoadingActivity activity, Intent intent, ExperimentProviderUtil experimentProviderUtil2) {
    Bundle extras = intent.getExtras();
    if (extras != null) {
      if (extras.containsKey(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY)) {
        long experimentId = extras.getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
        activity.setExperiment(experimentProviderUtil2.getExperimentByServerId(experimentId));
        if (activity.getExperiment() != null && extras.containsKey(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY)) {
          String experimentGroupName = extras.getString(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY);
          activity.setExperimentGroup(activity.getExperiment().getExperimentDAO().getGroupByName(experimentGroupName));
        }
      }
    }
  }

}
