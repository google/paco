package com.google.sampling.experiential.datastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Maps;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentDAOCore;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;

public class JsonConverter {

  public static final Logger log = Logger.getLogger(JsonConverter.class.getName());

  /**
   * @param experiments
   * @param pacoProtocol TODO
   * @param printWriter
   * @return
   */
  public static String jsonify(List<? extends ExperimentDAOCore> experiments, Integer limit, String cursor, String pacoProtocol) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

      if (pacoProtocol == null) {
        if (experiments == null) {
          experiments = Collections.EMPTY_LIST;
        }
        return mapper.writeValueAsString(experiments);
      } else if (pacoProtocol.equals("3.0")) {
        Map<String, Object> preJsonObject = buildV3ProtocolJson(experiments, limit, cursor);
        return mapper.writeValueAsString(preJsonObject);
      }

    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null;
  }

  private static Map<String, Object> buildV3ProtocolJson(List<? extends ExperimentDAOCore> experiments, Integer limit,
                                                         String cursor) {
    if (experiments == null) {
      experiments = Collections.EMPTY_LIST;
    }
    Map<String, Object> preJsonObject = Maps.newHashMap();
    preJsonObject.put("results", experiments);
    if (limit != null) {
      preJsonObject.put("limit", limit);
    }

    if (cursor != null) {
      preJsonObject.put("cursor", cursor);
    }
    return preJsonObject;
  }

  public static String shortJsonify(List<ExperimentDAO> experiments, Integer limit, String cursor, String pacoProtocol) {
    List<ExperimentDAOCore> shortExperiments = getShortExperiments(experiments);
    return jsonify(shortExperiments, limit, cursor, pacoProtocol);
  }

  private static List<ExperimentDAOCore> getShortExperiments(List<ExperimentDAO> experiments) {
    List<ExperimentDAOCore> shortExperiments = new ArrayList<ExperimentDAOCore>();
    for (ExperimentDAO experiment : experiments) {
      shortExperiments.add(experimentDAOCoreFromExperimentDAO(experiment));
    }
    return shortExperiments;

  }

  private static ExperimentDAOCore experimentDAOCoreFromExperimentDAO(ExperimentDAO experiment) {
    return new ExperimentDAOCore(experiment.getId(), experiment.getTitle(), experiment.getDescription(),
                                 experiment.getInformedConsentForm(), experiment.getCreator(),
                                 experiment.getFixedDuration(),
                                 experiment.getStartDate(), experiment.getEndDate(), experiment.getJoinDate());
  }

  public static String jsonify(ExperimentDAO experiment) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null;
  }

  public static List<ExperimentDAO> fromEntitiesJsonUpload(String experimentJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      List<ExperimentDAO> experiments = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {});
      return experiments;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }

  public static Map<String, Object> fromEntitiesJson(String resultsJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      Map<String, Object> resultObjects = mapper.readValue(resultsJson, new TypeReference<Map<String, Object>>() {});
      Object experimentResults = resultObjects.get("results");
      String experimentJson = mapper.writeValueAsString(experimentResults);
      List<ExperimentDAO> experiments = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {});
      resultObjects.put("results", experiments);
      return resultObjects;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }



  public static ExperimentDAO fromSingleEntityJson(String experimentJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      ExperimentDAO experiment = mapper.readValue(experimentJson, new TypeReference<ExperimentDAO>() {});
      return experiment;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.getDeserializationConfig().addMixInAnnotations(SignalingMechanismDAO.class, SignalingMechanismDAOMixIn.class);
    return mapper;
  }


  @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.PROPERTY,
                property = "type")
            @JsonSubTypes({
                @Type(value = SignalScheduleDAO.class, name = "signalSchedule"),
                @Type(value = TriggerDAO.class, name = "trigger") })
  private class SignalingMechanismDAOMixIn
  {
    // Nothing to be done here. This class exists for the sake of its annotations.
  }

}


