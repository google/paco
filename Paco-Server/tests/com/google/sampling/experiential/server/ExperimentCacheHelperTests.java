package com.google.sampling.experiential.server;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.paco.shared.model.ExperimentDAO;

public class ExperimentCacheHelperTests extends TestCase {
  
  private ExperimentCacheHelper experimentCacheHelper;
  
  protected void setUp() throws Exception {
    super.setUp();
    experimentCacheHelper = ExperimentCacheHelper.getInstance();
  }
  
  @Test
  public void testExperimentIsOver() {
    
    DateTime today = new DateTime();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");
    
    ExperimentDAO experiment = new ExperimentDAO();
    experiment.setFixedDuration(true);
    experiment.setStartDate(today.minusDays(2).toString(formatter));
    
    // End date yesterday
    experiment.setEndDate(today.minusDays(1).toString(formatter));
    assertTrue(experimentCacheHelper.isOver(experiment, today));
   
    // End date tomorrow
    experiment.setEndDate(today.plusDays(1).toString(formatter));
    assertFalse(experimentCacheHelper.isOver(experiment, today));
   
    // End date today
    experiment.setEndDate(today.toString(formatter));
    assertFalse(experimentCacheHelper.isOver(experiment, today));
    
  }

}
