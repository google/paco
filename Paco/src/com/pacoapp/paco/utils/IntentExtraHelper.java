package com.pacoapp.paco.utils;

import android.content.Intent;
import android.os.Bundle;

import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.ui.ExperimentLoadingActivity;

public class IntentExtraHelper {

  public static void loadExperimentInfoFromIntent(ExperimentLoadingActivity activity, Intent intent, ExperimentProviderUtil experimentProviderUtil2) {
    Bundle extras = intent.getExtras();
    loadExperimentInfoFromBundle(activity, extras, experimentProviderUtil2);
  }

  public static void loadExperimentInfoFromBundle(ExperimentLoadingActivity activity, Bundle extras,
                                                  ExperimentProviderUtil experimentProviderUtil) {
    if (extras != null) {
      if (extras.containsKey(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY)) {
        long experimentId = extras.getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
        activity.setExperiment(experimentProviderUtil.getExperimentByServerId(experimentId));
        if (activity.getExperiment() != null && extras.containsKey(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY)) {
          String experimentGroupName = extras.getString(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY);
          activity.setExperimentGroup(activity.getExperiment().getExperimentDAO().getGroupByName(experimentGroupName));
        }
      }
    }

  }

}
