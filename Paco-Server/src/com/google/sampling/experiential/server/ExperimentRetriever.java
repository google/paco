package com.google.sampling.experiential.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.ExperimentReference;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.shared.ExperimentDAO;

public class ExperimentRetriever {

  private static final Logger log = Logger.getLogger(ExperimentRetriever.class.getName());
  
  private static ExperimentRetriever instance;
  
  public synchronized static ExperimentRetriever getInstance() {
    if (instance == null) {
      instance = new ExperimentRetriever();      
    }
    return instance;
  }
  
  @VisibleForTesting
  ExperimentRetriever() {};
  
  public Experiment getExperiment(String experimentId) {
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

  public Experiment getReferredExperiment(Long referringExperimentId) {
    PersistenceManager pm = null;
    try {
      if (referringExperimentId != null) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(ExperimentReference.class);
        q.setFilter("referringId == idParam");
        q.declareParameters("Long idParam");
        List<ExperimentReference> experimentRefs = (List<ExperimentReference>) q.execute(Long.valueOf(referringExperimentId));
        if (experimentRefs.size() == 1) {
          ExperimentReference experimentRef = experimentRefs.get(0);
          return getExperiment(Long.toString(experimentRef.getReferencedExperimentId()));
        } else if (experimentRefs.size() > 1) {
          String message = "There are multiple experiments references for referring id: " + referringExperimentId;
          log.info(message);
          //throw new IllegalArgumentException(message);
        } else if (experimentRefs.size() < 1) {
          String message = "There are no experiments references for referring id: " + referringExperimentId;
          log.info(message);
          //throw new IllegalArgumentException(message);
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return null;

    
  }

  public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
    if (referringExperimentId == null || referencedExperimentId == null) {
      throw new IllegalArgumentException("Nust have two valid ids for referencing");
    }
    ExperimentReference ref = new ExperimentReference(referringExperimentId, referencedExperimentId);
    PersistenceManager pm = null;
    try {
        pm = PMF.get().getPersistenceManager();
        pm.makePersistent(ref);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public static void removeSensitiveFields(List<ExperimentDAO> availableExperiments) {
    for (ExperimentDAO experimentDAO : availableExperiments) {
      experimentDAO.setPublished(null);
      experimentDAO.setAdmins(null);
    }
    
  }

  public static boolean arrayContains(String[] strings, String targetString) {
    for (int i = 0; i < strings.length; i++) {
      if (strings[i].toLowerCase().equals(targetString.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  public static List<ExperimentDAO> getSortedExperimentsAvailableToUser(List<ExperimentDAO> experiments, String email) {
    List<ExperimentDAO> availableExperiments = Lists.newArrayList();
    for (ExperimentDAO experiment : experiments) {
      String creatorEmail = experiment.getCreator().toLowerCase();
      if (creatorEmail.equals(email) || ExperimentRetriever.arrayContains(experiment.getAdmins(), email) || 
          (experiment.getPublished() == true && 
                  (experiment.getPublishedUsers().length == 0 || ExperimentRetriever.arrayContains(experiment.getPublishedUsers(), email)))) {
        availableExperiments.add(experiment);
      }
    }
    Collections.sort(availableExperiments, new Comparator<ExperimentDAO>() {
      @Override
      public int compare(ExperimentDAO o1, ExperimentDAO o2) {
        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
      }      
    });
    return availableExperiments;
  }

}
