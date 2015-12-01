package com.google.sampling.experiential.datastore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class ExperimentCache {

  private static final String EXPERIMENT_CACHE_KEY_PREFIX = "exp:";
  private static final String PUBLIC_EXPERIMENT_IDS_CACHE_KEY = "publicExperimentIds";
  private static ExperimentCache instance;

  // call this
//  on server launch or upon memcache relaunch somehow
//  also add update, delete and insert caching.
//  then, setup an endpoint to query this and test it.
//  then integrate it
//  Then, make it more performant
//  then


  private MemcacheService syncCache;

  public static synchronized ExperimentCache getInstance() {
    if (instance == null) {
      instance = new ExperimentCache();
    }
    return instance;
  }

  private ExperimentCache() {
    this.syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    init();
  }

  public ExperimentQueryResult getExperimentsPublishedPublicly(DateTimeZone timezone, Integer limit, String cursor, String email) {
    List<ExperimentDAO> experimentList = getPublicExperiments();

    int startIndex = 0;
    if (cursor != null) {
      try {
        startIndex = Integer.parseInt(cursor);
      } catch (NumberFormatException nfe) {
        // nothing to be done.
      }
    }
    int endIndex = experimentList.size();
    if (limit != null) {
      endIndex = Math.min(endIndex, startIndex + limit);
    }
    List<ExperimentDAO> page = experimentList.subList(startIndex, endIndex);
    DefaultExperimentService.removeNonAdminData(email, page);
    return new ExperimentQueryResult(Integer.toString(endIndex), page);
  }

  /**
   * Takes an id and returns a model object, preferably from cache
   * @param experimentid
   * @return
   */
  public ExperimentDAO getPublicExperiment(Long experimentid) {
    String experimentJson = (String)syncCache.get(EXPERIMENT_CACHE_KEY_PREFIX + experimentid);
    if (experimentJson == null) {
      experimentJson = ExperimentJsonEntityManager.getExperiment(experimentid);
      syncCache.put(EXPERIMENT_CACHE_KEY_PREFIX + experimentid, experimentJson);
    }
    ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(experimentJson);
    return experiment;
  }



  private void init() {
    loadIdsAndExperiments();
  }

/**
 * Load all public experiment ids
 * Load all json for those experiments to get the names
 * sort ids by experiment titles
 * store sorted ids
 * load each experiment json into the cache keyed by id
 *
 */
  private void loadIdsAndExperiments() {
    List<Long> ids = PublicExperimentList.getAllPublicExperiments();
    List<String> experimentJsons = ExperimentJsonEntityManager.getExperimentsById(ids);
    List<ExperimentDAO> experiments = Lists.newArrayList();
    Map<Long, String> expIdMap = Maps.newHashMap(); // Save the json so we can map it to an id

    for (String json : experimentJsons) {
      ExperimentDAO exp = JsonConverter.fromSingleEntityJson(json);
      experiments.add(exp);
      expIdMap.put(exp.getId(), json);
    }
    sortExperimentsAlphabetically(experiments);

    List<Long> alphabeticalByTitleIds = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : experiments) {
      final Long experimentId = experimentDAO.getId();
      syncCache.put(EXPERIMENT_CACHE_KEY_PREFIX + experimentId, expIdMap.get(experimentId));
      alphabeticalByTitleIds.add(experimentId);
    }
    syncCache.put(PUBLIC_EXPERIMENT_IDS_CACHE_KEY, alphabeticalByTitleIds);
  }

  private void sortExperimentsAlphabetically(List<ExperimentDAO> experiments) {
    Collections.sort(experiments, new Comparator<ExperimentDAO>() {

      @Override
      public int compare(ExperimentDAO o1, ExperimentDAO o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }

    });
  }

  private String getExperimentJsonById(Long id) {

    String experimentJson = (String) syncCache.get(id);
    if (experimentJson == null) {
      experimentJson = ExperimentJsonEntityManager.getExperiment(id);
      syncCache.put(id, experimentJson);
    }
    return experimentJson;
  }

  /**
   * Returns all public experimentsm preferably from cache.
   * If not, it gets the list from the datastore and then
   * builds the model experiment objects, preferably from cache.
   * Next, it sorts the model objects into alphabetical order and
   * stores them in the cache. Finally, it returns the list of experiments.
   * @return
   */
  private List<ExperimentDAO> getPublicExperiments() {
    List<Long> ids =  (List<Long>) syncCache.get(PUBLIC_EXPERIMENT_IDS_CACHE_KEY);
    if (ids == null) {
      ids = PublicExperimentList.getAllPublicExperiments();
      syncCache.put(PUBLIC_EXPERIMENT_IDS_CACHE_KEY, ids);
    }
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (Long experimentid : ids) {
      ExperimentDAO experimentDAO = getPublicExperiment(experimentid);
      experiments.add(experimentDAO);
    }
    return experiments;
  }

}
