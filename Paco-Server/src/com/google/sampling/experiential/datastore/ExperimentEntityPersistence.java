package com.google.sampling.experiential.datastore;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.server.DAOConverter;

public class ExperimentEntityPersistence {

  public static final Logger log = Logger.getLogger(ExperimentEntityPersistence.class.getName());

  public static String EXPERIMENT_KIND = "experimentv3";
  private static final String ADMIN_KIND = "experiment_admins";
  private static final String PUBLISHED_KIND = "experiment_published_users";

  private static final String EXPERIMENT_TITLE_COLUMN = "title";
  private static final String EXPERIMENT_DEFINITION_COLUMN = "definition";

  private static final String WHO_COLUMN = "who";

  public static Key saveExperiment(ExperimentDAO experimentDAO) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction txn = ds.beginTransaction();
    try {
      Entity experimentEntity = null;
      if (experimentDAO.getId() != null) {
        try {
          experimentEntity = ds.get(KeyFactory.createKey(EXPERIMENT_KIND, experimentDAO.getId()));
        } catch (EntityNotFoundException e) {
          e.printStackTrace();
          throw new IllegalStateException("Could not retrieve experiment for id: " + experimentDAO.getId());
        }
      } else {
        experimentEntity = new Entity(EXPERIMENT_KIND);
      }
      experimentEntity.setProperty(EXPERIMENT_TITLE_COLUMN, experimentDAO.getTitle());

      Text experimentJson = new Text(JsonConverter.jsonify(experimentDAO));
      experimentEntity.setUnindexedProperty(EXPERIMENT_DEFINITION_COLUMN, experimentJson);
      Key experimentKey = ds.put(experimentEntity);
      experimentDAO.setId(experimentKey.getId());

      removeExistingForAncestor(ds, experimentKey, ADMIN_KIND);
      String[] admins = experimentDAO.getAdmins();

      for (int i = 0; i < admins.length; i++) {
        Entity adminExperimentEntity = new Entity(ADMIN_KIND, experimentKey);
        adminExperimentEntity.setProperty(WHO_COLUMN, admins[i]);
        ds.put(adminExperimentEntity);
      }

      removeExistingForAncestor(ds, experimentKey, PUBLISHED_KIND);
      if (experimentDAO.getPublished() != null && experimentDAO.getPublished()) {
        String[] publishedUsers = experimentDAO.getPublishedUsers();
        for (int i = 0; i < publishedUsers.length; i++) {
          Entity publishedEntity = new Entity(PUBLISHED_KIND, experimentKey);
          publishedEntity.setProperty(WHO_COLUMN, publishedUsers[i]);
          ds.put(publishedEntity);
        }
      }
      txn.commit();
      return experimentKey;
    } finally {
      if (txn.isActive()) {
          txn.rollback();
      }
    }
  }

  public static void removeExistingForAncestor(DatastoreService ds, Key ancestorKey, String kind) {
    Iterable<Entity> existingPublishedUsers = ds.prepare(new Query(kind, ancestorKey)).asIterable();
    List<Key> keys = Lists.newArrayList();
    for (Entity entity : existingPublishedUsers) {
      keys.add(entity.getKey());
    }
    ds.delete(keys);
  }

  public static Key saveExperiment(Experiment experiment) {
    return saveExperiment(DAOConverter.createDAO(experiment));
  }

  public static List<ExperimentDAO> getExperimentsPublishedTo(String loggedInUserEmail) {
    return getExperimentsForPersonFromKind(loggedInUserEmail, PUBLISHED_KIND);
  }

  public static List<ExperimentDAO> getExperimentsAdministeredBy(String loggedInUserEmail) {
    return getExperimentsForPersonFromKind(loggedInUserEmail, ADMIN_KIND);
  }

  public static List<ExperimentDAO> getExperimentsForPersonFromKind(String loggedInUserEmail, String targetKind) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(targetKind);
    query.addFilter(WHO_COLUMN, FilterOperator.EQUAL, loggedInUserEmail);
    QueryResultIterable<Entity> result = ds.prepare(query).asQueryResultIterable();
    List<Key> experimentKeys = Lists.newArrayList();
    for (Entity adminEntity : result) {
      experimentKeys.add(adminEntity.getParent());
    }

    List<Entity> experimentEntities = getExperimentsByKeys(ds, experimentKeys);
    return getExperimentDAOsFromEntities(experimentEntities);
  }

  public static List<ExperimentDAO> getExperimentDAOsFromEntities(List<Entity> experimentEntities) {
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (Entity entity : experimentEntities) {
      ExperimentDAO experimentDAO = JsonConverter.fromSingleEntityJson(((Text)entity.getProperty(EXPERIMENT_DEFINITION_COLUMN)).getValue());
      experiments.add(experimentDAO);
    }
    return experiments;
  }

  public static List<Entity> getExperimentsByKeys(DatastoreService ds, List<Key> experimentKeys) {
    List<Entity> experimentEntities = Lists.newArrayList();
    Map<Key, Entity> experimentEntityMap = ds.get(experimentKeys);
    for (Entity experimentEntity : experimentEntityMap.values()) {
      experimentEntities.add(experimentEntity);
    }
    return experimentEntities;
  }

  public static ExperimentDAO getExperimentById(long id) {
    Key experimentKey = KeyFactory.createKey(EXPERIMENT_KIND, id);
    return getExperiment(experimentKey);
  }

  public static ExperimentDAO getExperiment(Key experimentKey) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    try {
      Entity entity = ds.get(experimentKey);
      if (entity != null) {
        return JsonConverter.fromSingleEntityJson(((Text)entity.getProperty(EXPERIMENT_DEFINITION_COLUMN)).getValue());
      }
    } catch (EntityNotFoundException e) {
    }
    return null;
  }

}
