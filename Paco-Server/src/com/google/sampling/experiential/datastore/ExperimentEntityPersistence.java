package com.google.sampling.experiential.datastore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

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
import com.google.common.collect.Sets;
import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentEntityPersistence {

  public static final Logger log = Logger.getLogger(ExperimentEntityPersistence.class.getName());

  public static String EXPERIMENT_KIND = "experimentv3";
  private static final String ADMIN_KIND = "experiment_admins";
  private static final String PUBLISHED_KIND = "experiment_published_users";

  private static final String EXPERIMENT_TITLE_COLUMN = "title";
  private static final String EXPERIMENT_DEFINITION_COLUMN = "definition";

  private static final String WHO_COLUMN = "who";
  public static final String END_DATE_COLUMN = "end_date";
  public static final String PUBLISHED_TO_ALL_COLUMN = "published_to_all";

  public static void deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
    if (experimentDAO.getId() == null) {
      throw new IllegalStateException("No id for experiment");
    }
    Long experimentId = experimentDAO.getId();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction txn = ds.beginTransaction();
    Entity experimentEntity;
    try {
      experimentEntity = ds.get(KeyFactory.createKey(EXPERIMENT_KIND, experimentDAO.getId()));
      experimentDAO = JsonConverter.fromSingleEntityJson(((Text)experimentEntity.getProperty(EXPERIMENT_DEFINITION_COLUMN)).getValue());
      if (!isUserExperimentAdmin(loggedInUserEmail, experimentDAO)) {
        throw new IllegalArgumentException("User does not have permission to delete experiment");
      }
      Key experimentKey = experimentEntity.getKey();
      removeAdminUsersOfExperiment(ds, experimentKey);
      removePublishedUsersOfExperiment(ds, experimentKey);
      ds.delete(experimentKey);
      txn.commit();
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      throw new IllegalStateException("Could not retrieve experiment for id: " + experimentDAO.getId());
    } finally {
      if (txn.isActive()) {
          txn.rollback();
      }
    }
  }

  public static void removePublishedUsersOfExperiment(DatastoreService ds, Key experimentKey) {
    removeExistingUsersOfKindForExperiment(ds, PUBLISHED_KIND, experimentKey);
  }

  public static void removeAdminUsersOfExperiment(DatastoreService ds, Key experimentKey) {
    removeExistingUsersOfKindForExperiment(ds, ADMIN_KIND, experimentKey);
  }

  public static List<ExperimentDAO> getExperimentsPublishedToAll(DateTimeZone timezone) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(ExperimentEntityPersistence.EXPERIMENT_KIND);
//    query.addFilter(ExperimentEntityPersistence.END_DATE_COLUMN, FilterOperator.GREATER_THAN_OR_EQUAL,
//                    TimeUtil.getNowInUserTzAsString(timezone));
    query.addFilter(ExperimentEntityPersistence.PUBLISHED_TO_ALL_COLUMN, FilterOperator.EQUAL, Boolean.TRUE);
    QueryResultIterable<Entity> published = ds.prepare(query).asQueryResultIterable();
    return ExperimentEntityPersistence.getExperimentDAOsFromEntities(published);
  }

  public static Key saveExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction txn = ds.beginTransaction();
    try {
      Entity experimentEntity = null;
      if (experimentDAO.getId() != null) {
        experimentEntity = retrieveExistingExperimentById(ds, experimentDAO.getId(), loggedInUserEmail);
        ExperimentDAO existingExperimentDAO = createExperimentDAOFromEntity(experimentEntity);
        incrementExperimentVersion(experimentDAO, existingExperimentDAO);
      } else {
        experimentEntity = new Entity(EXPERIMENT_KIND);
        experimentDAO.setVersion(1);
      }
      ensureAdminsIncludesCreator(experimentDAO);
      experimentEntity.setProperty(EXPERIMENT_TITLE_COLUMN, experimentDAO.getTitle());
      boolean publishedToAll = experimentDAO.getPublished() != null
              && experimentDAO.getPublished()
              && experimentDAO.getPublishedUsers().length == 0;

      experimentEntity.setProperty(PUBLISHED_TO_ALL_COLUMN, publishedToAll);

      Text experimentJson = new Text(JsonConverter.jsonify(experimentDAO));
      experimentEntity.setUnindexedProperty(EXPERIMENT_DEFINITION_COLUMN, experimentJson);

      Key experimentKey = ds.put(experimentEntity);
      experimentDAO.setId(experimentKey.getId());

      setExperimentAdmins(ds, experimentKey, experimentDAO.getAdmins());
      setExperimentPublishedUsers(ds, experimentKey, experimentDAO.getPublishedUsers(), experimentDAO.getPublished());

      txn.commit();
      return experimentKey;
    } finally {
      if (txn.isActive()) {
          txn.rollback();
      }
    }
  }

  private static void ensureAdminsIncludesCreator(ExperimentDAO experimentDAO) {
    String creator = experimentDAO.getCreator();
    String[] admins = experimentDAO.getAdmins();
    for (int i = 0; i < admins.length; i++) {
      String admin = admins[i];
      if (admins.equals(creator)) {
        return;
      }
    }
    String[] biggerAdmins = new String[admins.length + 1];
    System.arraycopy(admins, 0, biggerAdmins, 0, admins.length);
    biggerAdmins[admins.length] = creator;
    experimentDAO.setAdmins(biggerAdmins);
  }

  private static void incrementExperimentVersion(ExperimentDAO experimentDAO, ExperimentDAO existingExperimentDAO) {
    Integer existingExperimentVersion = existingExperimentDAO.getVersion();
    if (existingExperimentVersion != null && existingExperimentVersion > experimentDAO.getVersion()) {
      throw new IllegalStateException("Experiment has already been edited!");
    } else {
      experimentDAO.setVersion(existingExperimentVersion != null ? existingExperimentVersion + 1 : 1);
    }
  }

  public static Entity retrieveExistingExperimentById(DatastoreService ds, Long experimentId,
                                                  String loggedInUserEmail) {
    try {
      Entity experimentEntity = ds.get(KeyFactory.createKey(EXPERIMENT_KIND, experimentId));
      ExperimentDAO existingExperiment = createExperimentDAOFromEntity(experimentEntity);
      if (!isUserExperimentAdmin(loggedInUserEmail, existingExperiment)) {
        throw new IllegalArgumentException("User does not have permission to save experiment");
      }
      return experimentEntity;
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
      throw new IllegalStateException("Could not retrieve experiment for id: " + experimentId);
    }
  }

  public static ExperimentDAO createExperimentDAOFromEntity(Entity experimentEntity) {
    ExperimentDAO fromSingleEntityJson = JsonConverter.fromSingleEntityJson(((Text)experimentEntity.getProperty(EXPERIMENT_DEFINITION_COLUMN)).getValue());
    fromSingleEntityJson.setId(experimentEntity.getKey().getId());
    return fromSingleEntityJson;
  }

  public static boolean isUserExperimentAdmin(String loggedInUserEmail, ExperimentDAO existingExperiment) {
    return Lists.newArrayList(existingExperiment.getAdmins()).contains(loggedInUserEmail);
  }

  public static void setExperimentPublishedUsers(DatastoreService ds, Key experimentKey,
                                                 String[] publishedUsers, Boolean published) {
    if (published == null || !published) {
      removePublishedUsersOfExperiment(ds, experimentKey);
    } else {
      List<String> usersToAdd = Lists.newArrayList(publishedUsers);
      Set<Key> keysToDelete = Sets.newHashSet();

      Iterable<Entity> existingUsers = ds.prepare(new Query(PUBLISHED_KIND, experimentKey)).asIterable();
      for (Entity existing : existingUsers) {
        String existingWho = (String)existing.getProperty(WHO_COLUMN);
        if (!usersToAdd.remove(existingWho)) {
          keysToDelete.add(existing.getKey());
        }
      }
      if (keysToDelete.size() > 0) {
        ds.delete(keysToDelete);
      }
      addUsersOfKindToExperiment(ds, usersToAdd, PUBLISHED_KIND, experimentKey);
    }
  }

  public static void setExperimentAdmins(DatastoreService ds, Key experimentKey, String[] admins) {

    List<String> adminsToAdd = Lists.newArrayList(admins);
    Set<Key> keysToDelete = Sets.newHashSet();

    Iterable<Entity> existingUsers = ds.prepare(new Query(ADMIN_KIND, experimentKey)).asIterable();
    for (Entity existingEntity : existingUsers) {
      String existingWho = (String)existingEntity.getProperty(WHO_COLUMN);
      if (!adminsToAdd.remove(existingWho)) {
        keysToDelete.add(existingEntity.getKey());
      }
    }
    if (keysToDelete.size() > 0) {
      ds.delete(keysToDelete);
    }
    addUsersOfKindToExperiment(ds, adminsToAdd, ADMIN_KIND, experimentKey);
  }

  public static void addUsersOfKindToExperiment(DatastoreService ds, List<String> adminsToAdd,
                                                  String kind, Key experimentKey) {
    List<Entity> entities = Lists.newArrayList();
    for (int i = 0; i < adminsToAdd.size(); i++) {
      Entity entity = new Entity(kind, experimentKey);
      entity.setProperty(WHO_COLUMN, adminsToAdd.get(i));
      entities.add(entity);
    }
    ds.put(entities);
  }

  public static void removeExistingUsersOfKindForExperiment(DatastoreService ds, String kind, Key experimentKey) {
    Iterable<Entity> existingPublishedUsers = ds.prepare(new Query(kind, experimentKey)).asIterable();
    List<Key> keys = Lists.newArrayList();
    for (Entity entity : existingPublishedUsers) {
      keys.add(entity.getKey());
    }
    ds.delete(keys);
  }

//  public static Key saveExperiment(Experiment experiment, String loggedInUserEmail) {
//    return saveExperiment(DAOConverter.createDAO(experiment), loggedInUserEmail);
//  }

  public static List<ExperimentDAO> getExperimentsPublishedTo(String loggedInUserEmail) {
    return getExperimentsForPersonFromKind(loggedInUserEmail, PUBLISHED_KIND);
  }

  public static List<ExperimentDAO> getExperimentsAdministeredBy(String loggedInUserEmail) {
    return getExperimentsForPersonFromKind(loggedInUserEmail, ADMIN_KIND);
  }

  public static List<ExperimentDAO> getExperimentsForPersonFromKind(String loggedInUserEmail, String targetKind) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(targetKind);
    query.setKeysOnly();
    query.addFilter(WHO_COLUMN, FilterOperator.EQUAL, loggedInUserEmail);
    QueryResultIterable<Entity> result = ds.prepare(query).asQueryResultIterable();
    List<Key> experimentKeys = Lists.newArrayList();
    for (Entity entity : result) {
      experimentKeys.add(entity.getParent());
    }

    List<Entity> experimentEntities = getExperimentsByKeys(ds, experimentKeys);
    return getExperimentDAOsFromEntities(experimentEntities);
  }

  public static List<ExperimentDAO> getExperimentDAOsFromEntities(Iterable<Entity> experimentEntities) {
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (Entity entity : experimentEntities) {
      ExperimentDAO experimentDAO = createExperimentDAOFromEntity(entity);
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
        return createExperimentDAOFromEntity(entity);
      }
    } catch (EntityNotFoundException e) {
    }
    return null;
  }

  public static List<ExperimentDAO> getExperimentsByIds(List<Long> experimentIds) {
    List<Key> keys = Lists.newArrayList();
    for (Long id : experimentIds) {
      Key experimentKey = KeyFactory.createKey(EXPERIMENT_KIND, id);
      keys.add(experimentKey);
    }
    List<Entity> experimentEntities = getExperimentsByKeys(DatastoreServiceFactory.getDatastoreService(), keys);

    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (Entity entity : experimentEntities) {
      experiments.add(createExperimentDAOFromEntity(entity));
    }
    return experiments;

  }

}
