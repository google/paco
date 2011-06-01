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


import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.DateStat;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.ExperimentStatsDAO;
import com.google.sampling.experiential.shared.FeedbackDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.SignalScheduleDAO;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;


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

  public void saveEvent(String who, String when, String lat, String lon, 
      Map<String, String> kvPairs, boolean shared) {
    Date whenDate = parseDateString(when);
    // TODO (Once all data has been cleaned up, just send the kvPairs, and change the constructor)
    Set<What> whats = parseWhats(kvPairs);
    User loggedInWho = getWhoFromLogin();    
    if (loggedInWho == null || (who != null && !who.isEmpty() 
        && !loggedInWho.getEmail().equals(who))) {
      throw new IllegalArgumentException("Who passed in is not the logged in user!");
    }
    EventRetriever.getInstance().postEvent(loggedInWho.getEmail(), lat, lon, whenDate, "webform", 
        "1", whats, shared, null, null, null, null, null);
  }

  private boolean isCorpInstance() {
    return EventServlet.DEV_HOST.equals(getHostFromRequest());
  }
  
  private String getHostFromRequest() {
    return getThreadLocalRequest().getHeader("Host");
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
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd:HH:mm:ssZ");
    Date whenDate;
    try {
      whenDate = df.parse(when);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Cannot parse date");
    }
    return whenDate;
  }

  @SuppressWarnings("unchecked")
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
    fromExperimentDAO(experimentDAO, experiment);
    Transaction tx = null;
    try {
      tx = pm.currentTransaction();
      tx.begin();    
      pm.makePersistent(experiment);
      tx.commit();
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
    List<Experiment> experiments = (List<Experiment>)jdoQuery.getQuery().execute(
        jdoQuery.getParameters());
    experiment = experiments.get(0);
    return experiment;
  }

  private Experiment fromExperimentDAO(ExperimentDAO experimentDAO, Experiment experiment) {
    experiment.setTitle(experimentDAO.getTitle());
    experiment.setDescription(experimentDAO.getDescription());
    if (experiment.getCreator() == null) {
      experiment.setCreator(getWhoFromLogin());
    }
    experiment.setInformedConsentFormText(experimentDAO.getInformedConsentForm());
    experiment.setQuestionsChange(experimentDAO.getQuestionsChange());
    experiment.setFixedDuration(experimentDAO.getFixedDuration());
    Long startDateDAO = experimentDAO.getStartDate();
    Date startDate = null;
    if (startDateDAO != null) {
      startDate = new DateTime(startDateDAO).toDate();
    }
    experiment.setStartDate(startDate);
    
    Long endDateDAO = experimentDAO.getEndDate();
    Date endDate = null;
    if (endDateDAO != null) {
      endDate = new DateTime(endDateDAO).toDate();
    }
    experiment.setEndDate(endDate);
    
    experiment.setModifyDate(experimentDAO.getModifyDate() != null ? new Date(experimentDAO
        .getModifyDate()) : new Date());
    
    Key key = null;
    if (experiment.getId() != null) {
      key = KeyFactory.createKey(Experiment.class.getSimpleName(), experiment.getId());
    }

    experiment.setSchedule(fromScheduleDAO(key, experimentDAO.getSchedule()));
    experiment.setInputs(fromInputDAOs(key, experimentDAO.getInputs(), 
        experiment.getQuestionsChange()));
    experiment.setFeedback(fromFeedbackDAOs(key, experimentDAO.getFeedback()));
    
    experiment.setPublished(experimentDAO.getPublished());
    experiment.setPublishedUsers(Lists.newArrayList(experimentDAO.getPublishedUsers()));
    experiment.setAdmins(Lists.newArrayList(experimentDAO.getAdmins()));
    experiment.setDeleted(experimentDAO.getDeleted());
    return experiment;
  }
  
  /**
   * @param key
   * @param schedule
   * @return
   */
  private SignalSchedule fromScheduleDAO(Key key, SignalScheduleDAO scheduleDAO) {
    SignalSchedule schedule = new SignalSchedule(key, scheduleDAO.getId(),
        scheduleDAO.getScheduleType(), scheduleDAO.getEsmFrequency(), 
        scheduleDAO.getEsmPeriodInDays(), scheduleDAO.getEsmStartHour(), 
        scheduleDAO.getEsmEndHour(), Arrays.asList(scheduleDAO.getTimes()), 
        scheduleDAO.getRepeatRate(), scheduleDAO.getWeekDaysScheduled(), 
        scheduleDAO.getNthOfMonth(), scheduleDAO.getByDayOfMonth(), scheduleDAO.getDayOfMonth(), 
        scheduleDAO.getEsmWeekends());
    return schedule;
  }

  /**
   * @param experimentKey TODO
   * @param feedback
   * @return
   */
  private List<Feedback> fromFeedbackDAOs(Key experimentKey, FeedbackDAO[] feedbackDAOs) {
    List<Feedback> feedback = Lists.newArrayList();
    for (FeedbackDAO feedbackDAO : feedbackDAOs) {
      feedback.add(new Feedback(experimentKey, feedbackDAO.getId(), feedbackDAO.getFeedbackType(), 
          feedbackDAO.getText()));      
    }
    return feedback;
  }

  /**
   * @param questionsChange 
   * @param inputs
   * @return
   */
  private List<Input> fromInputDAOs(Key experimentKey, InputDAO[] inputDAOs, 
      boolean questionsChange) {
    List<Input> inputs = Lists.newArrayList();
    for (InputDAO input : inputDAOs) {
      Date scheduleDate = null;
      if (questionsChange) {
        scheduleDate = new Date(input.getScheduleDate());
      } else {
        scheduleDate = null;
      }
      inputs.add(new Input(experimentKey, input.getId(), input.getName(), input.getText(), 
          scheduleDate, input.getQuestionType(), input.getResponseType(), input.getLikertSteps(), 
          input.getMandatory(), input.getConditional(), input.getConditionExpression(), 
          input.getLeftSideLabel(), input.getRightSideLabel(), 
          Arrays.asList(input.getListChoices() != null ? input.getListChoices() : new String[0])));      
    }
    return inputs;
  }

  public Boolean deleteExperiment(ExperimentDAO experimentDAO) {
    System.out.println("Delete called for " + experimentDAO.getId());
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
    
      if (experimentDAO.getId() != null) {
        Experiment experiment = retrieveExperimentForDAO(experimentDAO, pm);
        pm.deletePersistent(experiment);
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

  @SuppressWarnings("unchecked")
  public List<ExperimentDAO> getExperimentsForUser() {
    return getExpermentsForUserWithQuery();    
  }

  private List<ExperimentDAO> getExpermentsForUserWithQuery() {
    User user = getWhoFromLogin();
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      List<Experiment> experiments = getExperimentsForAdmin(user, pm);
      if (experiments != null) {      
        for (Experiment experiment : experiments) {
          experimentDAOs.add(createDAO(experiment));
          if (experiment.getInformedConsentForm() != null) {
            experiment.getInformedConsentFormText();
            experiment.getFeedback().get(0).getLongText();
            pm.makePersistent(experiment);
          }

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

  public static ExperimentDAO createDAO(Experiment experiment) {
    Long id = experiment.getId();
    String title = experiment.getTitle();
    String description = experiment.getDescription();
    String informedConsentForm = experiment.getInformedConsentFormText();
    String email = experiment.getCreator().getEmail();
    
    Boolean published = experiment.getPublished();

    SignalSchedule schedule = experiment.getSchedule();
    
    SignalScheduleDAO signalScheduleDAO = null;
    // BACKWard compatibility friendliness - create a schedule for this experiment
    if (schedule == null) {
      signalScheduleDAO = new SignalScheduleDAO();
      signalScheduleDAO.setScheduleType(SignalScheduleDAO.SELF_REPORT);
      published = Boolean.FALSE; // don't make old experiments available for download
    } else {
      signalScheduleDAO = createSignalScheduleDAO(schedule);
    }
    Boolean fixedDuration = experiment.getFixedDuration();
    Boolean questionsChange = experiment.getQuestionsChange();
    Boolean deleted = experiment.getDeleted();
    Long startDate = experiment.getStartDate() != null ? experiment.getStartDate().getTime() : null;
    Long endDate = experiment.getEndDate() != null ? experiment.getEndDate().getTime() : null;
    String hash = experiment.getHash();
    Long joinDate = experiment.getJoinDate() != null ? experiment.getJoinDate().getTime() : null;
    Long modifyDate = experiment.getModifyDate() != null ? experiment.getModifyDate().getTime() : 
      null;
    
    List<String> admins = experiment.getAdmins();
    String[] adminStrArray = new String[admins.size()];
    adminStrArray = admins.toArray(adminStrArray);
    
    List<String> userEmails = experiment.getPublishedUsers();
    String[] userEmailsStrArray = new String[userEmails.size()];
    userEmailsStrArray = userEmails.toArray(userEmailsStrArray);
    
    ExperimentDAO dao = new ExperimentDAO(id, title, 
        description, 
        informedConsentForm, 
        email, 
        signalScheduleDAO, fixedDuration, 
        questionsChange,  
        startDate, 
        endDate, 
        hash, 
        joinDate, 
        modifyDate, published, adminStrArray, userEmailsStrArray, deleted);
    List<Input> inputs = experiment.getInputs();
//    Collections.sort(inputs, new Comparator<Input>() {
//      @Override
//      public int compare(Input o1, Input o2) {
//        Long nextInputIdFor1 = o1.getNextInputId();
//        Long nextInputIdFor2 = o2.getNextInputId();
//        if (nextInputIdFor1 == null && nextInputIdFor2 == null) {
//          return 0;
//        } else if (nextInputIdFor1 == null) {
//          return 1;
//        } else if (nextInputIdFor2 == null) {
//          return -1;
//        } else if (nextInputIdFor1 == nextInputIdFor2){
//          return 0;
//        } else if (nextInputIdFor1 > nextInputIdFor2) {
//          return -1;
//        } else {
//          return 1;
//        }
//      }      
//    });
    InputDAO[] inputDAOs = new InputDAO[inputs.size()];
    for (int i=0; i < inputs.size(); i++) {      
      inputDAOs[i] = createDAO(inputs.get(i));
    }
    dao.setInputs(inputDAOs);
    
    FeedbackDAO[] fbDAOs = new FeedbackDAO[experiment.getFeedback().size()];
    for (int i=0; i < experiment.getFeedback().size(); i++) {
      fbDAOs[i] = createDAO(experiment.getFeedback().get(i));
    }
    dao.setFeedback(fbDAOs);
    return dao;
  }
  
  /**
   * @param schedule
   * @return
   */
  private static SignalScheduleDAO createSignalScheduleDAO(SignalSchedule schedule) {
    SignalScheduleDAO dao = new SignalScheduleDAO(schedule.getId().getId(),
        schedule.getScheduleType(), schedule.getByDayOfMonth(), schedule.getDayOfMonth(),
        schedule.getEsmEndHour(), schedule.getEsmFrequency(), schedule.getEsmPeriodInDays(),
        schedule.getEsmStartHour(), schedule.getNthOfMonth(), schedule.getRepeatRate(),
        toArray(schedule.getTimes()), schedule.getWeekDaysScheduled(), schedule.getEsmWeekends());
    
    return dao;
  }

  /**
   * @param times
   * @return
   */
  private static Long[] toArray(List<Long> times) {
    Long[] res = new Long[times.size()];
    return times.toArray(res);
  }

  public static FeedbackDAO createDAO(Feedback feedback) {
    return new FeedbackDAO(feedback.getId().getId(), feedback.getFeedbackType(), 
        feedback.getLongText());
  }
  
  public static InputDAO createDAO(Input input) {
    return new InputDAO(input.getId().getId(), 
        input.getName(),
        input.getQuestionType(),
        input.getResponseType(), 
        input.getText(), 
        input.getMandatory(), 
        input.getScheduleDate() != null ? input.getScheduleDate().getTime() : null, 
        input.getLikertSteps(),
        input.getConditional(), input.getConditionalExpression(), 
        input.getLeftSideLabel(), input.getRightSideLabel(), toStringArray(input.getListChoices()));
  }
  
  /**
   * @param listChoices
   * @return
   */
  private static String[] toStringArray(List<String> listChoices) {
    if (listChoices == null) {
      return new String[0];
    }
    String[] res = new String[listChoices.size()];
    return listChoices.toArray(res);
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
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    
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
    //TODO clean up this java type failing
    List<EventDAO> eventsForQuery = getEventsForQuery("joined:experimentId="+ experimentId);
    EventDAO[] arr = new EventDAO[eventsForQuery.size()];
    return eventsForQuery.toArray(arr);
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
            experimentDAOs.add(createDAO(experiment));
            if (experiment.getInformedConsentForm() != null) {
              experiment.getInformedConsentFormText();
              experiment.getFeedback().get(0).getLongText();
              pm.makePersistent(experiment);
            }
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
