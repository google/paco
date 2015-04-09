package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.users.User;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.paco.shared.model2.ActionTrigger;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.ExperimentQueryResult;
import com.google.paco.shared.model2.ExperimentValidator;
import com.google.paco.shared.model2.InterruptTrigger;
import com.google.paco.shared.model2.JsonConverter;
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.google.paco.shared.model2.SignalTime;
import com.google.paco.shared.model2.ValidationMessage;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.google.sampling.experiential.datastore.PublicExperimentList.CursorExerimentIdListPair;

class DefaultExperimentService implements ExperimentService {

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
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (String experimentJson : experimentJsons) {
      if (experimentJson != null) {
        experiments .add(JsonConverter.fromSingleEntityJson(experimentJson));
      }
    }
    return experiments;
  }


  protected List<String> getExperimentsByIdAsJson(List<Long> experimentIds, String email, DateTimeZone timezone) {
 //   TODO who can access this call and in what role?
    // is email a participant or an admin?
    return ExperimentJsonEntityManager.getExperimentsById(experimentIds);
  }


  // save experiments
  @Override
  public List<ValidationMessage> saveExperiment(ExperimentDAO experiment, User userFromLogin, DateTimeZone timezone) {
    ExperimentValidator validator = new ExperimentValidator();
    experiment.validateWith(validator);
    List<ValidationMessage> results = validator.getResults();
    if (!results.isEmpty()) {
      return results;
    }

    String loggedInUserEmail = userFromLogin.getEmail().toLowerCase();
    if (ExperimentAccessManager.isUserAllowedToSaveExperiment(experiment.getId(), loggedInUserEmail)) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      Transaction tx = ds.beginTransaction(options);
      try {
        if (experiment.getId() == null) {
          experiment.setCreator(loggedInUserEmail);
          if (!experiment.getAdmins().contains(loggedInUserEmail)) {
            experiment.getAdmins().add(loggedInUserEmail);
          }
        }
        if (Strings.isNullOrEmpty(experiment.getContactEmail())) {
          experiment.setContactEmail(experiment.getCreator());
        }

        Key experimentKey = ExperimentJsonEntityManager.saveExperiment(ds, tx, JsonConverter.jsonify(experiment),
                                                                       experiment.getId(),
                                                                       experiment.getTitle(),
                                                                       experiment.getVersion());
        experiment.setId(experimentKey.getId());
        ExperimentAccessManager.updateAccessControlEntities(ds, tx, experiment, experimentKey, timezone);
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
    }
    throw new IllegalStateException(loggedInUserEmail + " does not have permission to edit " + experiment.getTitle());

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
    List<Long> experimentIds = ExperimentAccessManager.getExistingExperimentsIdsForAdmin(email);
    experimentIds.addAll(ExperimentAccessManager.getExistingPublishedExperimentIdsForUser(email));
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(experimentIds, email, timeZoneForClient);
    removeNonAdminData(email, experiments);
    return new ExperimentQueryResult(cursor, experiments); // TODO honor the limit and cursor
  }


  @Override
  public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone timezone, Integer limit,
                                                               String cursor) {
    List<Long> experimentIds = ExperimentAccessManager.getExistingExperimentsIdsForAdmin(email);
    List<ExperimentDAO> experiments = getExperimentsByIdInternal(experimentIds, email, timezone);
    return new ExperimentQueryResult(cursor, experiments); // TODO honor the limit and cursor
  }

  @Override
  public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
    CursorExerimentIdListPair cursorIdPair = PublicExperimentList.getPublicExperiments(timezone.getID(), limit, cursor);
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


}