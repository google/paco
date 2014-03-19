package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;


public class ExperimentServletExperimentsForMeLoadHandler extends ExperimentServletShortLoadHandler {

  public ExperimentServletExperimentsForMeLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult pair = ExperimentCacheHelper.getInstance().getMyJoinableExperiments(email, timezone, limit, cursor);
    cursor = pair.getCursor();
    return pair.getExperiments();
  }

}
