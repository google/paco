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
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.paco.shared.model.Experiment;

import java.util.List;

/**
 * A class to provide CRUD methods for various entities in the appengine
 * datastore. Uses PacoConverter to generically convert between entities and
 * objects in a elegant way.
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
   * Creates an experiment in the datastore. We null out the id and set the
   * version to 1 since this must be a new entity.
   *
   * @param experiment the experiment to create
   * @return the id of the experiment or null if there was a problem
   */
  public Long createExperiment(Experiment experiment) {
    Entity entity = PacoConverter.toEntity(experiment);
    Key key;

    experiment.setId(null);
    experiment.setVersion(1);

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
      //ex.printStackTrace();
      return null;
    }

    Experiment experiment = PacoConverter.entityTo(entity);

    return (experiment.isDeleted() ? null : experiment);
  }

  /**
   * Update the oldExperiment with a newExperiment. This *should* version the
   * old experiment by saving it as another entity and providing a reference to
   * the new experiment. For we just overwrite it until a elegant solution can
   * be found.
   *
   * @param oldExperiment the old experiment to version
   * @param newExperiment the new experiment to update with
   * @return whether or not the experiment was updated
   */
  public boolean updateExperiment(Experiment oldExperiment, Experiment newExperiment) {
    Entity newEntity = PacoConverter.toEntity(newExperiment);

    newExperiment.setId(oldExperiment.getId());
    newExperiment.setVersion(oldExperiment.getVersion() + 1);

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
   * Deletes the specified experiment from the datastore. This is a soft-delete
   * in that the entity is not actually deleted but a flag is set.
   *
   * @param experiment the experiment to delete
   * @return whether the experiment was deleted
   */
  public boolean deleteExperiment(Experiment experiment) {
    experiment.setDeleted(true);

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
   * Retrieves a list of experiments that the specified user can observe. Note,
   * that this ignores any soft-deleted experiments.
   *
   * @param user a user
   * @return a list of experiments that the specified user can observe.
   */
  public List<Experiment> getObservedExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && observers == user
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("observers", user)));

    return PacoConverter.preparedQueryTo(ds.prepare(q), Experiment.class);
  }
}
