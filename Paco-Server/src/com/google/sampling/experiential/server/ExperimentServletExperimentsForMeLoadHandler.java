package com.google.sampling.experiential.server;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;


public class ExperimentServletExperimentsForMeLoadHandler extends ExperimentServletHandler {

  private String email;
  private String tz;
  
  public ExperimentServletExperimentsForMeLoadHandler(String email, String tz) {
    super();
    this.email = email;
    this.tz = tz;
  }
  
  @Override
  public String performLoad() {
    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(email, tz);
    return JsonConverter.shortJsonify(availableExperiments);
  }

  protected List<ExperimentDAO> getExperimentsAvailableToUser(String email, String tz) {
    List<ExperimentDAO> myExperiments = getMyExperiments(tz);
    if (myExperiments == null) {
      return Lists.newArrayList();
    }
    ExperimentRetriever.sortExperiments(myExperiments);        
    ExperimentRetriever.removeSensitiveFields(myExperiments);
    return myExperiments;
  }

  private List<ExperimentDAO> getMyExperiments(String tz) {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    List<ExperimentDAO> experiments = cacheHelper.getMyExperiments(email, tz);
    ExperimentRetriever.removeSensitiveFields(experiments);
    log.info("myExperiments " + ((experiments != null) ? Integer.toString(experiments.size()) : "none"));
    return experiments;
  }
  

}
