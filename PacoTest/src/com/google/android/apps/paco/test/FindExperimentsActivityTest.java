package com.google.android.apps.paco.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.FindExperimentsActivity;
import com.pacoapp.paco.R;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FindExperimentsActivityTest extends ActivityInstrumentationTestCase2<FindExperimentsActivity> {
  
  private ExperimentProviderUtil experimentProviderUtil;

  private FindExperimentsActivity activity;

  public FindExperimentsActivityTest(String name) {
    super(FindExperimentsActivity.class);
    setName(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setActivityInitialTouchMode(false);
    activity = getActivity();
    experimentProviderUtil = new ExperimentProviderUtil(activity);
  }
  
//TODO: this test does not work, as the adapter does not reload.
  public void testExperimentRefreshAdapterReload() throws JsonParseException, JsonMappingException, IOException {
    
//    List<Experiment> oldExperiments = experimentProviderUtil.loadExperimentsFromDisk();
//    Experiment insertingExperiment = ExperimentProviderUtil.getSingleExperimentFromJson(ExperimentTestConstants.FIXED_ESM);
//    oldExperiments.add(insertingExperiment);
//    List<String> experimentJsons = experimentProviderUtil.getJsonList(oldExperiments);
//    experimentProviderUtil.saveExperimentsToDisk(ExperimentTestConstants.joinExperimentJsons(experimentJsons));
//    
//    final ListView experimentListView = (ListView) activity.findViewById(R.id.find_experiments_list);
//    final int numExperiments = experimentListView.getAdapter().getCount();
//    
//    // TODO: somehow reload the view and the adapter.
//    activity.runOnUiThread(new Runnable() {
//      public void run() {
//        experimentListView.requestFocus();
//        activity.reloadAdapter();
//        BaseAdapter adapter = (BaseAdapter) experimentListView.getAdapter();
//        adapter.notifyDataSetChanged();
//      }
//    });
//    
//    checkExperimentIsInList(experimentListView, ExperimentTestConstants.FIXED_ESM_TITLE);
//    
//    List<String> modifiedExperimentJsons = Arrays.asList(ExperimentTestConstants.FIXED_ESM_RETITLED);
//    final String contentAsString = ExperimentTestConstants.joinExperimentJsons(modifiedExperimentJsons);
//    
//    // TODO: somehow reload the view and the adapter
//    activity.runOnUiThread(new Runnable() {
//      public void run() {
//        experimentListView.requestFocus();
//        activity.updateDownloadedExperiments(contentAsString);
//        BaseAdapter adapter = (BaseAdapter) experimentListView.getAdapter();
//        adapter.notifyDataSetChanged();
//      }
//    });
//    
//    assertEquals(numExperiments, experimentListView.getCount());
//  
//    checkExperimentIsInList(experimentListView, ExperimentTestConstants.FIXED_ESM_TITLE_MODIFIED);
//    
//    List<String> oldExperimentJsons = experimentProviderUtil.getJsonList(oldExperiments);
//    experimentProviderUtil.saveExperimentsToDisk(ExperimentTestConstants.joinExperimentJsons(oldExperimentJsons));
  }
  
  private void checkExperimentIsInList(final ListView experimentListView, String experimentTitle) {
    final int numExperiments = experimentListView.getAdapter().getCount();
    boolean experimentTitleExists = false;
    for (int i = 0; i < numExperiments; ++i) {
      View experimentPanel = experimentListView.getAdapter().getView(i, null, null);
      TextView experimentTitleView = (TextView) experimentPanel.findViewById(R.id.experimentListRowTitle);
      if (experimentTitleView.getText().toString().equals(experimentTitle)) {
        experimentTitleExists = true;
        break;
      }
    }
    assertTrue(experimentTitleExists);
  }

}
