package com.pacoapp.paco.ui;

import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.shared.model2.ExperimentGroup;

public interface ExperimentLoadingActivity {

  void setExperiment(Experiment experimentByServerId);

  Experiment getExperiment();

  void setExperimentGroup(ExperimentGroup groupByName);

}
