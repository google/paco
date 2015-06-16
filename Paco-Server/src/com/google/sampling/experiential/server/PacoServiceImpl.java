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

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;
import com.google.sampling.experiential.shared.PacoService;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.ValidationMessage;


/*
 * * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PacoServiceImpl extends RemoteServiceServlet implements PacoService {

  public List<EventDAO> eventsForUser() {
    List<Event> result = EventRetriever.getInstance().getEvents(getWho());
    return EventRetriever.convertEventsToDAOs(result);
  }

  private String getWho() {
    User whoFromLogin = getWhoFromLogin();
    if (!isCorpInstance() && whoFromLogin == null) {
      throw new IllegalArgumentException("Must be logged in.");
    }
    if (whoFromLogin != null) {
      return whoFromLogin.getEmail().toLowerCase();
    }
    return null;
  }

  public List<EventDAO> eventSearch(String tags) {
    return getEventsForQuery(tags);
  }

  private List<EventDAO> getEventsForQuery(String tags) {
    List<com.google.sampling.experiential.server.Query> queries = new QueryParser().parse(tags);
    List<Event> result = EventRetriever.getInstance().getEvents(queries, getWho(),
        getTimeZoneOnClient(), 0, 20000);
    return EventRetriever.convertEventsToDAOs(result);
  }

  public void saveEvent(String who,
      String scheduledTime,
      String responseTime,
      String experimentId,
      Map<String, String> kvPairs,
      Integer experimentVersion,
      boolean shared, String groupName, Long actionTriggerId, Long actionTriggerSpecId, Long actionId) {

    Date scheduledTimeDate = scheduledTime != null ? parseDateString(scheduledTime) : null;
    Date responseTimeDate = responseTime != null ? parseDateString(responseTime) : null;
    Date whenDate = new Date();
    // TODO (Once all data has been cleaned up, just send the kvPairs, and change the constructor)
    Set<What> whats = parseWhats(kvPairs);
    User loggedInWho = getWhoFromLogin();
    if (loggedInWho == null || (who != null && !who.isEmpty()
        && !loggedInWho.getEmail().toLowerCase().equals(who.toLowerCase()))) {
      throw new IllegalArgumentException("Who passed in is not the logged in user!");
    }

    ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(Long.parseLong(experimentId));

    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }

    if (!experiment.isWhoAllowedToPostToExperiment(loggedInWho.getEmail().toLowerCase())) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");
    }

    try {
      String tz = null;
      DateTimeZone timeZoneForClient = getTimeZoneOnClient();
      if (timeZoneForClient != null) {
        tz = timeZoneForClient.getID();
      }

      // TODO fix this to just pass timezone all the way through
      EventRetriever.getInstance().postEvent(loggedInWho.getEmail().toLowerCase(), null, null, whenDate, "webform",
          "", whats, shared, experimentId, null, experimentVersion, responseTimeDate, scheduledTimeDate, null, tz,
          groupName, actionTriggerId, actionTriggerSpecId, actionId);
    } catch (Throwable e) {
      throw new IllegalArgumentException("Could not post Event: ", e);
    }
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
  public Outcome saveExperiment(ExperimentDAO experimentDAO, String timezone) {
    Outcome outcome = new Outcome(0);
    User loggedInUser = getWhoFromLogin();
    DateTimeZone timezoneForId = getTimeZoneOnClient();
    if (timezoneForId != null) {
      timezoneForId = DateTimeZone.forID(timezone);
    }
    List<ValidationMessage> saveExperimentErrorResults = ExperimentServiceFactory.getExperimentService().saveExperiment(experimentDAO,
                                                                                                                        loggedInUser.getEmail().toLowerCase(),
                                                                                                                        timezoneForId);
    if (saveExperimentErrorResults != null) {
      StringBuilder buf = new StringBuilder();
      for (ValidationMessage validationMessage : saveExperimentErrorResults) {
        buf.append(validationMessage.toString());
        buf.append("\n");
      }
      outcome.setError("Could not save experiment:" + experimentDAO.getId()
                       + ". title: " + experimentDAO.getTitle() +"\nErrors:\n" + buf.toString());
    }

    return outcome;
  }

  public boolean deleteExperiment(ExperimentDAO experimentDAO) {
    System.out.println("Delete called for " + experimentDAO.getId());
    User loggedInUser = getWhoFromLogin();
    String loggedInUserEmail = loggedInUser.getEmail().toLowerCase();
    return ExperimentServiceFactory.getExperimentService().deleteExperiment(experimentDAO, loggedInUserEmail);
  }

  public ExperimentQueryResult getUsersAdministeredExperiments(Integer limit, String cursor) {
    String loggedInEmail = getWhoFromLogin().getEmail().toLowerCase();
    return ExperimentServiceFactory.getExperimentService().getUsersAdministeredExperiments(loggedInEmail, getTimeZoneOnClient(), limit, cursor);
  }

  public ExperimentStatsDAO statsForExperiment(Long experimentId, boolean justUser) {
    ExperimentStatsDAO stats = new ExperimentStatsDAO();

    String queryString = "";
    if (justUser) {
      queryString = "who="+getWho()+":";
    }
    List<EventDAO> events = getEventsForQuery(queryString + "experimentId="+experimentId);

    if (!justUser) {
      HashSet<EventDAO> uniqueEvents = new HashSet<EventDAO>();
      for (EventDAO eventDAO : events) {
        if (eventDAO.isJoinEvent()) {
          uniqueEvents.add(eventDAO);
        }
      }

      EventDAO[] arr = new EventDAO[uniqueEvents.size()];
      EventDAO[] joinedForExperiment = uniqueEvents.toArray(arr);
      stats.setJoinedEventsList(joinedForExperiment);
    }
    getDailyResponseRateFor(experimentId, events, stats, justUser);
    return stats;
  }

  /**
   * @param experimentId
   * @param justUser
   * @return
   */
  private void getDailyResponseRateFor(Long experimentId, List<EventDAO> events,
                                       ExperimentStatsDAO accum,
      boolean justUser) {
    Map<DateMidnight, DateStat> dateStatsMap = Maps.newHashMap();
    Map<DateMidnight, Set<String>> sevenDayMap = Maps.newHashMap();

    int missedSignals = 0;
    long totalMillisToRespond = 0;
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

  public ExperimentQueryResult getUsersJoinedExperiments(Integer limit, String cursor) {
      String loggedInUserEmail = getWhoFromLogin().getEmail().toLowerCase();

      final DateTimeZone timeZoneOnClient = getTimeZoneOnClient();

      return ExperimentServiceFactory.getExperimentService().getMyJoinedExperiments(loggedInUserEmail, timeZoneOnClient, 1000, cursor);
  }

  private DateTimeZone getTimeZoneOnClient() {
    return TimeUtil.getTimeZoneForClient(getThreadLocalRequest());
  }

  @Override
  public void saveEvent(EventDAO event) {
    User loggedInWho = getWhoFromLogin();
    if (loggedInWho == null) {
      throw new IllegalArgumentException("Not logged in");
    }

    if (event == null) {
      return;
    }
    String who = event.getWho();
    if (who != null) {
      who = who.toLowerCase();
    }
    if (who != null && !who.isEmpty() && !loggedInWho.getEmail().equals(who)) {
      throw new IllegalArgumentException("Who passed in is not the logged in user!");
    }

    Long experimentId = event.getExperimentId();
    if (experimentId == null) {
      throw new IllegalArgumentException("Invalid event. No experiment id.");
    }
    Integer experimentVersion = event.getExperimentVersion();
    Date scheduledTimeDate = event.getScheduledTime();
    Date responseTimeDate = event.getResponseTime();
    Date whenDate = new Date();

    String groupName = event.getExperimentGroupName();
    Long actionTriggerId = event.getActionTriggerId();
    Long actionTriggerSpecId = event.getActionTriggerSpecId();
    Long actionId = event.getActionId();

    Set<What> whats = parseWhats(event.getWhat());


    ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(experimentId);

    if (experiment == null) {
      throw new IllegalArgumentException("Must post to an existing experiment!");
    }

    if (!experiment.isWhoAllowedToPostToExperiment(loggedInWho.getEmail())) {
      throw new IllegalArgumentException("This user is not allowed to post to this experiment");
    }


    try {
      String tz = event.getTimezone();
      String experimentName = experiment.getTitle();
      EventRetriever.getInstance().postEvent(loggedInWho.getEmail().toLowerCase(), null, null, whenDate, "webform",
          "1", whats, event.isShared(), Long.toString(experimentId), experimentName,
          experimentVersion, responseTimeDate, scheduledTimeDate, null, tz,
          groupName, actionTriggerId, actionTriggerSpecId, actionId);
    } catch (Throwable e) {
      throw new IllegalArgumentException("Could not post Event: ", e);
    }
  }

  @Override
  public ExperimentDAO referencedExperiment(Long referringExperimentId) {
    return ExperimentServiceFactory.getExperimentService().getReferredExperiment(referringExperimentId);
  }

  @Override
  public void setReferencedExperiment(Long referringExperimentId, Long referencedExperimentId) {
    ExperimentServiceFactory.getExperimentService().setReferredExperiment(referringExperimentId, referencedExperimentId);
  }

  @Override
  public Map<Date, EventDAO> getEndOfDayEvents(String queryText) {
    List<EventDAO> events = eventSearch(queryText);
    Map<Date, EventDAO> eventsByDateMap = new EndOfDayEventProcessor().breakEventDAOsIntoDailyPingResponses(events);
    return eventsByDateMap;
  }

  @Override
  public ExperimentQueryResult getMyJoinableExperiments(String tz, Integer limit, String cursor) {
    return ExperimentServiceFactory.getExperimentService().getMyJoinableExperiments(getWhoFromLogin().getEmail().toLowerCase(),  getTimeZoneOnClient(), limit, cursor);
  }


  @Override
  public boolean joinExperiment(Long experimentId) {
    User loggedInWho = getWhoFromLogin();
    if (loggedInWho == null) {
      throw new IllegalArgumentException("Not logged in");
    }

    if (experimentId == null) {
      throw new IllegalArgumentException("Must supply experiment Id");
    }

    ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(experimentId);

    if (experiment == null) {
      throw new IllegalArgumentException("Unknown experiment!");
    }

    String lowerCase = loggedInWho.getEmail().toLowerCase();
    if (!experiment.isWhoAllowedToPostToExperiment(lowerCase)) {
      throw new IllegalArgumentException("This user is not allowed to join this experiment");
    }

    try {
      String tz = null;
      DateTimeZone timezone = getTimeZoneOnClient();
      if (timezone != null) {
        tz = timezone.toString();
      }
      Date responseTimeDate;
      if (tz != null) {
        responseTimeDate = new DateTime().withZone(timezone).toDate();
      } else {
        responseTimeDate = new Date();
      }

      String experimentName = experiment.getTitle();
      Set<What> whats = Sets.newHashSet();
      whats.add(new What("joined", "true"));

      for (ExperimentGroup group : experiment.getGroups()) {
        String groupSchedule = "schedule_" + group.getName();
        StringBuilder scheduleString = new StringBuilder();

        List<String> actionTriggerStrings = Lists.newArrayList();
        List<ActionTrigger> actionTriggers = group.getActionTriggers();
        for (ActionTrigger actionTrigger : actionTriggers) {
          actionTriggerStrings.add(actionTrigger.toString());
        }
        String scheduleAsString = Joiner.on(",").join(actionTriggerStrings);

        whats.add(new What(groupSchedule, scheduleAsString));
      }



      EventRetriever.getInstance().postEvent(lowerCase, null, null, new Date(), "webform",
          "1", whats, false, Long.toString(experimentId), experimentName, experiment.getVersion(), responseTimeDate, null, null, tz, null, null, null, null);

      // create entry in table with user Id, experimentId, joinDate

      return true;
    } catch (Throwable e) {
      throw new IllegalArgumentException("Could not join experiment: ", e);
    }
  }
}
