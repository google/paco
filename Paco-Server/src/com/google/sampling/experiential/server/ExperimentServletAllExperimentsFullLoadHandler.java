package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

public class ExperimentServletAllExperimentsFullLoadHandler extends ExperimentServletHandler {

  private String userId;

  public ExperimentServletAllExperimentsFullLoadHandler(String userId, String email, DateTimeZone timezone) {
    super(email, timezone);
    this.userId = userId;
  }

  @Override
  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.jsonify(availableExperiments);
  }
}
