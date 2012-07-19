/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ExperimentStats;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.TimeUtil;


/*
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MapServiceImpl extends RemoteServiceServlet implements MapService {

  @Override
  public List<Event> map() {
    return EventRetriever.getInstance().getEvents(getWho());
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

  @Override
  public List<Event> mapWithTags(String tags) {
    return getEventsForQuery(tags);
  }

  private List<Event> getEventsForQuery(String tags) {
    List<com.google.sampling.experiential.server.Query> queries = new QueryParser().parse(tags);
    return EventRetriever.getInstance()
        .getEvents(getWho(), queries, EventServlet.getTimeZoneForClient(getThreadLocalRequest()));
  }

  @Override
  public void saveEvent(String who,
      String scheduledTime,
      String responseTime,
      String experimentId,
      Map<String, String> whats,
      boolean shared) {
    Date scheduledTimeDate = scheduledTime != null ? parseDateString(scheduledTime) : null;
    Date responseTimeDate = responseTime != null ? parseDateString(responseTime) : null;
    Date whenDate = new Date();
    // TODO (Once all data has been cleaned up, just send the kvPairs, and change the constructor)
    User loggedInWho = getWhoFromLogin();
    if (loggedInWho == null
        || (who != null && !who.isEmpty() && !loggedInWho.getEmail().equals(who))) {
      throw new IllegalArgumentException("Who passed in is not the logged in user!");
    }

    Experiment experiment = ExperimentRetriever.getExperiment(experimentId);

    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }

    if (!ExperimentRetriever.isWhoAllowedToPostToExperiment(experiment, loggedInWho.getEmail())) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");
    }

    EventRetriever.getInstance().postEvent(loggedInWho.getEmail(),
        null,
        null,
        whenDate,
        "webform",
        "1",
        whats,
        shared,
        experimentId,
        null,
        responseTimeDate,
        scheduledTimeDate,
        null);
  }

  private boolean isCorpInstance() {
    return false;
    // is it possible to forge host headers?
    // return EventServlet.DEV_HOST.equals(getHostFromRequest());
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
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
  public void saveExperiment(Experiment experiment) {
    if (experiment.getId() != null) {
      User loggedInUser = getWhoFromLogin();
      String loggedInUserEmail = loggedInUser.getEmail();
      if (!(experiment.getCreator().equals(loggedInUser)
          || experiment.getAdmins().contains(loggedInUserEmail))) {
        // TODO (Bobevans): return a signal here that they are no longer allowed to edit this
        // experiment;
        return;
      }
    }

    DAO.getInstance().createExperiment(experiment);
  }

  @Override
  public Boolean deleteExperiment(Experiment experiment) {
    System.out.println("Delete called for " + experiment.getId());

    if (experiment.getId() != null) {
      DAO.getInstance().deleteExperiment(experiment);
      ExperimentCacheHelper.getInstance().clearCache();
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  @Override
  public List<Experiment> getExperimentsForUser() {
    return getExperimentsForUserWithQuery();
  }

  private List<Experiment> getExperimentsForUserWithQuery() {
    User user = getWhoFromLogin();
    return DAO.getInstance().getObserversExperiments(user.getEmail());
  }

  @Override
  public ExperimentStats statsForExperiment(Long experimentId, boolean justUser) {
    ExperimentStats stats = new ExperimentStats();
    if (!justUser) {
      stats.setJoinedEventsList(getJoinedForExperiment(experimentId));
    }
    getDailyResponseRateFor(experimentId, stats, justUser);
    System.out.println("leaving statsForExperiment");
    return stats;
  }

  private void getDailyResponseRateFor(Long experimentId, ExperimentStats accum, boolean justUser) {
    Map<DateMidnight, DateStat> dateStatsMap = Maps.newHashMap();
    Map<DateMidnight, Set<String>> sevenDayMap = Maps.newHashMap();

    int missedSignals = 0;
    long totalMillisToRespond = 0;
    String queryString = "";
    if (justUser) {
      queryString = "who=" + getWho() + ":";
    }
    List<Event> events = getEventsForQuery(queryString + "experimentId=" + experimentId);
    for (Event event : events) {
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
      float responseRate = ((float) respondedSignals / (float) events.size()) * 100;
      responseRateStr = Float.toString(responseRate) + "%";
    }
    accum.setResponseRate(responseRateStr);
    float avgResponseTime = (float) totalMillisToRespond / (float) respondedSignals / 60000;
    accum.setResponseTime(Float.toString(Math.round(avgResponseTime)));
  }

  private DateMidnight getBeginningOfWeek(DateMidnight dateMidnight) {
    int dow = dateMidnight.getDayOfWeek();
    int daysToBeginning = dow - DateTimeConstants.MONDAY;
    if (daysToBeginning != 0) {
      return dateMidnight.minusDays(daysToBeginning);
    }
    return dateMidnight;
  }

  private Event[] getJoinedForExperiment(Long experimentId) {
    List<Event> eventsForQuery = getEventsForQuery("joined:experimentId=" + experimentId);
    HashSet<Event> uniqueEvents = new HashSet<Event>(eventsForQuery);
    Event[] arr = new Event[eventsForQuery.size()];
    return uniqueEvents.toArray(arr);
  }

  @Override
  public List<Experiment> getUsersJoinedExperiments() {
    List<com.google.sampling.experiential.server.Query> queries =
        new QueryParser().parse("who=" + getWhoFromLogin().getEmail());
    List<Event> events = EventRetriever.getInstance()
        .getEvents(getWho(), queries, EventServlet.getTimeZoneForClient(getThreadLocalRequest()));
    Set<Long> experimentIds = Sets.newHashSet();
    for (Event event : events) {
      if (event.getExperimentId() == null) {
        continue; // legacy check
      }
      experimentIds.add(Long.parseLong(event.getExperimentId()));
    }
    ArrayList<Long> idList = Lists.newArrayList(experimentIds);
    System.out.println("Found " + experimentIds.size() + " unique experiments where joined.");
    System.out.println(Joiner.on(",").join(idList));

    List<Experiment> experiments = DAO.getInstance().getExperiments(idList);
    System.out.println("Got back " + experiments.size() + " experiments");
    return experiments;
  }
}
