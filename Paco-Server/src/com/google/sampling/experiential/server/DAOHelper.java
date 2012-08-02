// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ObservedExperiment;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

import java.io.IOException;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class DAOHelper {
  public static String experimentToJson(Experiment experiment) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String observedExperimentToJson(ObservedExperiment experiment) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Experiment jsonToExperiment(String json) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.readValue(json, Experiment.class);
    } catch (JsonParseException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ObservedExperiment jsonToObservedExperiment(String json) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      return mapper.readValue(json, ObservedExperiment.class);
    } catch (JsonParseException e) {
      e.printStackTrace();
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
