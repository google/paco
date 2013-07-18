package com.google.android.apps.paco.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.RunningExperimentsActivity;
import com.pacoapp.paco.R;

import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RunningExperimentsActivityTest extends ActivityInstrumentationTestCase2<RunningExperimentsActivity> {

  private ExperimentProviderUtil experimentProviderUtil;

  private RunningExperimentsActivity activity;

  public RunningExperimentsActivityTest(String name) {
    super(RunningExperimentsActivity.class);
    setName(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setActivityInitialTouchMode(false);
    activity = getActivity();
    experimentProviderUtil = new ExperimentProviderUtil(activity);
  }

  // TODO: this test does not work, as the adapter does not reload.
  public void testExperimentRefreshAdapterReload() throws JsonParseException, JsonMappingException, IOException {

//    experimentProviderUtil.insertFullJoinedExperiment(ExperimentTestConstants.FIXED_ESM);
//
//    final ListView experimentListView = (ListView) activity.findViewById(R.id.find_experiments_list);
//    final int numExperiments = experimentListView.getAdapter().getCount();
//    
//    checkExperimentIsInList(experimentListView, ExperimentTestConstants.FIXED_ESM_TITLE);
//
//    List<String> modifiedExperimentJsons = Arrays.asList(ExperimentTestConstants.FIXED_ESM_RETITLED);
//    String contentAsString = ExperimentTestConstants.joinExperimentJsons(modifiedExperimentJsons);
//    activity.saveDownloadedExperiments(contentAsString);
//
//    // TODO: somehow reload the view and the adapter.
//    activity.runOnUiThread(new Runnable() {
//      public void run() {
//        CursorAdapter adapter = (CursorAdapter) experimentListView.getAdapter();
//        adapter.notifyDataSetChanged();
//      }
//    });
//
//    assertEquals(numExperiments, experimentListView.getAdapter().getCount());
//
//    Long fixedEsmId = checkExperimentIsInList(experimentListView, 
//                                             ExperimentTestConstants.FIXED_ESM_TITLE_MODIFIED);
//    activity.deleteExperiment(fixedEsmId);
  }
  

  private Long checkExperimentIsInList(final ListView experimentListView, String experimentTitle) {
    final int numExperiments = experimentListView.getAdapter().getCount();
    boolean experimentTitleExists = false;
    Long fixedEsmId = Long.valueOf(-1);
    for (int i = 0; i < numExperiments; ++i) {
      View experimentPanel = experimentListView.getAdapter().getView(i, null, null);
      TextView experimentTitleView = (TextView) experimentPanel.findViewById(R.id.experimentListRowTitle);
      if (experimentTitleView.getText().toString().equals(experimentTitle)) {
        experimentTitleExists = true;
        fixedEsmId = Long.parseLong((String) experimentTitleView.getTag());
        break;
      }
    }
    assertTrue(experimentTitleExists);
    return fixedEsmId;
  }

}
