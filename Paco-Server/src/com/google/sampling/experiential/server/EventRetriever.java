/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.codec.binary.Base64;
import org.datanucleus.store.appengine.query.JDOCursorHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.datastore.EventEntityConverter;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.migration.MigrationOutput;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.util.ErrorMessages;

/**
 * Retrieve Event objects from the JDO store.
 *
 * @author Bob Evans
 *
 */
public class EventRetriever {

  private static final int DEFAULT_FETCH_LIMIT = 20000;
  private static EventRetriever instance;
  private static final Logger log = Logger.getLogger(EventRetriever.class.getName());
  private static CloudSQLDao cloudSqlDaoImpl = new CloudSQLDaoImpl();
  private static DateTimeFormatter df = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();
  private static int NULL_CTR = 0;

  @VisibleForTesting
  EventRetriever() {
  }

  public static synchronized EventRetriever getInstance() {
    if (instance == null) {
      instance = new EventRetriever();
    }
    return instance;
  }

  public void postEvent(boolean persistInCloudSqlOnly, JSONObject eventJson, String who, String lat, String lon,
                        Date whenDate, String appId, String pacoVersion, Set<What> whats, boolean shared,
                        String experimentId, String experimentName, Integer experimentVersion, DateTime responseTime,
                        DateTime scheduledTime, List<PhotoBlob> blobs, String groupName, Long actionTriggerId,
                        Long actionTriggerSpecId, Long actionId) {
    final String tz = responseTime != null
                      && responseTime.getZone() != null ? responseTime.getZone().toString()
                                                        : scheduledTime != null
                                                          && scheduledTime.getZone() != null ? scheduledTime.getZone()
                                                                                                            .toString()
                                                                                             : null;

    postEvent(persistInCloudSqlOnly, eventJson, who, lat, lon, whenDate, appId, pacoVersion, whats, shared,
              experimentId, experimentName, experimentVersion, responseTime != null ? responseTime.toDate() : null,
              scheduledTime != null ? scheduledTime.toDate() : null, blobs, tz, groupName, actionTriggerId,
              actionTriggerSpecId, actionId);

  }

  public void postEvent(String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                        Set<What> whats, boolean shared, String experimentId, String experimentName,
                        Integer experimentVersion, DateTime responseTime, DateTime scheduledTime, List<PhotoBlob> blobs,
                        String groupName, Long actionTriggerId, Long actionTriggerSpecId, Long actionId) {

    postEvent(true, null, who, lat, lon, whenDate, appId, pacoVersion, whats, shared, experimentId, experimentName,
              experimentVersion, responseTime, scheduledTime, blobs, groupName, actionTriggerId, actionTriggerSpecId,
              actionId);

  }

  public void postEvent(boolean persistInCloudSqlOnly, JSONObject eventJson, String who, String lat, String lon,
                        Date whenDate, String appId, String pacoVersion, Set<What> what, boolean shared,
                        String experimentId, String experimentName, Integer experimentVersion, Date responseTime,
                        Date scheduledTime, List<PhotoBlob> blobs, String tz, String groupName, Long actionTriggerId,
                        Long actionTriggerSpecId, Long actionId) {

    boolean isJoinEvent = false;
    for (What whatItem : what) {
      if (whatItem.getName().toLowerCase().equals("joined") && whatItem.getValue().equals("true")) {
        isJoinEvent = true;
      }
    }
    boolean isStopEvent = false;
    for (What whatItem : what) {
      if (whatItem.getName().toLowerCase().equals("joined") && whatItem.getValue().equals("false")) {
        isStopEvent = true;
      }
    }
    boolean isScheduleEvent = false;
    for (What whatItem : what) {
      if (whatItem.getName().toLowerCase().equals("schedule") && !Strings.isNullOrEmpty(whatItem.getValue())) {
        isScheduleEvent = true;
      }
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Event event = new Event(who, lat, lon, whenDate, appId, pacoVersion, what, shared, experimentId, experimentName,
                            experimentVersion, responseTime, scheduledTime, blobs, tz, groupName, actionTriggerId,
                            actionTriggerSpecId, actionId);
    // persistInCloudSql flag will determine which flow to go. Flow 1:persist in
    // data store and send event to the cloud sql queue
    // Flow 2: persist in cloud sql
    if (persistInCloudSqlOnly && eventJson != null) {
      try {
        event.setId(eventJson.getLong("id"));
        event.setPacoVersion(eventJson.getString("pacoVersion"));
        event.setAppId(eventJson.getString("appId"));
        event.setTimeZone(eventJson.getString("tz"));
        event.setWhen(df.parseDateTime(eventJson.getString("whenDate")).toDate());
        event.setWho(eventJson.getString("who"));
        cloudSqlDaoImpl.insertEvent(event);
      } catch (JSONException e) {
        log.info(ErrorMessages.JSON_EXCEPTION + " for request: " + eventJson);
      } catch (SQLException sqle) {
        log.info(ErrorMessages.SQL_INSERT_EXCEPTION + " for  request: " + eventJson);
      } catch (ParseException e) {
        log.warning(ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + "for request: " +eventJson);
      } catch (Exception e) { 
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + "for request: " +eventJson + e);
      }

    } else {

      Transaction tx = null;
      try {
        tx = pm.currentTransaction();
        tx.begin();
        pm.makePersistent(event);
        event.setWhat(what);
        if (isJoinEvent) {
          ExperimentAccessManager.addJoinedExperimentFor(who, Long.valueOf(experimentId), responseTime);
        } else if (!isScheduleEvent && !isStopEvent) {
          new ParticipationStatsService().updateResponseCountWithEvent(event);
        }
        sendToCloudSqlQueue(eventJson, event);
        tx.commit();
        log.info("Event saved in datastore");
      } finally {
        if (tx.isActive()) {
          log.info("Event rolled back");
          tx.rollback();
        }
        pm.close();
      }
    }
  }

  public void sendToCloudSqlQueue(JSONObject eventJson, Event event) {
    DateTimeFormatter fmt = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT);
    Queue queue = QueueFactory.getQueue("cloud-sql");
    try {
      // In the flow of saving event data to data store, pacoversion and appid
      // comes in request header.
      // Adding these values to json, so that we can persist this data on to
      // cloud sql too.
      eventJson.put("id", event.getId());
      eventJson.put("pacoVersion", event.getPacoVersion());
      eventJson.put("appId", event.getAppId());
      eventJson.put("tz", event.getTimeZone());
      DateTime whenDate = new DateTime(event.getWhen());
      eventJson.put("whenDate", fmt.print(whenDate));
      eventJson.put("who", event.getWho());
    } catch (JSONException e) {
      log.severe("while sending to cloud sql queue" + e);
    }
    queue.add(TaskOptions.Builder.withUrl("/csInsert").payload(eventJson.toString()));
  }

  @SuppressWarnings("unchecked")
  public List<Event> getEvents(String loggedInUser) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(Event.class);
    q.setOrdering("when desc");
    q.setFilter("who == whoParam");
    q.declareParameters("String whoParam");
    long t11 = System.currentTimeMillis();
    List<Event> events = (List<Event>) q.execute(loggedInUser);
    adjustTimeZone(events);
    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));
    return events;
  }

  public List<Event> getEvents(List<com.google.sampling.experiential.server.Query> queryFilters, String loggedInuser,
                               DateTimeZone clientTimeZone, int offset, int limit) {
    if (limit == 0) {
      limit = DEFAULT_FETCH_LIMIT;
    }

    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventJDOQuery eventJDOQuery = createJDOQueryFrom(pm, queryFilters, clientTimeZone);

    long t11 = System.currentTimeMillis();

    List<Long> adminExperiments = getExperimentsForAdmin(loggedInuser);
    log.info("Loggedin user's administered experiments: " + loggedInuser + " has ids: "
             + getIdsQuoted(adminExperiments));

    if (isDevMode() || isUserQueryingTheirOwnData(loggedInuser, eventJDOQuery)) {
      log.info("dev mode or user querying self");
      executeQuery(allEvents, eventJDOQuery);
    } else if (isAnAdministrator(adminExperiments)) {
      log.info("isAnAdmin");
      if (!hasAnExperimentIdFilter(queryFilters)) {
        log.info("No expeirmentfilter");
        eventJDOQuery.addFilters(":p.contains(experimentId)");
        eventJDOQuery.addParameterObjects("(" + getIdsQuoted(adminExperiments) + ")");
        executeQuery(allEvents, eventJDOQuery);
      } else if (hasAnExperimentIdFilter(queryFilters)
                 && !isAdminOfAllExperimentsInQuery(queryFilters, adminExperiments)) {
        if (!eventJDOQuery.hasAWho()) {
          addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
          executeQuery(allEvents, eventJDOQuery);
        } else if (!eventJDOQuery.who().equals(loggedInuser)) {
          addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
        }
      } else {
        executeQuery(allEvents, eventJDOQuery);
      }

    } else {
      addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
      executeQuery(allEvents, eventJDOQuery);
      // also get all shared data that matches the query
      addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
    }

    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));

    ArrayList<Event> newArrayList = Lists.newArrayList(allEvents);
    sortList(newArrayList);
    return newArrayList;
  }

  private void addWhoQueryForLoggedInuser(String loggedInuser, EventJDOQuery eventJDOQuery) {
    eventJDOQuery.addFilters("who == whoParam");
    eventJDOQuery.declareParameters("String whoParam");
    eventJDOQuery.addParameterObjects(loggedInuser);
  }

  private void addWhoQueryFilterForLoggedInuser(String loggedInuser, EventDSQuery eventDSQuery) {
    eventDSQuery.addFilter(new FilterPredicate("who", FilterOperator.EQUAL, loggedInuser));
  }

  /**
   * @param queryFilters
   * @param adminExperiments
   * @return
   */
  private boolean isAdminOfAllExperimentsInQuery(List<com.google.sampling.experiential.server.Query> queryFilters,
                                                 List<Long> adminExperiments) {
    if (!isAnAdministrator(adminExperiments)) {
      return false;
    }
    boolean filteringForAdminedExperiment = false;
    for (com.google.sampling.experiential.server.Query query : queryFilters) {
      if (query.getKey().equals("experimentId")) {
        final String queryValue = query.getValue();

        try {
          Long experimentId = Long.parseLong(queryValue);
          filteringForAdminedExperiment = adminExperiments.contains(experimentId);
          if (!filteringForAdminedExperiment) { // All filters must be admin'ed
                                                // experiments for now.
            return false;
          }
        } catch (NumberFormatException e) {
          return false;
        }
      }
    }
    return filteringForAdminedExperiment;
  }

  private boolean isUserQueryingTheirOwnData(String loggedInuser, EventJDOQuery eventJDOQuery) {
    return (eventJDOQuery.hasAWho() && eventJDOQuery.who().equals(loggedInuser));
  }

  private boolean isUserQueryingTheirOwnData(String loggedInuser, EventDSQuery eventDSQuery) {
    return (eventDSQuery.hasAWho() && eventDSQuery.who().equals(loggedInuser));
  }

  private void executeQuery(Set<Event> allEvents, EventJDOQuery eventJDOQuery) {
    Query query = eventJDOQuery.getQuery();

    log.info("Query = " + query.toString());
    log.info("Query params = " + Joiner.on(",").join(eventJDOQuery.getParameters()));

    final Object[] params = eventJDOQuery.getParameters().toArray();
    List<Event> queryResults = null;
    if (params != null && params.length > 0) {
      queryResults = (List<Event>) query.executeWithArray(params);
    } else {
      queryResults = (List<Event>) query.execute();
    }
    allEvents.addAll(queryResults);
    adjustTimeZone(allEvents);
  }

  private void executeQuery(Set<Event> allEvents, EventDSQuery eventJDOQuery) {
    log.info("Starting to execute ds query");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Cursor cursor = null;
    int count = 0;
    while (true) {
      log.info("entering query loop. Count = " + count);
      int pageSize = 1000;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);

      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("Event");
      eventJDOQuery.applyFiltersToQuery(q);

      PreparedQuery pq = datastore.prepare(q);
      log.info("execute query");

      QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
      count = count + results.size();
      if (results.isEmpty()) {
        log.info("empty results");
        break;
      }
      for (Entity entity : results) {
        Event event = createEventFromEntity(entity);
        Key key = entity.getKey();

        // todo async
        // already done with the keyslist and valueslist
        // Set<What> whats = fetchWhats(datastore, key);
        // event.setWhat(whats);
        //
        // todo async
        List<PhotoBlob> blobs = fetchBlobs(datastore, key);
        event.setBlobs(blobs);

        allEvents.add(event);
      }
      cursor = results.getCursor();
      if (cursor == null) {
        log.info("null cursor");
        break;
      }
    }
    adjustTimeZone(allEvents);
  }
  
  private DateTime getEarliestWhen() throws Exception{
    CloudSQLDaoImpl  daoImpl =  new CloudSQLDaoImpl();
    Long whenUtcInMillis = null;
    try {
      whenUtcInMillis = daoImpl.getEarliestWhen();
    } catch (SQLException | ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      log.warning("Not able to get earliest when" + e.getMessage());
    }
    if (whenUtcInMillis != null) {
      DateTime dt = new DateTime(whenUtcInMillis);
      log.info("Earliest date in cloud sql"+ dt);
      return dt;
      
    } else {
      throw new Exception("no date fetched from Cloud sql, Dont proceed");
    }
    
  }
  
  private boolean readEventDataStoreAndInsertToCloudSql(String oldCursor) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Cursor cursor = null;
    int count = 0;
    int pageSize = 1000;
    DateTime earliestWhenInCloudSql = null;
    List<Event> eventsBatch = null;
    QueryResultList<Entity> results = null;
    boolean isFinished = false;
    try {
      earliestWhenInCloudSql = getEarliestWhen();
    } catch (Exception e) { 
      log.warning("Do not proceed. CS does not have data yet.");
      return false;
    }
    if (earliestWhenInCloudSql == null) {
      earliestWhenInCloudSql = new DateTime();
    }
    
    if(oldCursor != null) {
      log.info("old cursor " + oldCursor);
      cursor = Cursor.fromWebSafeString(oldCursor);
    }
    while (true) {
      eventsBatch = Lists.newArrayList();
      log.info("Count = " + count);
      boolean isContinueCSInsert = true;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      try { 
        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("Event");
        Date utilEarlyWhen = earliestWhenInCloudSql.toDate();
        q.setFilter(new com.google.appengine.api.datastore.Query.FilterPredicate("when", FilterOperator.LESS_THAN, utilEarlyWhen));
        PreparedQuery pq = datastore.prepare(q);
        results = pq.asQueryResultList(fetchOptions);
        if (results.isEmpty()) {
          log.info("empty results");
          break;
        } 
        
        for (int i = 0; i < results.size(); i++) {
          Entity entity = results.get(i);
          Event event = createEventFromEntity(entity);
          event.setId(entity.getKey().getId());
          eventsBatch.add(event);
        }
      } catch ( DatastoreTimeoutException dte) {
        isContinueCSInsert = false;
        log.severe("datastore timing out" + dte);
        try {
          log.warning("Data store timing out, so sleeping for 1 hour");
          Thread.sleep(3600000);
        } catch (InterruptedException e) {
          log.warning("Data store timeout sleep interrupted" + e);
        }
      }
      
      if (isContinueCSInsert) {
        CloudSQLDaoImpl  daoImpl =  new CloudSQLDaoImpl();
        boolean isSuccess = daoImpl.insertEventsInBatch(eventsBatch);
        //Only if the batch cs insert of events, should we move the cursor to the next batch in datastore
        if (isSuccess) {
          cursor = results.getCursor();
          log.info("Moving the cursor:" + cursor.toWebSafeString());
          count = count + results.size();
        }
        
        
        if (cursor == null || !isSuccess) {
          log.info("null cursor or insert to cs failed, so break");
          isFinished = true;
          break;
        }
      }
    }
    return isFinished;
  }
  
  public boolean readOutputsDataStoreAndInsertToCloudSql(String oldCursor) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Cursor cursor = null;
    int count = 0;
    int pageSize = 1000;
    List<MigrationOutput> outputsBatch = null;
    QueryResultList<Entity> results = null;
    boolean isFinished = false;
    if(oldCursor != null) {
      log.info("old cursor " + oldCursor);
      cursor = Cursor.fromWebSafeString(oldCursor);
    }
    
    while (true) {
      outputsBatch = Lists.newArrayList();
      log.info("Count = " + count);
      boolean isContinueCSInsert = true;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);
      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }
     
      try { 
        com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("What");
        PreparedQuery pq = datastore.prepare(q);
        results = pq.asQueryResultList(fetchOptions);
        if (results.isEmpty()) {
          log.info("empty results");
          break;
        } 
       
        MigrationOutput output = null;
        for (int i = 0; i < results.size(); i++) {
          Entity entity = results.get(i);
          output = createOutputFromEntity(entity);
          outputsBatch.add(output);
        }
      } catch ( DatastoreTimeoutException dte) {
        isContinueCSInsert = false;
        log.severe("datastore timing out" + dte);
        try {
          log.warning("Data store timing out, so sleeping for 1 hour");
          Thread.sleep(3600000);
        } catch (InterruptedException e) {
          log.warning("Data store timeout sleep interrupted" + e);
        }
      }
      
      if (isContinueCSInsert) {
        //TODO enable foll code
        CloudSQLDaoImpl  daoImpl =  new CloudSQLDaoImpl();
        
        boolean isSuccess = daoImpl.insertOutputsInBatch(outputsBatch);
        //Only if the batch cs insert is successful, should we move the cursor to the next batch in datastore
        if (isSuccess) {
          cursor = results.getCursor();
          if (cursor!=null) {
            log.info("Moving the cursor: " + cursor.toWebSafeString());
          }
        } else {
          log.warning("cs insert batch failed, so restart from the cursor " + cursor.toWebSafeString());
        }
        count = count + results.size();
        
        if (cursor == null || !isSuccess) {
          log.warning("cursor is " + cursor );
          log.warning("Last sql batch insert was "+ isSuccess + ". If false, fix error and restart from cursor ");
          isFinished = true;
          break;
        }
      }//if continue csinsert
    }//while
    return isFinished;
  }


  private MigrationOutput createOutputFromEntity(Entity entity) {
    MigrationOutput output = new MigrationOutput();
    String tempName = null;
    String tempValue = null;
    
    output.setEventId(entity.getParent().getId());
    if(entity.getProperty("name") != null) {
      tempName = entity.getProperty("name").toString();
      if(tempName.equalsIgnoreCase("null")) {
        tempName = tempName + "-" + NULL_CTR++;
      }
    } else {
      tempName ="null-"+ NULL_CTR++;
    }
    output.setText(tempName);
    if (entity.getProperty("value") != null) {
      tempValue = entity.getProperty("value").toString();
    }
    output.setAnswer(tempValue);
    
    return output;
  }

  private Set<What> fetchWhats(DatastoreService datastore, Key key) {
    com.google.appengine.api.datastore.Query whatQuery = new com.google.appengine.api.datastore.Query("What", key);
    PreparedQuery whatPreparedQuery = datastore.prepare(whatQuery);
    // log.info("execute what query");
    QueryResultList<Entity> whatResults = whatPreparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());
    Set<What> whats = Sets.newHashSet();
    for (Entity whatEntity : whatResults) {
      whats.add(createWhatFromEntity(whatEntity));
    }
    return whats;
  }

  private List<PhotoBlob> fetchBlobs(DatastoreService datastore, Key key) {
    com.google.appengine.api.datastore.Query whatQuery = new com.google.appengine.api.datastore.Query("PhotoBlob", key);
    PreparedQuery whatPreparedQuery = datastore.prepare(whatQuery);
    // log.info("execute blob query");
    QueryResultList<Entity> whatResults = whatPreparedQuery.asQueryResultList(FetchOptions.Builder.withDefaults());
    List<PhotoBlob> whats = Lists.newArrayList();
    for (Entity whatEntity : whatResults) {
      whats.add(createPhotoBlobFromEntity(whatEntity));
    }
    return whats;
  }

  private What createWhatFromEntity(Entity whatEntity) {
    return new What((String) whatEntity.getProperty("name"), (String) whatEntity.getProperty("value"));
  }

  private PhotoBlob createPhotoBlobFromEntity(Entity entity) {
    final Object blobProperty = entity.getProperty("value");
    if (blobProperty instanceof Blob) {
      return new PhotoBlob((String) entity.getProperty("name"), ((Blob) blobProperty).getBytes());
    } else if (blobProperty instanceof byte[]) {
      return new PhotoBlob((String) entity.getProperty("name"), (byte[]) blobProperty);
    }
    return null;
  }

  private Event createEventFromEntity(Entity entity) {
    return EventEntityConverter.convertEntityToEvent(entity);
  }

  private void adjustTimeZone(Collection<Event> allEvents) {
    for (Event event : allEvents) {
      String tz = event.getTimeZone();
      event.setResponseTime(adjustTimeToTimezoneIfNecesssary(tz, event.getResponseTime()));
      event.setScheduledTime(adjustTimeToTimezoneIfNecesssary(tz, event.getScheduledTime()));
    }
  }

  public static Date adjustTimeToTimezoneIfNecesssary(String tz, Date responseTime) {
    if (responseTime == null) {
      return null;
    }
    DateTimeZone timezone = null;
    if (tz != null) {
      timezone = DateTimeZone.forID(tz);
    }

    if (timezone != null && responseTime.getTimezoneOffset() != timezone.getOffset(responseTime.getTime())) {
      responseTime = new DateTime(responseTime).withZone(timezone).toDate();
    }
    return responseTime;
  }

  private void addAllSharedEvents(List<com.google.sampling.experiential.server.Query> queryFilters,
                                  DateTimeZone clientTimeZone, Set<Event> allEvents, PersistenceManager pm) {
    EventJDOQuery sharedQ = createJDOQueryFrom(pm, queryFilters, clientTimeZone);
    sharedQ.addFilters("shared == true");
    Query queryShared = sharedQ.getQuery();
    List<Event> sharedEvents = (List<Event>) queryShared.executeWithArray(sharedQ.getParameters().toArray());
    allEvents.addAll(sharedEvents);
    adjustTimeZone(allEvents);
  }

  private boolean isAnAdministrator(List<Long> adminExperiments) {
    return adminExperiments != null && adminExperiments.size() > 0;
  }

  private boolean isDevMode() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
  }

  private void sortList(ArrayList<Event> newArrayList) {
    Comparator<Event> dateComparator = new Comparator<Event>() {
      @Override
      public int compare(Event o1, Event o2) {
        Date when1 = o1.getWhen();
        Date when2 = o2.getWhen();
        if (when1 == null || when2 == null) {
          return 0;
        } else if (when1.after(when2)) {
          return -1;
        } else if (when2.after(when1)) {
          return 1;
        }
        return 0;
      }
    };
    Collections.sort(newArrayList, dateComparator);
  }

  /**
   * @param experimentIdList
   * @return
   */
  private List<String> getNames(List<Experiment> experimentsForAdmin) {
    List<String> ids = Lists.newArrayList();
    for (Experiment experiment : experimentsForAdmin) {
      ids.add("'" + experiment.getTitle() + "'");
    }
    return ids;

  }

  /**
   * @param q
   * @return
   */
  private boolean hasAnExperimentIdFilter(List<com.google.sampling.experiential.server.Query> queryFilters) {
    for (com.google.sampling.experiential.server.Query query : queryFilters) {
      if (query.getKey().equals("experimentId") || query.getKey().equals("experimentName")) {
        return true;
      }
    }
    return false;
  }

  private Long getExperimentIdFromFilter(List<com.google.sampling.experiential.server.Query> queryFilters) {
    for (com.google.sampling.experiential.server.Query query : queryFilters) {
      if (query.getKey().equals("experimentId")) {
        String value = query.getValue();
        if (value != null) {
          try {
            return Long.parseLong(value);
          } catch (NumberFormatException e) {
            return null;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param adminExperiments
   * @return
   */
  private List<String> getIdsQuoted(List<Long> adminExperiments) {
    List<String> ids = Lists.newArrayList();
    for (Long long1 : adminExperiments) {
      ids.add("'" + long1 + "'");
    }
    return ids;
  }

  private List<String> getIds(List<Experiment> experimentsForAdmin) {
    List<String> ids = Lists.newArrayList();
    for (Experiment experiment : experimentsForAdmin) {
      ids.add(experiment.getId().toString());
    }
    return ids;
  }

  @SuppressWarnings("unchecked")
  private List<Long> getExperimentsForAdmin(String user) {
    return ExperimentAccessManager.getExistingExperimentIdsForAdmin(user, 0, null).getExperiments();
  }

  @SuppressWarnings("unchecked")
  private Experiment getExperimentById(Long experimentId, PersistenceManager pm) {
    if (experimentId == null) {
      return null;
    }
    return pm.getObjectById(Experiment.class, experimentId);
  }

  private EventJDOQuery createJDOQueryFrom(PersistenceManager pm,
                                           List<com.google.sampling.experiential.server.Query> queryFilters,
                                           DateTimeZone clientTimeZone) {
    Query newQuery = pm.newQuery(Event.class);
    JDOQueryBuilder queryBuilder = new JDOQueryBuilder(newQuery);
    queryBuilder.addFilters(queryFilters, clientTimeZone);
    return queryBuilder.getQuery();
  }

  public static void sortEvents(List<Event> greetings) {
    Comparator<Event> dateComparator = new Comparator<Event>() {
      @Override
      public int compare(Event o1, Event o2) {
        Date when1 = o1.getWhen();
        Date when2 = o2.getWhen();
        if (when1 == null || when2 == null) {
          return 0;
        } else if (when1.after(when2)) {
          return -1;
        } else if (when2.after(when1)) {
          return 1;
        }
        return 0;
      }
    };
    Collections.sort(greetings, dateComparator);
  }

  public static List<EventDAO> convertEventsToDAOs(List<Event> result) {
    List<EventDAO> eventDAOs = Lists.newArrayList();

    for (Event event : result) {
      eventDAOs.add(new EventDAO(event.getWho(), event.getWhen(), event.getExperimentName(), event.getLat(),
                                 event.getLon(), event.getAppId(), event.getPacoVersion(),
                                 convertToWhatDAOs(event.getWhat()), event.isShared(), event.getResponseTime(),
                                 event.getScheduledTime(), toBase64StringArray(event.getBlobs()),
                                 Long.parseLong(event.getExperimentId()), event.getExperimentVersion(),
                                 event.getTimeZone(), event.getExperimentGroupName(), event.getActionTriggerId(),
                                 event.getActionTriggerSpecId(), event.getActionId()));
    }
    return eventDAOs;
  }

  public static List<WhatDAO> convertToWhatDAOs(Set<What> what) {
    List<WhatDAO> daos = Lists.newArrayList();
    for (What currentWhat : what) {
      daos.add(new WhatDAO(currentWhat.getName(), currentWhat.getValue()));
    }
    return daos;
  }

  /**
   * @param blobs
   * @return
   */
  public static String[] toBase64StringArray(List<PhotoBlob> blobs) {
    if (blobs == null) {
      return new String[0];
    }
    String[] results = new String[blobs.size()];
    for (int i = 0; i < blobs.size(); i++) {
      results[i] = new String(Base64.encodeBase64(blobs.get(i).getValue()));
    }
    return results;
  }

  public EventQueryResultPair getEventsFromLowLevelDS(List<com.google.sampling.experiential.server.Query> query,
                                                      String requestorEmail, DateTimeZone timeZoneForClient) {
    log.info("Getting events from low level datastore");
    Set<Event> allEvents = Sets.newHashSet();
    EventDSQuery eventDSQuery = createDSQueryFrom(query, timeZoneForClient);

    long t11 = System.currentTimeMillis();

    List<Long> adminExperiments = getExperimentsForAdmin(requestorEmail);
    log.info("Loggedin user's administered experiments: " + requestorEmail + " has ids: "
             + getIdsQuoted(adminExperiments));

    if (isDevMode() || isUserQueryingTheirOwnData(requestorEmail, eventDSQuery)) {
      log.info("dev mode or user querying self data");
      executeQuery(allEvents, eventDSQuery);
    } else if (isAdminOfAllExperimentsInQuery(query, adminExperiments)) {
      log.info("isAnAdmin of experiment(s)");
      // if (!hasAnExperimentIdFilter(query)) {
      // log.info("No experimentfilter. Loading events for all admined
      // experiments");
      // Filter idFilter = new FilterPredicate("experimentId",
      // FilterOperator.IN, adminExperiments);
      // eventDSQuery.addFilter(idFilter);
      // executeQuery(allEvents, eventDSQuery);
      // } else if (hasAnExperimentIdFilter(query) &&
      // !isAdminOfAllExperimentsInQuery(query, adminExperiments) &&
      // !eventDSQuery.hasAWho()) {
      // addWhoQueryFilterForLoggedInuser(requestorEmail, eventDSQuery);
      // executeQuery(allEvents, eventDSQuery);
      // } else {
      executeQuery(allEvents, eventDSQuery);
      // }
    } else if (!eventDSQuery.hasAWho()) {
      log.info("No experiment specified, So querying for their own data");
      addWhoQueryFilterForLoggedInuser(requestorEmail, eventDSQuery);
      executeQuery(allEvents, eventDSQuery);
    } else {
      return new EventQueryResultPair(Collections.EMPTY_LIST, null);
    }

    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));

    ArrayList<Event> newArrayList = Lists.newArrayList(allEvents);
    sortList(newArrayList);
    return new EventQueryResultPair(newArrayList, null);
  }
  
  public boolean copyAllEventsFromLowLevelDSToCloudSql(String cursor) {
    log.info("Getting events from low level datastore for migrating to cloud sql");
    long t11 = System.currentTimeMillis();
   
    boolean finished = readEventDataStoreAndInsertToCloudSql(cursor);
   
    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));
    return finished;
  }
  
  public boolean copyAllOutputsFromLowLevelDSToCloudSql(String cursor) {
    log.info("Getting outputs from low level datastore for migrating to cloud sql");
    long t11 = System.currentTimeMillis();
   
    boolean finished = readOutputsDataStoreAndInsertToCloudSql(cursor);
   
    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));
    return finished;
  }
  
  public static boolean isExperimentAdministrator(String loggedInUserEmail, Experiment experiment) {
    return experiment.getCreator().getEmail().toLowerCase().equals(loggedInUserEmail)
           || experiment.getAdmins().contains(loggedInUserEmail);
  }

  private EventDSQuery createDSQueryFrom(List<com.google.sampling.experiential.server.Query> query,
                                         DateTimeZone timeZoneForClient) {
    com.google.appengine.api.datastore.Query newQuery = new com.google.appengine.api.datastore.Query(Event.class.getName());
    DSQueryBuilder queryBuilder = new DSQueryBuilder(newQuery);
    queryBuilder.addFilters(query, timeZoneForClient);
    return queryBuilder.getQuery();

  }

  public EventQueryResultPair getEventsInBatches(List<com.google.sampling.experiential.server.Query> queryFilters,
                                                 String loggedInuser, DateTimeZone clientTimeZone, int limit,
                                                 String cursor) {
    if (limit == 0) {
      limit = DEFAULT_FETCH_LIMIT;
    }
    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventJDOQuery eventJDOQuery = createJDOQueryFrom(pm, queryFilters, clientTimeZone);

    long t11 = System.currentTimeMillis();

    List<Long> adminExperiments = getExperimentsForAdmin(loggedInuser);
    log.info("Loggedin user's administered experiments: " + loggedInuser + " has ids: "
             + getIdsQuoted(adminExperiments));

    String nextCursor = null;
    if (isDevMode() || isUserQueryingTheirOwnData(loggedInuser, eventJDOQuery)) {
      log.info("dev mode or user querying self");
      nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, limit, cursor);
    } else if (isAnAdministrator(adminExperiments)) {
      log.info("isAnAdmin");
      if (!hasAnExperimentIdFilter(queryFilters)) {
        log.info("No experimentfilter");
        eventJDOQuery.addFilters(":p.contains(experimentId)");
        eventJDOQuery.addParameterObjects("(" + getIdsQuoted(adminExperiments) + ")");
        nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, limit, cursor);
      } else if (hasAnExperimentIdFilter(queryFilters)
                 && !isAdminOfAllExperimentsInQuery(queryFilters, adminExperiments)) {
        if (!eventJDOQuery.hasAWho()) {
          addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
          nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, limit, cursor);
        } else if (!eventJDOQuery.who().equals(loggedInuser)) {
          addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
        }
      } else {
        nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, limit, cursor);
      }

    } else {
      addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
      nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, limit, cursor);
      // also get all shared data that matches the query
      addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
    }

    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));
    log.info("retrieved " + allEvents.size() + " events");

    ArrayList<Event> newArrayList = Lists.newArrayList(allEvents);
    sortList(newArrayList);
    return new EventQueryResultPair(newArrayList, nextCursor);
  }

  private String executeQueryInBatches(Set<Event> allEvents, EventJDOQuery eventJDOQuery, int limit,
                                       String websafeCursor) {
    Query q = eventJDOQuery.getQuery();
    PersistenceManager persistenceManager = q.getPersistenceManager();

    Cursor cursor = null;
    if (!Strings.isNullOrEmpty(websafeCursor) && !websafeCursor.equals("null")) {
      cursor = Cursor.fromWebSafeString(websafeCursor);
    }
    if (cursor != null) {
      Map<String, Object> extensionMap = new HashMap<String, Object>();
      extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
      q.setExtensions(extensionMap);
      // q.getFetchPlan().addGroup("PhotoBlob").addGroup("keysList").addGroup("valuesList");
      // q.getFetchPlan().setFetchSize(limit);

    }
    q.setRange(0, limit);
    List<Event> currentResults = (List<Event>) q.executeWithArray(eventJDOQuery.getParameters().toArray());
    // log.info("Got back " + currentResults.size() + " results");
    if (currentResults != null && !currentResults.isEmpty()) {
      // Load each object so that is fetched when we detach.
      for (Event event : currentResults) {
        event.getBlobs();
        event.getWhat();

        // detach to free up resources in the persistence manager across the
        // huge result sets

        // allEvents.add(persistenceManager.detachCopy(event));
      }

      Collection<Event> detachedResults = q.getPersistenceManager().detachCopyAll(currentResults);
      allEvents.addAll(detachedResults);

      // log.info("Accumulated result count: " + allEvents.size());
      Cursor newCursor = JDOCursorHelper.getCursor(currentResults);
      if (newCursor == null || (cursor != null && newCursor.toWebSafeString().equals(websafeCursor))) {
        cursor = null;
      } else {
        cursor = newCursor;
      }
    }

    // }
    log.info("done with results gathering");
    adjustTimeZone(allEvents);
    log.info("done adjusting timezone");
    return cursor != null ? cursor.toWebSafeString() : null;
  }

  public EventQueryResultPair getEventsInBatchesOneBatch(List<com.google.sampling.experiential.server.Query> queryFilters,
                                                         String loggedInuser, DateTimeZone clientTimeZone, int limit,
                                                         String cursor) {

    return new EventQueryResultPair(getEvents(queryFilters, loggedInuser, clientTimeZone, 0, 20000), null);
  }
}
