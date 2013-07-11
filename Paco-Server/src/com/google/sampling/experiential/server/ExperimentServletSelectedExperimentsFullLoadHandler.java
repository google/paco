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
    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(email, tz);
    HashMap<Long, Long> experimentIds = parseExperimentIds(selectedExperimentsParam);
    return loadSelectedExperiments(experimentIds, availableExperiments);
  }
  
  private String loadSelectedExperiments(HashMap<Long,Long> experimentIds, List<ExperimentDAO> availableExperiments) {
    List<ExperimentDAO> experiments = Lists.newArrayList();
    for (ExperimentDAO experiment : availableExperiments) {
      if (experimentIds.containsKey(experiment.getId())) {
        experiments.add(experiment);
      }
    }
    if (experiments.isEmpty()) {
      log.severe("Experiment id's " + experimentIds + " are all invalid.  No experiments were fetched from server.");
      return "[]";
    }
    return JsonConverter.jsonify(experiments);
  }
  
  private HashMap<Long,Long> parseExperimentIds(String expStr) {
    HashMap<Long,Long> experimentIds = new HashMap<Long, Long>();
    Iterable<String> strIds = Splitter.on(",").trimResults().split(expStr);
    for (String id : strIds) {
      Long experimentId = extractExperimentId(id);
      if (!experimentId.equals(new Long(-1))) {
        experimentIds.put(experimentId, null);
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

}
