package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Experiment;

public class ExperimentRetriever {

  public static boolean isWhoAllowedToPostToExperiment(Experiment experiment, String who) {
    who = who.toLowerCase();
    return experiment.getAdmins().contains(who) || 
      (experiment.getPublished() && (experiment.getPublishedUsers().isEmpty() || experiment.getPublishedUsers().contains(who)));
  }

  public static Experiment getExperiment(String experimentId) {
    if (experimentId != null) {
      return DAO.getInstance().getExperiment(experimentId);
    }

    return null;
  }
}
