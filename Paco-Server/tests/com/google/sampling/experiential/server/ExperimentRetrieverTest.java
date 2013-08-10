package com.google.sampling.experiential.server;

import java.util.List;

import junit.framework.TestCase;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.model.Experiment;

public class ExperimentRetrieverTest extends TestCase {


  private final String email = "bobevans@google.com";
  private final String authDomain = "unused_auth_domain";

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private PacoServiceImpl pacoService;

  public void setUp() {
    helper.setUp();
    pacoService = new PacoServiceImpl();
    logInEnvironment();
  }

  private void logInEnvironment() {
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(email);
    helper.setEnvAuthDomain(authDomain);
  }

  private void createAndSaveExperiment(String experimentJson) {
    ExperimentDAO testExperiment = JsonConverter.fromSingleEntityJson(experimentJson);
    testExperiment.setId(null);
    saveToServer(testExperiment);
  }

  private void saveToServer(ExperimentDAO experiment) {
    pacoService.saveExperiment(experiment);
  }


  public void testRetrieveMatchingExperimentsOneExperimentId() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1);
    List<Long> experimentList = Lists.newArrayList(1l);
    List<Experiment> experiments = ExperimentRetriever.getInstance().getExperimentsFor(experimentList);
    assertTrue(experiments.size() == 1);
    assertEquals(experiments.get(0).getId(), experimentList.get(0));
  }

  public void testRetrieveMatchingExperimentsTwoExperimentIds() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1);
    List<Long> experimentList = Lists.newArrayList(1l, 6l);
    List<Experiment> experiments = ExperimentRetriever.getInstance().getExperimentsFor(experimentList);
    assertTrue(experiments.size() == 2);
    assertEquals(experiments.get(0).getId(), experimentList.get(0));
    assertEquals(experiments.get(1).getId(), experimentList.get(1)); // TODO unsure of ordering from server.
  }


}
