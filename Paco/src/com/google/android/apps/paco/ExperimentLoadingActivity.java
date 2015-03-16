package com.google.android.apps.paco;

import com.google.paco.shared.model2.ExperimentGroup;

public interface ExperimentLoadingActivity {

  void setExperiment(Experiment experimentByServerId);

  Experiment getExperiment();

  void setExperimentGroup(ExperimentGroup groupByName);

}
