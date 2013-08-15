package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.sampling.experiential.datastore.ExperimentEntityPersistence;
import com.google.sampling.experiential.datastore.ExperimentVersionEntity;
import com.google.sampling.experiential.model.ExperimentReference;

public class ExperimentRetriever {

  private static final Logger log = Logger.getLogger(ExperimentRetriever.class.getName());

  private static ExperimentRetriever instance;

  public synchronized static ExperimentRetriever getInstance() {
    if (instance == null) {
      instance = new ExperimentRetriever();
    }
    return instance;
  }

  @VisibleForTesting
  ExperimentRetriever() {};

  public ExperimentDAO getExperiment(String experimentIdStr) {
    Long longId = Long.valueOf(experimentIdStr);
    return getExperiment(longId);
  }

  ExperimentDAO getExperiment(Long longId) {
      if (longId != null) {
        ExperimentDAO experiment = ExperimentEntityPersistence.getExperimentById(longId);
        if (experiment != null) {
          return experiment;
        } else {
          String message = "There are no experiments for this id: " + longId;
          log.info(message);
          throw new IllegalArgumentException(message);
        }
      }
    return null;
  }

  public List<ExperimentDAO> getExperimentsFor(List<Long> experimentIds) {
    return ExperimentEntityPersistence.getExperimentsByIds(experimentIds);
  }

  public ExperimentDAO getReferredExperiment(Long referringExperimentId) {
    PersistenceManager pm = null;
    try {
      if (referringExperimentId != null) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(ExperimentReference.class);
        q.setFilter("referringId == idParam");
        q.declareParameters("Long idParam");
        List<ExperimentReference> experimentRefs = (List<ExperimentReference>) q.execute(Long.valueOf(referringExperimentId));
        if (experimentRefs.size() == 1) {
          ExperimentReference experimentRef = experimentRefs.get(0);
          return getExperiment(Long.toString(experimentRef.getReferencedExperimentId()));
        } else if (experimentRefs.size() > 1) {
          String message = "There are multiple experiments references for referring id: " + referringExperimentId;
          log.info(message);
          //throw new IllegalArgumentException(message);
        } else if (experimentRefs.size() < 1) {
          String message = "There are no experiments references for referring id: " + referringExperimentId;
          log.info(message);
          //throw new IllegalArgumentException(message);
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return null;
  }

  public void setReferredExperiment(Long referringExperimentId, Long referencedExperimentId) {
    if (referringExperimentId == null || referencedExperimentId == null) {
      throw new IllegalArgumentException("Nust have two valid ids for referencing");
    }
    ExperimentReference ref = new ExperimentReference(referringExperimentId, referencedExperimentId);
    PersistenceManager pm = null;
    try {
        pm = PMF.get().getPersistenceManager();
        pm.makePersistent(ref);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public static void removeSensitiveFields(List<ExperimentDAO> availableExperiments) {
    for (ExperimentDAO experimentDAO : availableExperiments) {
      experimentDAO.setPublishedUsers(null);
      experimentDAO.setAdmins(null);
    }
  }

  public static boolean arrayContains(String[] strings, String targetString) {
    for (int i = 0; i < strings.length; i++) {
      if (strings[i].toLowerCase().equals(targetString.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private List<ExperimentDAO> filterSortAndSanitizeExperimentsUnavailableToUser(List<ExperimentDAO> experiments, String email, DateTimeZone dateTimeZone) {
    List<ExperimentDAO> availableExperiments = Lists.newArrayList();
    for (ExperimentDAO experiment : experiments) {
      String creatorEmail = experiment.getCreator().toLowerCase();
      boolean isCreator = creatorEmail.equals(email);
      boolean isAdmin = ExperimentRetriever.arrayContains(experiment.getAdmins(), email);
      boolean isPublished = experiment.getPublished() != null && experiment.getPublished() == true;
      boolean isPublishedToAll = experiment.getPublishedUsers().length == 0;
      boolean isPublishedUser = ExperimentRetriever.arrayContains(experiment.getPublishedUsers(), email);

      if (isCreator || isAdmin || (isPublished && (isPublishedToAll || isPublishedUser))) {
        availableExperiments.add(experiment);
      }
    }

    sortExperiments(availableExperiments);
    ExperimentRetriever.removeSensitiveFields(availableExperiments);
    return filterFinishedAndDeletedExperiments(dateTimeZone, availableExperiments);
  }

  public static void sortExperiments(List<ExperimentDAO> availableExperiments) {
    Collections.sort(availableExperiments, new Comparator<ExperimentDAO>() {
      @Override
      public int compare(ExperimentDAO o1, ExperimentDAO o2) {
        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
      }
    });
  }

  public boolean saveExperiment(ExperimentDAO experimentDAO, User loggedInUser) {
    String loggedInUserEmail = loggedInUser.getEmail().toLowerCase();
    ExperimentEntityPersistence.saveExperiment(experimentDAO, loggedInUserEmail);
    ExperimentVersionEntity.saveExperimentVersion(experimentDAO);
    ExperimentCacheHelper.getInstance().clearCache();
    addAnyNewPeopleToTheWhitelist(experimentDAO);
    return true;
  }

  private boolean isSystemAdministrator() {
    return UserServiceFactory.getUserService().isUserAdmin();
  }

  private void addAnyNewPeopleToTheWhitelist(ExperimentDAO experiment) {
    List<String> publishedUsers = Lists.newArrayList(experiment.getPublishedUsers());
    List<String> adminUsers = Lists.newArrayList(experiment.getAdmins());
    publishedUsers.addAll(adminUsers);
    // TODO make this less clumsy
    new DBWhitelist().addAllUsers(publishedUsers);
  }

  public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
    if (experimentDAO.getId() == null) {
      return Boolean.FALSE;
    }

    ExperimentEntityPersistence.deleteExperiment(experimentDAO, loggedInUserEmail);
    ExperimentCacheHelper.getInstance().clearCache();
    return Boolean.TRUE;
  }

//  public List<ExperimentDAO> getAllJoinableExperiments(String email, DateTimeZone dateTimeZone) {
//    PersistenceManager pm = null;
//    try {
//      pm = PMF.get().getPersistenceManager();
//      javax.jdo.Query q = pm.newQuery(Experiment.class);
//      List<Experiment> experiments = (List<Experiment>) q.execute();
//      List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(experiments);
//      markEndOfDayExperiments(experimentDAOs);
//      return filterSortAndSanitizeExperimentsUnavailableToUser(experimentDAOs, email, dateTimeZone);
//    } finally {
//      if (pm != null) {
//        pm.close();
//      }
//    }
//  }

  private void markEndOfDayExperiments(List<ExperimentDAO> experimentDAOs) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Long> referringIds = Lists.newArrayList();
    List<ExperimentReference> references = (List<ExperimentReference>) pm.newQuery(ExperimentReference.class).execute();
    for (ExperimentReference experimentReference : references) {
      referringIds.add(experimentReference.getReferringExperimentId());
    }

    for (ExperimentDAO experiment : experimentDAOs) {
      experiment.setWebRecommended(referringIds.contains(experiment.getId()));
    }
  }

  public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
    List<ExperimentDAO> experimentDAOs = ExperimentEntityPersistence.getExperimentsByIds(experimentIds);
    experimentDAOs = filterSortAndSanitizeExperimentsUnavailableToUser(experimentDAOs, email, timezone);
    markEndOfDayExperiments(experimentDAOs);
    return experimentDAOs;
  }

  public List<ExperimentDAO> getMyJoinableExperiments(String email, DateTimeZone dateTimeZone) {
    List<ExperimentDAO> admined = ExperimentEntityPersistence.getExperimentsAdministeredBy(email);
    List<ExperimentDAO> publishedTo = ExperimentEntityPersistence.getExperimentsPublishedTo(email);

    HashSet<ExperimentDAO> both = Sets.newHashSet(admined);
    both.addAll(publishedTo);

    List<ExperimentDAO> bothAsList = Lists.newArrayList(both);

//      markEndOfDayExperiments(pm, experiments);
      removeSensitiveFields(bothAsList);
      sortExperiments(bothAsList);
      return filterFinishedAndDeletedExperiments(dateTimeZone, bothAsList);
  }

  private List<ExperimentDAO> filterFinishedAndDeletedExperiments(DateTimeZone dateTimeZone, List<ExperimentDAO> experiments) {
    List<ExperimentDAO> joinable = Lists.newArrayList(experiments);
    DateTime nowInUserTimezone = TimeUtil.getNowInUserTimezone(dateTimeZone);
    for (ExperimentDAO experiment : experiments) {
      if (experiment.getDeleted() != null && experiment.getDeleted() || isOver(experiment, nowInUserTimezone)) {
        joinable.remove(experiment);
      }
    }
    return joinable;
  }

  // TODO is it safe to send the joda time class info as part of the DAO when using GWT? It did not used to be serializable over gwt.
  // This is the reason we are doing this here instead of on the dao class where it belongs.
  public boolean isOver(ExperimentDAO experiment, DateTime nowInUserTimezone) {
    return experiment.getFixedDuration() != null && experiment.getFixedDuration()
           && getEndDateTime(experiment).isBefore(nowInUserTimezone);
  }

  private DateTime getEndDateTime(ExperimentDAO experiment) {
    SignalingMechanismDAO signalingMechanismDAO = experiment.getSignalingMechanisms()[0];
    if (signalingMechanismDAO instanceof SignalScheduleDAO
            && ((SignalScheduleDAO) signalingMechanismDAO).getScheduleType().equals(SignalScheduleDAO.WEEKDAY)) {
      Long[] times = ((SignalScheduleDAO)signalingMechanismDAO).getTimes();
      Arrays.sort(times);
      DateTime lastTimeForDay = new DateTime().plus(times[times.length - 1]);
      return com.google.sampling.experiential.server.TimeUtil.getDateMidnightForDateString(experiment.getEndDate()).toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /* if (getScheduleType().equals(SCHEDULE_TYPE_ESM)) */{
      return com.google.sampling.experiential.server.TimeUtil.getDateMidnightForDateString(experiment.getEndDate()).plusDays(1).toDateTime();
    }
  }

  public List<ExperimentDAO> getAdminedExperiments(String userLoggedInEmail) {
    return ExperimentEntityPersistence.getExperimentsAdministeredBy(userLoggedInEmail);
  }

  public List<ExperimentDAO> getNewAllJoinableExperiments(String email, DateTimeZone timezone) {
    Set<ExperimentDAO> allExperiments = Sets.newHashSet();
    allExperiments.addAll(ExperimentEntityPersistence.getExperimentsPublishedToAll(timezone));
    allExperiments.addAll(ExperimentEntityPersistence.getExperimentsAdministeredBy(email));
    allExperiments.addAll(ExperimentEntityPersistence.getExperimentsPublishedTo(email));
    List<ExperimentDAO> newArrayList = Lists.newArrayList(allExperiments);
    markEndOfDayExperiments(newArrayList);
    sortExperiments(newArrayList);
    ExperimentRetriever.removeSensitiveFields(newArrayList);
    return filterFinishedAndDeletedExperiments(timezone, newArrayList);
  }

  public List<ExperimentDAO> getExperimentsPublishedToAll(DateTimeZone timezone) {
    List<ExperimentDAO> experimentsPublishedToAll = ExperimentEntityPersistence.getExperimentsPublishedToAll(timezone);

    return experimentsPublishedToAll;
  }
}
