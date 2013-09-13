package com.google.sampling.experiential.server;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;

public class ExperimentCacheHelperTests extends TestCase {

  private ExperimentRetriever experimentRetriever;


  protected void setUp() throws Exception {
    super.setUp();
    experimentRetriever = ExperimentRetriever.getInstance();
  }

  @Test
  public void testExperimentIsOver() {

    DateTime today = new DateTime();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    ExperimentDAO experiment = new ExperimentDAO();
    experiment.getSignalGroups()[0].setFixedDuration(true);
    experiment.getSignalGroups()[0].setStartDate(today.minusDays(2).toString(formatter));
    TriggerDAO triggerDAO = new TriggerDAO();
    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];
    signalingMechanisms[0] = triggerDAO;
    experiment.getSignalGroups()[0].setSignalingMechanisms(signalingMechanisms);

    // End date yesterday
    experiment.getSignalGroups()[0].setEndDate(today.minusDays(1).toString(formatter));
    assertTrue(experimentRetriever.isOver(experiment, today));

    // End date tomorrow
    experiment.getSignalGroups()[0].setEndDate(today.plusDays(1).toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

    // End date today
    experiment.getSignalGroups()[0].setEndDate(today.toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

  }


}
