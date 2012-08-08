// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
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

    Entity entity = experimentToEntity(experiment);
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

  public ObservedExperiment getObservedExperiment(long id) {
    try {
      ObservedExperiment experiment =
          entityToObservedExperiment(ds.get(KeyFactory.createKey("experiment", id)));

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

    Entity newEntity = experimentToEntity(newExperiment);
    Key newKey = ds.put(newEntity);

    return (newKey.getId() == newExperiment.getId());
  }

  public boolean deleteExperiment(ObservedExperiment experiment) {
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
  public List<ObservedExperiment> getObserverExperiments(String user) {
    Query q = new Query("experiment");

    // deleted == false && observer == true
    q.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("deleted", false), FilterOperator.EQUAL.of("observers", user)));

    return queryToObservedExperiments(q);
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

    Entity observedExperimentEntity = experimentToEntity(observedExperiment);
    Key observedExperimentKey = ds.put(observedExperimentEntity);

    Entity signalScheduleEntity = null;

    if (signalSchedule != null) {
      signalScheduleEntity = signalScheduleToEntity(user, signalSchedule, observedExperimentKey);
      ds.put(signalScheduleEntity);
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

    return queryToExperiments(q);
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

    Entity entity = experimentToEntity(experiment);
    Key key = ds.put(entity);

    return ((key.getId() == experiment.getId()));
  }

  /*
   *
   * Subject's responses
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

    Entity entity = eventToEntity(event);
    ds.put(entity);

    event.setId(entity.getKey().getId());

    return event.hasId();
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


    Experiment experiment = DAOHelper.jsonTo(json, Experiment.class);

    if (experiment == null) {
      return null;
    }

    experiment.setId(entity.getKey().getId());
    experiment.setVersion((Long) entity.getProperty("version"));
    experiment.setDeleted((Boolean) entity.getProperty("deleted"));

    return experiment;
  }

  private List<ObservedExperiment> queryToObservedExperiments(Query q) {
    PreparedQuery pq = ds.prepare(q);
    List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
    return entitiesToObservedExperiments(entities);
  }

  private List<ObservedExperiment> entitiesToObservedExperiments(List<Entity> entities) {
    List<ObservedExperiment> experiments = Lists.newArrayList();

    for (Entity entity : entities) {
      experiments.add(entityToObservedExperiment(entity));
    }

    return experiments;
  }

  @SuppressWarnings("unchecked")
  private ObservedExperiment entityToObservedExperiment(Entity entity) {
    if (entity == null) {
      return null;
    }

    Text json = (Text) entity.getProperty("json");

    if (json == null) {
      return null;
    }

    ObservedExperiment experiment = DAOHelper.jsonTo(json, ObservedExperiment.class);

    if (experiment == null) {
      return null;
    }

    experiment.setId(entity.getKey().getId());
    experiment.setVersion((Long) entity.getProperty("version"));
    experiment.setDeleted((Boolean) entity.getProperty("deleted"));
    experiment.setPublished((Boolean) entity.getProperty("published"));
    experiment.setObservers((List<String>) entity.getProperty("observers"));
    experiment.setSubjects((List<String>) entity.getProperty("subjects"));
    experiment.setViewers((List<String>) entity.getProperty("viewers"));

    return experiment;
  }

  private Entity experimentToEntity(ObservedExperiment experiment) {
    String json = DAOHelper.toJson(experiment);

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
    entity.setProperty("deleted", experiment.isDeleted());
    entity.setProperty("published", experiment.isPublished());
    entity.setProperty("observers", experiment.getObservers());
    entity.setProperty("subjects", experiment.getSubjects());
    entity.setProperty("viewers", experiment.getViewers());
    entity.setProperty("json", new Text(json));

    return entity;
  }

  private Entity signalScheduleToEntity(
      String subject, SignalSchedule signalSchedule, Key observedExperiment) {
    String json = DAOHelper.toJson(signalSchedule);

    if (json == null) {
      return null;
    }

    Entity entity = new Entity("schedule", observedExperiment);

    entity.setProperty("subject", subject);
    entity.setProperty("signalSchedule", new Text(json));

    return entity;
  }

  private Entity eventToEntity(Event event) {
    Entity entity;

    if (event.hasId()) {
      entity = new Entity("event", event.getId());
    } else {
      entity = new Entity("event");
    }

    entity.setProperty("subject", event.getSubject());
    entity.setProperty("experimentId", event.getExperimentId());
    entity.setProperty("experimentVersion", event.getExperimentVersion());
    entity.setProperty("createTime", event.getCreateTime());
    entity.setProperty("signalTime", event.getSignalTime());
    entity.setProperty("responseTime", event.getResponseTime());

    EmbeddedEntity outputsEntity = new EmbeddedEntity();
    for (String key : event.getOutputs().keySet()) {
      outputsEntity.setProperty(key, event.getOutputByKey(key));
    }

    entity.setProperty("outputs", outputsEntity);

    return entity;
  }
}
