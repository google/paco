package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

import junit.framework.TestCase;

public class ExperimentServiceTest extends TestCase {


  private final String email = "user1@gmail.com";
  private final String authDomain = "unused_auth_domain";

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy() );

  public void setUp() {
    helper.setUp();
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
    Outcome result = new Outcome(0, "test needs to use ExperimentServlet.doPost()");
    if (!result.succeeded()) {
      throw new IllegalStateException("Could not save test experiments to server: " + result.getErrorMessage());
    }
  }


  public void testRetrieveMatchingExperimentsOneExperimentId() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1_NEW);
    List<Long> experimentList = Lists.newArrayList(1l);
    List<ExperimentDAO> experiments = ExperimentServiceFactory.getExperimentService().getExperimentsById(experimentList, email, null);
    assertTrue(experiments.size() == 1);
    assertEquals(experiments.get(0).getId(), experimentList.get(0));
  }

  public void testRetrieveMatchingExperimentsTwoExperimentIds() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1_NEW);
    List<Long> experimentList = Lists.newArrayList(1l, 4l);
    List<ExperimentDAO> experiments = ExperimentServiceFactory.getExperimentService().getExperimentsById(experimentList, email, null);
    assertTrue(experiments.size() == 2);
    assertEquals(experiments.get(0).getId(), experimentList.get(0));
    assertEquals(experiments.get(1).getId(), experimentList.get(1)); // TODO unsure of ordering from server.
  }

  public void testRetrieveMyExperiments() throws Exception {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_USER1_ADMIN_NEW);
    helper.setEnvEmail("user2@gmail.com"); // saving as use1 makes user1 an admin and creator.
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_USER2_ADMIN_NEW);
    helper.setEnvEmail("user2@gmail.com");
    DateTimeZone email1Timezone = DateTime.now().getZone();
    List<ExperimentDAO> experiments = ExperimentServiceFactory.getExperimentService().getMyJoinableExperiments("user1@gmail.com", email1Timezone, null, null).getExperiments();
    assertTrue(experiments.size() == 1);
    assertEquals("Test 5", experiments.get(0).getTitle());

    experiments = ExperimentServiceFactory.getExperimentService().getMyJoinableExperiments("user2@gmail.com", email1Timezone, null, null).getExperiments();
    assertTrue(experiments.size() == 2);
    assertEquals("Test 5", experiments.get(0).getTitle());
    assertEquals("Test 6", experiments.get(1).getTitle());
  }

  public void testRetrieveMyExperimentsUserPublishedUserButNotPublished() throws Exception {
    helper.setEnvEmail("user2@gmail.com");
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_USER1_PUBLISHED_USER_NOT_PUBLISHED_NEW);
    helper.setEnvEmail("user1@gmail.com");
    DateTimeZone email1Timezone = DateTime.now().getZone();
    List<ExperimentDAO> experiments = ExperimentServiceFactory.getExperimentService().getMyJoinableExperiments("user1@gmail.com", email1Timezone, null, null).getExperiments();
    assertTrue(experiments.size() == 0);
  }



  @Override
  protected void tearDown() throws Exception {
    helper.tearDown();
    super.tearDown();
  }


}
