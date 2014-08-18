package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;


public class ExperimentServletExperimentsShortPublicLoadHandler extends ExperimentServletShortLoadHandler {

  public ExperimentServletExperimentsShortPublicLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }

  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult result = ExperimentCacheHelper.getInstance().getPublicExperiments(timezone, limit, cursor);
    cursor = result.getCursor();
    return result.getExperiments();
  }


}
