package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;

public class ExperimentCacheHelper {

  private static final String PUBLIC_EXPERIMENT_KEY = "public_experiments";

  public static final Logger log = Logger.getLogger(ExperimentCacheHelper.class.getName());

  private static final String ALL_JOINABLE_EXPERIMENTS_CACHE_KEY = "ALL_JOINABLE_EXPERIMENTS_CACHE_KEY";
  private static final String MY_JOINABLE_EXPERIMENTS_CACHE_KEY = "MY_JOINABLE_EXPERIMENTS_CACHE_KEY";


  private static ExperimentCacheHelper instance;

  public static synchronized ExperimentCacheHelper getInstance() {
    if (instance == null) {
      instance = new ExperimentCacheHelper();
    }
    return instance;
  }

  private ExperimentRetriever experimentRetriever;

  private MemcacheService cache;

  private ExperimentCacheHelper() {
    super();
    createCache();
    experimentRetriever = ExperimentRetriever.getInstance();
  }

  public void clearCache() {
    if (cache != null) {
      cache.clearAll();
    }
  }

  private void createCache() {
    cache = MemcacheServiceFactory.getMemcacheService();
    cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
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
   * @param cursor
   * @param limit
   * @return List of experiments that satisfy the criteria.
   */
  public ExperimentQueryResult getJoinableExperiments(String loggedInEmail, DateTimeZone dateTimeZone, Integer limit, String cursor) {
    String experimentCacheKey = loggedInEmail + "_" + ALL_JOINABLE_EXPERIMENTS_CACHE_KEY;

    ExperimentQueryResult experimentDAOs = null;// = getCachedExperimentsByKey(experimentCacheKey);
//    if (experimentDAOs != null) {
//      return experimentDAOs;
//    }
    experimentDAOs = experimentRetriever.getAllJoinableExperiments(loggedInEmail, dateTimeZone, limit, cursor);

    //cacheExperimentsByKey(experimentCacheKey, experimentDAOs);
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
    return experimentRetriever.getExperimentsById(experimentIds, email, timezone);
  }

  /**
   * returns all experiments that the user can join.
   * This is the collection of experiments that either
   * 1) the user has created,
   * 2) the user admins,
   * 3) that have been published explicitly to the user.
   *
   * @param dateTimeZone used to decide if experiments are still running.
   * @param cursor
   * @param limit
   * @return List of experiments that satisfy the criteria.
   */
  public ExperimentQueryResult getMyJoinableExperiments(String email, DateTimeZone dateTimeZone, Integer limit, String cursor) {
    String experimentCacheKey = email + "_" + MY_JOINABLE_EXPERIMENTS_CACHE_KEY;

    ExperimentQueryResult experimentDAOs;// = getCachedExperimentsByKey(experimentCacheKey);
//    if (experimentDAOs != null) {
//      return experimentDAOs;
//    }
    experimentDAOs = experimentRetriever.getMyJoinableExperiments(email, dateTimeZone, limit, cursor);

    //cacheExperimentsByKey(experimentCacheKey, experimentDAOs);
    return experimentDAOs;
  }

  private void cacheExperimentsByKey(String experimentCacheKey, List<ExperimentDAO> experimentDAOs) {
    if (cache != null && !experimentDAOs.isEmpty()) {
      try {
        // we want the cache to be flushed every night so that experiments which have expired are no longer cached. (well, at least not for more than a day.)
        cache.put(experimentCacheKey, experimentDAOs, Expiration.onDate(new DateMidnight().plusDays(1).toDate()));
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

//  public void addPublicExperiment(ExperimentDAO newExperimentDAO) {
//    List<ExperimentDAO> currentExperiments = getCachedExperimentsByKey(PUBLIC_EXPERIMENT_KEY);
//    List<ExperimentDAO> newExperiments = Lists.newArrayList();
//    for (ExperimentDAO experimentDAO : currentExperiments) {
//      if (!experimentDAO.getId().equals(newExperimentDAO.getId())) {
//        newExperiments.add(experimentDAO);
//      }
//    }
//    newExperiments.add(newExperimentDAO);
//    cacheExperimentsByKey(PUBLIC_EXPERIMENT_KEY, newExperiments);
//  }

  public ExperimentQueryResult getPublicExperiments(DateTimeZone dateTimeZone, Integer limit, String cursor) {
    //List<ExperimentDAO> cachedExperiments = getCachedExperimentsByKey(PUBLIC_EXPERIMENT_KEY);
    //if (cachedExperiments == null) {
     ExperimentQueryResult cachedExperiments = experimentRetriever.getExperimentsPublishedPublicly(dateTimeZone, limit, cursor);
    //  cacheExperimentsByKey(PUBLIC_EXPERIMENT_KEY, cachedExperiments);
    //}
    return cachedExperiments;
  }

  public ExperimentQueryResult getUsersAdministeredExperiments(String email, DateTimeZone dateTimeZone, Integer limit, String cursor) {
    return experimentRetriever.getUsersAdministeredExperiments(email, dateTimeZone, limit, cursor);
  }


}
