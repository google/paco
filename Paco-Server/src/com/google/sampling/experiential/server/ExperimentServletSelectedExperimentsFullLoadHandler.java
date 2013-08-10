package com.google.sampling.experiential.server;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;

public class ExperimentServletSelectedExperimentsFullLoadHandler extends ExperimentServletHandler {

  private String selectedExperimentsParam;

  public ExperimentServletSelectedExperimentsFullLoadHandler(String email, DateTimeZone timezone,
                                                             String selectedExperimentsParam) {
    super(email, timezone);
    this.selectedExperimentsParam = selectedExperimentsParam;
  }

  @Override
  protected List<ExperimentDAO> getAllExperimentsAvailableToUser() {
    List<Long> experimentIds = parseExperimentIds(selectedExperimentsParam);
    if (experimentIds.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    return getFullExperimentsById(experimentIds, email, timezone);
  }

  protected List<ExperimentDAO> getFullExperimentsById(List<Long> experimentIds, String email, DateTimeZone timezone) {
    return ExperimentCacheHelper.getInstance().getExperimentsById(experimentIds, email, timezone);
  }

  @Override
  protected String jsonify(List<ExperimentDAO> availableExperiments) {
    return JsonConverter.jsonify(availableExperiments);
  }

  private List<Long> parseExperimentIds(String expStr) {
    List<Long> experimentIds = Lists.newArrayList();
    Iterable<String> strIds = Splitter.on(",").trimResults().split(expStr);
    for (String id : strIds) {
      Long experimentId = extractExperimentId(id);
      if (!experimentId.equals(new Long(-1))) {
        experimentIds.add(experimentId);
      }
    }
    return experimentIds;
  }

  private Long extractExperimentId(String expStr) {
    try {
      return Long.parseLong(expStr, 10);
    } catch (NumberFormatException e) {
      log.info("Invalid experiment id " + expStr + " sent to server.");
      return new Long(-1);
    }
  }

}
