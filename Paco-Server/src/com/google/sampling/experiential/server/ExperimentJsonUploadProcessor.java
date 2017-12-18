package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTimeZone;
import org.json.JSONException;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pacoapp.paco.shared.comm.ExperimentEditOutcome;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.ValidationMessage;

public class ExperimentJsonUploadProcessor {

  private static final Logger log = Logger.getLogger(ExperimentJsonUploadProcessor.class.getName());
  private ExperimentService experimentService;

  public ExperimentJsonUploadProcessor(ExperimentService experimentService) {
    this.experimentService = experimentService;
  }

  public static ExperimentJsonUploadProcessor create() {
    ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    return new ExperimentJsonUploadProcessor(experimentService);
  }
  
  public String processJsonExperiments(String postBodyString, User userFromLogin, String appIdHeader, String pacoVersion, DateTimeZone timezone) {
    boolean persistInCloudSqlOnly = false;
    return processJsonExperiments(persistInCloudSqlOnly, postBodyString, userFromLogin, appIdHeader, pacoVersion, timezone);
  }

  public String processJsonExperiments(boolean persistInCloudSqlOnly, String postBodyString, User userFromLogin, String appIdHeader, String pacoVersion, DateTimeZone timezone) {
    if (postBodyString.startsWith("[")) {
      List<ExperimentDAO> experiments = JsonConverter.fromEntitiesJsonUpload(postBodyString);
      return toJson(processDAOs(persistInCloudSqlOnly, experiments, userFromLogin, appIdHeader, pacoVersion, timezone));
    } else {
      ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(postBodyString);
      return toJson(processSingleJsonObject(persistInCloudSqlOnly, experiment, userFromLogin, appIdHeader, pacoVersion, timezone));
    }
  }

  static String toJson(List<Outcome> outcomes) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    try {
      return mapper.writeValueAsString(outcomes);
    } catch (JsonGenerationException e) {
      log.warning("could not generate outcome json. " + e.getMessage());
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    } catch (JsonMappingException e) {
      log.warning("could not map outcome to json. " + e.getMessage());
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      log.warning("io exception generating outcome json. " + e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  private List<Outcome> processSingleJsonObject(boolean persistInCloudSqlOnly, ExperimentDAO currentObject, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) {
    List<Outcome> results = Lists.newArrayList();
    try {
      results.add(postObject(persistInCloudSqlOnly, currentObject, 0, userFromLogin, appIdHeader, pacoVersionHeader, timezone));
    } catch (Throwable e) {
      results.add(new Outcome(0, "Exception posting event: 0. " + e.getMessage()));
    }
    return results;
  }

  private List<Outcome> processDAOs(boolean persistInCloudSqlOnly, List<ExperimentDAO> experiments, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) {
    List<Outcome> results = Lists.newArrayList();
    ExperimentDAO currentObject = null;
    for (int i = 0; i < experiments.size(); i++) {
      try {
        currentObject = experiments.get(i);
        results.add(postObject(persistInCloudSqlOnly, currentObject, i, userFromLogin, appIdHeader, pacoVersionHeader, timezone));
      } catch (JSONException e) {
        results.add(new Outcome(i, "JSONException posting experiment: " + i + ". " + e.getMessage()));
      } catch (Throwable e) {
        log.warning("Error posting: " + e.getMessage());
        results.add(new Outcome(i, "Exception posting experiment: " + i + ". " + e.getMessage()));
      }
    }
    return results;
  }

  private Outcome postObject(boolean persistInCloudSqlOnly, ExperimentDAO experimentDAO, int objectId, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) throws Throwable {
    String lowerCaseEmail = null;
    ExperimentEditOutcome outcome = new ExperimentEditOutcome(objectId);
  
    Long id = experimentDAO.getId();
    log.info("Retrieving experimentId, experimentName for experiment posting: " + id + ", " + experimentDAO.getTitle());
    ExperimentDAO existingExperiment = null;
    if (id != null) {
      existingExperiment = experimentService.getExperiment(id);
    }
    if (existingExperiment == null) {
      experimentDAO.setId(null);
    }
    if (persistInCloudSqlOnly) { 
      CloudSQLDaoImpl cloudSqlDaoImpl = new CloudSQLDaoImpl();
      cloudSqlDaoImpl.ensureUserId(experimentDAO.getId(), Sets.newHashSet(experimentDAO.getAdmins()), Sets.newHashSet(experimentDAO.getPublishedUsers()));
    } else {
      lowerCaseEmail = userFromLogin.getEmail().toLowerCase();
      if (existingExperiment != null && !existingExperiment.isAdmin(lowerCaseEmail)) {
        outcome.setExperimentId(id);
        outcome.setError("Existing experiment for this event: " + objectId + ". Not allowed to modify.");
        return outcome;
      }
      // TODO move this check into the tx in the experimentService to make it atomic
      if (existingExperiment != null && existingExperiment.getVersion() > experimentDAO.getVersion()) {
        outcome.setExperimentId(id);
        outcome.setError("Newer version of the experiment for this event: " + objectId + ". Refresh and try editing again.");
        return outcome;
      }
    }
    List<ValidationMessage> saveExperimentErrorResults = experimentService.saveExperiment(experimentDAO,
                                                                                          lowerCaseEmail,
                                                                                          timezone);
    if (saveExperimentErrorResults != null) {
      ObjectMapper mapper = JsonConverter.getObjectMapper();
      String json = mapper.writeValueAsString(saveExperimentErrorResults);
      outcome.setError(json);
    }
    outcome.setExperimentId(experimentDAO.getId());
    return outcome;
  }


}
