package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mortbay.log.Log;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.ExperimentReference;

public class ExperimentCacheHelper {

  public static final String EXPERIMENT_CACHE_KEY = "EXPERIMENT_CACHE_KEY";
  
  public static final Logger log = Logger.getLogger(ExperimentCacheHelper.class.getName());


  private static ExperimentCacheHelper instance;

  public static synchronized ExperimentCacheHelper getInstance() {
    if (instance == null) {
      instance = new ExperimentCacheHelper();
    }
    return instance;
  }

  private Cache cache = null;

  private ExperimentCacheHelper() {
    super();
    createCache();
  }

  private void createCache() {
    try {
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      cache = cacheFactory.createCache(Collections.emptyMap());
    } catch (CacheException e) {
      Log.warn("Could not get a cache in the ExperimentCacheHelper ctor: " + e.getMessage());
    }
  }

  public void clearCache() {
    if (cache != null) {
      cache.clear();
    }
  }

  public String getExperimentsJsonForUser(String userId) {
    String experimentsJson = null;
    if (cache != null) {
      experimentsJson = (String) cache.get(userId + EXPERIMENT_CACHE_KEY);
    }
    return experimentsJson;
  }

  public void putExperimentJsonForUser(String userId, String experimentsJson) {
    if (cache != null) {
      cache.put(userId + EXPERIMENT_CACHE_KEY, experimentsJson);
    }
  }

  public List<ExperimentDAO> getJoinableExperiments(String tz) {
    List<ExperimentDAO> experiments = getExperiments();
    List<ExperimentDAO> joinable = Lists.newArrayList(experiments);
    
    DateTime now = getDateForEndOfExperiments(tz);
    
    for (ExperimentDAO experiment : experiments) {
      if (experiment.getDeleted() != null && experiment.getDeleted() || isOver(experiment, now)) {
        joinable.remove(experiment);
      }
    }
    return joinable;
  }

  private DateTime getDateForEndOfExperiments(String tz) {
    DateTime now = new DateTime();
    if (tz != null) {
      DateTimeZone timeZone = DateTimeZone.forID(tz);
      if (timeZone != null) { 
        now = new DateTime().withZone(timeZone);
      } else {
        now = new DateTime();
      }
    } else {
      now = new DateTime();
    }
    return now;
  }

  // TODO is it safe to send the joda time class info as part of the DAO when using GWT? It did not used to be serializable over gwt.
  // This is the reason we are doing this here instead of on the dao class where it belongs.
  public boolean isOver(ExperimentDAO experiment, DateTime now) {
    return experiment.getFixedDuration() != null && experiment.getFixedDuration() && now.isAfter(getEndDateTime(experiment));
  }

  private DateTime getEndDateTime(ExperimentDAO experiment) {
    SignalingMechanismDAO signalingMechanismDAO = experiment.getSignalingMechanisms()[0];
    if (signalingMechanismDAO instanceof SignalScheduleDAO && ((SignalScheduleDAO) signalingMechanismDAO).getScheduleType().equals(SignalScheduleDAO.WEEKDAY)) {
      Long[] times = ((SignalScheduleDAO)signalingMechanismDAO).getTimes();
      Arrays.sort(times);
      DateTime lastTimeForDay = new DateTime().plus(times[times.length - 1]);
      return new DateMidnight(experiment.getEndDate()).toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /* if (getScheduleType().equals(SCHEDULE_TYPE_ESM)) */{
      return new DateMidnight(experiment.getEndDate()).plusDays(1).toDateTime();
    }
  }

  private synchronized List<ExperimentDAO> getExperiments() {
    List<ExperimentDAO> experimentDAOs;
    if (cache != null) {
      experimentDAOs = (List<ExperimentDAO>) cache.get(EXPERIMENT_CACHE_KEY);
      if (experimentDAOs != null) {
        return experimentDAOs;
      }
    }  
    experimentDAOs = getExperimentsFromDatastore();
    
    if (cache != null && experimentDAOs != null && !experimentDAOs.isEmpty()) {      
      
      try {
        cache.put(EXPERIMENT_CACHE_KEY, experimentDAOs);
      } catch (Exception e) {
        log.severe("Could not put experiment entry in cache:" + e.getMessage());
      }
      
      return experimentDAOs;
    } else {
      return Collections.EMPTY_LIST;   
    }      
  }

  private List<ExperimentDAO> getExperimentsFromDatastore() {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      javax.jdo.Query q = pm.newQuery(Experiment.class);
      List<Experiment> experiments = (List<Experiment>) q.execute();
      
      List<Long> referringIds = Lists.newArrayList();
      List<ExperimentReference> references = (List<ExperimentReference>) pm.newQuery(ExperimentReference.class).execute();
      for (ExperimentReference experimentReference : references) {
        referringIds.add(experimentReference.getReferringExperimentId());
      }
      
      
      List<ExperimentDAO> experimentDAOs = DAOConverter.createDAOsFor(experiments);
      for (ExperimentDAO experiment : experimentDAOs) {
        experiment.setWebRecommended(referringIds.contains(experiment.getId()));        
      }
      return experimentDAOs;      
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }
}
