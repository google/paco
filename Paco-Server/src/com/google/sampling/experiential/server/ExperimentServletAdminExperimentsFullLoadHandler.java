package com.google.sampling.experiential.server;

import org.joda.time.DateTimeZone;
import java.util.List;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;

public class ExperimentServletAdminExperimentsFullLoadHandler extends ExperimentServletHandler {

  public ExperimentServletAdminExperimentsFullLoadHandler(String email, DateTimeZone timezone, Integer limit, String cursor) {
    super(email, timezone, limit, cursor);
  }

  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult result = ExperimentCacheHelper.getInstance().getUsersAdministeredExperiments(email, timezone, limit, cursor);
    cursor = result.getCursor();
    return result.getExperiments();
  }


}
