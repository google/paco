/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.Event;
import com.google.paco.shared.model.Experiment;
import com.google.paco.shared.model.SignalSchedule;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

/**
 * A class to provide CRUD methods for various entities in the appengine datastore. Uses
 * PacoConverter to generically convert between entities and objects in a elegant way.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class PacoDAO {
  private static PacoDAO instance;

  /**
   * @return a singleton of this class
   */
  public static synchronized PacoDAO getInstance() {
    if (instance == null) {
      instance = new PacoDAO();
    }
    return instance;
  }

  private DatastoreService ds;

  /**
   * Default constructor
   */
  public PacoDAO() {
    ds = DatastoreServiceFactory.getDatastoreService();
  }

  /**
   * Creates an experiment in the datastore. We null out the id and set the version to 1 since this
   * must be a new entity.
   *
   * @param experiment the experiment to create
   * @return the id of the experiment or null if there was a problem
   */
  public Long createExperiment(Experiment experiment, String user) {
    experiment.setId(null);
    experiment.setModificationDate(new Date());
    experiment.addObserver(user);

    // Create entities
    Entity entity = PacoConverter.toEntity(experiment);
    Key key;

    try {
      key = ds.put(entity);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }

    return key.getId();
  }

  /**
   * Retrieve an experiment from the datastore according to the specifed id.
   *
   * @param id the id of the experiment to retrieve
   * @return the experiment with the matching id or null otherwise.
   */
  public Experiment getExperiment(long id) {
    Key key = KeyFactory.createKey("experiment", id);
    Entity entity;

    try {
      entity = ds.get(key);
    } catch (Exception ex) {
      // ex.printStackTrace();
      return null;
    }

    Experiment experiment = PacoConverter.entityTo(entity);

    return (experiment.isDeleted() ? null : experiment);
  }

  /**
   * Update the oldExperiment with a newExperiment. This *should* version the old experiment by
   * saving it as another entity and providing a reference to the new experiment. For we just
   * overwrite it until a elegant solution can be found.
   *
   * @param oldExperiment the old experiment to version
   * @param newExperiment the new experiment to update with
   * @return whether or not the experiment was updated
   */
  public boolean updateExperiment(Experiment oldExperiment, Experiment newExperiment) {
    newExperiment.setId(oldExperiment.getId());
    newExperiment.setModificationDate(new Date());

    // Create entities
    Entity newEntity = PacoConverter.toEntity(newExperiment);

    // FIXME: Save oldExperiment as ExperimentRevision with reference to newExperiment

    try {
      ds.put(newEntity);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Deletes the specified experiment from the datastore. This is a soft-delete in that the entity
   * is not actually deleted but a flag is set.
   *
   * @param experiment the experiment to delete
   * @return whether the experiment was deleted
   */
  public boolean deleteExperiment(Experiment experiment) {
    experiment.setDeleted(true);

    // Create entities
    Entity entity = PacoConverter.toEntity(experiment);

    try {
      ds.put(entity);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Adds the specified user as a subject of the specified experiments, optionally associating a
   * customized signal-schedule if allowed.
   *
   * @param experiment the experiment to join
   * @param signalSchedule a customized signal-schedule (or null if not customized)
   * @return whether the user was successfully enrolled in the experiment
   */
  public boolean joinExperiment(Experiment experiment, String user, SignalSchedule signalSchedule) {
    experiment.addSubject(user);

    if (experiment.getSignalSchedule().isEditable() == false) {
      signalSchedule = null;
    }

    if (signalSchedule != null) {
      signalSchedule.setExperimentId(experiment.getId());
      signalSchedule.setSubject(user);
    }

    // Create entities
    Entity experimentEntity = PacoConverter.toEntity(experiment);
    List<Entity> entities = Lists.newArrayList(experimentEntity);

    if (signalSchedule != null) {
      entities.add(PacoConverter.toEntity(signalSchedule));
    }

    try {
      ds.put(entities);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  public SignalSchedule getSignalSchedule(Experiment experiment, String user) {
    Query q = new Query("signal_schedule");

    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("experiment", experiment.getId()),
        FilterOperator.EQUAL.of("subject", user)));

    List<SignalSchedule> signalSchedules =
        PacoConverter.preparedQueryTo(ds.prepare(q), SignalSchedule.class);

    if (signalSchedules.size() > 1) {
      throw new IllegalStateException("Multiple signal schedules for " + user + " "
          + experiment.getId());
    }

    if (signalSchedules.size() == 0) {
      return null;
    }

    return signalSchedules.get(0);
  }

  /**
   * Removes the specified user from the experiment.
   *
   * @param experiment the experiment to remove the user from
   * @return whether the user was successfully removed from the experiment.
   */
  public boolean leaveExperiment(Experiment experiment, String user) {
    experiment.removeSubject(user);

    Entity entity = PacoConverter.toEntity(experiment);

    try {
      ds.put(entity);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Retrieves a list of experiments that the specified user can view. Note, that this ignores any
   * soft-deleted and unpublished experiments.
   *
   * @param user a user
   * @return a list of experiments the user can view
   */
  public List<Experiment> getViewedExperiments(String user, DateTime modifiedSince) {
    Query q = new Query("experiment");

    // deleted == false && published == true && (viewers == null || viewers == user)
    CompositeFilter filter =
        CompositeFilterOperator.and(FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL
            .of("published", true), CompositeFilterOperator.or(
            FilterOperator.EQUAL.of("viewers", null), FilterOperator.EQUAL.of("viewers", user)));

    if (modifiedSince != null) {
      filter =
          CompositeFilterOperator.and(filter,
              FilterOperator.GREATER_THAN.of("modificationDate", modifiedSince.toDate()));
    }

    q.setFilter(filter);

    return PacoConverter.preparedQueryTo(ds.prepare(q), Experiment.class);
  }

  /**
   * Retrieves a list of experiments that the specified user has joined. Note, that this ignores any
   * soft-deleted and unpublished experiments.
   *
   * @param user a user
   * @return a list of experiments the user has joined
   */
  public List<Experiment> getSubjectedExperiments(String user, DateTime modifiedSince) {
    Query q = new Query("experiment");

    // deleted == false && published == true && subjects == user
    CompositeFilter filter =
        CompositeFilterOperator.and(FilterOperator.EQUAL.of("deleted", false),
            FilterOperator.EQUAL.of("published", true), FilterOperator.EQUAL.of("subjects", user));

    if (modifiedSince != null) {
      filter =
          CompositeFilterOperator.and(filter,
              FilterOperator.GREATER_THAN.of("modificationDate", modifiedSince.toDate()));
    }

    q.setFilter(filter);

    return PacoConverter.preparedQueryTo(ds.prepare(q), Experiment.class);
  }

  /**
   * Retrieves a list of experiments that the specified user can observe. Note, that this ignores
   * any soft-deleted experiments.
   *
   * @param user a user
   * @return a list of experiments that the specified user can observe.
   */
  public List<Experiment> getObservedExperiments(String user, DateTime modifiedSince) {
    Query q = new Query("experiment");

    // deleted == false && observers == user
    CompositeFilter filter =
        CompositeFilterOperator.and(FilterOperator.EQUAL.of("deleted", false),
            FilterOperator.EQUAL.of("observers", user));

    if (modifiedSince != null) {
      filter =
          CompositeFilterOperator.and(filter,
              FilterOperator.GREATER_THAN.of("modificationDate", modifiedSince.toDate()));
    }

    q.setFilter(filter);

    return PacoConverter.preparedQueryTo(ds.prepare(q), Experiment.class);
  }

  /*
   * Events
   */
  public Long createEvent(Event event, Experiment experiment, String user) {
    if (event == null || event.hasId() == true) {
      throw new UnsupportedOperationException();
    }

    event.setExperimentId(experiment.getId());
    event.setSubject(user);
    event.setCreateTime(new Date());

    Entity entity = PacoConverter.toEntity(event);
    Key key = ds.put(entity);

    return key.getId();
  }

  public Event getEvent(long id) {
    if (id <= 0) {
      throw new UnsupportedOperationException();
    }

    try {
      return PacoConverter.entityTo(ds.get(KeyFactory.createKey("event", id)));
    } catch (Exception e) {
      return null;
    }
  }

  public List<Event> getEvents(Experiment experiment) {
    if (experiment == null || experiment.hasId() == false) {
      throw new UnsupportedOperationException();
    }

    Query q = new Query("event");

    // experimentId == experiment.id
    q.setFilter(FilterOperator.EQUAL.of("experimentId", experiment.getId()));

    return PacoConverter.preparedQueryTo(ds.prepare(q), Event.class);
  }

  public List<Event> getEvents(Experiment experiment, String user) {
    if (experiment == null || experiment.hasId() == false || user == null) {
      throw new UnsupportedOperationException();
    }

    Query q = new Query("event");

    // experimentId == experiment.id && subject == user
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("experimentId", experiment.getId()),
        FilterOperator.EQUAL.of("subject", user)));

    return PacoConverter.preparedQueryTo(ds.prepare(q), Event.class);
  }
}
