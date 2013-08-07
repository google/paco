package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;


public class ExperimentServletExperimentsForMeLoadHandler extends ExperimentServletShortLoadHandler {

  public ExperimentServletExperimentsForMeLoadHandler(String email, DateTimeZone timezone) {
    super(email, timezone);
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    return ExperimentCacheHelper.getInstance().getMyJoinableExperiments(email, timezone);
  }

}
