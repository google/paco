package com.pacoapp.paco.ui;

import com.google.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.model.Experiment;

public interface ExperimentLoadingActivity {

  void setExperiment(Experiment experimentByServerId);

  Experiment getExperiment();

  void setExperimentGroup(ExperimentGroup groupByName);

}
