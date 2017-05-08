package com.google.sampling.experiential.datastore;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ExperimentHelper.Pair;

public class ExperimentJsonEntityManager {
  public static String EXPERIMENT_KIND = "Experiment"; // we are updating the
                                                       // existing table with
                                                       // these properties

  public static final String TITLE_COLUMN = "title";
  private static final String VERSION_COLUMN = "version";
  private static final String DEFINITION_COLUMN = "definition";
  public static final String MODIFIED_COLUMN = "modified_date"; // milliseconds
                                                                // (long value)
                                                                // in utc
  public static final String ADMINS_COLUMN = "admin_list";

  public static final Logger log = Logger.getLogger(ExperimentJsonEntityManager.class.getName());

  public static final SortDirection ASCENDING = Query.SortDirection.ASCENDING;
  public static final SortDirection DESCENDING = Query.SortDirection.DESCENDING;

  public static Key saveExperiment(DatastoreService ds, Transaction tx, String experimentJson, Long experimentId,
                                   String experimentTitle, Integer version, Long modifiedDate, List<String> admins) {
    Entity entity = null;

    if (experimentId != null) {
      entity = new Entity(EXPERIMENT_KIND, experimentId);
    } else {
      entity = new Entity(EXPERIMENT_KIND);
    }
    entity.setProperty(TITLE_COLUMN, experimentTitle.toLowerCase());

    entity.setProperty(VERSION_COLUMN, version);

    entity.setProperty(MODIFIED_COLUMN, modifiedDate);
    entity.setProperty(ADMINS_COLUMN, admins);

    Text experimentJsonText = new Text(experimentJson);
    entity.setUnindexedProperty(DEFINITION_COLUMN, experimentJsonText);
    Key key = ds.put(/* tx, */entity);
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
      Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
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
    if (experiment == null) {
      return value; // this is to deal temporarily with migratin testing. TODO
                    // delete
    }
    if (experiment.getId() == null || !experiment.getId().equals(experimentId)) {
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
      log.info("returned experiment list is empty");
      return Lists.newArrayList();
    }
    for (Key entryKey : experimentKeys) {
      Entity experiment = experiments.get(entryKey);
      if (experiment != null) {
        Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
        if (json != null) {
          // TODO just return DAOs don't do the 2x conversion when it is going
          // to become a DAO anyway.
          experimentJsons.add(reapplyIdIfFirstTime(json.getValue(), experiment.getKey().getId()));
        } else {
          log.severe("No json for experiment: " + experiment.getProperty(TITLE_COLUMN));
        }

      }
    }
    return experimentJsons;
  }
  
  

//  public static Pair<List<String>, String> getExperimentsByIdSortedByTitle(List<Long> experimentIds, String cursor) {
//    List<Key> experimentKeys = createKeysForIds(experimentIds);
//
//    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
//    List<String> experimentJsons = Lists.newArrayList();
//
//    Query query = new Query(EXPERIMENT_KIND);
//    Filter experimentIdFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN,
//                                                          experimentKeys);
//    query.setFilter(experimentIdFilter);
//    query.addSort(TITLE_COLUMN, Query.SortDirection.ASCENDING);
//
//    PreparedQuery preparedQuery = ds.prepare(query);
//    FetchOptions options = null;
//    Cursor fromWebSafeString = null;
//    if (!Strings.isNullOrEmpty(cursor) && !"null".equals(cursor)) {
//      fromWebSafeString = Cursor.fromWebSafeString(cursor);
//    }
//    options = getFetchOptions(fromWebSafeString);
//
//    // preparedQuery.countEntities(getFetchOptions(cursor));
//    QueryResultList<Entity> iterable = preparedQuery.asQueryResultList(options);
//
//    log.info("reading retrieved entity jsons");
//    for (Entity experiment : iterable) {
//      Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
//      if (json != null) {
//        experimentJsons.add(reapplyIdIfFirstTime(json.getValue(), experiment.getKey().getId()));
//      }
//    }
//    final Cursor newCursor = iterable.getCursor();
//    String websafeCursor = null;
//    if (newCursor != null) {
//      newCursor.toWebSafeString();
//    }
//
//    log.info("returning experiment jsons");
//    return new Pair<List<String>, String>(experimentJsons, websafeCursor);
//  }

  public static Pair<List<String>, String> getExperimentsByAdminSorted(String admin, Integer limit,
                                                                              String websafeCursor, String sortColumn,
                                                                              String sortOrder) {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<String> experimentJsons = Lists.newArrayList();

    Query query = new Query(EXPERIMENT_KIND);
    Filter adminFilter = new Query.FilterPredicate(ADMINS_COLUMN, FilterOperator.IN,
                                                   Lists.newArrayList(admin.toLowerCase()));
    query.setFilter(adminFilter);

    String sortColumnToUse = TITLE_COLUMN;
    if (sortColumn != null && (sortColumn.equals(TITLE_COLUMN) || sortColumn.equals(MODIFIED_COLUMN))) {
      sortColumnToUse = sortColumn;
    }
    SortDirection sortOrderToUse = Query.SortDirection.ASCENDING;
    if (sortOrder != null && sortOrder.equals("desc")) {
      sortOrderToUse = Query.SortDirection.DESCENDING;
    }
    query.addSort(sortColumnToUse, sortOrderToUse);

    PreparedQuery preparedQuery = ds.prepare(query);
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

    // preparedQuery.countEntities(getFetchOptions(cursor));
    QueryResultList<Entity> iterable = preparedQuery.asQueryResultList(fetchOptions);

    log.info("reading retrieved entity jsons");
    for (Entity experiment : iterable) {
      Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
      if (json != null) {
        experimentJsons.add(reapplyIdIfFirstTime(json.getValue(), experiment.getKey().getId()));
      }
    }
    final Cursor newCursor = iterable.getCursor();
    String nextWebsafeCursor = null;
    if (newCursor != null) {
      nextWebsafeCursor = newCursor.toWebSafeString();
    }

    log.info("returning experiment jsons");
    return new Pair<List<String>, String>(experimentJsons, nextWebsafeCursor);
  }

  public static Pair<String, List<String>> getAllExperiments(String cursor) {
    List<String> entities = Lists.newArrayList();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(EXPERIMENT_KIND);
    PreparedQuery preparedQuery = ds.prepare(query);
    FetchOptions options = null;
    Cursor fromWebSafeString = null;
    if (!Strings.isNullOrEmpty(cursor) && !"null".equals(cursor)) {
      fromWebSafeString = Cursor.fromWebSafeString(cursor);
    }
    options = getFetchOptions(fromWebSafeString);
    // options.chunkSize(2000);

    // preparedQuery.countEntities(getFetchOptions(cursor));
    List<Pair<Long, String>> idJsons = Lists.newArrayList();
    QueryResultList<Entity> iterable = preparedQuery.asQueryResultList(options);
    log.info("reading retrieved entity jsons");
    for (Entity experiment : iterable) {
      Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
      if (json != null) {
        String value = json.getValue();
        idJsons.add(new Pair<Long, String>(experiment.getKey().getId(), value));
      }
    }
    String newCursor = iterable.getCursor().toWebSafeString();

    log.info("repairing any missing ids");
    for (Pair<Long, String> idJson : idJsons) {
      String value = idJson.second;
      value = reapplyIdIfFirstTime(value, idJson.first);
      entities.add(value);
    }
    log.info("returning experiment jsons");
    Pair<String, List<String>> res = new Pair<String, List<String>>(newCursor, entities);
    return res;
  }

  public static FetchOptions getFetchOptions(Cursor cursor) {
    FetchOptions options = null;
    if (cursor != null) {
      options = FetchOptions.Builder.withCursor(cursor);
    } else {
      options = FetchOptions.Builder.withDefaults();
    }
    return options;
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

  public static void delete(DatastoreService ds, Transaction tx, Long experimentId) {
    if (experimentId == null) {
      throw new IllegalArgumentException("no experimentId specified");
    }
    ds.delete(tx, createkeyForId(experimentId));
  }

  public static void delete(DatastoreService ds, Transaction tx, List<Long> experimentIds) {
    if (experimentIds == null || experimentIds.isEmpty()) {
      throw new IllegalArgumentException("no experimentIds specified");
    }
    ds.delete(tx, createkeysForIds(experimentIds));
  }

  private static List<Key> createkeysForIds(List<Long> experimentIds) {
    List<Key> keys = Lists.newArrayList();
    for (Long experimentId : experimentIds) {
      keys.add(createkeyForId(experimentId));
    }
    return keys;
  }

  public static com.pacoapp.paco.shared.util.ExperimentHelper.Pair<List<String>, String> getExperimentsByIdSorted(List<Long> experimentIds,
                                                                                                                  Integer limit,
                                                                                                                  String websafeCursor,
                                                                                                                  String sortColumn,
                                                                                                                  String sortOrder) {
    List<Key> experimentKeys = createKeysForIds(experimentIds);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<String> experimentJsons = Lists.newArrayList();

    Query query = new Query(EXPERIMENT_KIND);

    Filter experimentIdFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN,
                                                        experimentKeys);
    query.setFilter(experimentIdFilter);

    String sortColumnToUse = TITLE_COLUMN;
    if (sortColumn != null && (sortColumn.equals(TITLE_COLUMN) || sortColumn.equals(MODIFIED_COLUMN))) {
      sortColumnToUse = sortColumn;
    }
    SortDirection sortOrderToUse = Query.SortDirection.ASCENDING;
    if (sortOrder != null && sortOrder.equals("desc")) {
      sortOrderToUse = Query.SortDirection.DESCENDING;
    }
    query.addSort(sortColumnToUse, sortOrderToUse);

    PreparedQuery preparedQuery = ds.prepare(query);
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

    // preparedQuery.countEntities(getFetchOptions(cursor));
    QueryResultList<Entity> iterable = preparedQuery.asQueryResultList(fetchOptions);

    log.info("reading retrieved entity jsons");
    for (Entity experiment : iterable) {
      Text json = (Text) experiment.getProperty(DEFINITION_COLUMN);
      if (json != null) {
        experimentJsons.add(reapplyIdIfFirstTime(json.getValue(), experiment.getKey().getId()));
      }
    }
    final Cursor newCursor = iterable.getCursor();
    String nextWebsafeCursor = null;
    if (newCursor != null) {
      nextWebsafeCursor = newCursor.toWebSafeString();
    }

    log.info("returning experiment jsons");
    return new Pair<List<String>, String>(experimentJsons, nextWebsafeCursor);

  }

}
