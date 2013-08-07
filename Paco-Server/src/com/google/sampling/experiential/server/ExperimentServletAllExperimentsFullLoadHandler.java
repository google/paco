package com.google.sampling.experiential.server;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.common.base.Strings;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

public class ExperimentServletAllExperimentsFullLoadHandler extends ExperimentServletHandler {

  private String userId;
  private ExperimentCacheHelper cacheHelper;
  private String experimentsJson;

  public ExperimentServletAllExperimentsFullLoadHandler(String userId, String email, DateTimeZone timezone) {
    super(email, timezone);
    this.userId = userId;
    cacheHelper = ExperimentCacheHelper.getInstance();
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    experimentsJson = cacheHelper.getExperimentsJsonForUser(userId);
    if (experimentsJson == null) {
      return super.getAllExperimentsAvailableToUser();
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    if (!Strings.isNullOrEmpty(experimentsJson)) {
      return experimentsJson;
    }
    experimentsJson = JsonConverter.jsonify(availableExperiments);
    if (!availableExperiments.isEmpty()) {
      cacheHelper.putExperimentJsonForUser(userId, experimentsJson);
    }
    return experimentsJson;
  }



}
