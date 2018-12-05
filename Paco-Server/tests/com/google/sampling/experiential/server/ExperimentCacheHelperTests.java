package com.google.sampling.experiential.server;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;

import junit.framework.TestCase;

public class ExperimentCacheHelperTests extends TestCase {

  private ExperimentService experimentRetriever;

  protected void setUp() throws Exception {
    super.setUp();
    experimentRetriever = ExperimentServiceFactory.getExperimentService();
  }

  @Test
  public void testExperimentIsOver() {
    DateTime today = new DateTime();
    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    ExperimentDAO experiment = new ExperimentDAO();
    Schedule signalSchedule = new Schedule();
    signalSchedule.setScheduleType(Schedule.ESM);

    ScheduleTrigger st = new ScheduleTrigger(Lists.newArrayList(signalSchedule));
    PacoAction a = new PacoNotificationAction();
    st.setActions(Lists.newArrayList(a));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    actionTriggers.add(st);

    ExperimentGroup eg = new ExperimentGroup();
    eg.setActionTriggers(actionTriggers);

    List<ExperimentGroup> egl = Lists.newArrayList(eg);

    experiment.setGroups(egl);
    eg.setFixedDuration(true);
    eg.setStartDate(today.minusDays(2).toString(formatter));

    // End date yesterday
    eg.setEndDate(today.minusDays(1).toString(formatter));
    assertTrue(experimentRetriever.isOver(experiment, today));

    // End date tomorrow
    eg.setEndDate(today.plusDays(1).toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

    // End date today
    eg.setEndDate(today.toString(formatter));
    assertFalse(experimentRetriever.isOver(experiment, today));

  }

}
