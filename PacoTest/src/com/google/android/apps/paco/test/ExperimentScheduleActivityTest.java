package com.google.android.apps.paco.test;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.ExperimentScheduleActivity;
import com.google.android.apps.paco.SignalSchedule;
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
    startActivity(intent, null, null);
    activity = getActivity();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    activity.finish();
  }

  public void testEsmScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_ESM);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    setActivityExperimentEsmSchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentEsmSchedule(savedExperiment);
  }

  public void testWeekdayScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_WEEKDAY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentWeekdaySchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentWeekdaySchedule(timesLength, savedExperiment);
  }

  public void testDailyScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_DAILY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentDailySchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentDailySchedule(timesLength, savedExperiment);
  }

  public void testWeeklyScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_WEEKLY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentWeeklySchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentWeeklySchedule(timesLength, savedExperiment);
  }

  public void testWeeklySchedulingSetsOnlyCorrectDays() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_WEEKLY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentRepeatRateAndTimes();
    activity.getExperiment().getSchedule().removeAllWeekDaysScheduled();
    activity.getExperiment().getSchedule().addWeekDayToSchedule(SignalSchedule.SUNDAY);
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
    assertTrue(activity.getExperiment().getSchedule().isWeekDayScheduled(SignalSchedule.SUNDAY));
    for (int i = 1; i < 7; ++i) {
      assertFalse(activity.getExperiment().getSchedule().isWeekDayScheduled(SignalSchedule.DAYS_OF_WEEK[i]));
    }
  }

  public void testMonthlyDayOfMonthScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    setActivityExperimentMonthlyDayOfMonthSchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentMonthlyDayOfMonthSchedule(savedExperiment);
  }

  public void testMonthlyNthOfMonthScheduling() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    joinExperiment(experiment);
    configureActivityForTesting(experiment);

    setActivityExperimentMonthlyNthOfMonthSchedule();
    saveExperimentSchedule();

    Experiment savedExperiment = getSavedExperiment(experiment);
    checkSavedExperimentMonthlyNthOfMonthSchedule(savedExperiment);
  }

  public void testEsmJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_ESM);
    configureActivityForTesting(experiment);

    setActivityExperimentEsmSchedule();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentEsmSchedule(savedExperiment);
  }

  public void testWeekdayJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_WEEKDAY);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentRepeatRateAndTimes();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
  }

  public void testDailyJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_DAILY);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentRepeatRateAndTimes();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
  }

  public void testWeeklyJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_WEEKLY);
    configureActivityForTesting(experiment);

    int timesLength = setActivityExperimentWeeklySchedule();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentWeeklySchedule(timesLength, savedExperiment);
  }

  public void testMonthlyDayOfMonthJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    configureActivityForTesting(experiment);

    setActivityExperimentMonthlyDayOfMonthSchedule();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentMonthlyDayOfMonthSchedule(savedExperiment);
  }

  public void testMonthlyNthOfMonthJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_MONTHLY);
    configureActivityForTesting(experiment);

    setActivityExperimentMonthlyNthOfMonthSchedule();
    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
    checkSavedExperimentMonthlyNthOfMonthSchedule(savedExperiment);
  }

  public void testSelfReportJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_SELFREPORT);
    configureActivityForTesting(experiment);

    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
  }

  public void testTriggeredJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.ONGOING_TRIGGERED);
    configureActivityForTesting(experiment);

    simulateDownloadingAndSchedulingExperiment(experiment);

    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    checkExperimentProperlyJoined(savedExperiment);
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

  private Experiment getSavedExperiment(Experiment experiment) {
    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    return savedExperiment;
  }

  private void joinExperiment(Experiment experiment) {
    experiment.setJoinDate(new DateTime());
    experimentProviderUtil.insertFullJoinedExperiment(experiment);
  }

  private void configureActivityForTesting(Experiment experiment) {
    activity.setActivityProperties(experiment, experimentProviderUtil, true);
  }
  
  private void saveExperimentSchedule() {
    activity.scheduleExperiment();
  }

  private void simulateDownloadingAndSchedulingExperiment(Experiment experiment) {
    activity.saveDownloadedExperiment(experiment);
  }

  private void checkExperimentProperlyJoined(Experiment savedExperiment) {
    assertNotNull(savedExperiment);
    assertNotNull(savedExperiment.getJoinDate());
  }

  private void checkSavedExperimentEsmSchedule(Experiment savedExperiment) {
    assertEquals(savedExperiment.getSchedule().getEsmStartHour(), START_TIME);
    assertEquals(savedExperiment.getSchedule().getEsmEndHour(), END_TIME);
  }

  private void setActivityExperimentEsmSchedule() {
    activity.getExperiment().getSchedule().setEsmStartHour(START_TIME);
    activity.getExperiment().getSchedule().setEsmEndHour(END_TIME);
  }

  private void checkSavedExperimentWeekdaySchedule(int timesLength, Experiment savedExperiment) {
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
  }

  private int setActivityExperimentWeekdaySchedule() {
    return setActivityExperimentRepeatRateAndTimes();
  }

  private void checkSavedExperimentDailySchedule(int timesLength, Experiment savedExperiment) {
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
  }

  private int setActivityExperimentDailySchedule() {
    return setActivityExperimentRepeatRateAndTimes();
  }

  private void checkSavedExperimentWeeklySchedule(int timesLength, Experiment savedExperiment) {
    checkSavedExperimentRepeatRateAndTimes(timesLength, savedExperiment);
    assertTrue(activity.getExperiment().getSchedule().isWeekDayScheduled(SignalSchedule.WEDNESDAY));
    assertTrue(activity.getExperiment().getSchedule().isWeekDayScheduled(SignalSchedule.FRIDAY));
  }

  private int setActivityExperimentWeeklySchedule() {
    int timesLength = setActivityExperimentRepeatRateAndTimes();
    activity.getExperiment().getSchedule().addWeekDayToSchedule(SignalSchedule.WEDNESDAY);
    activity.getExperiment().getSchedule().addWeekDayToSchedule(SignalSchedule.FRIDAY);
    return timesLength;
  }

  private void checkSavedExperimentMonthlyDayOfMonthSchedule(Experiment savedExperiment) {
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getDayOfMonth(), DAY_OF_MONTH);
  }

  private void setActivityExperimentMonthlyDayOfMonthSchedule() {
    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setDayOfMonth(DAY_OF_MONTH);
  }

  private void checkSavedExperimentMonthlyNthOfMonthSchedule(Experiment savedExperiment) {
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    assertEquals(savedExperiment.getSchedule().getNthOfMonth(), DAY_OF_MONTH);
    assertEquals(savedExperiment.getSchedule().getWeekDaysScheduled(), DAY_OF_WEEK);
  }

  private void setActivityExperimentMonthlyNthOfMonthSchedule() {
    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    activity.getExperiment().getSchedule().setNthOfMonth(DAY_OF_MONTH);
    activity.getExperiment().getSchedule().setWeekDaysScheduled(DAY_OF_WEEK);
  }

  private void checkSavedExperimentRepeatRateAndTimes(int timesLength, Experiment savedExperiment) {
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    checkExperimentTimesList(timesLength, savedExperiment);
  }

  private int setActivityExperimentRepeatRateAndTimes() {
    activity.getExperiment().getSchedule().setRepeatRate(REPEAT_RATE);
    int timesLength = setExperimentTimes();
    return timesLength;
  }

  private int setExperimentTimes() {
    List<Long> times = activity.getExperiment().getSchedule().getTimes();
    int timesLength = times.size();
    for (int i = 0; i < timesLength; ++i) {
      times.set(i, ADAPTER_TIME);
    }
    return timesLength;
  }

  private void checkExperimentTimesList(int timesLength, Experiment savedExperiment) {
    List<Long> newTimes = savedExperiment.getSchedule().getTimes();
    assertEquals(newTimes.size(), timesLength);
    for (int j = 0; j < timesLength; ++j) {
      assertEquals(newTimes.get(j), ADAPTER_TIME);
    }
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
