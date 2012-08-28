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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.Experiment;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

import java.io.IOException;
import java.util.List;

/**
 * A class with many static methods to convert between json, entities, and pojos.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class PacoConverter {
  /**
   * Converts the given datastore Text value into the specified pojo type.
   *
   * @param value a datastore Text object containing json encoded object.
   * @param valueType the type of pojo to convert to
   * @return an instance of the pojo converted from the json value
   */
  public static <T> T jsonTo(Text value, Class<T> valueType) {
    return jsonTo(value.getValue(), valueType);
  }

  /**
   * Converts the specified json string value into the specified pojo type.
   * 
   * @param value a json string containing a encoded object
   * @param valueType the type of the pojo to convert to
   * @return an instance of the pojo converted from the json vaue
   */
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

  /**
   * Converts the specified pojo into json encoded value.
   *
   * @param value the pojo to convert
   * @return a json encoded string representing the pojo
   */
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

  /**
   * Convert the specified list of entities into the given type of pojos.
   *
   * @param entities the list of entities to convert from
   * @param type the type of pojo to convert o
   * @return a list of pojos of the specified type converted from the list of entities
   */
  public static <T> List<T> entitiesTo(List<Entity> entities, Class<T> type) {
    List<T> objects = Lists.newArrayList();

    for (Entity entity : entities) {
      T object = entityTo(entity, type);
      objects.add(object);
    }

    return objects;
  }

  /**
   * Convert the specified entity into a pojo according to the entity's kind.
   *
   * @param entity the entity to convert from
   * @return an pojo instance of the enitity's kind converted from the entity
   */
  @SuppressWarnings("unchecked")
  public static <T> T entityTo(Entity entity) {
    if (entity.getKind().equals("experiment")) {
      return (T) entityTo(entity, Experiment.class);
    }

    throw new UnsupportedOperationException("entityTo1(" + entity.getKind() + ")");
  }

  /**
   * Converts the specified entity into a pojo of the specified type
   *
   * @param entity the entity to convert from
   * @param type the type of pojo to convert to
   * @return a pojo instance of the specified type converted from the entity
   */
  @SuppressWarnings("unchecked")
  public static <T> T entityTo(Entity entity, Class<T> type) {
    if (Experiment.class == type) {
      return (T) entityToExperiment(entity);
    }

    throw new UnsupportedOperationException("entityTo2(" + type.toString() + ")");
  }

  /**
   * Convert an entity into an experiment. The magic is here we store some fields
   * natively in the datastore when performance is necessary, but also store the
   * whole experiment as a json object. Natively stored fields are authoritative.
   *
   * @param entity an experiment entity
   * @return an experiment initialized from the datastore
   */
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

  /**
   * Converts the specified pojo into a datastore entity, if such a conversion
   * exists.
   *
   * @param object the pojo to convert
   * @return an entity converted from the pojo
   */
  public static <T> Entity toEntity(T object) {
    if (object == null) {
      return null;
    }

    return toEntity(object, object.getClass());
  }

  /**
   * Converts the specified pojo into a datastore entity according to the
   * specified type.
   *
   * @param object the pojo to convert
   * @param type the type of entity to convert the pojo to
   * @return an entity converted from the pojo accorinding to the specified type
   */
  public static <T> Entity toEntity(T object, Class<? extends Object> type) {
    if (object == null || type == null) {
      return null;
    }

    if (Experiment.class == type) {
      return experimentToEntity((Experiment) object);
    }

    throw new UnsupportedOperationException("toEntity2(" + type.toString() + ")");
  }

  /**
   * Converts the specified experiment into an entity. As above, some properties
   * are stored natively for performance reasons. These native properties are 
   * authoritative, since the whole experiment is jsonifed and stored as a blob.
   *
   * @param experiment the experiment to convert
   * @return an entity representing the experiment
   */
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

  /**
   * Generically converts the specified PreparedQuery into a list of pojos of
   * the specified type.
   *
   * @param pq the preparedquery to execute
   * @param type the type of pojo to conver to
   * @return a list of pojos of the specified type convered from the entities returned by the prepared query.
   */
  public static <T> List<T> preparedQueryTo(PreparedQuery pq, Class<T> type) {
    List<Entity> entities = pq.asList(FetchOptions.Builder.withDefaults());
    return PacoConverter.entitiesTo(entities, type);
  }
}
