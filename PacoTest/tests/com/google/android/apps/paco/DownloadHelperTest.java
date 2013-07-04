package com.google.android.apps.paco;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Preconditions;

import android.app.Activity;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

public class DownloadHelperTest extends AndroidTestCase {

  private Context context;
  private UserPreferences userPrefs;
  private ExperimentProviderUtil experimentProviderUtil;
  private Random randomGenerator;
  private DownloadFullExperimentsTask testTask;


  @Override
  protected void setUp() {
    context = getContext();
    userPrefs = new UserPreferences(context);
    experimentProviderUtil = new ExperimentProviderUtil(context);
    randomGenerator = new Random();
  }

  public void testSingleExperimentRetrieval() throws InterruptedException, ExecutionException {
    Preconditions.checkArgument(NetworkUtil.isConnected(context));
    DownloadFullExperimentsTaskListener listener = new DownloadFullExperimentsTaskListener() {

      public void done(String resultCode) {
        System.out.println("errorCode: " + resultCode);   // PRIYA
        Preconditions.checkArgument(resultCode.equals(DownloadHelper.SUCCESS));      
        finishTestSingleExperimentRetrieval();
      }
    };
    testTask = new DownloadFullExperimentsTask(context, listener, userPrefs, 
                                               Arrays.asList(randomGenerator.nextLong()));
    testTask.execute();
    testTask.onPostExecute(testTask.get());
  }

  private void finishTestSingleExperimentRetrieval() {
    String jsonContent = testTask.getContentAsString();
    assertTrue(jsonContent != null && jsonContent.length() > 0);
    try {
      List<Experiment> experiments = ExperimentProviderUtil.getExperimentsFromJson(jsonContent);
      assertTrue(experiments.size() == 0 || experiments.size() == 1);
    } catch (JsonParseException e) {
      assertTrue(false);      // PRIYA
      e.printStackTrace();
    } catch (JsonMappingException e) {
      assertTrue(false);
      e.printStackTrace();
    } catch (IOException e) {
      assertTrue(false);
      e.printStackTrace();
    }
  }
  
  // PRIYA - TODO: add case for multi-experiment after refining this test.
  // PRIYA - make this generalized for DownloadHelper

}
