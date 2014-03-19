package com.google.sampling.experiential.server;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;


public class ExperimentServletShortLoadHandler extends ExperimentServletHandler {

  public ExperimentServletShortLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }

  @Override
  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.shortJsonify(availableExperiments, limit, cursor, pacoProtocol);
  }

}
