package com.google.paco.shared.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.EventInterface;
import com.pacoapp.paco.shared.model2.EventStore;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.scheduling.EsmSignalStore;
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

  @Test
  public void testEsmFiveDays() {
    ExperimentDAO experiment = new ExperimentDAO();
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experiment.setGroups(Lists.newArrayList(experimentGroup));
    experimentGroup.setFixedDuration(true);
    final DateTime day1 = DateTime.now();
    experimentGroup.setStartDate(TimeUtil.formatDate(day1.minusDays(3).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(day1.plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     22 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     9 * 60 * 60 * 1000l,
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

    final Map<Long, List<DateTime>> store = Maps.newConcurrentMap();
    EsmSignalStore alarmStore = new EsmSignalStore() {

      // date, list of times


      @Override
      public void storeSignal(Long date, Long experimentId, Long alarmTime, String groupName, Long actionTriggerId,
                              Long scheduleId) {
        List<DateTime> existing = store.get(date);
        if (existing == null) {
          existing = Lists.newArrayList();
          store.put(date, existing);
        }
        existing.add(new DateTime(alarmTime));

      }

      @Override
      public List<DateTime> getSignals(Long experimentId, Long periodStart, String groupName, Long actionTriggerId,
                                       Long scheduleId) {
        List<DateTime> list = store.get(periodStart);
        if (list != null) {
          return list;
        } else {
          return Lists.newArrayList();
        }
      }

      @Override
      public void deleteAll() {
        store.clear();

      }

      @Override
      public void deleteAllSignalsForSurvey(Long experimentId) {
        store.clear();

      }

      @Override
      public void deleteSignalsForPeriod(Long experimentId, Long periodStart, String groupName, Long actionTriggerId,
                                         Long scheduleId) {
        store.remove(periodStart);

      }

    };

    EventStore eventStore = new EventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void updateEvent(EventInterface correspondingEvent) {
        // TODO Auto-generated method stub

      }

      @Override
      public void insertEvent(EventInterface event) {
        // TODO Auto-generated method stub

      }

    };

    final ArrayList<ExperimentDAO> experiments = Lists.newArrayList(experiment);

    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1.minusDays(3), alarmStore, eventStore);
    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1.minusDays(2), alarmStore, eventStore);
    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1.minusDays(1), alarmStore, eventStore);
    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1, alarmStore, eventStore);
    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1.plusDays(1), alarmStore, eventStore);
    ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, day1.plusDays(2), alarmStore, eventStore);


    for (Entry<Long, List<DateTime>> key : store.entrySet()) {
      System.out.println("Date: " + new DateMidnight(key.getKey()));
      List<DateTime> values = key.getValue();
      for (DateTime dateTime : values) {
        System.out.println("  time: " + dateTime.toString());
      }

    }
    int count = countDatesInStoreForAllDays(store);

    assertEquals("should match", 56, count);
  }

  public int countDatesInStoreForAllDays(final Map<Long, List<DateTime>> store) {
    int count = 0;
    for (Entry<Long, List<DateTime>> entry : store.entrySet()) {
      count += entry.getValue().size();
    }
    return count;
  }

  @Test
  public void testEsmGenMultiThread() throws InterruptedException, ExecutionException {
    ExperimentDAO experiment = new ExperimentDAO();
    ExperimentGroup experimentGroup = new ExperimentGroup();
    experiment.setGroups(Lists.newArrayList(experimentGroup));
    experimentGroup.setFixedDuration(true);
    final DateTime day1 = DateTime.now();
    experimentGroup.setStartDate(TimeUtil.formatDate(day1.minusDays(3).getMillis()));
    experimentGroup.setEndDate(TimeUtil.formatDate(day1.plusDays(2).getMillis()));
    List<ActionTrigger> actionTriggers = Lists.newArrayList();
    List<Schedule> schedules = Lists.newArrayList();
    Schedule schedule = new Schedule(Schedule.ESM, (Boolean)null,
                                     (Integer)null,
                                     22 * 60 * 60 * 1000l,
                                     8,
                                     Schedule.ESM_PERIOD_DAY,
                                     9 * 60 * 60 * 1000l,
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

    final Map<Long, List<DateTime>> store = Maps.newConcurrentMap();
    final EsmSignalStore alarmStore = new EsmSignalStore() {

      // date, list of times


      @Override
      public synchronized void storeSignal(Long date, Long experimentId, Long alarmTime, String groupName, Long actionTriggerId,
                              Long scheduleId) {
        List<DateTime> existing = store.get(date);
        if (existing == null) {
          existing = Lists.newArrayList();
          store.put(date, existing);
        }
        final DateTime at = new DateTime(alarmTime);
//        Log.info("Adding new signal time: " + at.toString() );
//        Log.info("Thread = " + Thread.currentThread().toString());
        existing.add(at);

      }

      @Override
      public synchronized List<DateTime> getSignals(Long experimentId, Long periodStart, String groupName, Long actionTriggerId,
                                       Long scheduleId) {
        List<DateTime> list = store.get(periodStart);
        if (list != null) {
          return list;
        } else {
          return Lists.newArrayList();
        }
      }

      @Override
      public void deleteAll() {
        store.clear();

      }

      @Override
      public synchronized void deleteAllSignalsForSurvey(Long experimentId) {
        store.clear();

      }

      @Override
      public synchronized void deleteSignalsForPeriod(Long experimentId, Long periodStart, String groupName, Long actionTriggerId,
                                         Long scheduleId) {
        store.remove(periodStart);

      }

    };

    final EventStore eventStore = new EventStore() {

      @Override
      public EventInterface getEvent(Long experimentId, DateTime scheduledTime, String groupName, Long actionTriggerId,
                                     Long scheduleId) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void updateEvent(EventInterface correspondingEvent) {
        // TODO Auto-generated method stub

      }

      @Override
      public void insertEvent(EventInterface event) {
        // TODO Auto-generated method stub

      }

    };

    final ArrayList<ExperimentDAO> experiments = Lists.newArrayList(experiment);
    ExecutorService tp = Executors.newFixedThreadPool(5);
    List<FutureTask<Void>> futures = Lists.newArrayList();
    for (int i=0; i < 1; i++) {
      FutureTask<Void> r = new FutureTask<Void>(new Runnable() {

        @Override
        public void run() {
          final DateTime date = day1.minusDays(3);
          ActionScheduleGenerator.arrangeExperimentsByNextTimeFrom(experiments, date, alarmStore, eventStore);
          assertEquals(8, countDatesInStoreForAllDays(store, date));
        }
      }, null);
      futures.add(r);
      tp.execute(r);
    }

    for (FutureTask<Void> futureTask : futures) {
      futureTask.get();
    }

    int count = countDatesInStoreForAllDays(store);

//    Log.info("Ready to assert");
    assertEquals("should not be null", 16, count);
  }

  protected int countDatesInStoreForAllDays(Map<Long, List<DateTime>> store, DateTime date) {
    return store.get(new DateMidnight(date).getMillis()).size();
  }


}
