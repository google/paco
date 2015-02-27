package com.google.sampling.experiential.datastore;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;

public class ExperimentJsonEntityManager {
  public static String EXPERIMENT_KIND = "experiment_json";

  private static final String TITLE_COLUMN = "title";
  private static final String VERSION_COLUMN = "version";
  private static final String DEFINITION_COLUMN = "definition";
  private static String END_DATE_COLUMN = "end_date";

  public static final Logger log = Logger.getLogger(ExperimentJsonEntityManager.class.getName());

//  public static Key saveExperiment(String experimentJson, Long experimentId, String experimentTitle, Integer version) {
//    System.out.println("JSON experiment received:\n " + experimentJson);
//
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    Entity entity = new Entity(EXPERIMENT_KIND);
//    if (experimentId != null) {
//      entity.setProperty("id", experimentId);
//    }
//    entity.setProperty(TITLE_COLUMN, experimentTitle);
//
//    if (version == null || version == 0) {
//      version = 1;
//    }
//    entity.setProperty(VERSION_COLUMN, version);
//
//    Text experimentJsonText = new Text(experimentJson);
//    entity.setUnindexedProperty(DEFINITION_COLUMN, experimentJsonText);
//    Key key = ds.put(entity);
//    return key;
//  }

  public static Key saveExperiment(DatastoreService ds, Transaction tx, String experimentJson, Long experimentId, String experimentTitle, Integer version) {
    System.out.println("JSON experiment received:\n " + experimentJson);
    Entity entity = null;

    if (experimentId != null) {
      entity = new Entity(EXPERIMENT_KIND, experimentId);
    } else {
      entity = new Entity(EXPERIMENT_KIND);
    }
    entity.setProperty(TITLE_COLUMN, experimentTitle);

    if (version == null || version == 0) {
      version = 1;
    }
    entity.setProperty(VERSION_COLUMN, version);

    Text experimentJsonText = new Text(experimentJson);
    entity.setUnindexedProperty(DEFINITION_COLUMN, experimentJsonText);
    Key key = ds.put(tx, entity);
    return key;
  }



  public static String getExperiment(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Must specify experiment entity id");
    }
    Key experimentKey = createkeyForId(id);

    return getExperiment(experimentKey);
  }


  public static String getExperiment(Key experimentKey) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity experiment;

      experiment = ds.get(experimentKey);
      if (experiment == null) {
        return null;
      }
      Text json = (Text)experiment.getProperty(DEFINITION_COLUMN);
      if (json != null) {
        String value = json.getValue();
        value = reapplyIdIfFirstTime(value, experimentKey.getId());
        return value;
      }
      return null;
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }


  private static String reapplyIdIfFirstTime(String value, long experimentId) {
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(value);
    if (experiment.getId() == null || !experiment.getId().equals(experimentId) ) {
      experiment.setId(experimentId);
      return JsonConverter.jsonify(experiment);
    }
    return value;

  }



  public static List<String> getExperimentsById(List<Long> experimentIds) {
    List<Key> experimentKeys = createKeysForIds(experimentIds);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<String> experimentJsons = Lists.newArrayList();
    Map<Key, Entity> experiments = ds.get(experimentKeys);
    if (experiments == null) {
      return Lists.newArrayList();
    }
    for (Entry<Key, Entity> entry : experiments.entrySet()) {
      Entity experiment = entry.getValue();
      Text json = (Text)experiment.getProperty(DEFINITION_COLUMN);
      if (json != null) {
        // TODO just return DAOs don't do the 2x conversion when it is going to become a DAO anyway.
        experimentJsons.add(reapplyIdIfFirstTime(json.getValue(), experiment.getKey().getId()));
      }
    }
    return experimentJsons;
  }


  public static Key createkeyForId(Long id) {
    Key experimentKey = KeyFactory.createKey(EXPERIMENT_KIND, id);
    return experimentKey;
  }



  public static List<Key> createKeysForIds(List<Long> experimentIds) {
    List<Key> experimentKeys = Lists.newArrayList();
    for (Long experimentId : experimentIds) {
      experimentKeys.add(createkeyForId(experimentId));
    }
    return experimentKeys;
  }


  public static Boolean delete(Long experimentId) {
    if (experimentId == null) {
      return false;
    }
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    try {
      ds.delete(createkeyForId(experimentId));
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

}
