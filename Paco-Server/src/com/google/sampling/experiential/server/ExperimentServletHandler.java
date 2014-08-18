package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentQueryResult;
import com.google.sampling.experiential.datastore.JsonConverter;

abstract class ExperimentServletHandler {

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  protected String email;
  protected DateTimeZone timezone;
  protected Integer limit;
  protected String cursor;
  protected String pacoProtocol;

  public ExperimentServletHandler(String email, DateTimeZone timezone2, Integer limit, String cursor, String pacoProtocol) {
    this.email = email;
    this.timezone = timezone2;
    this.limit = limit;
    this.cursor = cursor;
    this.pacoProtocol = pacoProtocol;
  }

  public String performLoad() {
    return jsonify(getAllExperimentsAvailableToUser());
  }

  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    ExperimentQueryResult result = ExperimentCacheHelper.getInstance().getJoinableExperiments(email, timezone, limit, cursor);
    cursor = result.getCursor();
    return result.getExperiments();
  }

  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.jsonify(availableExperiments, limit, cursor, pacoProtocol);
  }

}