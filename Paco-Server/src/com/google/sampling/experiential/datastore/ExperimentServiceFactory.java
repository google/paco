package com.google.sampling.experiential.datastore;

import com.google.sampling.experiential.server.ExperimentService;


public class ExperimentServiceFactory {

  public static ExperimentService getExperimentService() {
    return new DefaultExperimentService();
  }
}
