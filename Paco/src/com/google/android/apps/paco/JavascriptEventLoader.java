package com.google.android.apps.paco;

import java.util.Map;

public class JavascriptEventLoader {
  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;

  /**
   * 
   */
  JavascriptEventLoader(ExperimentProviderUtil experimentProviderUtil, Experiment experiment) {
    this.experimentProviderUtil = experimentProviderUtil;
    this.experiment = experiment;
  }

  public String loadAllEvents() {
    experimentProviderUtil.loadEventsForExperiment(experiment);    
    final Feedback feedback = experiment.getFeedback().get(0);
    return FeedbackActivity.convertExperimentResultsToJsonString(feedback, experiment);  
  }
  
  public void saveResponse(Map<String, String> responses) {
    throw new IllegalArgumentException("Save response not yet implemented!");
  }
}