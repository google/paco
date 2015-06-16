package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

abstract class ExperimentServletHandler {

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  protected String email;
  protected DateTimeZone timezone;
  protected Integer limit;
  protected String cursor;
  protected String pacoProtocol;

  public ExperimentServletHandler(String email, DateTimeZone timezone2, Integer limit, String cursor, String pacoProtocol) {
    this.email = email;
    if (timezone2 != null) {
      this.timezone = timezone2;
    } else {
      this.timezone = DateTimeZone.getDefault();
    }
    this.limit = limit;
    this.cursor = cursor;
    this.pacoProtocol = pacoProtocol;
  }

  public String performLoad() {
    return jsonify(getAllExperimentsAvailableToUser());
  }

  protected abstract List<ExperimentDAO> getAllExperimentsAvailableToUser();

  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.jsonify(availableExperiments, limit, cursor, pacoProtocol);
  }

}