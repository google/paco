package com.google.sampling.experiential.server;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Joiner;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.shared.MapService;

import junit.framework.TestCase;

public class ExperimentServletHandlerTest extends TestCase {
  
  private static final Integer FIRST_EXPERIMENT_ID = 1;
  private static final Integer SECOND_EXPERIMENT_ID = 6;
  private static final Integer THIRD_EXPERIMENT_ID = 11;
  private static final Integer NONEXISTANT_EXPERIMENT_ID = 2;
  
  private final String email = "bobevans@google.com";
  private final String tz = null;
  private final String userId = "bobevans@google.com";
  private final String authDomain = "unused_auth_domain";
  private MapService mapService;
  
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
                                                                           new LocalMemcacheServiceTestConfig());
  
  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(email);
    helper.setEnvAuthDomain(authDomain);
    mapService = new MapServiceImpl();
    
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_0);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_1);
    createAndSaveExperiment(ExperimentTestConstants.TEST_EXPERIMENT_3);
  }


  private void createAndSaveExperiment(String experimentTitle) {
    ExperimentDAO testExperiment1 = JsonConverter.fromSingleEntityJson(experimentTitle);
    testExperiment1.setId(null);
    saveToServer(testExperiment1);
  }
  
  
  public void testShortLoadIsShortButComplete() {
    ExperimentServletHandler shortHandler = new ExperimentServletShortLoadHandler(email, tz);
    ExperimentServletHandler longHandler = new ExperimentServletAllExperimentsFullLoadHandler(userId, email, tz);

    String shortContent = shortHandler.performLoad();
    String longContent = longHandler.performLoad();
    assertTrue(shortContent.length() <= longContent.length());
    
    List<ExperimentDAO> shortLoadExperiments = getExperimentList(shortContent);
    List<ExperimentDAO> longLoadExperiments = getExperimentList(longContent);
    assertEquals(shortLoadExperiments.size(), 3);
    assertEquals(longLoadExperiments.size(), 3);
  }
  
  public void testSelectedLoadReturningNoExperiments() {
    List<Integer> experimentIds = Arrays.asList(NONEXISTANT_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, tz, param);
    
    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 0);
  }
  
  public void testSelectedLoadReturningOneExperiment() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, tz, param);
    
    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 1);
  }
  
  public void testSelectedLoadReturningMultipleExperiments() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID, SECOND_EXPERIMENT_ID,
                                                THIRD_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, tz, param);
    
    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 3);
  }
  
  public void testSelectedLoadWithListContainingNonexistantId() {
    List<Integer> experimentIds = Arrays.asList(FIRST_EXPERIMENT_ID, NONEXISTANT_EXPERIMENT_ID,
                                                THIRD_EXPERIMENT_ID);
    String param = createExperimentIdParam(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, tz, param);
    
    String content = handler.performLoad();
    assertTrue(content != null);
    List<ExperimentDAO> experiments = getExperimentList(content);
    assertEquals(experiments.size(), 2);
  }

  
  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }
  
  private void saveToServer(ExperimentDAO experiment) {
    mapService.saveExperiment(experiment);
  }
  
  private List<ExperimentDAO> getExperimentList(String content) {
    List<ExperimentDAO> experiments = JsonConverter.fromEntitiesJson(content);
    return experiments;
  }
  
  private String createExperimentIdParam(List<Integer> experimentIds) {
    String param = Joiner.on(",").join(experimentIds);
    return param;
  }

  

}
