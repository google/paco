package com.google.paco.shared.scheduling;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.TimeUtil;

public class ActionScheduleGeneratorTest {

  @Test
  public void testEsmExperimentGroupNotStartedYet() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().plusDays(1).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     20 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     8 * 60 * 60 * 1000l,
                                     null,
                                     null,
                                     null,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertFalse("should not be started yet",
               ActionScheduleGenerator.isExperimentGroupStarted(experimentGroup));
  }

  @Test
  public void testEsmExperimentGroupIsStarted() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(1).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     20 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     8 * 60 * 60 * 1000l,
                                     null,
                                     null,
                                     null,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertTrue("should be started",
               ActionScheduleGenerator.isExperimentGroupStarted(experimentGroup));
  }

  @Test
  public void testFixedScheduleExperimentGroupNotStartedYet() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().plusDays(1).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    List<SignalTime> times = Lists.newArrayList();
    SignalTime signalTime = new SignalTime(SignalTime.FIXED_TIME,
                                           SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                           8 * 60 * 60 * 1000,
                                           null,
                                           null,
                                           "morning sample time");
    times.add(signalTime);
    Schedule schedule = new Schedule(Schedule.DAILY, (Boolean)null,
                                     (Integer)null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     1,
                                     times,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertFalse("should not be started yet",
               ActionScheduleGenerator.isExperimentGroupStarted(experimentGroup));
  }

  @Test
  public void testFixedScheduleExperimentGroupStarted() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(1).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    List<SignalTime> times = Lists.newArrayList();
    SignalTime signalTime = new SignalTime(SignalTime.FIXED_TIME,
                                           SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                           8 * 60 * 60 * 1000,
                                           null,
                                           null,
                                           "morning sample time");
    times.add(signalTime);
    Schedule schedule = new Schedule(Schedule.DAILY, (Boolean)null,
                                     (Integer)null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     1,
                                     times,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertTrue("should be started",
               ActionScheduleGenerator.isExperimentGroupStarted(experimentGroup));
  }

  @Test
  public void testFixedScheduleExperimentGroupStartedSameDay() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    List<SignalTime> times = Lists.newArrayList();
    SignalTime signalTime = new SignalTime(SignalTime.FIXED_TIME,
                                           SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                           20 * 60 * 60 * 1000,
                                           null,
                                           null,
                                           "morning sample time");
    times.add(signalTime);
    Schedule schedule = new Schedule(Schedule.DAILY, (Boolean)null,
                                     (Integer)null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     1,
                                     times,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertTrue("should be started",
               ActionScheduleGenerator.isExperimentGroupStarted(experimentGroup));
  }

  @Test
  public void testFixedScheduleExperimentGroupIsOver() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(2).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().minusDays(1).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    List<SignalTime> times = Lists.newArrayList();
    SignalTime signalTime = new SignalTime(SignalTime.FIXED_TIME,
                                           SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                           20 * 60 * 60 * 1000,
                                           null,
                                           null,
                                           "morning sample time");
    times.add(signalTime);
    Schedule schedule = new Schedule(Schedule.DAILY, (Boolean)null,
                                     (Integer)null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     1,
                                     times,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertTrue("should be over",
               ActionScheduleGenerator.isExperimentGroupOver(experimentGroup));
  }

  @Test
  public void testFixedScheduleExperimentGroupIsOverSameDay() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(2).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    List<SignalTime> times = Lists.newArrayList();
    SignalTime signalTime = new SignalTime(SignalTime.FIXED_TIME,
                                           SignalTime.OFFSET_BASIS_SCHEDULED_TIME,
                                           01 * 60 * 60 * 1000,
                                           null,
                                           null,
                                           "morning sample time");
    times.add(signalTime);
    Schedule schedule = new Schedule(Schedule.DAILY, (Boolean)null,
                                     (Integer)null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     1,
                                     times,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertFalse("should not be over because we don't evaluate below the level of the day -- today still gets a shot",
               ActionScheduleGenerator.isExperimentGroupOver(experimentGroup));
  }

  @Test
  public void testEsmExperimentGroupIsOver() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(2).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().minusDays(1).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     20 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     8 * 60 * 60 * 1000l,
                                     null,
                                     null,
                                     null,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertTrue("should be over",
               ActionScheduleGenerator.isExperimentGroupOver(experimentGroup));
  }

  @Test
  public void testEsmExperimentGroupIsNotOver() {
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experimentGroup.setFixedDuration(true);
    experimentGroup.setStartDate(TimeUtil.formatDate(DateTime.now().minusDays(2).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(DateTime.now().plusDays(1).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     20 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     8 * 60 * 60 * 1000l,
                                     null,
                                     null,
                                     null,
                                     null,
                                     true,
                                     15,
                                     59,
                                     0,
                                     0);
    schedules.add(schedule);
    ScheduleTrigger actionTrigger = new ScheduleTrigger(schedules);
    actionTriggers.add(actionTrigger);
    experimentGroup.setActionTriggers(actionTriggers);
    assertFalse("should not be over",
               ActionScheduleGenerator.isExperimentGroupOver(experimentGroup));
  }




}
