package com.google.sampling.experiential.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.google.sampling.experiential.datastore.PublicExperimentList.CursorExerimentIdListPair;
import com.google.sampling.experiential.model.Event;
import com.pacoapp.paco.shared.model.SignalTimeDAO;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentIdQueryResult;
import com.pacoapp.paco.shared.model2.ExperimentJoinQueryResult;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.ExperimentValidator;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.Pair;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.model2.ValidationMessage;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;

class DefaultExperimentService implements ExperimentService {

  private static final Logger log = Logger.getLogger(DefaultExperimentService.class.getName());

  private String getExperimentAsJson(Long id) {
    return ExperimentJsonEntityManager.getExperiment(id);
  }


  @Override
  /**
   * This is to be used server side only because it does not scrub out participant id information.
   */
  public ExperimentDAO getExperiment(Long id) {
    String experimentJson = getExperimentAsJson(id);
    if (experimentJson != null) {
      return  JsonConverter.fromSingleEntityJson(experimentJson);
    }
    return null;
  }

  @Override
  public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
    List<Long> allowedExperimentIds = Lists.newArrayList();
    for (Long experimentId : experimentIds) {
      if (ExperimentAccessManager.isUserAllowedToGetExperiments(experimentId, email)) {
        allowedExperimentIds.add(experimentId);
      }
    }

    List<String> experimentJsons = getExperimentsByIdAsJson(allowedExperimentIds, email, timezone);
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (String experimentJson : experimentJsons) {
      if (experimentJson != null) {
        experiments .add(JsonConverter.fromSingleEntityJson(experimentJson));
      }
    }
    removeNonAdminData(email, experiments);
    return experiments;
  }

  protected List<ExperimentDAO> getExperimentsByIdInternal(List<Long> experimentIds, String email, DateTimeZone timezone) {
    List<String> experimentJsons = getExperimentsByIdAsJson(experimentIds, email, timezone);
    log.info("Got back " + experimentJsons.size() +" jsons for " + experimentIds.size() + " ids ( " + Joiner.on(",").join(experimentIds) + ")");
    return turnJsonsIntoExperiments(experimentJsons);
  }

//  protected ExperimentQueryResult getExperimentsByIdInternalSorted(List<Long> experimentIds, String cursor) {
//    com.pacoapp.paco.shared.util.ExperimentHelper.Pair<List<String>, String> experimentJsonsResult = getExperimentsByIdAsJsonSorted(experimentIds, cursor);
//    log.info("Got back " + experimentJsonsResult.first.size() +" jsons for " + experimentIds.size() + " ids ( " + Joiner.on(",").join(experimentIds) + ")");
//    List<ExperimentDAO> experiments = turnJsonsIntoExperiments(experimentJsonsResult.first);
//    return new ExperimentQueryResult(cursor, experiments);
//  }


  private List<ExperimentDAO> turnJsonsIntoExperiments(List<String> experimentJsons) {
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (String experimentJson : experimentJsons) {
      if (experimentJson != null) {
        final ExperimentDAO fromSingleEntityJson = JsonConverter.fromSingleEntityJson(experimentJson);
        if (fromSingleEntityJson != null) {
          experiments.add(fromSingleEntityJson);
          log.info("Retrieved experimentDAO from json for experiment: " + fromSingleEntityJson.getTitle());
        } else {
          log.severe("could not recreate experiment for experiment data: " + experimentJson);
        }
      }
    }
    return experiments;
  }


  protected List<String> getExperimentsByIdAsJson(List<Long> experimentIds, String email, DateTimeZone timezone) {
 //   TODO who can access this call and in what role?
    // is email a participant or an admin?
    return ExperimentJsonEntityManager.getExperimentsById(experimentIds);
  }

//  protected com.pacoapp.paco.shared.util.ExperimentHelper.Pair<List<String>, String> getExperimentsByIdAsJsonSorted(List<Long> experimentIds,
//                                                                                                                    String cursor) {
//    // TODO who can access this call and in what role?
//    // is email a participant or an admin?
//    return ExperimentJsonEntityManager.getExperimentsByIdSortedByTitle(experimentIds, cursor);
//  }

  protected ExperimentQueryResult getExperimentsByAdminAsJsonSorted(String admin, Integer limit, String cursor, String sortColumn, String sortOrder) {
    // TODO who can access this call and in what role?
    // is email a participant or an admin?
    com.pacoapp.paco.shared.util.ExperimentHelper.Pair<List<String>, String> experimentJsonsResult = ExperimentJsonEntityManager.getExperimentsByAdminSorted(admin, limit, cursor, sortColumn, sortOrder);
    log.info("Got back " + experimentJsonsResult.first.size() +" jsons");
    List<ExperimentDAO> experiments = turnJsonsIntoExperiments(experimentJsonsResult.first);
    return new ExperimentQueryResult(experimentJsonsResult.second, experiments);
  }

  protected ExperimentQueryResult getExperimentsByIdAsJsonSorted(List<Long> experimentIds, Integer limit, String cursor, String sortColumn, String sortOrder) {
    // TODO who can access this call and in what role?
    // is email a participant or an admin?
    com.pacoapp.paco.shared.util.ExperimentHelper.Pair<List<String>, String> experimentJsonsResult = ExperimentJsonEntityManager.getExperimentsByIdSorted(experimentIds, limit, cursor, sortColumn, sortOrder);
    log.info("Got back " + experimentJsonsResult.first.size() +" jsons");
    List<ExperimentDAO> experiments = turnJsonsIntoExperiments(experimentJsonsResult.first);
    return new ExperimentQueryResult(experimentJsonsResult.second, experiments);
  }

  // save experiments
  @Override
  public List<ValidationMessage> saveExperiment(ExperimentDAO experiment,
                                                String loggedInUserEmail,
                                                DateTimeZone timezone) {
    return saveExperiment(experiment, loggedInUserEmail, timezone, true);
  }

  @Override
  public List<ValidationMessage> saveExperiment(ExperimentDAO experiment,
                                                String loggedInUserEmail,
                                                DateTimeZone timezone,
                                                Boolean validate) {
    if (ExperimentAccessManager.isUserAllowedToSaveExperiment(experiment.getId(), loggedInUserEmail)) {
      ensureIdsOnActionTriggerObjects(experiment);
      lowercaseAllEmailAddresses(experiment);

      if (validate) {
        ExperimentValidator validator = new ExperimentValidator();
        experiment.validateWith(validator);
        List<ValidationMessage> results = validator.getResults();
        if (!results.isEmpty()) {
          return results;
        }
      }

      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      Transaction tx = ds.beginTransaction(options);
      try {
        if (experiment.getId() == null) {
          experiment.setCreator(loggedInUserEmail);
        }
        if (!experiment.getAdmins().contains(loggedInUserEmail)) {
          experiment.getAdmins().add(loggedInUserEmail);
        }
        if (Strings.isNullOrEmpty(experiment.getContactEmail())) {
          experiment.setContactEmail(experiment.getCreator());
        }
        Integer version = experiment.getVersion();
        if (version == null || version == 0) {
          version = 1;
        } else {
          version++;
        }
        experiment.setVersion(version);

        final long millis = new DateTime().getMillis();
        experiment.setModifyDate(com.pacoapp.paco.shared.util.TimeUtil.formatDate(millis));
        Key experimentKey = ExperimentJsonEntityManager.saveExperiment(ds, tx, JsonConverter.jsonify(experiment),
                                                                       experiment.getId(),
                                                                       experiment.getTitle(),
                                                                       experiment.getVersion(),
                                                                       millis,
                                                                       experiment.getAdmins());

        experiment.setId(experimentKey.getId());
        ExperimentAccessManager.updateAccessControlEntities(ds, tx, experiment, experimentKey, timezone);
        sendToCloudSqlQueue(experiment, loggedInUserEmail);
        tx.commit();
        return null;
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      } finally {
        if (tx.isActive()) {
          tx.rollback();
        }
      }
    } else {
      throw new IllegalStateException(loggedInUserEmail + " does not have permission to edit " + experiment.getTitle());
    }
  }

  public void sendToCloudSqlQueue(ExperimentDAO experiment, String loggedInUserEmail) {
    Queue queue = QueueFactory.getQueue("cloud-sql");
    TaskOptions to = TaskOptions.Builder.withUrl("/csExpInsert").payload(JsonConverter.jsonify(experiment));
    if (EnvironmentUtil.isDevInstance()) {
      log.info("In dev instance task sent to Queue");
      queue.add(to.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname("mapreduce", null)));
    } else {
      queue.add(to);
    }
  }


  private void lowercaseAllEmailAddresses(ExperimentDAO experiment) {
    experiment.setAdmins(ExperimentAccessManager.lowerCaseEmails(experiment.getAdmins()));
    if (experiment.getPublishedUsers() != null && !experiment.getPublishedUsers().isEmpty()) {
      experiment.setPublishedUsers(ExperimentAccessManager.lowerCaseEmails(experiment.getPublishedUsers()));
    }
    experiment.setCreator(experiment.getCreator().toLowerCase());
  }

  private void ensureIdsOnActionTriggerObjects(ExperimentDAO experiment) {
    // ill-formed experiments will be handled next in the validation phase before saving.
    long id = new Date().getTime();
    List<ExperimentGroup> groups = experiment.getGroups();
    if (groups == null) {
      return;
    }
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
      if (actionTriggers != null) {
        for (ActionTrigger actionTrigger : actionTriggers) {
          if (actionTrigger.getId() == null) {
            actionTrigger.setId(id++);
          }
          List<PacoAction> actions = actionTrigger.getActions();
          if (actions != null) {

            for (PacoAction pacoAction : actions) {
              if (pacoAction.getId() == null) {
                pacoAction.setId(id++);
              }
            }
          }
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
            List<Schedule> schedules = scheduleTrigger.getSchedules();
            if (schedules != null) {
              for (Schedule schedule : schedules) {
                if (schedule.getId() == null) {
                  schedule.setId(id++);
                }
              }
            }
          } else if (actionTrigger instanceof InterruptTrigger) {
            InterruptTrigger interruptTrigger = (InterruptTrigger)actionTrigger;
            List<InterruptCue> cues = interruptTrigger.getCues();
            for (InterruptCue interruptCue : cues) {
              if (interruptCue.getId() == null) {
                interruptCue.setId(id++);
              }
            }
          }
        }
      }
    }

  }


  // delete experiments
  @Override
  public Boolean deleteExperiment(Long experimentId, String loggedInUserEmail)  {
    if (experimentId == null) {
      throw new IllegalArgumentException("Cannot delete experiment: " + experimentId + " because it does not exist");
    }
    if (ExperimentAccessManager.isUserAllowedToDeleteExperiment(experimentId, loggedInUserEmail)) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      Transaction tx = ds.beginTransaction(options);
      try {
        ExperimentJsonEntityManager.delete(ds, tx, experimentId);
        ExperimentAccessManager.deleteAccessControlEntitiesFor(ds, tx, experimentId);
        tx.commit();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (tx.isActive()) {
          tx.rollback();
        }
      }
      return false;
    }
    throw new IllegalStateException(loggedInUserEmail + " does not have permission to delete " + experimentId);
  }

  @Override
  public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
    return deleteExperiment(experimentDAO.getId(), loggedInUserEmail);
  }

  @Override
  public Boolean deleteExperiments(List<Long> experimentIds, String email) {
    if (experimentIds == null || experimentIds.isEmpty()) {
      throw new IllegalArgumentException("No ids specified for deletion");
    }
    if (ExperimentAccessManager.isUserAllowedToDeleteExperiments(experimentIds, email)) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      Transaction tx = ds.beginTransaction(options);
      try {
        ExperimentJsonEntityManager.delete(ds, tx, experimentIds);
        ExperimentAccessManager.deleteAccessControlEntitiesFor(ds, tx, experimentIds);
        tx.commit();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (tx.isActive()) {
          tx.rollback();
        }
      }
      return false;
    }
    throw new IllegalStateException(email + " does not have permission to delete one or more of the experiments: " + Joiner.on(",").join(experimentIds));
  }

  @Override
  public ExperimentQueryResult getMyJoinableExperiments(String email, DateTimeZone timeZoneForClient,
                                                        Integer limit, String cursor) {
    // TODO figure out what to do about getting a paginated result over two tables. Right now, return everything.
    // Actually, the experiment hub will get rid of this broad query
    ExperimentIdQueryResult adminExperimentIdQueryResult = ExperimentAccessManager.getExistingExperimentIdsForAdmin(email, 0, null);
    List<Long> experimentIds = Lists.newArrayList();
    experimentIds.addAll(adminExperimentIdQueryResult.getExperiments());

    ExperimentIdQueryResult existingPublishedExperimentIdsForUser = ExperimentAccessManager.getExistingPublishedExperimentIdsForUser(email, 0, null);
    experimentIds.addAll(existingPublishedExperimentIdsForUser.getExperiments());

    List<ExperimentDAO> experiments = getExperimentsByIdInternal(experimentIds, email, timeZoneForClient);
    experiments = removeEnded(experiments, timeZoneForClient);
    removeNonAdminData(email, experiments);

    // for now, use the cursor as an offset to return the requested subset.
    int offset = 0;
    if (!Strings.isNullOrEmpty(cursor)) {
      try {
        offset = Integer.parseInt(cursor);
      } catch (NumberFormatException e) {
        offset = 0;
      }
    }
    if (limit != null && limit > 0) {
      int end = Math.min(offset + limit, experiments.size());
      List<ExperimentDAO> experimentSubset = experiments.subList(offset, end);
      if (end < experiments.size()) {
        cursor = Integer.toString(end);
      } else {
        cursor = null;
      }
      return new ExperimentQueryResult(cursor, experimentSubset);
    } else {
      return new ExperimentQueryResult(null, experiments);
    }

  }

  private List<ExperimentDAO> removeEnded(List<ExperimentDAO> experiments, DateTimeZone timeZoneForClient) {
    List<ExperimentDAO> keepers = Lists.newArrayList();
    DateMidnight now = DateTime.now().withZone(timeZoneForClient).toDateMidnight();
    for (ExperimentDAO experimentDAO : experiments) {
      final DateTime latestEndDate = getLatestEndDate(experimentDAO);
      if (latestEndDate == null || !now.isAfter(latestEndDate)) {
        keepers.add(experimentDAO);
      }
    }
    return keepers;
  }


  private DateTime getLatestEndDate(ExperimentDAO experimentDAO) {
    return ActionScheduleGenerator.getLastEndTime(experimentDAO);
  }


  @Override
  public ExperimentQueryResult getMyJoinedExperiments(String email, DateTimeZone timeZoneForClient,
                                                        Integer limit, String cursor, String sortColumn, String sortOrder) {
    //TODO stopping at 30 because we cannot do an inline request for more than 30 experiments.
    // maybe there is another way to do it where we are pulling all joined table ids for user but then loading by 30 from
    // experiment table and resorting in memory.
    // The expectation is that users would not normally be joining more than 30 experiments.
    // The other option is that if you are an admin, we always make you look in the admin list. That is lame.
    ExperimentJoinQueryResult experimentIdJoinDatePairs = ExperimentAccessManager.getJoinedExperimentsFor(email, limit == null ? 1000 : limit, cursor);

    if (experimentIdJoinDatePairs.getExperiments() == null ||
            // we got zero back, so we haven't updated yet. This is true if there is no cursor. If there is a cursor, we have just exhausted the list.
            (experimentIdJoinDatePairs.getExperiments().size() == 0 && cursor == null)) {
      List<Event> events = getJoinEventsForLoggedInUser(email, timeZoneForClient);
      List<Pair<Long, Date>> uniqueExperimentIdJoinDatePairs = getUniqueExperimentIdAndJoinDates(events);
      if (uniqueExperimentIdJoinDatePairs.size() > 0) {
        ExperimentAccessManager.addJoinedExperimentsFor(email, uniqueExperimentIdJoinDatePairs);
      }
      experimentIdJoinDatePairs.setExperiments(uniqueExperimentIdJoinDatePairs);
    }

    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    if (experimentIdJoinDatePairs.getExperiments().size() == 0) {
      return new ExperimentQueryResult(null, experimentDAOs);
    }

    Map<Long, Date> experimentIds = Maps.newHashMap();

    for (Pair<Long,Date> pair: experimentIdJoinDatePairs.getExperiments()) {
      experimentIds.put(pair.first, pair.second);
    }
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(Lists.newArrayList(experimentIds.keySet()), email, timeZoneForClient);
    removeNonAdminData(email, experiments);
    addJoinDate(experiments, experimentIds);
    return new ExperimentQueryResult(experimentIdJoinDatePairs.getCursor(), experiments); // TODO honor the limit and cursor
  }

  private void addJoinDate(List<ExperimentDAO> experiments, Map<Long, Date> experimentIds) {
    // Are the experiments returned in the order the ids were sent?
    // Can't say for sure so n^2 it is
    for (ExperimentDAO experiment : experiments) {
      Date date = experimentIds.get(experiment.getId());
      experiment.setJoinDate(com.pacoapp.paco.shared.util.TimeUtil.formatDate(date.getTime()));
    }
  }


  public static List<Pair<Long, Date>> getUniqueExperimentIdAndJoinDates(List<Event> events) {
    Set<Pair<Long, Date>> experimentIds = Sets.newHashSet();
    for(Event event : events) {
      if (event.getExperimentId() == null) {
        continue; // legacy check
      }
      long experimentId = Long.parseLong(event.getExperimentId());
      Date joinDate = event.getResponseTime();
      Pair<Long, Date> pair = new Pair<Long, Date>(experimentId, joinDate);
      experimentIds.add(pair);
    }
    return Lists.newArrayList(experimentIds);
  }

  public static List<Event> getJoinEventsForLoggedInUser(String loggedInUserEmail, DateTimeZone timeZoneOnClient) {
    List<com.google.sampling.experiential.server.Query> queries = new QueryParser().parse("who=" + loggedInUserEmail);
    List<Event> events = EventRetriever.getInstance().getEvents(queries, loggedInUserEmail, timeZoneOnClient, 0, 20000);
    System.out.println("DEFAULT EVENT SIZE: " + events.size());

    Map<String, Event> eventsByExperimentId = Maps.newHashMap();
    for (Event event : events) {
      if (event.isJoined()) {
        String experimentId = event.getExperimentId();
        if (experimentId == null) {
          continue;
        }
        final Event eventWithSameId = eventsByExperimentId.get(experimentId);
        if (eventWithSameId != null) {
          if (eventWithSameId.getResponseTime().before(event.getResponseTime())) {
            eventsByExperimentId.put(event.getExperimentId(), event);
          }
        } else {
          eventsByExperimentId.put(event.getExperimentId(), event);
        }
      }
    }
    return Lists.newArrayList(eventsByExperimentId.values());
  }


  @Override
  public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                               String cursor, String sortColumn, String sortOrder) {
    //return getUsersAdministeredExperimentsPagingById(email, timezone, limit, cursor);
    //return getUsersAdministeredExperimentsSorted(email, timezone, limit, cursor);
    return getExperimentsByAdminAsJsonSorted(email, limit == null ? 0 : limit, cursor, sortColumn, sortOrder);
  }


  @Override
  public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
    CursorExerimentIdListPair cursorIdPair = PublicExperimentList.getPublicExperiments(timezone.getID(), limit, cursor);
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(cursorIdPair.ids, null, timezone);
    removeNonAdminData(email, experiments);
    return new ExperimentQueryResult(cursorIdPair.cursor, experiments);
  }

  public ExperimentQueryResult getExperimentsPublishedPubliclyNew(DateTimeZone timezone, Integer limit, String cursor, String email) {
    CursorExerimentIdListPair cursorIdPair = PublicExperimentList.getPublicExperimentsNew(timezone.getID(), limit, cursor);
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(cursorIdPair.ids, null, timezone);
    removeNonAdminData(email, experiments);
    return new ExperimentQueryResult(cursorIdPair.cursor, experiments);
  }

  public ExperimentQueryResult getExperimentsPublishedPubliclyPopular(DateTimeZone timezone, Integer limit, String cursor, String email) {
    CursorExerimentIdListPair cursorIdPair = PublicExperimentList.getPublicExperimentsPopular(timezone.getID(), limit, cursor);
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(cursorIdPair.ids, null, timezone);
    removeNonAdminData(email, experiments);
    return new ExperimentQueryResult(cursorIdPair.cursor, experiments);
  }

  public void removeNonAdminData(String email, List<ExperimentDAO> experiments) {
    removeOtherPublished(experiments, email);
    removeAdmins(experiments, email);
    removeCreator(experiments, email);
  }


  private void removeOtherPublished(List<ExperimentDAO> experiments, String email) {
    for (ExperimentDAO experimentDAO : experiments) {
      if (!experimentDAO.getAdmins().contains(email)) {
        List<String> empty = Lists.newArrayList();
        experimentDAO.setPublishedUsers(empty);
      }
    }
  }

  private void removeCreator(List<ExperimentDAO> experiments, String email) {
    for (ExperimentDAO experimentDAO : experiments) {
      if (!experimentDAO.getAdmins().contains(email)) {
        String contact = experimentDAO.getContactEmail();
        if (Strings.isNullOrEmpty(contact)) {
          experimentDAO.setContactEmail(experimentDAO.getCreator());
        } else {
          experimentDAO.setCreator(null);
        }
      }
    }
  }

  private void removeAdmins(List<ExperimentDAO> experiments ,String email) {
    for (ExperimentDAO experimentDAO : experiments) {
      if (!experimentDAO.getAdmins().contains(email)) {
        List<String> empty = Lists.newArrayList();
        experimentDAO.setAdmins(empty);
      }
    }
  }

  // referred experiments
  @Override
  public ExperimentDAO getReferredExperiment(long referredId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
    // TODO Auto-generated method stub

  }



  //utility apis that probably belong somewhere else
  @Override
  public boolean isOver(ExperimentDAO experiment, DateTime nowInUserTimezone) {
    for (ExperimentGroup group : experiment.getGroups()) {
      boolean groupIsOver = group.getFixedDuration() != null && group.getFixedDuration()
              && getEndDateTime(group).isBefore(nowInUserTimezone);

      if (!groupIsOver) {
        return false;
      }
    }
    return true;
  }

  private DateTime getEndDateTime(ExperimentGroup group) {
    DateTime lastEndDate = null;
    List<ActionTrigger> activeTriggers = group.getActionTriggers();
    for (ActionTrigger actionTrigger : activeTriggers) {
      if (actionTrigger instanceof InterruptTrigger) {
        DateTime thisActionTriggerEndDate = TimeUtil.getDateMidnightForDateString(group.getEndDate()).plusDays(1).toDateTime();
        lastEndDate = latestOf(lastEndDate, thisActionTriggerEndDate);
      } else {
        ScheduleTrigger scheduleTrigger = (ScheduleTrigger) actionTrigger;
        for (Schedule schedule : scheduleTrigger.getSchedules()) {
          if (schedule.getScheduleType().equals(Schedule.WEEKDAY)) {
            List<SignalTime> signalTimes = schedule.getSignalTimes();
            SignalTime lastSignalTime = signalTimes.get(signalTimes.size() - 1);
            if (lastSignalTime.getType() == SignalTimeDAO.FIXED_TIME) {
              DateTime thisScheduleEndDate = TimeUtil.getDateMidnightForDateString(group.getEndDate()).toDateTime().withMillisOfDay(lastSignalTime.getFixedTimeMillisFromMidnight());
              lastEndDate = latestOf(lastEndDate, thisScheduleEndDate);
            } else {
              DateTime thisScheduleEndDate = TimeUtil.getDateMidnightForDateString(group.getEndDate()).plusDays(1).toDateTime();
              lastEndDate = latestOf(lastEndDate, thisScheduleEndDate);
            }
          } else /* if (schedule.getScheduleType().equals(SCHEDULE_TYPE_ESM)) */{
            DateTime thisActionTriggerEndDate = TimeUtil.getDateMidnightForDateString(group.getEndDate()).plusDays(1).toDateTime();
            lastEndDate = latestOf(lastEndDate, thisActionTriggerEndDate);
          }
        }
      }
    }
    return lastEndDate;
  }

  private DateTime latestOf(DateTime lastEndDate, DateTime thisActionTriggerEndDate) {
    if (lastEndDate == null || thisActionTriggerEndDate.isAfter(lastEndDate)) {
      return thisActionTriggerEndDate;
    } else {
      return lastEndDate;
    }
  }


  @Override
  public ExperimentQueryResult getAllExperiments(String cursor) {
    ExperimentQueryResult result = new ExperimentQueryResult();
    ExperimentHelper.Pair<String, List<String>> jsonResults = ExperimentJsonEntityManager.getAllExperiments(cursor);

    List<ExperimentDAO> experiments = Lists.newArrayList();
    List<String> experimentEntities = jsonResults.second;
    for (String experimentJson : experimentEntities) {
      experiments.add(JsonConverter.fromSingleEntityJson(experimentJson));
    }

    result.setExperiments(experiments);
    result.setCursor(jsonResults.first);
    return result;
  }


}