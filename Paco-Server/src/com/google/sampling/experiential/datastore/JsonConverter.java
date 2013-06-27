package com.google.sampling.experiential.datastore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.ExperimentDAOCore;

public class JsonConverter {

  public static final Logger log = Logger.getLogger(JsonConverter.class.getName());

  /**
   * @param experiments
   * @param printWriter 
   * @return
   */
  public static String jsonify(List<ExperimentDAO> experiments) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      return mapper.writeValueAsString(experiments);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null; 
  }
  
  public static String shortJsonify(List<ExperimentDAO> experiments) {
    ObjectMapper mapper = new ObjectMapper();
    List<ExperimentDAOCore> shortExperiments = getShortExperiments(experiments);
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    try {
      return mapper.writeValueAsString(shortExperiments);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null; 
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
                                 experiment.getSignalingMechanisms(), experiment.getFixedDuration(),
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

  public static List<ExperimentDAO> fromEntitiesJson(String experimentJson) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
  
  public static ExperimentDAO fromSingleEntityJson(String experimentJson) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      ExperimentDAO experiment = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {});
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
}


