// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Response;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Schedule;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
  public boolean createExperiment(Experiment experiment) {
    experiment.setId(null);
    experiment.setVersion(1);

    Entity entity = experimentToEntity(experiment);
    ds.put(entity);

    experiment.setId(entity.getKey().getId());

    return experiment.hasId();
  }

  public List<Experiment> getExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && (subject == true || published == true)
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), CompositeFilterOperator.or(
            FilterOperator.EQUAL.of("viewers", user), FilterOperator.EQUAL.of("published", true))));

    return queryToExperiments(q);
  }

  public Experiment getExperiment(long id) {
    try {
      Experiment experiment = entityToExperiment(ds.get(KeyFactory.createKey("experiment", id)));

      if (experiment.isDeleted()) {
        return null;
      } else {
        return experiment;
      }
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public boolean updateExperiment(Experiment experiment) {
    if (experiment.hasId() == false) {
      return false;
    }

    experiment.setVersion(experiment.getVersion() + 1);

    Entity entity = experimentToEntity(experiment);
    Key key = ds.put(entity);

    return ((key.getId() == experiment.getId()));
  }

  public boolean deleteExperiment(Experiment experiment) {
    if (experiment.hasId() == false) {
      return false;
    }

    experiment.setDeleted(true);

    Entity entity = experimentToEntity(experiment);
    Key key = ds.put(entity);

    return (key.getId() == experiment.getId());
  }

  /*
   *
   * Observer's experiments
   */
  public List<Experiment> getObserverExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && observer == true
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("observers", user)));

    return queryToExperiments(q);
  }

  /*
   *
   * Subject's experiments
   */
  public boolean joinExperiment(String user, Experiment experiment, Schedule schedule) {
    if (experiment.hasId() == false) {
      return false;
    }

    if (experiment.addSubject(user) == false) {
      return false;
    }

    Entity entity = experimentToEntity(experiment);
    Key key = ds.put(entity);

    return ((key.getId() == experiment.getId()));
  }

  public List<Experiment> getSubjectExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && subject == true
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("subjects", user)));

    return queryToExperiments(q);
  }

  public boolean leaveExperiment(String user, Experiment experiment) {
    if (experiment.hasId() == false) {
      return false;
    }

    if (experiment.removeSubject(user) == false) {
      return false;
    }

    Entity entity = experimentToEntity(experiment);
    Key key = ds.put(entity);

    return ((key.getId() == experiment.getId()));
  }

  /*
   *
   * Subject's responses
   */
  public boolean createResponse(Response response) {
    return false;
  }

  /*
   *
   * Helper functions
   */
  private List<Experiment> queryToExperiments(Query q) {
    PreparedQuery pq = ds.prepare(q);
    List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
    return entitiesToExperiments(entities);
  }

  private List<Experiment> entitiesToExperiments(List<Entity> entities) {
    List<Experiment> experiments = Lists.newArrayList();

    for (Entity entity : entities) {
      experiments.add(entityToExperiment(entity));
    }

    return experiments;
  }

  @SuppressWarnings("unchecked")
  private Experiment entityToExperiment(Entity entity) {
    if (entity == null) {
      return null;
    }

    Text json = (Text) entity.getProperty("json");

    if (json == null) {
      return null;
    }

    ObjectMapper mapper = new ObjectMapper();
    Experiment experiment;

    try {
      experiment = mapper.readValue(json.getValue(), Experiment.class);
      experiment.setId(entity.getKey().getId());
      experiment.setVersion((Long) entity.getProperty("version"));
      experiment.setPublished((Boolean) entity.getProperty("published"));
      experiment.setDeleted((Boolean) entity.getProperty("deleted"));
      experiment.setObservers((List<String>) entity.getProperty("observers"));
      experiment.setSubjects((List<String>) entity.getProperty("subjects"));
      experiment.setViewers((List<String>) entity.getProperty("viewers"));
    } catch (JsonParseException e) {
      System.out.println(e.toString());
      experiment = null;
    } catch (JsonMappingException e) {
      System.out.println(e.toString());
      experiment = null;
    } catch (IOException e) {
      System.out.println(e.toString());
      experiment = null;
    }

    return experiment;
  }

  private Entity experimentToEntity(Experiment experiment) {
    ObjectMapper mapper = new ObjectMapper();
    Text json;

    try {
      json = new Text(mapper.writeValueAsString(experiment));
    } catch (JsonGenerationException e) {
      json = null;
    } catch (JsonMappingException e) {
      json = null;
    } catch (IOException e) {
      json = null;
    }

    if (json == null) {
      return null;
    }

    Entity entity;

    if (experiment.hasId()) {
      entity = new Entity("experiment", experiment.getId());
    } else {
      entity = new Entity("experiment");
    }

    entity.setProperty("version", experiment.getVersion());
    entity.setProperty("published", experiment.isPublished());
    entity.setProperty("deleted", experiment.isDeleted());
    entity.setProperty("observers", experiment.getObservers());
    entity.setProperty("subjects", experiment.getSubjects());
    entity.setProperty("viewers", experiment.getViewers());
    entity.setProperty("json", json);

    return entity;
  }
}
