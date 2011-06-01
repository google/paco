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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Retrieve Event objects from the JDO store.
 * 
 * @author Bob Evans
 *
 */
public class EventRetriever {

  private static EventRetriever instance;
  private static final Logger log = Logger.getLogger(EventRetriever.class.getName());
  
  private EventRetriever() {
  }

  public static synchronized EventRetriever getInstance() {
    if (instance == null) {
      instance = new EventRetriever();
    }
    return instance;
  }

  public void postEvent(String who, String lat, String lon, Date whenDate, String appId,
      String pacoVersion, Set<What> what, boolean shared, String experimentId, 
      String experimentName, Date responseTime, Date scheduledTime, List<PhotoBlob> blobs) {
    long t1 = System.currentTimeMillis();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Event event = new Event(who, lat, lon, whenDate, appId, pacoVersion, what, shared,
        experimentId, experimentName, responseTime, scheduledTime, blobs);
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(event);
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
    long t2 = System.currentTimeMillis();
    log.info("POST Event time: " + (t2 - t1));
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
      long t12 = System.currentTimeMillis();
      log.info("get execute time: " + (t12 - t11));
      return events;
  }

  @SuppressWarnings("unchecked")
  public List<Event> getEvents(List<com.google.sampling.experiential.server.Query> queryFilters, 
      String loggedInuser, DateTimeZone clientTimeZone) {
    doOneTimeCleanup();
    Set<Event> allEvents = Sets.newHashSet();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    EventJDOQuery eventJDOQuery = createJDOQueryFrom(pm, queryFilters, clientTimeZone);

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

  private void executeQuery(Set<Event> allEvents, EventJDOQuery eventJDOQuery) {
    Query query = eventJDOQuery.getQuery();    
    log.info("Query = " + query.toString());
    log.info("Query params = " + Joiner.on(",").join(eventJDOQuery.getParameters()));      
    allEvents.addAll((List<Event>) query.executeWithArray(eventJDOQuery.getParameters().toArray()));
  }

  private void addAllSharedEvents(List<com.google.sampling.experiential.server.Query> queryFilters,
      DateTimeZone clientTimeZone, Set<Event> allEvents, PersistenceManager pm) {
    EventJDOQuery sharedQ = createJDOQueryFrom(pm, queryFilters, clientTimeZone);
    sharedQ.addFilters("shared == true");
    Query queryShared = sharedQ.getQuery(); 
    List<Event> sharedEvents = (List<Event>)queryShared.executeWithArray(
        sharedQ.getParameters().toArray());
    allEvents.addAll(sharedEvents);
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
   * @param q
   * @return
   */
  private boolean isExperimentAnAdminExperiment(List<Experiment> experimentIdList,
      List<com.google.sampling.experiential.server.Query> queryFilters) {
    if (experimentIdList == null || experimentIdList.size() == 0) {
      log.info("Empty list of experiments for admin");
      return false;
    }
    String experimentId = null;
    for (int i=0; i < queryFilters.size(); i++) {
      com.google.sampling.experiential.server.Query query = queryFilters.get(i);
      if (query.getKey().equals("experimentId")) {
        log.info("found experimentId key. value is "+ query.getValue());
        // TODO (bobevans): handle all this more cleanly by not appending strings for eqls test!!!
        return getIdsQuoted(experimentIdList).contains("'" + query.getValue() + "'");
      } else if (query.getKey().equals("experimentName")) {
        log.info("found experimentName key. value is "+ query.getValue());
        return getNames(experimentIdList).contains("'" + query.getValue() + "'");
      }
      
    }
    return false;
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
    long t11 = System.currentTimeMillis();
    return (List<Experiment>) q.execute(user);      
  }
  private void doOneTimeCleanup() {
//    List<com.google.sampling.experiential.server.Query> query =
//      new QueryParser().parse("survey=esm2");
//    long t1= System.currentTimeMillis();
//    PersistenceManager pm = PMF.get().getPersistenceManager();
//    Query q = pm.newQuery(Event.class);
//    try {
//    List<Event> events = (List<Event>) q.execute();
//    for (Event event : events) {
//      Set<What> newWhats = Sets.newHashSet();
//      Set<What> whatSet = event.getWhat();
//      for (What what : whatSet) {
//        if (what.getName().equals("reportTime") || what.getName().equals("signalTime")) {
//          try {
//            what.setValue(fixDateValue(what.getValue()));
//          } catch (IllegalArgumentException ia) {
//            what.setValue(new DateTime().toString(formatter)); 
//          }
//        }
//        newWhats.add(what);
//      }    
//      event.setWhat(newWhats);
//      pm.makePersistent(event);
//    }
//    } finally {
//      pm.close();
//    }
//    Transaction tx = null;
//    try {
//      tx = pm.currentTransaction();
//      tx.begin();
//      pm.makePersistentAll(events);
//      tx.commit();
//    } finally {
//      if (tx.isActive()) {
//        tx.rollback();
//      }
//      log.info("One time cleanup persistAll done: " + (System.currentTimeMillis() - t1));
//      pm.close();
//    }

  }

//  private DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd:HH:mm:ssZ");
//  private DateTimeFormatter oldFormatter = ISODateTimeFormat.dateTime();
//  private DateTimeFormatter fullFormatter = DateTimeFormat.longDateTime();
  
//  private String fixDateValue(String value) {
//    try {
//      DateTime dateTime = fullFormatter.parseDateTime(value);
//      return dateTime.toString(formatter);      
//    } catch (IllegalArgumentException iae) {
//      try {
//        DateTime dateTime = oldFormatter.parseDateTime(value);
//        return dateTime.toString(formatter);
//      } catch (IllegalArgumentException iae2) {
//        try {
//          DateTime dateTime = new DateTime(Long.valueOf(value));
//          return dateTime.toString(formatter);
//        } catch (IllegalArgumentException iae3) {
//          try {
//         // is it already converted to the new format?
//            formatter.parseDateTime(value);    
//            return value;
//          } catch (IllegalArgumentException iae4) {
//            log.info("Could not parse old date at all: " + value);
//            throw iae4;
//          }
//        }
//      }
//    }
//  }

  private EventJDOQuery createJDOQueryFrom(PersistenceManager pm, 
      List<com.google.sampling.experiential.server.Query> queryFilters, 
      DateTimeZone clientTimeZone) {
    Query newQuery = pm.newQuery(Event.class);
    JDOQueryBuilder queryBuilder = new JDOQueryBuilder(newQuery);
    queryBuilder.addFilters(queryFilters, clientTimeZone);
    return queryBuilder.getQuery();
  }
  
}
