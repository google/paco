package com.google.android.apps.paco.test;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.test.AndroidTestCase;

import com.google.android.apps.paco.AndroidUtils;
import com.google.android.apps.paco.DownloadHelper;
import com.google.android.apps.paco.UserPreferences;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.corp.productivity.specialprojects.android.comm.Request;

public class DownloadHelperTest extends AndroidTestCase {

  private Context applicationContext;
  private DownloadHelper downloadHelper;
  private UserPreferences userPrefs;
  private Random randomGenerator;

  protected void setUp() {
    applicationContext = getContext().getApplicationContext();
    userPrefs = new UserPreferences(applicationContext);
    downloadHelper = new DownloadHelper(applicationContext, userPrefs, (Integer)null, (String)null);
    randomGenerator = new Random();
  }

  public void testShortExperimentRequest() throws Exception {
    downloadHelper.makeAvailableExperimentsRequest();
    Request request = downloadHelper.getRequest();
    assertEquals(request.getHeaderValue("http.useragent"), "Android");
    assertEquals(request.getHeaderValue("paco.version"), AndroidUtils.getAppVersion(applicationContext));
    assertEquals(request.getUrl().replaceFirst("https?://", ""),
                 userPrefs.getServerAddress() + "/experiments?short");
  }

  public void testFullSelectedExperimentRequest() throws Exception {
    int numExperiments = randomGenerator.nextInt(100);
    List<Long> experimentIds = Lists.newArrayList();
    for (int i=0; i < numExperiments; ++i) {
      experimentIds.add(randomGenerator.nextLong());
    }
    downloadHelper.makeRunningExperimentsRequest(experimentIds);
    Request request = downloadHelper.getRequest();
    assertEquals(request.getHeaderValue("http.useragent"), "Android");
    assertEquals(request.getHeaderValue("paco.version"), AndroidUtils.getAppVersion(applicationContext));
    assertEquals(request.getUrl().replaceFirst("https?://", ""),
                 userPrefs.getServerAddress() + "/experiments?id=" + formatExperimentIdList(experimentIds));
  }

  private static String formatExperimentIdList(List<Long> experimentIds) {
    String list = Joiner.on(",").join(experimentIds);
    return list;
  }

}
