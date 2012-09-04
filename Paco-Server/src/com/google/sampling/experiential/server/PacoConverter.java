// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.Event;
import com.google.paco.shared.model.Experiment;
import com.google.paco.shared.model.SignalSchedule;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoConverter {
  /*
   * Convert from json
   */

  public static <T> T jsonTo(Text value, Class<T> valueType) {
    return jsonTo(value.getValue(), valueType);
  }

  public static <T> T jsonTo(String value, Class<T> valueType) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.readValue(value, valueType);
    } catch (JsonParseException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * Convert to json
   */
  public static String toJson(Object value, Class<?> view) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.writerWithView(view).writeValueAsString(value);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String toJson(Object value) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.writeValueAsString(value);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * Convert from entity
   */

  public static <T> List<T> entitiesTo(List<Entity> entities, Class<T> type) {
    List<T> objects = Lists.newArrayList();

    for (Entity entity : entities) {
      T object = entityTo(entity, type);
      objects.add(object);
    }

    return objects;
  }

  @SuppressWarnings("unchecked")
  public static <T> T entityTo(Entity entity) {
    if (entity.getKind().equals("experiment")) {
      return (T) entityTo(entity, Experiment.class);
    }

    if (entity.getKind().equals("event")) {
      return (T) entityTo(entity, Event.class);
    }

    throw new UnsupportedOperationException("entityTo1(" + entity.getKind() + ")");
  }

  @SuppressWarnings("unchecked")
  public static <T> T entityTo(Entity entity, Class<T> type) {
    if (Experiment.class == type) {
      return (T) entityToExperiment(entity);
    }

    if (Event.class == type) {
      return (T) entityToEvent(entity);
    }

    if (SignalSchedule.class == type) {
      return (T) entityToSignalSchedule(entity);
    }

    throw new UnsupportedOperationException("entityTo2(" + type.toString() + ")");
  }

  @SuppressWarnings("unchecked")
  private static Experiment entityToExperiment(Entity entity) {
    if (entity == null) {
      return null;
    }

    Text json = (Text) entity.getProperty("json");

    if (json == null) {
      return null;
    }

    Experiment experiment = PacoConverter.jsonTo(json, Experiment.class);

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

  private static SignalSchedule entityToSignalSchedule(Entity entity) {
    if (entity == null) {
      return null;
    }

    Text json = (Text) entity.getProperty("signalSchedule");

    SignalSchedule signalSchedule = PacoConverter.jsonTo(json, SignalSchedule.class);

    if (signalSchedule == null) {
      return null;
    }

    return signalSchedule;
  }

  private static Event entityToEvent(Entity entity) {
    if (entity == null) {
      return null;
    }

    Event event = new Event();

    event.setId(entity.getKey().getId());
    event.setSubject((String) entity.getProperty("subject"));
    event.setExperimentId((Long) entity.getProperty("experimentId"));
    event.setExperimentVersion((Long) entity.getProperty("experimentVersion"));
    event.setCreateTime((Date) entity.getProperty("createTime"));
    event.setSignalTime((Date) entity.getProperty("signalTime"));
    event.setResponseTime((Date) entity.getProperty("responseTime"));

    EmbeddedEntity entityOutputs = (EmbeddedEntity) entity.getProperty("outputs");

    for (String key : entityOutputs.getProperties().keySet()) {
      event.setOutputByKey(key, (String) entityOutputs.getProperty(key));
    }

    return event;
  }

  /*
   * Convert to entity
   */
  public static <T> Entity toEntity(T object) {
    if (object == null) {
      return null;
    }

    return toEntity(object, object.getClass());
  }

  public static <T> Entity toEntity(T object, Class<? extends Object> type) {
    if (object == null || type == null) {
      return null;
    }

    if (Experiment.class == type) {
      return experimentToEntity((Experiment) object);
    }

    if (Event.class == type) {
      return eventToEntity((Event) object);
    }

    if (SignalSchedule.class == type) {
      return signalScheduleToEntity((SignalSchedule) object);
    }

    throw new UnsupportedOperationException("toEntity2(" + type.toString() + ")");
  }

  private static Entity experimentToEntity(Experiment experiment) {
    String json = PacoConverter.toJson(experiment);

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

  private static Entity signalScheduleToEntity(SignalSchedule signalSchedule) {
    String json = PacoConverter.toJson(signalSchedule);

    if (json == null) {
      return null;
    }

    Entity entity = new Entity(
        "schedule", KeyFactory.createKey("experiment", signalSchedule.getExperimentId()));

    entity.setProperty("experiment", signalSchedule.getExperimentId());
    entity.setProperty("subject", signalSchedule.getSubject());
    entity.setProperty("signalSchedule", new Text(json));

    return entity;
  }

  private static Entity eventToEntity(Event event) {
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

  /*
   * Convert from PreparedQuery
   */

  public static <T> List<T> preparedQueryTo(PreparedQuery pq, Class<T> type) {
    List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
    return PacoConverter.entitiesTo(entities, type);
  }
}
