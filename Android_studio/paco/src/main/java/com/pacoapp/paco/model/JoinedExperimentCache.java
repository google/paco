package com.pacoapp.paco.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JoinedExperimentCache {

  private static JoinedExperimentCache instance;

  public static synchronized JoinedExperimentCache getInstance() {
    if (instance == null) {
      instance = new JoinedExperimentCache();
    }
    return instance;
  }


  private Map<Long, Experiment> experimentsByContentProviderId = Maps.newConcurrentMap();

  private JoinedExperimentCache() {
    super();
  }

  public void deleteAllExperiments() {
    experimentsByContentProviderId.clear();

  }

  public void insertExperiment(Experiment experiment) {
    experimentsByContentProviderId.put(experiment.getId(), experiment);
  }

  public void deleteExperiment(Long experimentId) {
    experimentsByContentProviderId.remove(experimentId);
  }

  public List<Experiment> getExperimentsByServerId(long id) {
    List<Experiment> experiments = Lists.newArrayList();
    for (Entry<Long, Experiment> experimentEntrySet : experimentsByContentProviderId.entrySet()) {
      if (experimentEntrySet.getValue().getServerId() == id) {
        experiments.add(experimentEntrySet.getValue());
      }
    }
    return experiments;
  }

  public void insertExperiments(List<Experiment> foundExperiments) {
    for (Experiment experiment : foundExperiments) {
      insertExperiment(experiment);
    }

  }

  public Experiment getExperimentByServerId(long id) {
    for (Entry<Long, Experiment> experimentEntrySet : experimentsByContentProviderId.entrySet()) {
      if (experimentEntrySet.getValue().getServerId() == id) {
        return experimentEntrySet.getValue();
      }
    }
    return null;
  }

  public List<Experiment> getExperiments() {
    return Lists.newArrayList(experimentsByContentProviderId.values());
  }

}
