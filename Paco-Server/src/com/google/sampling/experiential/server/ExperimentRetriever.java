package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;

public class ExperimentRetriever {

  private static final Logger log = Logger.getLogger(ExperimentRetriever.class.getName());
  
  public static boolean isWhoAllowedToPostToExperiment(Experiment experiment, String who) {
    who = who.toLowerCase();
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
        if (experiments.size() == 1) {
          Experiment experiment = experiments.get(0);
          // load related piecs before we close the Persistence Manager.
          // TODO eager load the experiment's object graph
          // we now need to actually access related objects for them to get loaded.
          // Also, defaultFetchGroup was causing errors. TODO: Revisit this in the future.
          List<Feedback> feedback = experiment.getFeedback();
          feedback.get(0);
          List<Input> inputs = experiment.getInputs();
          inputs.get(0);
          SignalSchedule schedule = experiment.getSchedule();
          schedule.getId();
          return experiment;
        } else if (experiments.size() > 1) {
          String message = "There are multiple experiments for this id: " + experimentId;
          log.info(message);
          throw new IllegalArgumentException(message);
        } else if (experiments.size() < 1) {
          String message = "There are no experiments for this id: " + experimentId;
          log.info(message);
          throw new IllegalArgumentException(message);
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
