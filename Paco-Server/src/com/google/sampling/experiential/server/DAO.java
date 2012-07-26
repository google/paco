// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.Response;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.SignalSchedule;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;
import com.googlecode.objectify.Query;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class DAO extends DAOBase {
  static {
    ObjectifyService.register(Experiment.class);
    ObjectifyService.register(Response.class);
    ObjectifyService.register(PhotoBlob.class);
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
   */
  public Experiment getExperiment(String experimentId) {
    return ofy().get(Experiment.class, Long.getLong(experimentId));
  }

  public Long createExperiment(Experiment experiment) {
    ofy().put(experiment);

    return experiment.getId();
  }

  public Boolean updateExperiment(Experiment oldExperiment, Experiment newExperiment) {
    return null;
  }

  public Boolean deleteExperiment(Experiment experiment) {
    return null;
  }


  /*
   *
   * Observer's experiments
   */
  public List<Experiment> getObserverExperiments(User user) {
    return getObserverExperiments(user.getEmail());
  }

  public List<Experiment> getObserverExperiments(String user) {
    return getExperiments().filter("admins", user).list();
  }


  /*
   *
   * Subject's experiments
   */
  public Long joinExperiment(User user, Experiment experiment, SignalSchedule schedule) {
    return joinExperiment(user.getEmail(), experiment, schedule);
  }

  public Long joinExperiment(String user, Experiment experiment, SignalSchedule schedule) {
    return null;
  }

  public List<Experiment> getSubjectExperiments(User user) {
    return getSubjectExperiments(user.getEmail());
  }

  public List<Experiment> getSubjectExperiments(String user) {
    return getExperiments().filter("publishedUsers", user).list();
  }

  public Boolean leaveExperiment(User user, Experiment experiment) {
    return leaveExperiment(user.getEmail(), experiment);
  }

  public Boolean leaveExperiment(String user, Experiment experiment) {
    return null;
  }


  /*
   *
   * Subject's responses
   */
  public Long createResponse(Response response) {
    ofy().put(response);

    return response.getId();
  }

  public List<Response> getSubjectResponses(String user) {
    return getResponses().filter("subject", user).list();
  }


  /*
   *
   * Helper functions
   */
  private Query<Experiment> getExperiments() {
    return ofy().query(Experiment.class);
  }

  public List<Experiment> getPublishedExperiments() {
    return getExperiments().filter("published", "true").list();
  }

  public List<Experiment> getExperiments(Iterable<Long> ids) {
    return Lists.newArrayList(ofy().get(Experiment.class, ids).values());
  }

  private Query<Response> getResponses() {
    return ofy().query(Response.class);
  }

  private List<PhotoBlob> getPhotoBlobs(Iterable<Long> ids) {
    return Lists.newArrayList(ofy().get(PhotoBlob.class, ids).values());
  }
}
