package com.google.sampling.experiential.server;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

public class ExperimentServletSelectedExperimentsFullLoadHandler extends ExperimentServletHandler {

  private String email;
  private String tz;
  private String selectedExperimentsParam;
  
  public ExperimentServletSelectedExperimentsFullLoadHandler(String email, String tz, 
                                                             String selectedExperimentsParam) {
    this.email = email;
    this.tz = tz;
    this.selectedExperimentsParam = selectedExperimentsParam;
  }
  
  @Override
  public String performLoad() {
    List<Long> experimentIds = parseExperimentIds(selectedExperimentsParam);
    if (experimentIds.isEmpty()) {
      log.info("Experiment list is empty.");
      return "[]";
    }

    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(experimentIds, email, tz);
    
    return loadSelectedExperiments(availableExperiments);
  }
  
  private String loadSelectedExperiments(List<ExperimentDAO> experiments) {
//    List<ExperimentDAO> experiments = Lists.newArrayList();
//    for (ExperimentDAO experiment : availableExperiments) {
//      if (experimentIds.containsKey(experiment.getId())) {
//        experiments.add(experiment);
//      }
//    }
    if (experiments.isEmpty()) {
      log.info("Experiment list is empty.");
      return "[]";
    }

    return JsonConverter.jsonify(experiments);
  }
  
  private List<Long> parseExperimentIds(String expStr) {
    List<Long> experimentIds = Lists.newArrayList();
    Iterable<String> strIds = Splitter.on(",").trimResults().split(expStr);
    for (String id : strIds) {
      Long experimentId = extractExperimentId(id);
      if (!experimentId.equals(new Long(-1))) {
        experimentIds.add(experimentId);
      }
    }
    return experimentIds;
  }

  private Long extractExperimentId(String expStr) {
    try {
      Long experimentId = Long.parseLong(expStr, 10);
      return experimentId;
    } catch (NumberFormatException e) {
      log.severe("Invalid experiment id " + expStr + " sent to server.");
      return new Long(-1);
    }
  }
  
  //
  

  protected List<ExperimentDAO> getExperimentsAvailableToUser(List<Long> experimentIds, String email, String tz) {
    List<ExperimentDAO> joinableExperiments = getJoinableExperiments(experimentIds, tz);
    List<ExperimentDAO> availableExperiments = null;
    if (joinableExperiments == null) {
      joinableExperiments = Lists.newArrayList();
      availableExperiments = joinableExperiments;        
    } else {
      availableExperiments = ExperimentRetriever.filterExperimentsUnavailableToUser(joinableExperiments, email);        
    }
    ExperimentRetriever.removeSensitiveFields(availableExperiments);
    return availableExperiments;
  }

  private List<ExperimentDAO> getJoinableExperiments(List<Long> experimentIds, String tz) {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    List<ExperimentDAO> experiments = cacheHelper.getJoinableExperiments(experimentIds, tz);
    log.info("joinable experiments " + ((experiments != null) ? Integer.toString(experiments.size()) : "none"));
    return experiments;
  }

}
