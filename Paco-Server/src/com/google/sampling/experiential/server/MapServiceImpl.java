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


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.TimeUtil;


/*
 * * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MapServiceImpl extends RemoteServiceServlet implements MapService {

  public List<EventDAO> map() {
    List<Event> result = EventRetriever.getInstance().getEvents(getWho());
    return convertEventsToDAOs(result);
  }

  private String getWho() {
    User whoFromLogin = getWhoFromLogin();
    if (!isCorpInstance() && whoFromLogin == null) {
      throw new IllegalArgumentException("Must be logged in.");
    }
    if (whoFromLogin != null) {
      return whoFromLogin.getEmail();
    }
    return null;
  }
  
  private List<EventDAO> convertEventsToDAOs(List<Event> result) {
    List<EventDAO> eventDAOs = Lists.newArrayList();

    for (Event event : result) {
      eventDAOs.add(new EventDAO(event.getWho(), event.getWhen(), event.getExperimentName(), 
          event.getLat(), event.getLon(), event.getAppId(), event.getPacoVersion(), 
          event.getWhatMap(), event.isShared(), event.getResponseTime(), event.getScheduledTime(),
          toBase64StringArray(event.getBlobs())));
    }
    return eventDAOs;
  }

  /**
   * @param blobs
   * @return
   */
  private String[] toBase64StringArray(List<PhotoBlob> blobs) {
    String[] results = new String[blobs.size()];
    for (int i =0; i < blobs.size(); i++) {
      results[i] = new String(Base64.encodeBase64(blobs.get(i).getValue()));
    }
    return results;
  }

  public List<EventDAO> mapWithTags(String tags) {
    return getEventsForQuery(tags);
  }

  private List<EventDAO> getEventsForQuery(String tags) {
    List<com.google.sampling.experiential.server.Query> queries = new QueryParser().parse(tags);
    List<Event> result = EventRetriever.getInstance().getEvents(queries, getWho(), 
        EventServlet.getTimeZoneForClient(getThreadLocalRequest()));
    return convertEventsToDAOs(result);
  }

  public void saveEvent(String who, 
      String scheduledTime, 
      String responseTime, 
      String experimentId,
      Map<String, String> kvPairs, 
      boolean shared) {
    
    Date scheduledTimeDate = scheduledTime != null ? parseDateString(scheduledTime) : null;
    Date responseTimeDate = responseTime != null ? parseDateString(responseTime) : null;
    Date whenDate = new Date();
    // TODO (Once all data has been cleaned up, just send the kvPairs, and change the constructor)
    Set<What> whats = parseWhats(kvPairs);
    User loggedInWho = getWhoFromLogin();    
    if (loggedInWho == null || (who != null && !who.isEmpty() 
        && !loggedInWho.getEmail().equals(who))) {
      throw new IllegalArgumentException("Who passed in is not the logged in user!");
    }
    
    
    Experiment experiment = ExperimentRetriever.getExperiment(experimentId);
    
    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }
    
    if (!ExperimentRetriever.isWhoAllowedToPostToExperiment(experiment, loggedInWho.getEmail())) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");      
    }
    
    
    EventRetriever.getInstance().postEvent(loggedInWho.getEmail(), null, null, whenDate, "webform", 
        "1", whats, shared, experimentId, null, responseTimeDate, scheduledTimeDate, null);
  }

  private boolean isCorpInstance() {
    return false;
    // is it possible to forge host headers?
    //    return EventServlet.DEV_HOST.equals(getHostFromRequest());
  }
  
  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

  private Set<What> parseWhats(Map<String, String> kvPairs) {
    Set<What> whats = Sets.newHashSet();
    Set<String> keys = kvPairs.keySet();
    for (String key : keys) {
      What w = new What(key, kvPairs.get(key));
      whats.add(w);
    }
    return whats;
  }
 
  private Date parseDateString(String when) {
    SimpleDateFormat df = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT);
    Date whenDate;
    try {
      whenDate = df.parse(when);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Cannot parse date");
    }
    return whenDate;
  }

  @Override
  public void saveExperiment(ExperimentDAO experimentDAO) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Experiment experiment = null;
    if (experimentDAO.getId() != null) {
      experiment = retrieveExperimentForDAO(experimentDAO, pm);
    } else {
      experiment = new Experiment();
    }
    
    if (experiment.getId() != null) {
      User loggedInUser = getWhoFromLogin();
      String loggedInUserEmail = loggedInUser.getEmail();
      if (!(experiment.getCreator().equals(loggedInUser) || 
        experiment.getAdmins().contains(loggedInUserEmail))) {
        // TODO (Bobevans): return a signal here that they are no longer allowed to edit this
        // experiment;
        return;
      }
      JDOHelper.makeDirty(experiment, "inputs");
      JDOHelper.makeDirty(experiment, "feedback");
      JDOHelper.makeDirty(experiment, "schedule");
    }
    DAOConverter.fromExperimentDAO(experimentDAO, experiment, getWhoFromLogin());
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();    
      pm.makePersistent(experiment);
      tx.commit();
      ExperimentCacheHelper.getInstance().clearCache();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  private Experiment retrieveExperimentForDAO(ExperimentDAO experimentDAO, PersistenceManager pm) {
    Experiment experiment;
    ExperimentJDOQuery jdoQuery = new ExperimentJDOQuery(pm.newQuery(Experiment.class));
    jdoQuery.addFilters("id == idParam");
    jdoQuery.declareParameters("Long idParam");
    jdoQuery.addParameterObjects(experimentDAO.getId());
    @SuppressWarnings("unchecked")
    List<Experiment> experiments = (List<Experiment>)jdoQuery.getQuery().execute(
        jdoQuery.getParameters());
    experiment = experiments.get(0);
    return experiment;
  }

  public Boolean deleteExperiment(ExperimentDAO experimentDAO) {
    System.out.println("Delete called for " + experimentDAO.getId());
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
    
      if (experimentDAO.getId() != null) {
        Experiment experiment = retrieveExperimentForDAO(experimentDAO, pm);
        pm.deletePersistent(experiment);
        ExperimentCacheHelper.getInstance().clearCache();
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public List<ExperimentDAO> getExperimentsForUser() {
    return getExperimentsForUserWithQuery();    
  }

  private List<ExperimentDAO> getExperimentsForUserWithQuery() {
    User user = getWhoFromLogin();
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      List<Experiment> experiments = getExperimentsForAdmin(user, pm);
      if (experiments != null) {      
        for (Experiment experiment : experiments) {
          experimentDAOs.add(DAOConverter.createDAO(experiment));
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return experimentDAOs;
  }

  @SuppressWarnings("unchecked")
  private List<Experiment> getExperimentsForAdmin(User user, PersistenceManager pm) {
    Query q = pm.newQuery(Experiment.class);
    q.setFilter("admins == whoParam");
    q.declareParameters("String whoParam");
    return (List<Experiment>) q.execute(user.getEmail());         
  }

  public ExperimentStatsDAO statsForExperiment(Long experimentId, boolean justUser) {
    ExperimentStatsDAO stats = new ExperimentStatsDAO();
    if (!justUser) {
      stats.setJoinedEventsList(getJoinedForExperiment(experimentId));
    }
    getDailyResponseRateFor(experimentId, stats, justUser);
    System.out.println("leaving statsForExperiment");
    return stats;    
  }

  /**
   * @param experimentId
   * @param justUser 
   * @return
   */
  private void getDailyResponseRateFor(Long experimentId, ExperimentStatsDAO accum, 
      boolean justUser) {
    Map<DateMidnight, DateStat> dateStatsMap = Maps.newHashMap();
    Map<DateMidnight, Set<String>> sevenDayMap = Maps.newHashMap();
    
    int missedSignals = 0;
    long totalMillisToRespond = 0;
    String queryString = "";
    if (justUser) {
      queryString = "who="+getWho()+":";
    }
    List<EventDAO> events = getEventsForQuery(queryString + "experimentId="+experimentId);
    for(EventDAO event : events) {
      if (event.isJoinEvent()) {
        continue;
      }
      if (event.isMissedSignal()) {
        missedSignals++;
        continue;
      }
      totalMillisToRespond += event.responseTime();
      Date date = event.getResponseTime();
      if (date == null) {
        date = event.getWhen();
      }
      DateMidnight dateMidnight = new DateMidnight(date);
      
      // Daily response count
      DateStat currentStat = dateStatsMap.get(dateMidnight);
      if (currentStat == null) {
        currentStat = new DateStat(dateMidnight.toDate());
        currentStat.addValue(new Double(1));
        dateStatsMap.put(dateMidnight, currentStat);
      } else {
        List<Double> values = currentStat.getValues();
        values.set(0, values.get(0) + 1);
      }
      
      // 7 day counts
      DateMidnight beginningOfWeekDateMidnight = getBeginningOfWeek(dateMidnight);
      Set<String> current7Day = sevenDayMap.get(beginningOfWeekDateMidnight);
      if (current7Day == null) {
        current7Day = Sets.newHashSet();
        sevenDayMap.put(beginningOfWeekDateMidnight, current7Day);
      }
      current7Day.add(event.getWho());
    }
    // daily response
    ArrayList<DateStat> dateStats = Lists.newArrayList(dateStatsMap.values());
    Collections.sort(dateStats);
    for (DateStat dateStat : dateStats) {
      dateStat.computeStats();
    }
    // 7 day count
    ArrayList<DateStat> sevenDayDateStats = Lists.newArrayList();
    for (DateMidnight dateKey : sevenDayMap.keySet()) {
      int count = sevenDayMap.get(dateKey).size();
      DateStat dateStat = new DateStat(dateKey.toDate());
      dateStat.addValue(new Double(count));
      sevenDayDateStats.add(dateStat);
      dateStat.computeStats();
    }    
    Collections.sort(sevenDayDateStats);
    
    DateStat[] dsArray = new DateStat[dateStats.size()];
    accum.setDailyResponseRate(dateStats.toArray(dsArray));
    dsArray = new DateStat[sevenDayDateStats.size()];
    accum.setSevenDayDateStats(sevenDayDateStats.toArray(dsArray));
    
    String responseRateStr = "0%";
    int respondedSignals = events.size() - missedSignals;
    if (events.size() > 0) {      
      float responseRate = ((float)respondedSignals / (float)events.size()) * 100;
      responseRateStr = Float.toString(responseRate) + "%";
    }
    accum.setResponseRate(responseRateStr);
    float avgResponseTime = (float)totalMillisToRespond / (float)respondedSignals / 60000;
    accum.setResponseTime(Float.toString(Math.round(avgResponseTime)));
  }

  /**
   * @param dateMidnight
   * @return
   */
  private DateMidnight getBeginningOfWeek(DateMidnight dateMidnight) {
    int dow = dateMidnight.getDayOfWeek();
    int daysToBeginning = dow - DateTimeConstants.MONDAY;
    if (daysToBeginning != 0) {
      return dateMidnight.minusDays(daysToBeginning);
    }
    return dateMidnight;
  }

  /**
   * @param experimentId
   * @return
   */
  private EventDAO[] getJoinedForExperiment(Long experimentId) {
    List<EventDAO> eventsForQuery = getEventsForQuery("joined:experimentId="+ experimentId);
    HashSet<EventDAO> uniqueEvents = new HashSet<EventDAO>(eventsForQuery);
    EventDAO[] arr = new EventDAO[eventsForQuery.size()];
    return uniqueEvents.toArray(arr);
  }
  
  public List<ExperimentDAO> getUsersJoinedExperiments() {
      List<com.google.sampling.experiential.server.Query> queries = new QueryParser().parse("who=" +
          getWhoFromLogin().getEmail());
      List<Event> events = EventRetriever.getInstance().getEvents(queries, getWho(), 
          EventServlet.getTimeZoneForClient(getThreadLocalRequest()));
      Set<Long> experimentIds = Sets.newHashSet();
      for(Event event : events) {
        if (event.getExperimentId() == null) {
          continue; // legacy check
        }
        experimentIds.add(Long.parseLong(event.getExperimentId()));
      }
      List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
      if (experimentIds.size() == 0) {
        return experimentDAOs;
      }
      
      ArrayList<Long> idList = Lists.newArrayList(experimentIds);
      System.out.println("Found " + experimentIds.size() +" unique experiments where joined.");
      System.out.println(Joiner.on(",").join(idList));
      
      
      PersistenceManager pm = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Experiment.class, ":p.contains(id)");

        
        List<Experiment> experiments = (List<Experiment>) q.execute(idList); 
        System.out.println("Got back " + experiments.size() + " experiments");
        if (experiments != null) {      
          for (Experiment experiment : experiments) {
            experimentDAOs.add(DAOConverter.createDAO(experiment));
            idList.remove(experiment.getId().longValue());
          }
        }
        for (Long id : idList) {
          experimentDAOs.add(new ExperimentDAO(id, "Deleted Experiment Definition", "", "", "", 
              null, null, null, null, null, null, null, null, null, null, null, null));
        }
      } finally {
        if (pm != null) {
          pm.close();
        }
      }
      return experimentDAOs;
  }     
  
  private List<String> getIds(Set<String> experimentsForAdmin) {
    List<String> ids = Lists.newArrayList();
    for(String experimentId : experimentsForAdmin) {
      ids.add("'" + experimentId +"'");
    }
    return ids;
  }
}
