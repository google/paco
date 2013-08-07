package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;

abstract class ExperimentServletHandler {

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  protected String email;
  protected DateTimeZone timezone;

  public ExperimentServletHandler(String email, DateTimeZone timezone2) {
    this.email = email;
    this.timezone = timezone2;
  }

  public String performLoad() {
    return jsonify(getAllExperimentsAvailableToUser());
  }

  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    return ExperimentCacheHelper.getInstance().getJoinableExperiments(email, timezone);
  }

  protected abstract String jsonify(List<ExperimentDAO> availableExperiments);

}