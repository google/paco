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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.codec.binary.Base64;
import org.datanucleus.store.appengine.query.JDOCursorHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.google.sampling.experiential.datastore.EventEntityConverter;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.EventDAO;

/**
 * Retrieve Event objects from the JDO store.
 *
 * @author Bob Evans
 *
 */
public class EventRetriever {

  private static final int DEFAULT_FETCH_LIMIT = 100;
  private static EventRetriever instance;
  private static final Logger log = Logger.getLogger(EventRetriever.class.getName());

  @VisibleForTesting
  EventRetriever() {
  }

  public static synchronized EventRetriever getInstance() {
    if (instance == null) {
      instance = new EventRetriever();
    }
    return instance;
  }

  public void postEvent(String who, String lat, String lon, Date whenDate, String appId,
      String pacoVersion, Set<What> what, boolean shared, String experimentId,
      String experimentName, Integer experimentVersion, Date responseTime, Date scheduledTime, List<PhotoBlob> blobs, String tz) {
//    long t1 = System.currentTimeMillis();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Event event = new Event(who, lat, lon, whenDate, appId, pacoVersion, what, shared,
        experimentId, experimentName, experimentVersion, responseTime, scheduledTime, blobs, tz);
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(event);
      tx.commit();
      log.info("Event saved");
    } finally {
      if (tx.isActive()) {
        log.info("Event rolled back");
        tx.rollback();
      }
      pm.close();
    }
//    long t2 = System.currentTimeMillis();
//    log.info("POST Event time: " + (t2 - t1));
  }

  @SuppressWarnings("unchecked")
  public List<Event> getEvents(String loggedInUser) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      Query q = pm.newQuery(Event.class);
      q.setOrdering("when desc");
      q.setFilter("who = whoParam");
      q.declareParameters("String whoParam");
      long t11 = System.currentTimeMillis();
      List<Event> events = (List<Event>) q.execute(loggedInUser);
      adjustTimeZone(events);
      long t12 = System.currentTimeMillis();
      log.info("get execute time: " + (t12 - t11));
      return events;
  }

  public List<Event> getEvents(List<com.google.sampling.experiential.server.Query> queryFilters,
      String loggedInuser, DateTimeZone clientTimeZone, int offset, int limit) {
    if (limit == 0) {
      limit = DEFAULT_FETCH_LIMIT;
    }
    doOneTimeCleanup();
    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventJDOQuery eventJDOQuery = createJDOQueryFrom(pm, queryFilters, clientTimeZone, offset, limit);

    long t11 = System.currentTimeMillis();

    List<Experiment> adminExperiments = getExperimentsForAdmin(loggedInuser, pm);
    log.info("Loggedin user's administered experiments: " +loggedInuser +" has ids: " +
        getIdsQuoted(adminExperiments));

    if (isDevMode(loggedInuser) || isUserQueryingTheirOwnData(loggedInuser, eventJDOQuery)) {
      log.info("dev mode or user querying self");
      executeQuery(allEvents, eventJDOQuery);
    } else if (isAnAdministrator(adminExperiments)) {
      log.info("isAnAdmin");
      if (!hasAnExperimentIdFilter(queryFilters)) {
        log.info("No expeirmentfilter");
        eventJDOQuery.addFilters(":p.contains(experimentId)");
        eventJDOQuery.addParameterObjects("(" + getIdsQuoted(adminExperiments) + ")");
        executeQuery(allEvents, eventJDOQuery);
      } else if (hasAnExperimentIdFilter(queryFilters) &&
          !isAdminOfFilteredExperiments(queryFilters, adminExperiments)) {
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

  private void addWhoQueryForLoggedInuser(String loggedInuser, EventDSQuery eventDSQuery) {
    eventDSQuery.addFilters("who == whoParam");
    eventDSQuery.declareParameters("String whoParam");
    eventDSQuery.addParameterObjects(loggedInuser);
  }


  /**
   * @param queryFilters
   * @param adminExperiments
   * @return
   */
  private boolean isAdminOfFilteredExperiments(List<com.google.sampling.experiential.server.Query>
    queryFilters, List<Experiment> adminExperiments) {
    List<String> adminIds = getIds(adminExperiments);
    boolean filteringForAdminedExperiment = false;
    for (com.google.sampling.experiential.server.Query query : queryFilters) {
      if (query.getKey().equals("experimentId") || query.getKey().equals("experimentName")) {
        // TODO (bobevans) experimentName is broken here. Need then to test against names
        filteringForAdminedExperiment = adminIds.contains(query.getValue());
        if (!filteringForAdminedExperiment) { // All filters must be admin'ed experiments for now.
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

    List<Event> queryResults = (List<Event>) query.executeWithArray(eventJDOQuery.getParameters().toArray());
    allEvents.addAll(queryResults);
    adjustTimeZone(allEvents);
  }

  private void executeQuery(Set<Event> allEvents, EventDSQuery eventJDOQuery) {

    //
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Cursor cursor = null;
    while (true) {
      int pageSize = 1000;
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(pageSize);

      if (cursor != null) {
        fetchOptions.startCursor(cursor);
      }

      com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query("Event");
      eventJDOQuery.getLowLevelDatastoreEntityQuery(q);

      PreparedQuery pq = datastore.prepare(q);

      QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
      if (results.isEmpty()) {
        break;
      }
      for (Entity entity : results) {
        allEvents.add(createEventFromEntity(entity));
      }
      cursor = results.getCursor();
      if (cursor == null) {
        break;
      }
    }
    adjustTimeZone(allEvents);
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
    EventJDOQuery sharedQ = createJDOQueryFrom(pm, queryFilters, clientTimeZone, 0, 0);
    sharedQ.addFilters("shared == true");
    Query queryShared = sharedQ.getQuery();
    List<Event> sharedEvents = (List<Event>)queryShared.executeWithArray(sharedQ.getParameters().toArray());
    allEvents.addAll(sharedEvents);
    adjustTimeZone(allEvents);
  }

  private boolean isAnAdministrator(List<Experiment> adminExperiments) {
    return adminExperiments != null && adminExperiments.size() > 0;
  }

  private boolean isDevMode(String loggedInuser) {
    return loggedInuser == null;
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
    for(Experiment experiment : experimentsForAdmin) {
      ids.add("'" + experiment.getTitle() +"'");
    }
    return ids;

  }

  /**
   * @param q
   * @return
   */
  private boolean hasAnExperimentIdFilter(List<com.google.sampling.experiential.server.Query>
      queryFilters) {
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
   * @param experimentsForAdmin
   * @return
   */
  private List<String> getIdsQuoted(List<Experiment> experimentsForAdmin) {
    List<String> ids = Lists.newArrayList();
    for(String experimentId : getIds(experimentsForAdmin)) {
      ids.add("'" + experimentId +"'");
    }
    return ids;
  }

  private List<String> getIds(List<Experiment> experimentsForAdmin) {
    List<String> ids = Lists.newArrayList();
    for(Experiment experiment : experimentsForAdmin) {
      ids.add(experiment.getId().toString());
    }
    return ids;
  }

  @SuppressWarnings("unchecked")
  private List<Experiment> getExperimentsForAdmin(String user, PersistenceManager pm) {
    Query q = pm.newQuery(Experiment.class);
    q.setFilter("admins == whoParam");
    q.declareParameters("String whoParam");
    return (List<Experiment>) q.execute(user);
  }

  @SuppressWarnings("unchecked")
  private Experiment getExperimentById(Long experimentId, PersistenceManager pm) {
    if (experimentId == null) {
      return null;
    }
    return pm.getObjectById(Experiment.class, experimentId);
  }


  private void doOneTimeCleanup() {
  }

  private EventJDOQuery createJDOQueryFrom(PersistenceManager pm,
      List<com.google.sampling.experiential.server.Query> queryFilters,
      DateTimeZone clientTimeZone, int offset, int limit) {
    Query newQuery = pm.newQuery(Event.class);
    JDOQueryBuilder queryBuilder = new JDOQueryBuilder(newQuery);
    queryBuilder.addFilters(queryFilters, clientTimeZone);
    return queryBuilder.getQuery();
  }

  public void postEvent(String who, String lat, String lon, Date whenDate, String appId, String pacoVersion,
                        Set<What> whats, boolean shared, String experimentId, String experimentName,
                        Integer experimentVersion, DateTime responseTime, DateTime scheduledTime, List<PhotoBlob> blobs) {

    postEvent(who, lat, lon, whenDate, appId, pacoVersion, whats, shared, experimentId, experimentName, experimentVersion,
              responseTime != null ? responseTime.toDate() : null,
              scheduledTime != null ? scheduledTime.toDate() : null, blobs,
              responseTime != null && responseTime.getZone() != null ? responseTime.getZone().toString()
                                                                     : scheduledTime!= null && scheduledTime.getZone() != null ?
                                                                                                                                 scheduledTime.getZone().toString() : null);
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
      eventDAOs.add(new EventDAO(event.getWho(), event.getWhen(), event.getExperimentName(),
          event.getLat(), event.getLon(), event.getAppId(), event.getPacoVersion(),
          event.getWhatMap(), event.isShared(), event.getResponseTime(), event.getScheduledTime(),
          toBase64StringArray(event.getBlobs()), Long.parseLong(event.getExperimentId()), event.getExperimentVersion(), event.getTimeZone()));
    }
    return eventDAOs;
  }

  /**
   * @param blobs
   * @return
   */
  public static String[] toBase64StringArray(List<PhotoBlob> blobs) {
    String[] results = new String[blobs.size()];
    for (int i =0; i < blobs.size(); i++) {
      results[i] = new String(Base64.encodeBase64(blobs.get(i).getValue()));
    }
    return results;
  }

  public List<Event> getEventsFromLowLevelDS(List<com.google.sampling.experiential.server.Query> query,
                                             String requestorEmail, DateTimeZone timeZoneForClient, int offset, int limit) {
    if (limit == 0) {
      limit = DEFAULT_FETCH_LIMIT;
    }
    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventDSQuery eventJDOQuery = createDSQueryFrom(pm, query, timeZoneForClient, offset, limit);

    long t11 = System.currentTimeMillis();

    if (isDevMode(requestorEmail) || isUserQueryingTheirOwnData(requestorEmail, eventJDOQuery)) {
      log.info("dev mode or user querying self data");
      executeQuery(allEvents, eventJDOQuery);
    } else if (hasAnExperimentIdFilter(query) && !eventJDOQuery.hasAWho() &&
            ExperimentRetriever.isExperimentAdministrator(requestorEmail,
                                                          getExperimentById(getExperimentIdFromFilter(query), pm))) {
      log.info("Admin query for an experiment");
      executeQuery(allEvents, eventJDOQuery);
    } else {
      log.info("No experiment specified, So querying for their own data");
      addWhoQueryForLoggedInuser(requestorEmail, eventJDOQuery);
      executeQuery(allEvents, eventJDOQuery);
    }


    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));

    ArrayList<Event> newArrayList = Lists.newArrayList(allEvents);
    sortList(newArrayList);
    return newArrayList;
  }

  private EventDSQuery createDSQueryFrom(PersistenceManager pm,
                                         List<com.google.sampling.experiential.server.Query> query,
                                         DateTimeZone timeZoneForClient, int offset, int limit) {
    Query newQuery = pm.newQuery(Event.class);
    DSQueryBuilder queryBuilder = new DSQueryBuilder(newQuery);
    queryBuilder.addFilters(query, timeZoneForClient);
    return queryBuilder.getQuery();

  }

  public EventQueryResultPair getEventsInBatches(List<com.google.sampling.experiential.server.Query> queryFilters,
                                        String loggedInuser, DateTimeZone clientTimeZone, int offset, int limit, String cursor) {
    if (limit == 0) {
      limit = DEFAULT_FETCH_LIMIT;
    }
    doOneTimeCleanup();
    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventJDOQuery eventJDOQuery = createJDOQueryFrom(pm, queryFilters, clientTimeZone, offset, limit);

    long t11 = System.currentTimeMillis();

    List<Experiment> adminExperiments = getExperimentsForAdmin(loggedInuser, pm);
    log.info("Loggedin user's administered experiments: " + loggedInuser + " has ids: "
             + getIdsQuoted(adminExperiments));

    String nextCursor = null;
    if (isDevMode(loggedInuser) || isUserQueryingTheirOwnData(loggedInuser, eventJDOQuery)) {
      log.info("dev mode or user querying self");
      nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, cursor);
    } else if (isAnAdministrator(adminExperiments)) {
      log.info("isAnAdmin");
      if (!hasAnExperimentIdFilter(queryFilters)) {
        log.info("No experimentfilter");
        eventJDOQuery.addFilters(":p.contains(experimentId)");
        eventJDOQuery.addParameterObjects("(" + getIdsQuoted(adminExperiments) + ")");
        nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, cursor);
      } else if (hasAnExperimentIdFilter(queryFilters) && !isAdminOfFilteredExperiments(queryFilters, adminExperiments)) {
        if (!eventJDOQuery.hasAWho()) {
          addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
          nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, cursor);
        } else if (!eventJDOQuery.who().equals(loggedInuser)) {
          addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
        }
      } else {
        nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, cursor);
      }

    } else {
      addWhoQueryForLoggedInuser(loggedInuser, eventJDOQuery);
      nextCursor = executeQueryInBatches(allEvents, eventJDOQuery, cursor);
      // also get all shared data that matches the query
      addAllSharedEvents(queryFilters, clientTimeZone, allEvents, pm);
    }

    long t12 = System.currentTimeMillis();
    log.info("get execute time: " + (t12 - t11));
    log.info("retrieved " + allEvents.size() + " experiments");

    ArrayList<Event> newArrayList = Lists.newArrayList(allEvents);
    sortList(newArrayList);
    return new EventQueryResultPair(newArrayList, nextCursor);
  }

  private String executeQueryInBatches(Set<Event> allEvents, EventJDOQuery eventJDOQuery, String websafeCursor) {
    Query q = eventJDOQuery.getQuery();
    PersistenceManager persistenceManager = q.getPersistenceManager();

    Cursor cursor = null;
    if (!Strings.isNullOrEmpty(websafeCursor) && !websafeCursor.equals("null")) {
      cursor = Cursor.fromWebSafeString(websafeCursor);
    }
    //while (true) {


      if (cursor != null) {
        Map<String, Object> extensionMap = new HashMap<String, Object>();
        extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
        q.setExtensions(extensionMap);
        //q.getFetchPlan().addGroup("PhotoBlob").addGroup("keysList").addGroup("valuesList");

      }
      q.setRange(0, 1000);

      List<Event> currentResults = (List<Event>) q.executeWithArray(eventJDOQuery.getParameters().toArray());
      //log.info("Got back " + currentResults.size() + " results");
      if (currentResults != null && !currentResults.isEmpty()) {
        // Load each object so that is fetched when we detach.
        for (Event event : currentResults) {
          event.getBlobs();
          event.getWhat();

          // detach to free up resources in the persistence manager across the huge result sets

          //allEvents.add(persistenceManager.detachCopy(event));
        }

        Collection<Event> detachedResults = q.getPersistenceManager().detachCopyAll(currentResults);
        allEvents.addAll(detachedResults);



        //log.info("Accumulated result count: " + allEvents.size());
        Cursor newCursor = JDOCursorHelper.getCursor(currentResults);
        if (newCursor == null || (cursor != null && newCursor.toWebSafeString().equals(cursor.toWebSafeString()))) {
          //break;
        } else {
          cursor = newCursor;
        }
      }
      //}
    log.info("done with results gathering");
    adjustTimeZone(allEvents);
    log.info("done adjusting timezone");
    return cursor != null ? cursor.toWebSafeString() : null;
  }

}
