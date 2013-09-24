package com.google.sampling.experiential.server;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;

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
    SignalScheduleDAO signalSchedule = new SignalScheduleDAO();
    signalSchedule.setScheduleType(SignalScheduleDAO.ESM);

    experiment.setSignalingMechanisms(new SignalingMechanismDAO[] {signalSchedule});
    experiment.setFixedDuration(true);
    experiment.setStartDate(today.minusDays(2).toString(formatter));

    // End date yesterday
    experiment.setEndDate(today.minusDays(1).toString(formatter));
    assertTrue(experimentRetriever.isOver(experiment, today));

    // End date tomorrow
    experiment.setEndDate(today.plusDays(1).toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

    // End date today
    experiment.setEndDate(today.toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

  }

}
