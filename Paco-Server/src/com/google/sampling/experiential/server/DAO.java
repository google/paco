// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.SignalSchedule;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;
import com.googlecode.objectify.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class DAO extends DAOBase {
  static {
    ObjectifyService.register(Experiment.class);
  }

  private static DAO instance;

  public static synchronized DAO getInstance() {
    if (instance == null) {
      instance = new DAO();
    }
    return instance;
  }


  /*
   *
   * General experiments
   *
   */
  public Experiment getExperiment(String experimentId)
  {
    return ofy().get(Experiment.class, Long.getLong(experimentId));
  }

  public List<Experiment> getPublishedExperiments()
  {
    return getExperiments().filter("published", true).list();
  }

  public List<Experiment> getTargetedExperiments(String user)
  {
    return getExperiments().filter("publishedUsers", user).list();
  }


  /*
   *
   * Observer's experiments
   *
   */
  public Long createExperiment(Experiment experiment)
  {
    ofy().put(experiment);

    return experiment.getId();
  }

  public Boolean updateExperiment(Experiment oldExperiment, Experiment newExperiment)
  {
    return null;
  }

  public List<Experiment> getObserversExperiments(String user)
  {
    return getExperiments().filter("admins", user).list();
  }

  public Boolean deleteExperiment(Experiment experiment)
  {
    return null;
  }


  /*
   *
   * Subject's experiments
   *
   */
  public Long joinExperiment(String user, Experiment experiment)
  {
    return null;
  }

  public List<Experiment> getSubjectsExperiments(String user)
  {
    return getExperiments().filter("publishedUsers", user).list();
  }

  public Boolean leaveExperiment(String user, Experiment experiment)
  {
    return null;
  }


  /*
   *
   * Helper functions
   *
   */
  private Query<Experiment> getExperiments()
  {
    return ofy().query(Experiment.class);
  }

  public List<Experiment> getExperiments(Iterable<Long> ids)
  {
    return Lists.newArrayList(ofy().get(Experiment.class, ids).values());
  }
}
