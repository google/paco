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
    List<Long> times = activity.getExperiment().getSchedule().getTimes();
    int timesLength = times.size();
    for (int i=0; i < timesLength; ++i) {
      times.set(i, ADAPTER_TIME);
    }
    
    activity.save();
    
    Experiment savedExperiment = experimentProviderUtil.getExperiment(experiment.getId());
    assertNotNull(savedExperiment);
    assertEquals(savedExperiment.getSchedule().getRepeatRate(), REPEAT_RATE);
    List<Long> newTimes = savedExperiment.getSchedule().getTimes();
    assertEquals(newTimes.size(), timesLength);
    for (int j=0; j < timesLength; ++j) {
      assertEquals(newTimes.get(j), ADAPTER_TIME);
    }
    
    activity.finish();
  }
//  
//  public void testDailyScheduling() {
//    
//  }  
//  
//  public void testWeeklyScheduling() {
//    
//  }
//  
//  public void testMonthlyScheduling() {
//    
//  }
//  
//  public void testSelfReportScheduling() {
//    
//  }
//  
//  public void testTriggeredScheduling() {
//    
//  }
//  
//  public void testEsmJoining() {
//    
//  }
//  
//  public void testWeekdayJoining() {
//    
//  }
//  
//  public void testDailyJoining() {
//    
//  }  
//  
//  public void testWeeklyJoining() {
//    
//  }
//  
//  public void testMonthlyJoining() {
//    
//  }
//  
//  public void testSelfReportJoining() {
//    
//  }
//  
//  public void testTriggeredJoining() {
//    
//  }
  
  private Experiment getTestExperiment(String experimentTitle) {
    Experiment experiment = getExperimentFromJson(experimentTitle);
    addFillerExperimentInfo(experiment);
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
  
  private void addFillerExperimentInfo(Experiment experiment) {
    experiment.setId(experimentId++);
    experiment.setIcon(new byte[1]);
    experiment.getSchedule().setId(Long.valueOf(0));
    experiment.getSchedule().setExperimentId(Long.valueOf(0));
    int length = experiment.getInputs().size();
    for (int i=0; i<length; ++i) {
      experiment.getInputs().get(i).setId(Long.valueOf(0));
      experiment.getInputs().get(i).setExperimentId(Long.valueOf(0));
      experiment.getInputs().get(i).setScheduleDateFromLong(Long.valueOf(0));
      experiment.getInputs().get(i).setMultiselect(false);
    }
    int length2 = experiment.getFeedback().size();
    for (int j=0; j<length2; ++j) {
      experiment.getFeedback().get(j).setId(Long.valueOf(0));
      experiment.getFeedback().get(j).setExperimentId(Long.valueOf(0));
    }
  }
  
  private void joinExperiment(Experiment experiment) {
    experiment.setJoinDate(new DateTime());
    experimentProviderUtil.insertFullJoinedExperiment(experiment);
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
