package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.pacoapp.paco.shared.model2.ExperimentDAO;

public class ExperimentAccessManager {

  private static final String EXPERIMENT_ID = "experimentId";
  private static final String ADMIN_USER_KIND = "admin_user";
  private static final String ADMIN_ID = "admin_id";
  private static final String PUBLISHED_USER_KIND = "published_user";
  private static final String JOINED_USER_KIND = "joined_user";
  private static final String USER_ID = "user_id";
  private static final String JOIN_DATE = "join_date";

  public static boolean isUserAllowedToDeleteExperiment(Long experimentId, String loggedInUserEmail) {
    return isUserAdmin(experimentId, loggedInUserEmail);
  }

  public static boolean isUserAllowedToDeleteExperiments(List<Long> experimentIds, String email) {
    return isUserAdmin(experimentIds,email);
  }


  public static boolean isUserAllowedToSaveExperiment(Long experimentId, String loggedInUserEmail) {
    return experimentId == null || isUserAdmin(experimentId, loggedInUserEmail);
  }

  public static boolean isUserAdmin(Long experimentId, String loggedInUserEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    List<Filter> filters = Lists.newArrayList();
    filters.add(new com.google.appengine.api.datastore.Query.FilterPredicate(EXPERIMENT_ID, FilterOperator.EQUAL, experimentId));
    filters.add(new com.google.appengine.api.datastore.Query.FilterPredicate(ADMIN_ID, FilterOperator.EQUAL, loggedInUserEmail));

    Filter filter = new com.google.appengine.api.datastore.Query.CompositeFilter(CompositeFilterOperator.AND, filters);
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    query.setFilter(filter);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results.size() > 0;
  }

  public static boolean isUserAdmin(List<Long> experimentIds, String loggedInUserEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    List<Filter> filters = Lists.newArrayList();
    filters.add(new com.google.appengine.api.datastore.Query.FilterPredicate(EXPERIMENT_ID, FilterOperator.IN, experimentIds));
    filters.add(new com.google.appengine.api.datastore.Query.FilterPredicate(ADMIN_ID, FilterOperator.EQUAL, loggedInUserEmail));

    Filter filter = new com.google.appengine.api.datastore.Query.CompositeFilter(CompositeFilterOperator.AND, filters);
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    query.setFilter(filter);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results.size() == experimentIds.size();
  }


  public static FetchOptions getFetchOptions() {
    return FetchOptions.Builder.withDefaults();
  }

  public static void deleteAccessControlEntitiesFor(DatastoreService ds, Transaction tx, Long experimentId) {
    Key experimentKey = ExperimentJsonEntityManager.createkeyForId(experimentId);
    deleteAdminTableEntries(tx, ds, experimentKey);
    deleteParticipantTableEntries(tx, ds, experimentKey);
    deletePublicTableEntry(tx, ds, experimentKey);
  }

  public static void deleteAccessControlEntitiesFor(DatastoreService ds, Transaction tx, List<Long> experimentIds) {
    deleteAdminTableEntries(tx, ds, experimentIds);
    deleteParticipantTableEntries(tx, ds, experimentIds);
    deletePublicTableEntries(tx, ds, experimentIds);
  }


  private static void deleteAdminTableEntries(Transaction tx, DatastoreService ds, List<Long> experimentIds) {
    List<Entity> existingAdminList = getExistingAdminsForExperiments(tx, ds, experimentIds, true);
    List<Key> adminKeys = Lists.newArrayList();
    for (Entity entity : existingAdminList) {
      adminKeys.add(entity.getKey());
    }
    ds.delete(tx, adminKeys);


  }

  private static List<Entity> getExistingAdminsForExperiments(Transaction tx, DatastoreService ds,
                                                              List<Long> experimentIds, boolean keysOnly) {
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    if (keysOnly) {
      query.setKeysOnly();
    }
    query.addFilter(EXPERIMENT_ID, FilterOperator.IN, experimentIds);
    PreparedQuery preparedQuery = ds.prepare(query);

    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results;

  }

  private static void deleteParticipantTableEntries(Transaction tx, DatastoreService ds, List<Long> experimentIds) {
    List<Entity> existingParticipantList = getExistingPublishedUsersForExperiments(tx, ds, experimentIds, true);
    List<Key> participantKeys = Lists.newArrayList();
    for (Entity entity : existingParticipantList) {
      participantKeys.add(entity.getKey());
    }
    ds.delete(tx, participantKeys);
  }

  private static void deletePublicTableEntry(Transaction tx, DatastoreService ds, Key experimentKey) {
    PublicExperimentList.deletePublicExperiment(tx, ds, experimentKey);
  }

  private static void deletePublicTableEntries(Transaction tx, DatastoreService ds, List<Long> experimentIds) {
    PublicExperimentList.deletePublicExperiments(tx, ds, experimentIds);
  }


  private static void deleteParticipantTableEntries(Transaction tx, DatastoreService ds, Key experimentKey) {
    List<Entity> existingParticipantList = getExistingPublishedUsersForExperiment(tx, ds, experimentKey, false);
    List<Key> participantKeys = Lists.newArrayList();
    for (Entity entity : existingParticipantList) {
      participantKeys.add(entity.getKey());
    }
    ds.delete(tx, participantKeys);
  }

  private static void deleteAdminTableEntries(Transaction tx, DatastoreService ds, Key experimentKey) {
    List<Entity> existingAdminList = getExistingAdminsForExperiment(tx, ds, experimentKey, false);
    List<Key> adminKeys = Lists.newArrayList();
    for (Entity entity : existingAdminList) {
      adminKeys.add(entity.getKey());
    }
    ds.delete(tx, adminKeys);
  }

  public static void updateAccessControlEntities(DatastoreService ds, Transaction tx, ExperimentDAO experiment, Key experimentKey, DateTimeZone timezone) {
    updateAdminTable(tx, ds, experiment, experimentKey);
    updateParticipantTable(tx, ds, experiment, experimentKey);
    updatePublicTable(tx, ds, experiment, experimentKey, timezone);
  }

  private static void updatePublicTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey, DateTimeZone timezone) {
    final DateTime now = new DateTime().withZone(timezone);
    PublicExperimentList.updatePublicExperimentsList(tx, ds, experiment, experimentKey, now);
  }

  private static void updateParticipantTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    List<Entity> existingPublishedUserAcls = getExistingPublishedUsersForExperiment(tx, ds, experimentKey, false);
    if (!experiment.getPublished() && existingPublishedUserAcls.isEmpty()) {
      return; // not published and nothing to remove and no reason to add
    } else if (!experiment.getPublished() && existingPublishedUserAcls.size() > 0) {
      List<Key> aclsToBeRemoved = Lists.newArrayList();
      for (Entity existing : existingPublishedUserAcls) {
        aclsToBeRemoved.add(existing.getKey());
      }
      removePublishedUserAcls(tx, ds, aclsToBeRemoved);
      return;
    } else {
      List<String> newPublishedUsers = experiment.getPublishedUsers();
      if (!existingPublishedUserAcls.isEmpty()) {
        List<Key> aclsToBeRemoved = Lists.newArrayList();
        for (Entity entity : existingPublishedUserAcls) {
          String existingUserEmail = (String) entity.getProperty(USER_ID);
          if (!newPublishedUsers.contains(existingUserEmail)) {
            aclsToBeRemoved.add(entity.getKey());
          } else {
            newPublishedUsers.remove(existingUserEmail);
          }
        }
        removePublishedUserAcls(tx, ds, aclsToBeRemoved);
      }
      addPublishedUserAcls(tx, ds, newPublishedUsers, experimentKey.getId());
    }
  }

  public static void addPublishedUserAcls(Transaction tx, DatastoreService ds, List<String> newPublishedList,
                                         final long experimentId) {
    List<Entity> newPublishedAcls = Lists.newArrayList();
    for (String admin : newPublishedList) {
      Entity userAccess = createPublishedUserAclEntity(experimentId, admin);
      newPublishedAcls.add(userAccess);
    }
    if (!newPublishedAcls.isEmpty()) {
      ds.put(tx, newPublishedAcls);
    }
  }

  public static Entity createPublishedUserAclEntity(final long experimentId, String admin) {
    Entity userAccess = new Entity(PUBLISHED_USER_KIND);
    userAccess.setProperty(EXPERIMENT_ID, experimentId);
    userAccess.setProperty(USER_ID, admin);
    return userAccess;
  }

  public static void removePublishedUserAcls(Transaction tx, DatastoreService ds, List<Key> toBeRemovedList) {
    if (!toBeRemovedList.isEmpty()) {
      ds.delete(tx, toBeRemovedList);
    }
  }

  private static void updateAdminTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    String creator = experiment.getCreator();
    List<String> newAdminList = experiment.getAdmins();
    if (!newAdminList.contains(creator)) {
      newAdminList.add(creator);
    }


    List<Entity> existingAdminList = getExistingAdminsForExperiment(tx, ds, experimentKey, false);
    List<Key> toBeRemovedList = Lists.newArrayList();
    List<String> notToBeModified = Lists.newArrayList();

    for (Entity entity : existingAdminList) {
      String adminName = (String) entity.getProperty(ADMIN_ID);
      if (!newAdminList.contains(adminName)) {
        toBeRemovedList.add(entity.getKey());
      } else {
        notToBeModified.add(adminName);
      }
    }
    // build add list
    newAdminList.removeAll(notToBeModified);

    // add new admins
    List<Entity> adminAccessRules = Lists.newArrayList();
    for (String admin : newAdminList) {
      Entity adminAccess = createAdminAcl(experimentKey, admin);
      adminAccessRules.add(adminAccess);
    }
    if (!adminAccessRules.isEmpty()) {
      ds.put(tx, adminAccessRules);
    }
    removePublishedUserAcls(tx, ds, toBeRemovedList);
  }

  public static Entity createAdminAcl(Key experimentKey, String admin) {
    Entity adminAccess = new Entity(ADMIN_USER_KIND);
    adminAccess.setProperty(EXPERIMENT_ID, experimentKey.getId());
    adminAccess.setProperty(ADMIN_ID, admin);
    return adminAccess;
  }

  private static List<Entity> getExistingAdminsForExperiment(Transaction tx, DatastoreService ds, Key experimentKey, boolean keysOnly) {
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    if (keysOnly) {
      query.setKeysOnly();
    }
    query.addFilter(EXPERIMENT_ID, FilterOperator.EQUAL, experimentKey.getId());
    PreparedQuery preparedQuery = ds.prepare(query);

    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results;
  }

  public static List<Long> getExistingExperimentsIdsForAdmin(String adminEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    query.addFilter(ADMIN_ID, FilterOperator.EQUAL, adminEmail);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    List<Long> keys = Lists.newArrayList();
    for (Entity entity : results) {
      keys.add((Long) entity.getProperty(EXPERIMENT_ID));
    }
    return keys;
  }

  public static List<Long> getExistingPublishedExperimentIdsForUser(String userEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(PUBLISHED_USER_KIND);
    query.addFilter(USER_ID, FilterOperator.EQUAL, userEmail);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    List<Long> keys = Lists.newArrayList();
    for (Entity entity : results) {
      keys.add((Long) entity.getProperty(EXPERIMENT_ID));
    }
    return keys;
  }

  private static List<Entity> getExistingPublishedUsersForExperiment(Transaction tx, DatastoreService ds, Key experimentKey, boolean keysOnly) {
    Query query = new com.google.appengine.api.datastore.Query(PUBLISHED_USER_KIND);
    if (keysOnly) {
      query.setKeysOnly();
    }
    query.addFilter(EXPERIMENT_ID, FilterOperator.EQUAL, experimentKey.getId());
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results;
  }

  private static List<Entity> getExistingPublishedUsersForExperiments(Transaction tx, DatastoreService ds, List<Long> experimentIds, boolean keysOnly) {
    Query query = new com.google.appengine.api.datastore.Query(PUBLISHED_USER_KIND);
    if (keysOnly) {
      query.setKeysOnly();
    }
    query.addFilter(EXPERIMENT_ID, FilterOperator.IN, experimentIds);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return results;
  }


  public static boolean isUserAllowedToGetExperiments(Long experimentId, String email) {
    if (isAdminForExperiment(email, experimentId) || isExperimentPublishedToUser(email, experimentId)
        || isPublicExperiment(experimentId)) {
      return true;
    }
    return false;
  }


  private static boolean isPublicExperiment(Long experimentId) {
    return PublicExperimentList.isPublicExperiment(experimentId);
  }

  private static boolean isExperimentPublishedToUser(String email, Long experimentId) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(PUBLISHED_USER_KIND);
    query.addFilter(USER_ID, FilterOperator.EQUAL, email);
    query.addFilter(EXPERIMENT_ID, FilterOperator.EQUAL, experimentId);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return !results.isEmpty();
  }

  static boolean isAdminForExperiment(String email, Long experimentId) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
    query.addFilter(ADMIN_ID, FilterOperator.EQUAL, email);
    query.addFilter(EXPERIMENT_ID, FilterOperator.EQUAL, experimentId);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    return !results.isEmpty();
  }

  public static List<Pair<Long, Date>> getJoinedExperimentsFor(String loggedInUserEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(JOINED_USER_KIND);
    query.addFilter(USER_ID, FilterOperator.EQUAL, loggedInUserEmail);
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> results = preparedQuery.asList(getFetchOptions());
    List<Pair<Long, Date>> keys = Lists.newArrayList();
    for (Entity entity : results) {
      keys.add(new Pair(entity.getProperty(EXPERIMENT_ID), entity.getProperty(JOIN_DATE)));
    }

    return keys;
  }

  public static Entity createUserJoinedAclEntity(final long experimentId, String admin, Date joinDate) {
    Entity userAccess = new Entity(JOINED_USER_KIND);
    userAccess.setProperty(EXPERIMENT_ID, experimentId);
    userAccess.setProperty(USER_ID, admin);
    userAccess.setProperty(JOIN_DATE, joinDate);
    return userAccess;
  }

  /**
   * this method is expressly for upgrading the algorithm from identifying unique experiment ids in events to
   * using the ExperimentAccessManager table.
   *
   * @param loggedInUserEmail
   * @param idDatePairs
   */
  public static void addJoinedExperimentsFor(String loggedInUserEmail, List<Pair<Long, Date>> idDatePairs) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = null;
    try {
      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      tx = ds.beginTransaction(options);
      List<Entity> newJoinedAcls = Lists.newArrayList();
      for (Pair<Long, Date> pair : idDatePairs) {
        Entity userAccess = createUserJoinedAclEntity(pair.first, loggedInUserEmail, pair.second);
        newJoinedAcls.add(userAccess);
      }
      if (!newJoinedAcls.isEmpty()) {
        ds.put(tx, newJoinedAcls);
        tx.commit();
      }
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
    }
  }

  public static void addJoinedExperimentFor(String loggedInUserEmail, Long experimentId, Date joinDate) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = null;
    try {
      tx = ds.beginTransaction();
      addJoinedExperiment(ds, tx, loggedInUserEmail, experimentId, joinDate);
      tx.commit();
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
    }
  }

  public static void addJoinedExperiment(DatastoreService ds, Transaction tx, String loggedInUserEmail,
                                         Long experimentId, Date joinDate) {
    Entity userAccess = createUserJoinedAclEntity(experimentId, loggedInUserEmail, joinDate);
    ds.put(tx, userAccess);
  }

}