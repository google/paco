// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ObservedExperiment;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.SignalSchedule;

import java.util.Date;
import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class DAO {
  private static DAO instance;

  public static synchronized DAO getInstance() {
    if (instance == null) {
      instance = new DAO();
    }
    return instance;
  }

  private DatastoreService ds;

  public DAO() {
    ds = DatastoreServiceFactory.getDatastoreService();
  }

  /*
   *
   * General experiments
   */
  public boolean createExperiment(ObservedExperiment experiment) {
    if (experiment == null) {
      return false;
    }

    experiment.setId(null);
    experiment.setVersion(1);

    Entity entity = DAOHelper.toEntity(experiment);
    ds.put(entity);

    experiment.setId(entity.getKey().getId());

    return experiment.hasId();
  }

  public List<Experiment> getExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && published == true && (viewer == true || viewer == null)
    q.setFilter(CompositeFilterOperator.and(FilterOperator.EQUAL.of("deleted", false),
        FilterOperator.EQUAL.of("published", true), CompositeFilterOperator.or(
            FilterOperator.EQUAL.of("viewers", user), FilterOperator.EQUAL.of("viewers", null))));

    return DAOHelper.preparedQueryTo(ds.prepare(q), Experiment.class);
  }

  public Experiment getExperiment(long id) {
    try {
      Experiment experiment = DAOHelper.entityTo(ds.get(KeyFactory.createKey("experiment", id)));

      if (experiment.isDeleted()) {
        return null;
      } else {
        return experiment;
      }
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public ObservedExperiment getObservedExperiment(long id) {
    try {
      ObservedExperiment experiment = DAOHelper.entityTo(
          ds.get(KeyFactory.createKey("experiment", id)), ObservedExperiment.class);

      if (experiment.isDeleted()) {
        return null;
      } else {
        return experiment;
      }
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public boolean updateExperiment(
      ObservedExperiment newExperiment, ObservedExperiment oldExperiment) {
    if (newExperiment == null || oldExperiment == null) {
      return false;
    }

    if (oldExperiment.hasId() == false) {
      return false;
    }

    newExperiment.setId(oldExperiment.getId());
    newExperiment.setVersion(oldExperiment.getVersion() + 1);

    Entity newEntity = DAOHelper.toEntity(newExperiment);
    Key newKey = ds.put(newEntity);

    return (newKey.getId() == newExperiment.getId());
  }

  public boolean deleteExperiment(ObservedExperiment experiment) {
    if (experiment.hasId() == false) {
      return false;
    }

    experiment.setDeleted(true);

    Entity entity = DAOHelper.toEntity(experiment);
    Key key = ds.put(entity);

    return (key.getId() == experiment.getId());
  }

  /*
   *
   * Observer's experiments
   */
  public List<ObservedExperiment> getObserverExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && observer == true
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("observers", user)));

    return DAOHelper.preparedQueryTo(ds.prepare(q), ObservedExperiment.class);
  }

  /*
   *
   * Subject's experiments
   */
  public boolean joinExperiment(
      String user, ObservedExperiment observedExperiment, SignalSchedule signalSchedule) {
    if (observedExperiment.hasId() == false) {
      return false;
    }

    if (observedExperiment.addSubject(user) == false) {
      return false;
    }

    Transaction txn = ds.beginTransaction();

    Key observedExperimentKey = ds.put(DAOHelper.toEntity(observedExperiment));

    if (signalSchedule != null) {
      signalSchedule.setSubject(user);
      signalSchedule.setExperimentId(observedExperiment.getId());
      ds.put(DAOHelper.toEntity(signalSchedule));
    }

    txn.commit();

    return ((observedExperimentKey.getId() == observedExperiment.getId()));
  }

  public List<Experiment> getSubjectExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && published == true && subject == true
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("published", true),
        FilterOperator.EQUAL.of("subjects", user)));

    return DAOHelper.preparedQueryTo(ds.prepare(q), Experiment.class);
  }

  public boolean leaveExperiment(String user, ObservedExperiment experiment) {
    if (user == null || experiment == null) {
      return false;
    }

    if (experiment.hasId() == false) {
      return false;
    }

    if (experiment.removeSubject(user) == false) {
      return false;
    }

    Entity entity = DAOHelper.toEntity(experiment);
    Key key = ds.put(entity);

    return ((key.getId() == experiment.getId()));
  }

  /*
   *
   * Subject's events
   */
  public boolean createEvent(String user, Event event, Experiment experiment) {
    if (user == null || event == null || experiment == null) {
      return false;
    }

    event.setId(null);
    event.setSubject(user);
    event.setCreateTime(new Date());
    event.setExperimentId(experiment.getId());

    if (event.getExperimentVersion() > experiment.getVersion()) {
      return false;
    }

    Entity entity = DAOHelper.toEntity(event);
    ds.put(entity);

    event.setId(entity.getKey().getId());

    return event.hasId();
  }

  public Event getEvent(long id) {
    try {
      return DAOHelper.entityTo(ds.get(KeyFactory.createKey("event", id)));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public List<Event> getEvents(Experiment experiment) {
    Query q = new Query("event");

    // experimentId == experiment.id
    q.setFilter(FilterOperator.EQUAL.of("experimentId", experiment.getId()));

    return DAOHelper.preparedQueryTo(ds.prepare(q), Event.class);
  }

  public List<Event> getEvents(Experiment experiment, String user) {
    Query q = new Query("event");

    // experimentId == experiment.id && subject == user
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("experimentId", experiment.getId()),
        FilterOperator.EQUAL.of("subject", user)));

    return DAOHelper.preparedQueryTo(ds.prepare(q), Event.class);
  }
}
