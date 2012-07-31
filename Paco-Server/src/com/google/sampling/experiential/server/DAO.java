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
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.Input;
import com.google.sampling.experiential.shared.Response;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.SignalSchedule;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

  public List<Experiment> getExperiments(List<Long> ids) {
    List<Key> keys = Lists.newArrayListWithCapacity(ids.size());

    for (Long id : ids) {
      keys.add(KeyFactory.createKey("experiment", id));
    }

    Map<Key, Entity> entitiesByKey = ds.get(keys);

    List<Entity> entities = Lists.newArrayList();

    for (Key key : keys) {
      entities.add(entitiesByKey.get(key));
    }

    return entitiesToExperiments(entities);
  }

  public Experiment getExperiment(long id) {
    return getExperiments(Lists.newArrayList(id)).get(0);
  }

  public Long createExperiment(Experiment experiment) {
    Entity entity = experimentToEntity(null, experiment);
    ds.put(entity);
    return entity.getKey().getId();
  }

  public Boolean updateExperiment(long id, Experiment experiment) {
    Entity entity = experimentToEntity(id, experiment);
    ds.put(entity);
    return (entity.getKey().getId() == id);
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
    Query q = queryExperiments().addFilter("observers", FilterOperator.EQUAL, user);
    return queryToExperiments(q);
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

  private Entity experimentToEntity(Long id, Experiment experiment) {
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

    if (id == null) {
      entity = new Entity("experiment");
    } else {
      entity = new Entity("experiment", id);
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
