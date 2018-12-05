package com.google.sampling.experiential.server;

import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

abstract class ExperimentServletHandler {

  public static final Logger log = Logger.getLogger(ExperimentServlet.class.getName());
  protected String email;
  protected DateTimeZone timezone;
  protected Integer limit;
  protected String cursor;
  protected String pacoProtocol;
  protected String sortColumn;
  protected String sortOrder;

  public ExperimentServletHandler(String email, DateTimeZone timezone2, Integer limit, String cursor, String pacoProtocol) {
    this(email, timezone2, limit, cursor, pacoProtocol, null, null);
  }

  public ExperimentServletHandler(String email, DateTimeZone timezone, Integer limit, String cursor,
                                  String pacoProtocol, String sortColumn, String sortOrder) {
    this.email = email;
    if (timezone != null) {
      this.timezone = timezone;
    } else {
      this.timezone = DateTimeZone.getDefault();
    }
    this.limit = limit;
    this.cursor = cursor;
    this.pacoProtocol = pacoProtocol;
    if (sortColumn == null || sortColumn.isEmpty()) {
      this.sortColumn = ExperimentJsonEntityManager.TITLE_COLUMN;
    } else {
      this.sortColumn = sortColumn;
    }
    if (sortOrder == null || !sortOrder.toLowerCase().equals("desc")) {
      this.sortOrder = "asc";
    } else {
      this.sortOrder = "desc";
    }
  }

  public String performLoad() {
    return jsonify(getAllExperimentsAvailableToUser());
  }

  protected abstract List<ExperimentDAO> getAllExperimentsAvailableToUser();

  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.jsonify(availableExperiments, limit, cursor, pacoProtocol);
  }

}