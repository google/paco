package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;

public class ExperimentServletJoinedExperimentsShortLoadHandler extends ExperimentServletShortLoadHandler {

  public ExperimentServletJoinedExperimentsShortLoadHandler(String email, DateTimeZone timezone,
                                                            Integer limit, String cursor,
                                                            String pacoProtocol) {
    super(email, timezone, limit, cursor, pacoProtocol);
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult pair = ExperimentServiceFactory.getExperimentService().getMyJoinedExperiments(email, timezone, limit, cursor, sortColumn, sortOrder);
    cursor = pair.getCursor();
    return pair.getExperiments();
  }

}
