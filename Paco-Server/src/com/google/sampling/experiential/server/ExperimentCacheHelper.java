package com.google.sampling.experiential.server;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;

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
  private ExperimentRetriever experimentRetriever;

  private ExperimentCacheHelper() {
    super();
    createCache();
    experimentRetriever = ExperimentRetriever.getInstance();
  }

  public void clearCache() {
    if (cache != null) {
      cache.clear();
    }
  }

  private void createCache() {
    try {
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      cache = cacheFactory.createCache(Collections.emptyMap());
    } catch (CacheException e) {
      log.warning("Could not get a cache in the ExperimentCacheHelper ctor: " + e.getMessage());
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

  /**
   * NOTE: About to be deprecated by getMyJoinableExperiments (below).
   *
   * returns all experiments that the user can join.
   * This is the collection of experiments that either
   * 1) the user has created,
   * 2) the user admins,
   * 3) that have been published explicitly to the user.
   *
   * @param experimentIds List of ids of experiments that the user can join
   * @param dateTimeZone used to decide if experiments are still running.
   * @return List of experiments that satisfy the criteria.
   */
  public List<ExperimentDAO> getJoinableExperiments(String loggedInEmail, DateTimeZone dateTimeZone) {
    String experimentCacheKey = EXPERIMENT_CACHE_KEY;

    List<ExperimentDAO> experimentDAOs = getCachedExperimentsByKey(experimentCacheKey);
    if (experimentDAOs != null) {
      return experimentDAOs;
    }
    experimentDAOs = experimentRetriever.getAllJoinableExperiments(loggedInEmail, dateTimeZone);

    cacheExperimentsByKey(experimentCacheKey, experimentDAOs);
    return experimentDAOs;
  }

  /**
   * returns all experiments that the user can join.
   * This is the collection of experiments that either
   * 1) the user has created,
   * 2) the user admins,
   * 3) that have been published explicitly to the user.
   *
   * @param experimentIds List of ids of experiments that the user can join
   * @param timezone used to decide if experiments are still running.
   * @return List of experiments that satisfy the criteria.
   */
  public List<ExperimentDAO> getExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
//    TODO Cache a specific experimentIdGroup for a specific user. Or not.

//    List<ExperimentDAO> experimentDAOs = getCachedExperimentsByKey(experimentCacheKey);
//    if (experimentDAOs != null) {
//      return experimentDAOs;
//    }

    List<ExperimentDAO> experimentDAOs = experimentRetriever.getExperimentsById(experimentIds, email, timezone);

    //    cacheExperimentsByKey(experimentCacheKey, experimentDAOs);
    return experimentDAOs;
  }

  /**
   * returns all experiments that the user can join.
   * This is the collection of experiments that either
   * 1) the user has created,
   * 2) the user admins,
   * 3) that have been published explicitly to the user.
   *
   * @param dateTimeZone used to decide if experiments are still running.
   * @return List of experiments that satisfy the criteria.
   */
  public List<ExperimentDAO> getMyJoinableExperiments(String email, DateTimeZone dateTimeZone) {
    return experimentRetriever.getMyJoinableExperiments(email, dateTimeZone);
  }

  private void cacheExperimentsByKey(String experimentCacheKey, List<ExperimentDAO> experimentDAOs) {
    if (cache != null && !experimentDAOs.isEmpty()) {
      try {
        cache.put(experimentCacheKey, experimentDAOs);
      } catch (Exception e) {
        log.severe("Could not put experiment entry in cache:" + e.getMessage());
      }
    }
  }

  private List<ExperimentDAO> getCachedExperimentsByKey(String experimentCacheKey) {
    if (cache != null) {
      return (List<ExperimentDAO>) cache.get(experimentCacheKey);
    }
    return null;
  }

}
