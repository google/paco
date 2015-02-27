package com.google.sampling.experiential.server;

import java.util.List;

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
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;

public class ExperimentAccessManager {

  private static final String EXPERIMENT_ID = "experimentId";
  private static final String ADMIN_USER_KIND = "admin_user";
  private static final String ADMIN_ID = "admin_id";

  public static boolean isUserAllowedToDeleteExperiment(Long experimentId, String loggedInUserEmail) {
    return isUserAdmin(experimentId, loggedInUserEmail);
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

  public static FetchOptions getFetchOptions() {
    return FetchOptions.Builder.withDefaults();
  }

  public static boolean deleteAccessControlEntitiesFor(Long experimentId) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Transaction tx = ds.beginTransaction();
    Object experimentKey = ExperimentJsonEntityManager.createkeyForId(experimentId);
    try {
      deleteAdminTableEntries(tx, ds, experimentKey);
      updateParticipantTable(tx, ds, experimentKey);
      updatePublicTable(tx, ds, experimentKey);
      tx.commit();
      return true;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
    }
    return false;
  }

  private static void updatePublicTable(Transaction tx, DatastoreService ds, Object experimentKey) {
    // TODO Auto-generated method stub

  }

  private static void updateParticipantTable(Transaction tx, DatastoreService ds, Object experimentKey) {
    // TODO Auto-generated method stub

  }

  private static void deleteAdminTableEntries(Transaction tx, DatastoreService ds, Object experimentKey) {
    // TODO Auto-generated method stub

  }

  public static boolean updateAccessControlEntities(DatastoreService ds, Transaction tx, ExperimentDAO experiment, Key experimentKey, DateTimeZone timezone) {
    // TODO
    // update admin table, published to table, and public table as necessary (remove old entries for experiment, add new entries for experiment)

      updateAdminTable(tx, ds, experiment, experimentKey);
      updateParticipantTable(tx, ds, experiment, experimentKey);
      updatePublicTable(tx, ds, experiment, experimentKey);
      return true;
  }

  private static void updatePublicTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    // TODO Auto-generated method stub

  }

  private static void updateParticipantTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    // TODO Auto-generated method stub

  }

  private static void updateAdminTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    String creator = experiment.getCreator();
    List<String> newAdminList = experiment.getAdmins();
    if (!newAdminList.contains(creator)) {
      newAdminList.add(creator);
    }


    List<Entity> existingAdminList = getExistingAdminsForExperiment(tx, ds, experimentKey);
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
      Entity adminAccess = new Entity(ADMIN_USER_KIND);
      adminAccess.setProperty(EXPERIMENT_ID, experimentKey.getId());
      adminAccess.setProperty(ADMIN_ID, admin);
      adminAccessRules.add(adminAccess);
    }
    if (!adminAccessRules.isEmpty()) {
      ds.put(tx, adminAccessRules);
    }
    if (!toBeRemovedList.isEmpty()) {
      ds.delete(tx, toBeRemovedList);
    }
  }

  private static List<Entity> getExistingAdminsForExperiment(Transaction tx, DatastoreService ds, Key experimentKey) {
    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
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

//  static List<Entity> getExistingExperimentsForAdmin(String adminEmail) {
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    Query query = new com.google.appengine.api.datastore.Query(ADMIN_USER_KIND);
//    query.addFilter(ADMIN_ID, FilterOperator.EQUAL, adminEmail);
//    PreparedQuery preparedQuery = ds.prepare(query);
//    List<Entity> results = preparedQuery.asList(getFetchOptions());
//    return results;
//  }


//  public static List<Long> getExistingExperimentsForAdminOld(String email) {
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    List<Long> experimentIds = Lists.newArrayList();
//    Key key = KeyFactory.createKey(ADMIN_USER_KIND, email);
//    Query query = new com.google.appengine.api.datastore.Query(key);
//    PreparedQuery preparedQuery = ds.prepare(query);
//    List<Entity> results = preparedQuery.asList(null);
//    for (Entity entity : results) {
//      experimentIds.add((Long) entity.getProperty(EXPERIMENT_ID));
//
//    }
//    return experimentIds;
//  }
//
//  public static List<Long> getExistingExperimentsForAdmin(String email) {
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    List<Long> experimentIds = Lists.newArrayList();
//    Key key = KeyFactory.createKey(ADMIN_USER_KIND, email);
//
//    Filter keyFilter =
//            new FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
//                                FilterOperator.EQUAL,
//                                key);
//          Query q =  new Query(ADMIN_USER_KIND).setFilter(keyFilter);
//    Query query = new com.google.appengine.api.datastore.Query(key);
//    PreparedQuery preparedQuery = ds.prepare(query);
//    List<Entity> results = preparedQuery.asList(null);
//    for (Entity entity : results) {
//      experimentIds.add((Long) entity.getProperty(EXPERIMENT_ID));
//
//    }
//    return experimentIds;
//  }
//

}