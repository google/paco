// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.Experiment;

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
    ObjectifyService.register(Event.class);
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

  public List<Experiment> getPublishedExperiments() {
    return getExperiments().filter("published", true).list();
  }

  public List<Experiment> getTargetedExperiments(String user) {
    return getExperiments().filter("publishedUsers", user).list();
  }


  /*
   *
   * Observer's experiments
   */
  public Long createExperiment(Experiment experiment) {
    ofy().put(experiment);

    return experiment.getId();
  }

  public Boolean updateExperiment(Experiment oldExperiment, Experiment newExperiment) {
    return null;
  }

  public List<Experiment> getObserversExperiments(User user) {
    return getObserversExperiments(user.getEmail());
  }

  public List<Experiment> getObserversExperiments(String user) {
    return getExperiments().filter("admins", user).list();
  }

  public Boolean deleteExperiment(Experiment experiment) {
    return null;
  }


  /*
   *
   * Subject's experiments
   */
  public Long joinExperiment(User user, Experiment experiment) {
    return joinExperiment(user.getEmail(), experiment);
  }

  public Long joinExperiment(String user, Experiment experiment) {
    return null;
  }

  public List<Experiment> getSubjectsExperiments(User user) {
    return getSubjectsExperiments(user.getEmail());
  }

  public List<Experiment> getSubjectsExperiments(String user) {
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
   * Subject's events
   */
  public Long createEvent(Event event) {
    ofy().put(event);

    return event.getId();
  }

  public List<Event> getSubjectsEvents(String user) {
    return getEvents().filter("who", user).list();
  }

  public Long createPhotoBlob(PhotoBlob photoBlob) {
    ofy().put(photoBlob);

    return photoBlob.getId();
  }

  public List<PhotoBlob> getEventPhotoBlobs(Event event) {
    List<Long> ids = Lists.newArrayList();

    for (String blob : event.getBlobs()) {
      ids.add(Long.parseLong(blob));
    }

    return getPhotoBlobs(ids);
  }


  /*
   *
   * Helper functions
   */
  private Query<Experiment> getExperiments() {
    return ofy().query(Experiment.class);
  }

  public List<Experiment> getExperiments(Iterable<Long> ids) {
    return Lists.newArrayList(ofy().get(Experiment.class, ids).values());
  }

  private Query<Event> getEvents() {
    return ofy().query(Event.class);
  }

  private List<PhotoBlob> getPhotoBlobs(Iterable<Long> ids) {
    return Lists.newArrayList(ofy().get(PhotoBlob.class, ids).values());
  }
}
