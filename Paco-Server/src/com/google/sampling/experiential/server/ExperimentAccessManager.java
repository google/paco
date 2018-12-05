package com.google.sampling.experiential.server;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.Cursor;
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
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentIdQueryResult;
import com.pacoapp.paco.shared.model2.ExperimentJoinQueryResult;
import com.pacoapp.paco.shared.model2.Pair;

public class ExperimentAccessManager {

  private static Logger log = Logger.getLogger(ExperimentAccessManager.class.getName());
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
    filters.add(new com.google.appengine.api.datastore.Query.FilterPredicate(ADMIN_ID, FilterOperator.EQUAL, loggedInUserEmail.toLowerCase()));

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
    List<Entity> existingParticipantList = getExistingPublishedUsersForExperiment(/*tx,*/ ds, experimentKey, false);
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
    updateParticipantTable(/*tx,*/ ds, experiment, experimentKey);
    updatePublicTable(/*tx,*/ ds, experiment, experimentKey, timezone);
  }

  private static void updatePublicTable(/*Transaction tx, */DatastoreService ds, ExperimentDAO experiment, Key experimentKey, DateTimeZone timezone) {
    final DateTime now = new DateTime().withZone(timezone);
    PublicExperimentList.updatePublicExperimentsList(/*tx,*/ ds, experiment, experimentKey, now);
  }

  private static void updateParticipantTable(/*Transaction tx, */DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    List<Entity> existingPublishedUserAcls = getExistingPublishedUsersForExperiment(/*tx,*/ ds, experimentKey, false);
    if (!experiment.getPublished() && existingPublishedUserAcls.isEmpty()) {
      return; // not published and nothing to remove and no reason to add
    } else if (!experiment.getPublished() && existingPublishedUserAcls.size() > 0) {
      List<Key> aclsToBeRemoved = Lists.newArrayList();
      for (Entity existing : existingPublishedUserAcls) {
        aclsToBeRemoved.add(existing.getKey());
      }
      removePublishedUserAcls(/*tx,*/ ds, aclsToBeRemoved);
      return;
    } else {
      List<String> newPublishedUsersMixedCase = experiment.getPublishedUsers();
      List<String> newPublishedUsers = lowerCaseEmails(newPublishedUsersMixedCase);
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
        removePublishedUserAcls(/*tx,*/ ds, aclsToBeRemoved);
      }
      addPublishedUserAcls(/*tx,*/ ds, newPublishedUsers, experimentKey.getId());
    }
  }

  public static void addPublishedUserAcls(/*Transaction tx2,*/ DatastoreService ds, List<String> newPublishedList,
                                         final long experimentId) {
//    List<Entity> newPublishedAcls = Lists.newArrayList();
//    for (String admin : newPublishedList) {
//      Entity userAccess = createPublishedUserAclEntity(experimentId, admin);
//      newPublishedAcls.add(userAccess);
//    }
//    if (!newPublishedAcls.isEmpty()) {
//      ds.put(/*tx,*/ newPublishedAcls);
//    }

    int startPosition = 0;
    int totalCount = newPublishedList.size();
    log.info("AddPublishedUsers. Count: " + newPublishedList.size());
    final int batchsize = 5;
    while (startPosition < totalCount) {
      int fullrangeLeft = totalCount - startPosition;
      int nextBucketSize = Math.min(batchsize, fullrangeLeft);

      List<String> subList = newPublishedList.subList(startPosition, startPosition + nextBucketSize);

      Transaction tx = null;
      try {
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        tx = ds.beginTransaction(options);
        List<Entity> newJoinedAcls = Lists.newArrayList();
        for (String admin : subList) {
          Entity userAccess = createPublishedUserAclEntity(experimentId, admin);
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
      startPosition = startPosition + nextBucketSize;
    }

  }

  public static Entity createPublishedUserAclEntity(final long experimentId, String admin) {
    Entity userAccess = new Entity(PUBLISHED_USER_KIND);
    userAccess.setProperty(EXPERIMENT_ID, experimentId);
    userAccess.setProperty(USER_ID, admin);
    return userAccess;
  }

  public static void removePublishedUserAcls(/*Transaction tx,*/ DatastoreService ds, List<Key> toBeRemovedList) {
    if (!toBeRemovedList.isEmpty()) {
      ds.delete(/*tx,*/ toBeRemovedList);
    }
  }

  private static void updateAdminTable(Transaction tx, DatastoreService ds, ExperimentDAO experiment, Key experimentKey) {
    String creator = experiment.getCreator();
    List<String> newAdminListMixedCase = experiment.getAdmins();
    if (!newAdminListMixedCase.contains(creator)) {
      newAdminListMixedCase.add(creator);
    }
    List<String> newAdminList = lowerCaseEmails(newAdminListMixedCase);


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
      ds.put(/*tx,*/ adminAccessRules);
    }
    removePublishedUserAcls(/*tx,*/ ds, toBeRemovedList);
  }

  public static List<String> lowerCaseEmails(List<String> emailListMixedCase) {
    List<String> newAdminList = Lists.newArrayList();
    for (String email : emailListMixedCase) {
      newAdminList.add(email.toLowerCase());
    }
    return newAdminList;
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


  public static ExperimentIdQueryResult getExistingPublishedExperimentIdsForUser(String userEmail, int limit, String websafeCursor) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new com.google.appengine.api.datastore.Query(PUBLISHED_USER_KIND);
    query.addFilter(USER_ID, FilterOperator.EQUAL, userEmail);
    return getIdsMatchingQuery(query, limit, websafeCursor);

  }

  private static List<Entity> getExistingPublishedUsersForExperiment(/*Transaction tx,*/ DatastoreService ds, Key experimentKey, boolean keysOnly) {
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
    if (isPublicExperiment(experimentId) ||
        isAdminForExperiment(email, experimentId) ||
        isExperimentPublishedToUser(email, experimentId)) {
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

  public static ExperimentJoinQueryResult getJoinedExperimentsFor(String loggedInUserEmail, int limit, String websafeCursor) {
    Query query = new com.google.appengine.api.datastore.Query(JOINED_USER_KIND);
    query.addFilter(USER_ID, FilterOperator.EQUAL, loggedInUserEmail);

    if (limit == 0) {
      limit = 1000;
    }
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(limit);

    Cursor cursor = null;
    if (!Strings.isNullOrEmpty(websafeCursor) && !websafeCursor.equals("null")) {
      cursor = Cursor.fromWebSafeString(websafeCursor);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
    }


    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    PreparedQuery pq = datastore.prepare(query);

    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
    List<Pair<Long, Date>> keys = Lists.newArrayList();
    for (Entity entity : results) {
      keys.add(new Pair(entity.getProperty(EXPERIMENT_ID), entity.getProperty(JOIN_DATE)));
    }


    cursor = results.getCursor();
    String nextWebsafeCursor = null;
    if (cursor != null) {
      nextWebsafeCursor = cursor.toWebSafeString();
    }



    return new ExperimentJoinQueryResult(nextWebsafeCursor, keys);
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
  public static void addJoinedExperimentsFor(String loggedInUserEmail, List<Pair<Long, Date>> idDatePairsFull) {

    int startPosition = 0;
    int totalCount = idDatePairsFull.size();
    log.info("AddJoinedExperimentsFor user: " + loggedInUserEmail + ". Count: " + idDatePairsFull.size());
    final int batchsize = 5;
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    while (startPosition < totalCount) {
      int fullrangeLeft = totalCount - startPosition;
      int nextBucketSize = Math.min(batchsize, fullrangeLeft);

      List<Pair<Long, Date>> subList = idDatePairsFull.subList(startPosition, startPosition + nextBucketSize);

      Transaction tx = null;
      try {
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        tx = ds.beginTransaction(options);
        List<Entity> newJoinedAcls = Lists.newArrayList();
        for (Pair<Long, Date> pair : subList) {
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
      startPosition = startPosition + nextBucketSize;
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


  public static ExperimentIdQueryResult getExistingExperimentIdsForAdmin(String adminEmail,
                                             int limit, String websafeCursor) {
    Query query = new Query(ADMIN_USER_KIND);
    query.addFilter(ADMIN_ID, FilterOperator.EQUAL, adminEmail);

    return getIdsMatchingQuery(query, limit, websafeCursor);
  }

  public static ExperimentIdQueryResult getIdsMatchingQuery(Query query, int limit, String websafeCursor) {
    //log.info("getExistingExperimentIdsForAdmin: websafeCursor" + websafeCursor +" limit: " + limit);
    if (limit == 0) {
      limit = 1000;
    }
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(limit);

    Cursor cursor = null;
    if (!Strings.isNullOrEmpty(websafeCursor) && !websafeCursor.equals("null")) {
      cursor = Cursor.fromWebSafeString(websafeCursor);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(query);
    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
    //log.info("getExistingExperimentidsForAdmin: result size: " + results.size());
    List<Long> keys = Lists.newArrayList();
    for (Entity entity : results) {
      keys.add((Long) entity.getProperty(EXPERIMENT_ID));
    }

    cursor = results.getCursor();
    String nextWebsafeCursor = null;
    if (cursor != null) {
      nextWebsafeCursor = cursor.toWebSafeString();
    }

    ExperimentIdQueryResult experimentIdQueryResult = new ExperimentIdQueryResult(nextWebsafeCursor, keys);
    return experimentIdQueryResult;
  }

  /**
   *  Method used by Usage statistics cron job
   *
   * @return total number of joined rows
   */
  public static Long getTotalJoinedParticipantsCount() {
    Query query = new com.google.appengine.api.datastore.Query(JOINED_USER_KIND);
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    final PreparedQuery prepare = datastore.prepare(query);
    int countEntities = prepare.countEntities(fetchOptions);
    if (countEntities > 0) {
      return (long) countEntities;
    } else {
      log.info("retrieved 0 joined entities");
      return 0l;
    }

  }

  static class JoinEvent implements Serializable{
    DateTime date;
    String userId;
    Long experimentId;

    JoinEvent(DateTime date, String userId, Long experimentId) {
      this.date = date;
      this.userId = userId;
      this.experimentId = experimentId;
    }
  }

  static class DateCount implements Serializable{
    String date;
    Integer count;
    public DateCount(String date, Integer count) {
      super();
      this.date = date;
      this.count = count;
    }
    public String getDate() {
      return date;
    }
    public void setDate(String date) {
      this.date = date;
    }
    public Integer getCount() {
      return count;
    }
    public void setCount(Integer count) {
      this.count = count;
    }


  }

  public static List<DateCount>[] getTotalJoinedParticipantsCountsByMonth() {
    Query query = new com.google.appengine.api.datastore.Query(JOINED_USER_KIND);
    FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> joinEntities = Lists.newArrayList();


    QueryResultList<Entity> results = datastore.prepare(query).asQueryResultList(fetchOptions);
    while (!results.isEmpty()) {
      joinEntities.addAll(results);
      Cursor newCursor = results.getCursor();
      if (newCursor != null) {
        results.clear();
        results = datastore.prepare(query).asQueryResultList(fetchOptions.startCursor(newCursor));
      }
    }

    if (joinEntities != null) {
      log.info("Retrieved join entities count: " + joinEntities.size());
      return computeJoinStats(joinEntities);
    } else {
      log.info("retrieved 0 joined entities");
      return null;
    }

  }

  private static List<DateCount>[] computeJoinStats(List<Entity> joinEntities) {
    List<JoinEvent> joinList = buildJoinEventList(joinEntities);
    Map<Integer, Map<Integer, List<JoinEvent>>> countMap = groupByYearAndMonth(joinList);
    List<DateCount> listOfUniqueCountsByYearAndMonth = getUniqueParticipantCountByYearMonth(countMap);
    List<DateCount> listOfCountsByYearAndMonth = getParticipantJoinCountByYearMonth(countMap);
    return  new List[] {listOfCountsByYearAndMonth, listOfUniqueCountsByYearAndMonth};
  }

  private static List<JoinEvent> buildJoinEventList(List<Entity> joinEntities) {
    List<JoinEvent> joinList = Lists.newArrayList();
    for (Entity entity : joinEntities) {
      Date joinDate = (Date)entity.getProperty("join_date");
      String userId = (String)entity.getProperty("user_id");
      Long experimentId = (Long)entity.getProperty("experimentId");
      joinList.add(new JoinEvent(new DateTime(joinDate), userId, experimentId));
    }
    Collections.sort(joinList, new Comparator<JoinEvent>() {

      @Override
      public int compare(JoinEvent o1, JoinEvent o2) {
        return o1.date.compareTo(o2.date);
      }
    });
    return joinList;
  }

  private static Map<Integer, Map<Integer, List<JoinEvent>>> groupByYearAndMonth(List<JoinEvent> joinList) {
    Map<Integer, Map<Integer, List<JoinEvent>>> countMap = Maps.newConcurrentMap();
    for (JoinEvent joinEvent : joinList) {
      int year = joinEvent.date.getYear();
      int month = joinEvent.date.getMonthOfYear();
      Map<Integer, List<JoinEvent>> yearMap = countMap.get(year);
      if (yearMap == null) {
        yearMap = Maps.newConcurrentMap();
        countMap.put(year, yearMap);
      }
      List<JoinEvent> monthCount = yearMap.get(month);
      if (monthCount == null) {
        monthCount = Lists.newArrayList();
      }
      monthCount.add(joinEvent);
      yearMap.put(month, monthCount);
      if (year > 2016) {
        log.info("Future YEAR: " + year);
        log.info("event " + joinEvent.date +", " + joinEvent.userId + ", " + joinEvent.experimentId);

      }
    }
    return countMap;
  }

  /**
   * Note this does not break it out by experiment so we may get duplicate join events within an experiment for a person.
   * If we broke it out by experiment and then unique'ed on each person, then we would just get the unique people joins per experiment.
   * It is just noise as the population grows so not worth the effort.
   *
   * @param countMap
   * @return
   */
  private static List<DateCount> getParticipantJoinCountByYearMonth(Map<Integer, Map<Integer, List<JoinEvent>>> countMap) {
    List<DateCount> listOfCountsByYearAndMonth = Lists.newArrayList();
    List<Integer> yearKeys = Lists.newArrayList(countMap.keySet());
    Collections.sort(yearKeys);
    for (Integer yearKey : yearKeys) {
      Map<Integer, List<JoinEvent>> monthMap = countMap.get(yearKey);
      List<Integer> months = Lists.newArrayList(monthMap.keySet());
      Collections.sort(months);
      for (Integer monthKey : months) {
        final List<JoinEvent> joinEventList = monthMap.get(monthKey);
        int count = joinEventList.size();
        listOfCountsByYearAndMonth.add(new DateCount(yearKey +":" + monthKey, count));
      }
    }
    return listOfCountsByYearAndMonth;
  }

  private static List<DateCount> getUniqueParticipantCountByYearMonth(Map<Integer, Map<Integer, List<JoinEvent>>> countMap) {
    Set<String> lastParticipants = Sets.newConcurrentHashSet();
    List<DateCount> listOfUniqueCountsByYearAndMonth = Lists.newArrayList();
    List<Integer> yearKeysUnique = Lists.newArrayList(countMap.keySet());
    Collections.sort(yearKeysUnique);
    for (Integer yearKey : yearKeysUnique) {
      Map<Integer, List<JoinEvent>> monthMapUnique = countMap.get(yearKey);
      List<Integer> months = Lists.newArrayList(monthMapUnique.keySet());
      Collections.sort(months);
      for (Integer monthKey : months) {
        Set<String> participants = Sets.newConcurrentHashSet();
        List<JoinEvent> participantList = monthMapUnique.get(monthKey);
        for (JoinEvent joinEvent : participantList) {
          participants.add(joinEvent.userId +":" + joinEvent.experimentId);
        }
        int uniqueCountThisMonth = 0;
        if (lastParticipants.size() == 0) {
          uniqueCountThisMonth = participants.size();
          lastParticipants = participants;
        } else {
          SetView<String> diff = Sets.difference(participants, lastParticipants);
          if (diff != null) {
            uniqueCountThisMonth = diff.size();
            lastParticipants.addAll(diff);
          }

        }

        final DateCount dateCount = new DateCount(yearKey +":" + monthKey, uniqueCountThisMonth);

        listOfUniqueCountsByYearAndMonth.add(dateCount);

      }
    }
    return listOfUniqueCountsByYearAndMonth;
  }

}