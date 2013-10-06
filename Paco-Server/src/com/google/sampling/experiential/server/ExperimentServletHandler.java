package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;

abstract class ExperimentServletHandler {

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());

  protected List<ExperimentDAO> getExperimentsAvailableToUser(String email, String tz) {
    List<ExperimentDAO> joinableExperiments = getJoinableExperiments(tz);
    List<ExperimentDAO> availableExperiments = null;
    if (joinableExperiments == null) {
      availableExperiments = Lists.newArrayList();        
    } else {
      availableExperiments = ExperimentRetriever.getSortedExperimentsAvailableToUser(joinableExperiments, email);        
    }
    ExperimentRetriever.removeSensitiveFields(availableExperiments);
    return availableExperiments;
  }

  private List<ExperimentDAO> getJoinableExperiments(String tz) {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    List<ExperimentDAO> experiments = cacheHelper.getJoinableExperiments(tz);
    log.info("joinable experiments " + ((experiments != null) ? Integer.toString(experiments.size()) : "none"));
    return experiments;
  }
  
  public abstract String performLoad();

}