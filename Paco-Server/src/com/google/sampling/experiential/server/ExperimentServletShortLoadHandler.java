package com.google.sampling.experiential.server;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;


public class ExperimentServletShortLoadHandler extends ExperimentServletHandler {

  public ExperimentServletShortLoadHandler(String email, DateTimeZone timezone) {
    super(email, timezone);
  }

  @Override
  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.shortJsonify(availableExperiments);
  }
}
