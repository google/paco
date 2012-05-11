package com.google.sampling.experiential.server;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.sampling.experiential.model.Experiment;

public class ExperimentRetriever {

  public static boolean isWhoAllowedToPostToExperiment(Experiment experiment, String who) {
    return experiment.getAdmins().contains(who) || 
      (experiment.getPublished() && (experiment.getPublishedUsers().isEmpty() || experiment.getPublishedUsers().contains(who)));
  }

  public static Experiment getExperiment(String experimentId) {
    PersistenceManager pm = null;
    try {
      if (experimentId != null) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(Experiment.class);
        q.setFilter("id == idParam");
        q.declareParameters("Long idParam");
        List<Experiment> experiments = (List<Experiment>) q.execute(Long.valueOf(experimentId));
        if (experiments.size() > 0) {
          Experiment experiment = experiments.get(0);
          // load related piecs before we close the Persistence Manager.
          // TODO eager load the experiment's object graph
          experiment.getFeedback();
          experiment.getInputs();
          experiment.getSchedule();
          return experiment;
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return null;
  }

}
