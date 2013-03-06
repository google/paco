package com.google.android.apps.paco;

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
}