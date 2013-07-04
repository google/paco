package com.google.sampling.experiential.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.sampling.experiential.datastore.JsonConverter;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.shared.MapService;
import com.google.sampling.experiential.shared.MapServiceAsync;

import junit.framework.TestCase;

public class ExperimentServletHandlerTest extends TestCase {
  
  private final String email = "bobevans@google.com";
  private final String tz = null;
  private final String userId = "bobevans@google.com";
  private Random randomGenerator;
  private MapService mapService;
  
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
                                                                           new LocalMemcacheServiceTestConfig());
  
  protected void setUp() throws Exception {
    super.setUp();
    helper.setUp();
    randomGenerator = new Random();
    mapService = new MapServiceImpl();
    
//   List<ExperimentDAO> testDAOs = JsonConverter.fromEntitiesJson(ExperimentTestConstants.experimentsList);
    
    ExperimentDAO testExperiment0 = JsonConverter.fromSingleEntityJson(ExperimentTestConstants.TEST_EXPERIMENT_0);
    testExperiment0.setId(null);
    testExperiment0.setCreator("rbe5000@gmail.com");
//    ExperimentDAO testExperiment1 = JsonConverter.fromSingleEntityJson(ExperimentTestConstants.TEST_EXPERIMENT_1);
//    ExperimentDAO testExperiment2 = JsonConverter.fromSingleEntityJson(ExperimentTestConstants.TEST_EXPERIMENT_2);
//    ExperimentDAO testExperiment3 = JsonConverter.fromSingleEntityJson(ExperimentTestConstants.TEST_EXPERIMENT_3);
    saveToServer(testExperiment0);
//    saveToServer(testExperiment1);
//    saveToServer(testExperiment2);
//    saveToServer(testExperiment3);
  }
  
  
  public void testShortLoadIsShortButComplete() {
    
   new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalMemcacheServiceTestConfig()).setUp();
    
    ExperimentServletHandler shortHandler = new ExperimentServletShortLoadHandler(email, tz);
    ExperimentServletHandler longHandler = new ExperimentServletAllExperimentsFullLoadHandler(userId, email, tz);
    
    String shortContent = shortHandler.performLoad();
    String longContent = longHandler.performLoad();
    assertTrue(shortContent.length() <= longContent.length());
    
    List<ExperimentDAO> shortLoadExperiments = JsonConverter.fromEntitiesJson(shortContent);
    List<ExperimentDAO> longLoadExperiments = JsonConverter.fromEntitiesJson(longContent);
    assertEquals(shortLoadExperiments.size(), longLoadExperiments.size());
  }
  
  public void testSelectedLoadReturnsOnlySelectedExperiments() {
    int numIdsToGenerate = randomGenerator.nextInt(100);
    List<Integer> experimentIds = new ArrayList<Integer>();
    for (int i=0; i < numIdsToGenerate; ++i) {
      experimentIds.add(randomGenerator.nextInt());
    }
    String param = Joiner.on(",").join(experimentIds);
    ExperimentServletHandler handler = new ExperimentServletSelectedExperimentsFullLoadHandler(email, tz, param);
    
    String content = handler.performLoad();
    assertTrue(content != null);
    System.out.println("Content is: " + content);
    
    List<ExperimentDAO> experiments = JsonConverter.fromEntitiesJson(content);
    assertTrue(experiments.size() <= numIdsToGenerate);
  }
  
  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }
  
  private void saveToServer(ExperimentDAO experiment) {
    mapService.saveExperiment(experiment);
  }

  

}
