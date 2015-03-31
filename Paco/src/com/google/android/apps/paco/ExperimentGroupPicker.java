package com.google.android.apps.paco;

import java.util.List;

import org.joda.time.DateTime;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.R;
import com.pacoapp.paco.ui.ScheduleListActivity;

public class ExperimentGroupPicker extends ListActivity implements ExperimentLoadingActivity {

  public static final String SHOULD_GO_TO_RENDER_NEXT = "should_render_next";
  public static final int FEEDBACK_NEXT = 1;
  public static final int RENDER_NEXT = 2;
  public static final int SCHEDULE_NEXT = 3;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private List<ExperimentGroup> experimentGroups;
  private List<ExperimentGroup> choosableGroups;
  private List<String> choosableGroupNames;
  private int shouldRender;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    shouldRender = getIntent().getIntExtra(SHOULD_GO_TO_RENDER_NEXT, RENDER_NEXT);
    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      experimentGroups = experiment.getExperimentDAO().getGroups();
      choosableGroupNames = Lists.newArrayList();

      ExperimentDAO experimentDAO = experiment.getExperimentDAO();
      List<ExperimentGroup> groups = experimentDAO.getGroups();
      choosableGroups = Lists.newArrayList();
      for (ExperimentGroup experimentGroup : groups) {
        if (!experimentGroup.getFixedDuration() || (!ActionScheduleGenerator.isOver(new DateTime(), experimentDAO))) {
          choosableGroups.add(experimentGroup);
        }
      }

      for (ExperimentGroup eg : choosableGroups) {
        choosableGroupNames.add(eg.getName());
      }
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                                                              choosableGroupNames);
      setListAdapter(adapter);
    }
  }



  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void setExperiment(Experiment experimentByServerId) {
    this.experiment = experimentByServerId;

  }

  @Override
  public Experiment getExperiment() {
    return experiment;
  }

  @Override
  public void setExperimentGroup(ExperimentGroup groupByName) {
    // no-op for this activity
  }



  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ExperimentGroup chosenGroup = choosableGroups.get(position);
    Class clazz = null;
    switch (shouldRender) {
    case RENDER_NEXT:
      if (chosenGroup.getCustomRendering()) {
        clazz = ExperimentExecutorCustomRendering.class;
      } else {
        clazz = ExperimentExecutor.class;
      }
      break;
    case FEEDBACK_NEXT:
      clazz = FeedbackActivity.class;
      break;
    case SCHEDULE_NEXT:
      clazz = ScheduleListActivity.class;
      break;
    }
    Intent experimentIntent = new Intent(this, clazz);
    experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    experimentIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, chosenGroup.getName());
    startActivity(experimentIntent);
    finish();

  }



}
