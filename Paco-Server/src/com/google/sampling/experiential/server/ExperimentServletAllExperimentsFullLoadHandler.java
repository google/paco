package com.google.sampling.experiential.server;

import java.util.List;

import com.google.appengine.api.users.User;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

public class ExperimentServletAllExperimentsFullLoadHandler extends ExperimentServletHandler {
  
  private String userId;
  private String email;
  private String tz;

  public ExperimentServletAllExperimentsFullLoadHandler(String userId, String email, String tz) {
    this.userId = userId;
    this.email = email;
    this.tz = tz;
  }
  
  @Override
  public String performLoad() {
    ExperimentCacheHelper cacheHelper = ExperimentCacheHelper.getInstance();
    String experimentsJson = cacheHelper.getExperimentsJsonForUser(userId);
    if (experimentsJson != null) {
      log.info("Got cached experiments for " + email);
    } else {
      log.info("No cached experiments for " + email);
      List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(email, tz);
      experimentsJson = JsonConverter.jsonify(availableExperiments);
      cacheHelper.putExperimentJsonForUser(userId, experimentsJson); 
    } 
    return experimentsJson;
  }

}
