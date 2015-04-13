package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;


public class ExperimentServletExperimentsForMeLoadHandler extends ExperimentServletShortLoadHandler {

  public ExperimentServletExperimentsForMeLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult pair = ExperimentServiceFactory.getExperimentService().getMyJoinableExperiments(email, timezone, limit, cursor);
    cursor = pair.getCursor();
    return pair.getExperiments();
  }

}
