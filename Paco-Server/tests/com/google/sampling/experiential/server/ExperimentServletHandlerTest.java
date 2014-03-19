package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.shared.PacoService;

public class ExperimentServletHandlerTest extends TestCase {

  private static final Integer FIRST_EXPERIMENT_ID = 1;
  private static final Integer SECOND_EXPERIMENT_ID = 6;
  private static final Integer THIRD_EXPERIMENT_ID = 11;
  private static final Integer NONEXISTANT_EXPERIMENT_ID = 2;

  private final String email = "bobevans@google.com";
  private final String userId = "bobevans@google.com";
  private final String authDomain = "unused_auth_domain";
  private PacoService mapService;

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
                                                                           new LocalMemcacheServiceTestConfig());
  private String pacoProtocol = null;

  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    logInEnvironment();
    mapService = new PacoServiceImpl();

    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_2);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_3);
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
    ExperimentServletHandler shortHandler = new ExperimentServletShortLoadHandler(email, null, null, null, pacoProtocol);
    ExperimentServletHandler longHandler = new ExperimentServletAllExperimentsFullLoadHandler(email, null, null, null, pacoProtocol);

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
    assertEquals(experiments.size(), 2);
  }

  public void testPublicExperimentNoPagination() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3);
    ExperimentServletHandler handler = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, null, null, pacoProtocol );
    String content = handler.performLoad();

    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(4, experiments.size());
  }

  public void testPublicExperimentWithPaginationCoveringAll() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3);
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
    assertEquals(0, experiments2.size());
  }


  public void testPublicExperimentWithPaginationOnMultiplePages() {
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_1);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_2);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_PUBLISHED_3);
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
      ExperimentDAO experimentDAO = experiments.get(i);
      experimentsGroup1Names.add(experimentDAO.getTitle());
    }
    for (ExperimentDAO experimentDAO : experiments2) {
      assertTrue(experimentDAO.getTitle() + " should not be in first page of experiments", !experimentsGroup1Names.contains(experimentDAO.getTitle()));
    }

    ExperimentServletHandler handler3 = new ExperimentServletExperimentsShortPublicLoadHandler(email, null, 2, handler2.cursor, pacoProtocol);
    String content3 = handler3.performLoad();

    assertNotSame(handler2.cursor, handler3.cursor);

    assertTrue(content3 != null);
    List<ExperimentDAO> experiments3 = getExperimentList(content3);
    assertEquals(0, experiments3.size());

  }



  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }

  private void saveToServer(ExperimentDAO experiment) {
    mapService.saveExperiment(experiment, null);
  }

  private List<ExperimentDAO> getExperimentList(String content) {
    Map<String, Object> results = JsonConverter.fromEntitiesJson(content);
    List<ExperimentDAO> experiments = (List<ExperimentDAO>) results.get("results");
    return experiments;
  }

  private String createExperimentIdParam(List<Integer> experimentIds) {
    String param = Joiner.on(",").join(experimentIds);
    return param;
  }



}
