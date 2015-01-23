package com.google.sampling.experiential.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.datanucleus.store.appengine.query.JDOCursorHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;
import com.google.sampling.experiential.datastore.ExperimentVersionEntity;
import com.google.sampling.experiential.datastore.PublicExperimentList;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.ExperimentReference;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.Trigger;

public class ExperimentRetriever {

  private static final int DEFAULT_LIMIT_SIZE = 50;

  private static final int MAX_LIMIT_SIZE = 80;

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

  public Experiment getExperiment(String experimentIdStr) {
    Long longId = Long.valueOf(experimentIdStr);
    return getExperiment(longId);
  }

  Experiment getExperiment(Long longId) {
    PersistenceManager pm = null;
    try {
      if (longId != null) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(Experiment.class);
        q.setFilter("id == idParam");
        q.declareParameters("Long idParam");

        List<Experiment> experiments = (List<Experiment>) q.execute(longId);
        if (experiments.size() == 1) {
          Experiment experiment = experiments.get(0);
          triggerLoadingOfMemberObjects(experiment);
          return experiment;
        } else if (experiments.size() > 1) {
          String message = "There are multiple experiments for this id: " + longId;
          log.info(message);
          return null;
        } else if (experiments.size() < 1) {
          String message = "There are no experiments for this id: " + longId;
          log.info(message);
          return null;
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return null;
  }

  public List<Experiment> getExperimentsFor(List<Long> experimentIds) {
    List<Experiment> resultingExperiments = Lists.newArrayList();
    PersistenceManager pm = null;
    try {
      if (experimentIds != null && !experimentIds.isEmpty()) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(Experiment.class, ":p.contains(id)");
        List<Experiment> experiments = (List<Experiment>) q.execute(experimentIds);

        for (Experiment experiment : experiments) {
          triggerLoadingOfMemberObjects(experiment);
          resultingExperiments.add(experiment);
        }
      }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return resultingExperiments;
  }

  public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone dateTimeZone, Integer limit, String cursor) {
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();

    PersistenceManager pm = null;
    Transaction tx = null;
    try {
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      tx.begin();
      Query q = pm.newQuery(Experiment.class);
      q.setFilter("admins == whoParam");
      q.declareParameters("String whoParam");
      List<Experiment> experiments = (List<Experiment>) q.execute(email.toLowerCase());
      if (experiments != null) {
        for (Experiment experiment : experiments) {
          experimentDAOs.add(DAOConverter.createDAO(experiment));
        }
      }
      tx.commit();
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      if (pm != null) {
        pm.close();
      }
    }
    return new ExperimentQueryResult(cursor, experimentDAOs);
}

  // load related piecs before we close the Persistence Manager.
  // TODO eager load the experiment's object graph
  // we now need to actually access related objects for them to get loaded.
  // Also, defaultFetchGroup was causing errors. TODO: Revisit this in the future.
  private void triggerLoadingOfMemberObjects(Experiment experiment) {
    List<Feedback> feedback = experiment.getFeedback();
    feedback.get(0);
    List<Input> inputs = experiment.getInputs();
    inputs.get(0);
    SignalSchedule schedule = experiment.getSchedule();
    Trigger trigger = experiment.getTrigger();
    if (schedule != null) {
      schedule.getId();
    }
    if (trigger != null) {
      trigger.getId();
    }
  }

  public Experiment getReferredExperiment(Long referringExperimentId) {
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

  public boolean saveExperiment(ExperimentDAO experimentDAO, User loggedInUser, String userTz) {
    String loggedInUserEmail = loggedInUser.getEmail().toLowerCase();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Experiment experiment = null;

    // workaround for appengine bug where dependent entities are not deleted when assigned null.
    boolean deletingTrigger = false;
    boolean deletingSchedule = false;

    if (experimentDAO.getId() != null) {
      experiment = retrieveExperimentForDAO(experimentDAO, pm);
      SignalingMechanismDAO newSignalingMechanism = experimentDAO.getSignalingMechanisms()[0];
      if (experiment.getTrigger() != null && (!(newSignalingMechanism instanceof TriggerDAO))) {
        deletingTrigger = true;
      }
      if (experiment.getSchedule() != null && (!(newSignalingMechanism instanceof SignalScheduleDAO))) {
        deletingSchedule = true;
      }
    } else {
      experiment = new Experiment();
      experiment.setVersion(1);
    }

    if (experiment.getId() != null) {
      if (!hasAdministrativeRightsOnExperiment(loggedInUserEmail, experiment)) {
        return false;
      }

    }

    Transaction tx = null;
    boolean committed = false;
    try {
      tx = pm.currentTransaction();
      tx.begin();

      if (experiment.getId() != null) {
        incrementExperimentVersionNumber(experimentDAO, experiment);
      }

      if (deletingTrigger) {
        pm.deletePersistent(experiment.getTrigger());
      }
      if (deletingSchedule) {
        pm.deletePersistent(experiment.getSchedule());
      }

      DAOConverter.fromExperimentDAO(experimentDAO, experiment, loggedInUser);

      JDOHelper.makeDirty(experiment, "inputs");
      JDOHelper.makeDirty(experiment, "feedback");
      JDOHelper.makeDirty(experiment, "schedule");
      JDOHelper.makeDirty(experiment, "trigger");


      pm.makePersistent(experiment);


      tx.commit();
      committed  = true;
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }

    if (committed) {
      ExperimentVersionEntity.saveExperimentAsEntity(experiment);
      PublicExperimentList.updatePublicExperimentsList(experiment, TimeUtil.getNowInUserTimezone(userTz));

//      ExperimentCacheHelper.getInstance().clearCache(); // TODO do we need this
//      if (experiment.getPublished() && experiment.getPublishedUsers().size() == 0) {
//        ExperimentDAO newExperimentDAO = DAOConverter.createDAO(experiment);
//        ExperimentCacheHelper.getInstance().addPublicExperiment(newExperimentDAO);
//      }
    }
    return true;
  }

  private boolean hasAdministrativeRightsOnExperiment(String loggedInUserEmail, Experiment experiment) {
    return isExperimentAdministrator(loggedInUserEmail, experiment) || isSystemAdministrator();
  }

  private boolean isSystemAdministrator() {
    return UserServiceFactory.getUserService().isUserAdmin();
  }

  public static boolean isExperimentAdministrator(String loggedInUserEmail, Experiment experiment) {
    return experiment.getCreator().getEmail().toLowerCase().equals(loggedInUserEmail) ||
          experiment.getAdmins().contains(loggedInUserEmail);
  }

  private void incrementExperimentVersionNumber(ExperimentDAO experimentDAO, Experiment experiment) {
    Integer existingExperimentVersion = experiment.getVersion();
    if (existingExperimentVersion != null && existingExperimentVersion > experimentDAO.getVersion()) {
      throw new IllegalStateException("Experiment has already been edited!");
    } else {
      experiment.setVersion(existingExperimentVersion != null ? existingExperimentVersion + 1 : 1);
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

  public Boolean deleteExperiment(ExperimentDAO experimentDAO, String loggedInUserEmail) {
    if (experimentDAO.getId() == null) {
      return Boolean.FALSE;
    }

    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();

      Experiment experiment = retrieveExperimentForDAO(experimentDAO, pm);
      if (experiment == null || experiment.getId() == null) {
        return Boolean.FALSE;
      }
      if (!hasAdministrativeRightsOnExperiment(loggedInUserEmail, experiment)) {
            return Boolean.FALSE;
       }
       pm.deletePersistent(experiment);
       ExperimentCacheHelper.getInstance().clearCache();
       PublicExperimentList.deletePublicExperiment(experiment);
       return Boolean.TRUE;
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

public ExperimentQueryResult getAllJoinableExperiments(String email, DateTimeZone dateTimeZone, Integer limit, String cursorString) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query q = pm.newQuery(Experiment.class);
      if (cursorString != null) {
        Cursor cursor = Cursor.fromWebSafeString(cursorString);
        Map<String, Object> extensionMap = Maps.newHashMap();
        extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
        q.setExtensions(extensionMap);
      }
      q.setRange(0, limit != null ? Math.max(MAX_LIMIT_SIZE, limit) : DEFAULT_LIMIT_SIZE);

      List<Experiment> experiments = (List<Experiment>) q.execute();
      Cursor newCursor = JDOCursorHelper.getCursor(experiments);
      String newCursorString = newCursor.toWebSafeString();
      List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(experiments);
      markEndOfDayExperiments(pm, experimentDAOs);
      return new ExperimentQueryResult(newCursorString, filterSortAndSanitizeExperimentsUnavailableToUser(experimentDAOs, email, dateTimeZone));
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  private void markEndOfDayExperiments(PersistenceManager pm, List<ExperimentDAO> experimentDAOs) {
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
    PersistenceManager pm = null;
    try {
      //if (tz != null && !tz.isEmpty()) {
        pm = PMF.get().getPersistenceManager();
        javax.jdo.Query q = pm.newQuery(Experiment.class, ":p.contains(id)");
        List<Experiment> experiments = (List<Experiment>) q.execute(experimentIds);
        for (Experiment experiment : experiments) {
          triggerLoadingOfMemberObjects(experiment);
        }

        List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(experiments);
        experimentDAOs = filterSortAndSanitizeExperimentsUnavailableToUser(experimentDAOs, email, timezone);

        markEndOfDayExperiments(pm, experimentDAOs);
        return experimentDAOs;
     // }
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    //return Lists.newArrayList();
  }

  public ExperimentQueryResult getMyJoinableExperiments(String email, DateTimeZone dateTimeZone, Integer limit, String cursor) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      @SuppressWarnings("unchecked")
      List<Experiment> experiments = getExperimentsAdministeredBy(email, pm);
      Set<Experiment> experimentSet = Sets.newHashSet(experiments);

      List<Experiment> experimentsPublishedToMe = getExperimentsPublishedTo(email, pm);

      experimentSet.addAll(experimentsPublishedToMe);
      List<Experiment> totalSetAsList = Lists.newArrayList(experimentSet);
      List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(totalSetAsList);
//      markEndOfDayExperiments(pm, experiments);
      removeSensitiveFields(experimentDAOs);
      sortExperiments(experimentDAOs);
      List<ExperimentDAO> finalExperiments = filterFinishedAndDeletedExperiments(dateTimeZone, experimentDAOs);
      return new ExperimentQueryResult(cursor, finalExperiments);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  private List<Experiment> getExperimentsAdministeredBy(String email, PersistenceManager pm) {
    ExperimentJDOQuery jdoQuery = new ExperimentJDOQuery(pm.newQuery(Experiment.class));
    jdoQuery.addFilters("admins == idParam");
    jdoQuery.declareParameters("String idParam");
    jdoQuery.addParameterObjects(email);
    @SuppressWarnings("unchecked")
    List<Experiment> experiments = (List<Experiment>)jdoQuery.getQuery().execute(jdoQuery.getParameters());
    return experiments;
  }

  private List<Experiment> getExperimentsPublishedTo(String email, PersistenceManager pm) {
    ExperimentJDOQuery jdoQuery = new ExperimentJDOQuery(pm.newQuery(Experiment.class));
    jdoQuery.addFilters("publishedUsers == idParam", "published == true");
    jdoQuery.declareParameters("String idParam");
    jdoQuery.addParameterObjects(email);
    @SuppressWarnings("unchecked")
    List<Experiment> experiments = (List<Experiment>)jdoQuery.getQuery().execute(jdoQuery.getParameters());
    return experiments;
  }

  public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone dateTimeZone, Integer limit, String cursorString) {
    List<Long> publicExperimentIds = PublicExperimentList.getPublicExperiments(dateTimeZone.getID());

//    Iterable<String> publicExperimentIdStrings = Longs.stringConverter().reverse().convertAll(publicExperimentIds);
//    String idsInQuery = Joiner.on(",").join(publicExperimentIdStrings);
    if (publicExperimentIds.isEmpty()) {
      return new ExperimentQueryResult(null, new ArrayList<ExperimentDAO>());
    }


    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();

      List<Key> keysHere = Lists.newArrayList();
      List objIds = Lists.newArrayList();
      for (Long id : publicExperimentIds) {
//        Key key = KeyFactory.createKey(Experiment.class.getSimpleName(), id);
//        keysHere.add(key);
        objIds.add(pm.newObjectIdInstance(Experiment.class, id));
      }


      List<Experiment> experiments = (List<Experiment>) pm.getObjectsById(objIds);
//      Query newQuery = pm.newQuery("select from " + Experiment.class.getName() + " where :keys.contains(id)");
//      newQuery.setOrdering("title asc");
//
//      if (cursorString != null) {
//        Cursor cursor = Cursor.fromWebSafeString(cursorString);
//        Map<String, Object> extensionMap = Maps.newHashMap();
//        extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
//        newQuery.setExtensions(extensionMap);
//      }
//      newQuery.setRange(0, limit != null ? Math.min(MAX_LIMIT_SIZE, limit) : DEFAULT_LIMIT_SIZE);
//
//      log.severe("Query = " + newQuery.toString());
//      log.severe("Number of public experiment Ids = " + publicExperimentIds.size());
//      @SuppressWarnings("unchecked")
//      List<Experiment> experiments = (List<Experiment>) newQuery.execute(publicExperimentIds);

      Cursor newCursor = JDOCursorHelper.getCursor(experiments);
      String newCursorString = null;
      if (newCursor != null) {
        newCursorString = newCursor.toWebSafeString();
      }

      List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(experiments);
      markEndOfDayExperiments(pm, experimentDAOs);
      removeSensitiveFields(experimentDAOs);
      sortExperiments(experimentDAOs);
      // fakey pagination since we can load everything quickly in memory by id at the moment
      int startIndex = 0;
      if (cursorString != null) {
        try {
          startIndex = Integer.parseInt(cursorString);
        } catch (NumberFormatException e) {
          // no cursor for you...
        }
      }

      if (limit != null) {
        int endIndex = Math.min(startIndex + limit, experimentDAOs.size());
        if (endIndex < experimentDAOs.size()) {
          newCursorString = new Integer(endIndex).toString();
        } else {
          newCursorString = null;
        }
        experimentDAOs = experimentDAOs.subList(startIndex, endIndex);

      }
      //List<ExperimentDAO> results = filterFinishedAndDeletedExperiments(dateTimeZone, experimentDAOs);
      return new ExperimentQueryResult(newCursorString, experimentDAOs);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
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
      List<SignalTimeDAO> signalTimes = ((SignalScheduleDAO)signalingMechanismDAO).getSignalTimes();
      SignalTimeDAO lastSignalTime = signalTimes.get(signalTimes.size() - 1);
      if (lastSignalTime.getType() == SignalTimeDAO.FIXED_TIME) {
        return com.google.sampling.experiential.server.TimeUtil.getDateMidnightForDateString(experiment.getEndDate()).toDateTime().withMillisOfDay(lastSignalTime.getFixedTimeMillisFromMidnight());
      } else {
        return com.google.sampling.experiential.server.TimeUtil.getDateMidnightForDateString(experiment.getEndDate()).plusDays(1).toDateTime();
      }
    } else /* if (getScheduleType().equals(SCHEDULE_TYPE_ESM)) */{
      return com.google.sampling.experiential.server.TimeUtil.getDateMidnightForDateString(experiment.getEndDate()).plusDays(1).toDateTime();
    }
  }
}
