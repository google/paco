package com.google.android.apps.paco.test;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.ExperimentScheduleActivity;
import com.google.common.collect.Lists;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;

/*
 * TODO: Make this into instrumentation testing, changing the experiment schedule
 *       by using button clicks.
 *  To do this, change the class to ActivityInstrumentationTestCase2<ExperimentScheduleActivity>.
 *  Change the ExperimentScheduleActivity.setActivityProperties method to display
 *  the appropriate layout, and be sure to call activity.setActivityProperties(...)
 *  within a call to activity.runOnUiThread(...).
 */
public class ExperimentScheduleActivityTest extends ActivityUnitTestCase<ExperimentScheduleActivity> {

  private ExperimentScheduleActivity activity;

  private static final Long START_TIME = Long.valueOf(500000);
  private static final Long END_TIME = Long.valueOf(1000000);
  private static final Integer REPEAT_RATE = 4;
  private static final Long ADAPTER_TIME = Long.valueOf(75000);
  private static final Integer DAY_OF_MONTH = 4;
  private static final Integer DAY_OF_WEEK = 2;

  private MockExperimentProviderUtil experimentProviderUtil;
  private Context context;

  private Intent intent;
  private Bundle bundle;

  private long experimentId = 0;


  public ExperimentScheduleActivityTest() {
    super(ExperimentScheduleActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = getInstrumentation().getContext();
    experimentProviderUtil = new MockExperimentProviderUtil(context);
    intent = new Intent();
    bundle = new Bundle();
    bundle.putBoolean(ExperimentScheduleActivity.IS_TESTING_KEY, true);
    intent.putExtras(bundle);
    startActivity(intent, null, null);
    activity = getActivity();
  }

  public void testEsmScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_ESM);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setEsmStartHour(START_TIME);
    activity.getExperiment().getSchedule().setEsmEndHour(END_TIME);
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getEsmStartHour(), START_TIME);
    assertEquals(savedExperiment.getSchedule().getEsmEndHour(), END_TIME);
    activity.finish();
  }

  public void testWeekdayScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_WEEKDAY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    activity.finish();
  }

  public void testDailyScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_DAILY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    activity.finish();
  }  

  public void testWeeklyScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_WEEKLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    // Bob: I didn't quite understand how setting the days of week worked.
    // How do we do that (i.e. how do we set multiple days of the week to
    // be scheduled)?
    // activity.getExperiment().getSchedule().setWeekDaysScheduled(1);
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    // Priya - TODO: check week days scheduled.
    activity.finish();
  }

  public void testMonthlyDayOfMonthScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setDayOfMonth(DAY_OF_MONTH);
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getDayOfMonth(), DAY_OF_MONTH);
    activity.finish();
  }

  public void testMonthlyNthOfMonthScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setNthOfMonth(DAY_OF_MONTH);
    activity.getExperiment().getSchedule().setWeekDaysScheduled(DAY_OF_WEEK);
    activity.save();

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getNthOfMonth(), DAY_OF_MONTH);
    assertEquals(savedExperiment.getSchedule().getWeekDaysScheduled(), DAY_OF_WEEK);
    activity.finish();
  }

  public void testEsmJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_ESM);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setEsmStartHour(START_TIME);
    activity.getExperiment().getSchedule().setEsmEndHour(END_TIME);
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getEsmStartHour(), START_TIME);
    assertEquals(savedExperiment.getSchedule().getEsmEndHour(), END_TIME);
    activity.finish();
  }

  public void testWeekdayJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_WEEKDAY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    activity.finish();
  }

  public void testDailyJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_DAILY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    activity.finish();
  }  

  public void testWeeklyJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_WEEKLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    // Bob: I didn't quite understand how setting the days of week worked.
    // How do we do that (i.e. how do we set multiple days of the week to
    // be scheduled)?
    // activity.getExperiment().getSchedule().setWeekDaysScheduled(1);
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
    // Priya - TODO: check week days scheduled.
    activity.finish();
  }

  public void testMonthlyDayOfMonthJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setDayOfMonth(DAY_OF_MONTH);
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getDayOfMonth(), DAY_OF_MONTH);
    activity.finish();
  }

  public void testMonthlyNthOfMonthJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setNthOfMonth(DAY_OF_MONTH);
    activity.getExperiment().getSchedule().setWeekDaysScheduled(DAY_OF_WEEK);
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getNthOfMonth(), DAY_OF_MONTH);
    assertEquals(savedExperiment.getSchedule().getWeekDaysScheduled(), DAY_OF_WEEK);
    activity.finish();
  }

  public void testSelfReportJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_SELFREPORT);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    activity.finish();
  }

  public void testTriggeredJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_TRIGGERED);
    joinExperiment(experiment);
    activity.setActivityProperties(experiment, experimentProviderUtil, true);

    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    activity.finish();
  }

  private Experiment getTestExperiment(String experimentTitle) {
    Experiment experiment = getExperimentFromJson(experimentTitle);
    experiment.setId(experimentId++);
    return experiment;
  }

  private Experiment getExperimentFromJson(String contentAsString) {
    try {
      Experiment experiment = ExperimentProviderUtil.getSingleExperimentFromJson(contentAsString);
      return experiment;
    } catch (JsonParseException e) {
      assertTrue(false);
      return null;
    } catch (JsonMappingException e) {
      assertTrue(false);
      return null;
    } catch (IOException e) {
      assertTrue(false);
      return null;
    }
  }

  private void joinExperiment(Experiment experiment) {
    experiment.setJoinDate(new DateTime());
    experimentProviderUtil.insertFullJoinedExperiment(experiment);
  }

  private int setExperimentTimes() {
    List<Long> times = activity.getExperiment().getSchedule().getTimes();
    int timesLength = times.size();
    for (int i=0; i < timesLength; ++i) {
      times.set(i, ADAPTER_TIME);
    }
    return timesLength;
  }

  private void checkExperimentTimesList(int timesLength, Experiment savedExperiment) {
    List<Long> newTimes = savedExperiment.getSchedule().getTimes();
    assertEquals(newTimes.size(), timesLength);
    for (int j=0; j < timesLength; ++j) {
      assertEquals(newTimes.get(j), ADAPTER_TIME);
    }
  }

  private void simulateDownloadingAndSchedulingExperiment(Experiment experiment) {
    activity.saveDownloadedExperiment(experiment);
  }

  private void checkExperimentProperlyJoined(Experiment savedExperiment) {
    assertNotNull(savedExperiment);
    assertNotNull(savedExperiment.getJoinDate());
  }

  private class MockExperimentProviderUtil extends ExperimentProviderUtil {

    private List<Experiment> experimentList;

    MockExperimentProviderUtil(Context context) {
      super(context);
      experimentList = Lists.newArrayList();
    }

    @Override
    public Uri insertFullJoinedExperiment(Experiment experiment) {
      experimentList.add(experiment);
      return Uri.parse("http://www.thisIsATest.com");
    }

    @Override
    public int deleteNotificationsForExperiment(Long experimentId) {
      return 0;
    }

    @Override
    public void updateJoinedExperiment(Experiment experiment) {
      Experiment experimentToDelete = null;
      for (Experiment e : experimentList) {
        if (e.getId().equals(experiment.getId())) {
          experimentToDelete = e;
        }
      }
      if (experimentToDelete != null) {
        experimentList.remove(experimentToDelete);
        experimentList.add(experiment);
      }
    }

    @Override
    public Experiment getExperiment(long experimentId) {
      for (Experiment e : experimentList) {
        if (e.getId().equals(experimentId)) {
          return e;
        }
      }
      return null;
    }

    @Override
    public void deleteExperiment(long experimentId) {
      Experiment experimentToDelete = null;
      for (Experiment e : experimentList) {
        if (e.getId().equals(experimentId)) {
          experimentToDelete = e;
        }
      }
      if (experimentToDelete != null) {
        experimentList.remove(experimentToDelete);
      }
    }

  }

}
