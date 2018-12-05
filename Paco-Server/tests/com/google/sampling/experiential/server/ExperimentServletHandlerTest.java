package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTimeZone;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAOCore;
import com.pacoapp.paco.shared.model2.JsonConverter;

import junit.framework.TestCase;

public class ExperimentServletHandlerTest extends TestCase {

  private static final Integer FIRST_EXPERIMENT_ID = 1;
  private static final Integer SECOND_EXPERIMENT_ID = 4;
  private static final Integer THIRD_EXPERIMENT_ID = 7;
  private static final Integer NONEXISTANT_EXPERIMENT_ID = 2;

  private final String email = "bobevans@google.com";
  private final String userId = "bobevans@google.com";
  private final String authDomain = "unused_auth_domain";

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
                                                                           new LocalMemcacheServiceTestConfig());
  private String pacoProtocol = null;

  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    logInEnvironment();

    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_2_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_3_NEW);
  }


  private void logInEnvironment() {
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(email);
    helper.setEnvAuthDomain(authDomain);
  }


  private void createAndSaveExperiment(String experimentTitle) {
    ExperimentDAO testExperiment = JsonConverter.fromSingleEntityJson(experimentTitle);
    testExperiment.setId(null);
    saveToServer(testExperiment);
  }


  public void testShortLoadIsShortButComplete() {
    ExperimentServletHandler shortHandler = new ExperimentServletExperimentsForMeLoadHandler(email, null, null, null, pacoProtocol);
    ExperimentServletHandler longHandler = new ExperimentServletAdminExperimentsFullLoadHandler(email, null, null, null, pacoProtocol, null, null);

    String shortContent = shortHandler.performLoad();
    String longContent = longHandler.performLoad();
    assertTrue(shortContent.length() <= longContent.length());

    List<ExperimentDAO> shortLoadExperiments = getExperimentList(shortContent);
    List<ExperimentDAO> longLoadExperiments = getExperimentList(longContent);
    assertEquals(shortLoadExperiments.size(), 4);
    assertEquals(longLoadExperiments.size(), 4);
  }

  public void testSelectedLoadReturningNoExperiments() {
    List<Integer> experimentIds = Arrays.asList(NONEXISTANT_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, null, param, pacoProtocol);

    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 0);
  }

  public void testSelectedLoadReturningOneExperiment() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, null, param, pacoProtocol);

    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 1);
  }

  public void testSelectedLoadReturningMultipleExperiments() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID, SECOND_EXPERIMENT_ID,
                                                THIRD_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, null, param, pacoProtocol);

    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 3);
  }

  public void testSelectedLoadWithListContainingNonexistantId() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID, NONEXISTANT_EXPERIMENT_ID,
                                                THIRD_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, null, param, pacoProtocol);

    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(2, experiments.size());
  }

  public void testPublicExperimentNoPagination() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3_NEW);
    DateTimeZone timezone = DateTimeZone.getDefault();
    ExperimentServletHandler handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, timezone, null, null, pacoProtocol );
    String content = handler.performLoad();

    assertTrue(!Strings.isNullOrEmpty(content));
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(6, experiments.size());
  }

  public void testPublicExperimentWithPaginationCoveringAll() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3_NEW);
    ExperimentServletHandler handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 4, null, pacoProtocol);
    String content = handler.performLoad();


    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(4, experiments.size());

    String cursor = handler.cursor;
    assertNotNull(cursor);

    ExperimentServletHandler handler2 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 4, cursor, pacoProtocol);
    String content2 = handler2.performLoad();
    assertNotSame(cursor, handler2.cursor);
    assertTrue(content2 != null);
    List<ExperimentDAO> experiments2 = getExperimentList(content2);
    assertEquals(2, experiments2.size());

    ExperimentServletHandler handler3 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 4, handler2.cursor, pacoProtocol);
    String content3 = handler3.performLoad();
    assertNotSame(cursor, handler3.cursor);
    assertTrue(content3 != null);
    List<ExperimentDAO> experiments3 = getExperimentList(content3);
    assertEquals(0, experiments3.size());
  }


  public void testPublicExperimentWithPaginationOnMultiplePages() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2_NEW);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3_NEW);
    ExperimentServletHandler handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 2, null, pacoProtocol);
    String content = handler.performLoad();


    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(2, experiments.size());

    String cursor = handler.cursor;
    assertNotNull(cursor);

    ExperimentServletHandler handler2 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 2, cursor, pacoProtocol);
    String content2 = handler2.performLoad();

    assertNotSame(cursor, handler2.cursor);

    assertTrue(content2 != null);
    List<ExperimentDAO> experiments2 = getExperimentList(content2);
    assertEquals(2, experiments2.size());

    List<String> experimentsGroup1Names = Lists.newArrayList();
    for (int i=0; i < experiments.size(); i++) {
      ExperimentDAOCore experimentDAO = experiments.get(i);
      experimentsGroup1Names.add(experimentDAO.getTitle());
    }
    for (ExperimentDAOCore experimentDAO : experiments2) {
      assertTrue(experimentDAO.getTitle() + " should not be in first page of experiments", !experimentsGroup1Names.contains(experimentDAO.getTitle()));
    }
///////////////////////////////
    ExperimentServletHandler handler3 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 2, handler2.cursor, pacoProtocol);
    String content3 = handler3.performLoad();

    assertNotSame(handler2.cursor, handler3.cursor);

    assertTrue(content2 != null);
    List<ExperimentDAO> experiments3 = getExperimentList(content2);
    assertEquals(2, experiments3.size());

    List<String> experimentsGroup2Names = Lists.newArrayList();
    for (int i=0; i < experiments.size(); i++) {
      ExperimentDAOCore experimentDAO = experiments.get(i);
      experimentsGroup2Names.add(experimentDAO.getTitle());
    }
    for (ExperimentDAOCore experimentDAO : experiments3) {
      assertTrue(experimentDAO.getTitle() + " should not be in second page of experiments", !experimentsGroup2Names.contains(experimentDAO.getTitle()));
    }



    //////////////////////////////////////
    ExperimentServletHandler handler4 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 2, handler3.cursor, pacoProtocol);
    String content4 = handler4.performLoad();

    assertNotSame(handler3.cursor, handler4.cursor);

    assertTrue(content4 != null);
    List<ExperimentDAO> experiments4 = getExperimentList(content4);
    assertEquals(0, experiments4.size());

  }



  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }

  private void saveToServer(ExperimentDAO experiment) {
    Outcome outcome = new Outcome(0, "Test needs to be fixed to save using ExperimentServlet.doPost()");
    if (!outcome.succeeded()) {
      throw new IllegalStateException("Could not save test experiments to server: " + outcome.getErrorMessage());
    }
  }

  private List<ExperimentDAO> getExperimentList(String content) {
    List<ExperimentDAO> experiments = JsonConverter.fromEntitiesJsonUpload(content);
    return experiments;
  }

  private String createExperimentIdParam(List<Integer> experimentIds) {
    String param = Joiner.on(",").join(experimentIds);
    return param;
  }



}
