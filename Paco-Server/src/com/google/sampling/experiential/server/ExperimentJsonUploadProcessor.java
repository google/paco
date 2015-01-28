package com.google.sampling.experiential.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTimeZone;
import org.json.JSONException;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.paco.shared.Outcome;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.model.Experiment;

public class ExperimentJsonUploadProcessor {

  private static final Logger log = Logger.getLogger(ExperimentJsonUploadProcessor.class.getName());
  private ExperimentRetriever experimentRetriever;

  public ExperimentJsonUploadProcessor(ExperimentRetriever experimentRetriever) {
    this.experimentRetriever = experimentRetriever;
  }

  public static ExperimentJsonUploadProcessor create() {
    return new ExperimentJsonUploadProcessor(ExperimentRetriever.getInstance());
  }

  public String processJsonExperiments(String postBodyString, User userFromLogin, String appIdHeader, String pacoVersion, DateTimeZone timezone) {
    if (postBodyString.startsWith("[")) {
      List<ExperimentDAO> experiments = JsonConverter.fromEntitiesJsonUpload(postBodyString);
      return toJson(processDAOs(experiments, userFromLogin, appIdHeader, pacoVersion, timezone));
    } else {
      ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(postBodyString);
      return toJson(processSingleJsonObject(experiment, userFromLogin, appIdHeader, pacoVersion, timezone));
    }
  }

  private String toJson(List<Outcome> outcomes) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
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

  private List<Outcome> processSingleJsonObject(ExperimentDAO currentObject, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) {
    List<Outcome> results = Lists.newArrayList();
    try {
      results.add(postObject(currentObject, 0, userFromLogin, appIdHeader, pacoVersionHeader, timezone));
    } catch (Throwable e) {
      results.add(new Outcome(0, "Exception posting event: 0. "+ e.getMessage()));
    }
    return results;
  }

  private List<Outcome> processDAOs(List<ExperimentDAO> experiments, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) {
    List<Outcome> results = Lists.newArrayList();
    ExperimentDAO currentObject = null;
    for (int i = 0; i < experiments.size(); i++) {
      try {
        currentObject = experiments.get(i);
        results.add(postObject(currentObject, i, userFromLogin, appIdHeader, pacoVersionHeader, timezone));
      } catch (JSONException e) {
        results.add(new Outcome(i, "JSONException posting experiment: " + i + ". " + e.getMessage()));
      } catch (Throwable e) {
        log.warning("Error posting: " + e.getMessage());
        results.add(new Outcome(i, "Exception posting experiment: " + i + ". " + e.getMessage()));
      }
    }
    return results;
  }

  private Outcome postObject(ExperimentDAO experimentDAO, int objectId, User userFromLogin, String appIdHeader, String pacoVersionHeader, DateTimeZone timezone) throws Throwable {
    Outcome outcome = new Outcome(objectId);

    Long id = experimentDAO.getId();
    log.info("Retrieving experimentId, experimentName for experiment posting: " + id + ", " + experimentDAO.getTitle());
    Experiment experiment = null;
    if (id != null) {
      experiment = experimentRetriever.getExperiment(id);
    }
    if (experiment == null) {
      experimentDAO.setId(null);
    }

    if (experiment != null && !experiment.isAdmin(userFromLogin.getEmail().toLowerCase())) {
      outcome.setError("Existing experiment for this event: " + objectId + ". Not allowed to modify.");
      return outcome;
    }

    if (!experimentRetriever.saveExperiment(experimentDAO, userFromLogin, timezone.getID())) {
      outcome.setError("Could not save experiment: " + objectId +". ExperimentId: " + experimentDAO.getId()
                       + ". title: " + experimentDAO.getTitle());
    }
    return outcome;
  }


}
