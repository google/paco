package com.google.android.apps.paco.test;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentColumns;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.InformedConsentActivity;

public class InformedConsentActivityTest extends ActivityUnitTestCase<InformedConsentActivity> {

  private InformedConsentActivity activity;
  private MockExperimentProviderUtil experimentProviderUtil;
  private Context context;
  private Intent intent;
  private long experimentId = 0;

  public InformedConsentActivityTest() {
    super(InformedConsentActivity.class);
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

  public void testExperimentJoining() {
    Experiment experiment = getTestExperiment(ExperimentTestConstants.FIXED_ESM);
    configureActivityForTesting(experiment);

    simulateDownloadingAndSavingExperiment(experiment);

    checkExperimentUriProperlyUpdated();
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
  
  private void configureActivityForTesting(Experiment experiment) {
    activity.setActivityProperties(experiment, experimentProviderUtil);
  }

  private void simulateDownloadingAndSavingExperiment(Experiment experiment) {
    activity.saveDownloadedExperimentBeforeScheduling(experiment);
  }
  
  private void checkExperimentUriProperlyUpdated() {
    Uri uri = activity.getExperimentUri();
    assertTrue(uri.getPathSegments().get(0)
    .equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI.getPathSegments().get(0)));
  }

  private void checkExperimentProperlyJoined(Experiment savedExperiment) {
    assertNotNull(savedExperiment);
    assertNotNull(savedExperiment.getJoinDate());
  }

}
