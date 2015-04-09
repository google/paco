package com.google.sampling.experiential.server;


public class ExperimentServiceFactory {

  public static ExperimentService getExperimentService() {
    return new DefaultExperimentService();
  }
}
