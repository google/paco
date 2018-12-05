package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;

public class ExperimentServletAdminExperimentsFullLoadHandler extends ExperimentServletHandler {

  public ExperimentServletAdminExperimentsFullLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor, String pacoProtocol, String sortColumn, String sortOrder) {
    super(email, timezone, limit, cursor, pacoProtocol, sortColumn, sortOrder);

  }

  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult result = ExperimentServiceFactory.getExperimentService().getUsersAdministeredExperiments(email, timezone, limit, cursor, sortColumn, sortOrder);
    cursor = result.getCursor();
    return result.getExperiments();
  }


}
