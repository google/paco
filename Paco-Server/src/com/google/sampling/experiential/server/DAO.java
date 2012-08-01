// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
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
    ds.put(entity);

    return (experiment.getId() == entity.getKey().getId());
  }

  public boolean deleteExperiment(Experiment experiment) {
    experiment.setDeleted(true);

    return updateExperiment(experiment);
  }

  /*
   *
   * Observer's experiments
   */
  public List<Experiment> getObserverExperiments(User user) {
    return getObserverExperiments(user.getEmail());
  }

  public List<Experiment> getObserverExperiments(String user) {
    Query q = queryExperiments().addFilter("observers", FilterOperator.EQUAL, user);
    return queryToExperiments(q);
  }


  /*
   *
   * Subject's experiments
   */
  public Long joinExperiment(User user, Experiment experiment, Schedule schedule) {
    return joinExperiment(user.getEmail(), experiment, schedule);
  }

  public Long joinExperiment(String user, Experiment experiment, Schedule schedule) {
    return null;
  }

  public List<Experiment> getSubjectExperiments(User user) {
    return getSubjectExperiments(user.getEmail());
  }

  public List<Experiment> getSubjectExperiments(String user) {
    Query q = queryExperiments().addFilter("subjects", FilterOperator.EQUAL, user);
    return queryToExperiments(q);
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
    return null;
  }

  public List<Response> getSubjectResponses(String user) {
    return null;
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
    entity.setProperty("json", json);

    return entity;
  }

  private Query queryExperiments() {
    return new Query("experiment");
  }

  private Query queryResponses() {
    return new Query("response");
  }
}
