package com.google.paco.shared.model2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.paco.shared.scheduling.ActionScheduleGenerator;

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
      ObjectMapper mapper = getObjectMapper();

      Float pacoProtocolFloat = null;
      if (pacoProtocol != null) {
        try {
          pacoProtocolFloat = Float.parseFloat(pacoProtocol);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
      if (pacoProtocolFloat == null) {
        if (experiments == null) {
          experiments = Collections.EMPTY_LIST;
        }
        return mapper.writeValueAsString(experiments);
      } else if (pacoProtocolFloat >= 3.0) {
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
    Map<String, Object> preJsonObject = new HashMap();
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

    Date earliestStartDate = null;
    DateMidnight earliestStartDate2 = ActionScheduleGenerator.getEarliestStartDate(experiment);
    if (earliestStartDate2 != null) {
      earliestStartDate = earliestStartDate2.toDate();
    }
    Date endDate = null;
    DateTime lastEndTime = ActionScheduleGenerator.getLastEndTime(experiment);
    if (lastEndTime != null) {
      endDate = lastEndTime.toDateMidnight().toDate();
    }
    return new ExperimentDAOCore(experiment.getId(), experiment.getTitle(), experiment.getDescription(),
        experiment.getInformedConsentForm(), experiment.getCreator(),
        experiment.getJoinDate(),
        experiment.getRecordPhoneDetails(), experiment.getDeleted(), experiment.getExtraDataCollectionDeclarations(),
        experiment.getOrganization(), experiment.getContactPhone(), experiment.getContactEmail(),
        earliestStartDate,
        endDate);
  }

  public static String jsonify(ExperimentDAOCore experiment) {
    ObjectMapper mapper = getObjectMapper();
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

//  public static String jsonify(ExperimentDAO experiment) {
//    ObjectMapper mapper = getObjectMapper();
//    try {
//      return mapper.writeValueAsString(experiment);
//    } catch (JsonGenerationException e) {
//      log.severe("Json generation error " + e);
//    } catch (JsonMappingException e) {
//      log.severe("JsonMapping error getting experiments: " + e.getMessage());
//    } catch (IOException e) {
//      log.severe("IO error getting experiments: " + e.getMessage());
//    }
//    return null;
//  }

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
    return new ArrayList();
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

  public static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    mapper.getDeserializationConfig().addMixInAnnotations(ActionTrigger.class, ActionTriggerMixIn.class);
    mapper.getDeserializationConfig().addMixInAnnotations(PacoAction.class, PacoActionMixIn.class);
    return mapper;
  }


  @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.PROPERTY,
                property = "type")
            @JsonSubTypes({
                @Type(value = ScheduleTrigger.class, name = "scheduleTrigger"),
                @Type(value = InterruptTrigger.class, name = "interruptTrigger") })
  private class ActionTriggerMixIn
  {
    // Nothing to be done here. This class exists for the sake of its annotations.
  }

  @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.PROPERTY,
                property = "type")
            @JsonSubTypes({
              @Type(value = PacoActionAllOthers.class, name = "pacoActionAllOthers"),
                @Type(value = PacoNotificationAction.class, name = "pacoNotificationAction")})
  private class PacoActionMixIn
  {
    // Nothing to be done here. This class exists for the sake of its annotations.
  }


}


