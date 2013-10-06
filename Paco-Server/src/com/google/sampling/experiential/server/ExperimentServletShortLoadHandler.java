package com.google.sampling.experiential.server;
import java.util.List;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;


public class ExperimentServletShortLoadHandler extends ExperimentServletHandler {
  
  private String email;
  private String tz;
  
  public ExperimentServletShortLoadHandler(String email, String tz) {
    super();
    this.email = email;
    this.tz = tz;
  }
  
  @Override
  public String performLoad() {
    List<ExperimentDAO> availableExperiments = getExperimentsAvailableToUser(email, tz);
    return JsonConverter.shortJsonify(availableExperiments);
  }

}
