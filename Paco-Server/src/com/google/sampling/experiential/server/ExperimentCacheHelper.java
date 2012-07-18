package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.mortbay.log.Log;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.SignalSchedule;

public class ExperimentCacheHelper {

  public static final String EXPERIMENT_CACHE_KEY = "EXPERIMENT_CACHE_KEY";

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

  public List<Experiment> getJoinableExperiments() {
    List<Experiment> experiments = getExperiments();
    List<Experiment> joinable = Lists.newArrayList(experiments);
    DateTime now = new DateTime();
    for (Experiment experiment : experiments) {
      if (experiment.getDeleted() != null && experiment.getDeleted() || isOver(experiment, now)) {
        joinable.remove(experiment);
      }
    }
    return joinable;
  }

  // TODO is it safe to send the joda time class info as part of the DAO when using GWT? It did not used to be serializable over gwt.
  // This is the reason we are doing this here instead of on the dao class where it belongs.
  public boolean isOver(Experiment experiment, DateTime now) {
    return experiment.getFixedDuration() != null && experiment.getFixedDuration() && now.isAfter(getEndDateTime(experiment));
  }

  private DateTime getEndDateTime(Experiment experiment) {
    if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKDAY)) {
      List<Long> times = experiment.getSchedule().getTimes();
      // get the latest time
      Collections.sort(times);

      DateTime lastTimeForDay = new DateTime().plus(times.get(times.size() - 1));
      return new DateMidnight(experiment.getEndDate()).toDateTime().withMillisOfDay(lastTimeForDay.getMillisOfDay());
    } else /* if (getScheduleType().equals(SCHEDULE_TYPE_ESM)) */{
      return new DateMidnight(experiment.getEndDate()).plusDays(1).toDateTime();
    }
  }

  private synchronized List<Experiment> getExperiments() {
    List<Experiment> experiments;
    if (cache != null) {
      experiments = (List<Experiment>) cache.get(EXPERIMENT_CACHE_KEY);
      if (experiments != null) {
        return experiments;
      }
    }
    experiments = getExperimentsFromDatastore();

    if (cache != null && experiments != null && !experiments.isEmpty()) {
      cache.put(EXPERIMENT_CACHE_KEY, experiments);
      return experiments;
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  private List<Experiment> getExperimentsFromDatastore() {
    return DAO.getInstance().getPublishedExperiments();
  }
}
