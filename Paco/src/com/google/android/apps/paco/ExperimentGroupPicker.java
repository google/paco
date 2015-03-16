package com.google.android.apps.paco;

import java.util.List;

import org.joda.time.DateTime;

import android.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.scheduling.ActionScheduleGenerator;

public class ExperimentGroupPicker extends ListActivity implements ExperimentLoadingActivity {

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    if (experiment == null) {
      // TODO throw an error about no experiment
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    ExperimentDAO experimentDAO = experiment.getExperimentDAO();
    List<ExperimentGroup> groups = experimentDAO.getGroups();
    List<ExperimentGroup> choosableGroups = Lists.newArrayList();
    for (ExperimentGroup experimentGroup : groups) {
      if (!experimentGroup.getFixedDuration() || (!ActionScheduleGenerator.isOver(new DateTime(), experimentDAO))) {
        choosableGroups.add(experimentGroup);
      }
    }
    ArrayAdapter<ExperimentGroup> ls = new ArrayAdapter<ExperimentGroup>(this, R.layout.activity_list_item, choosableGroups);
    //ls.
    setListAdapter(ls);
  }

  @Override
  public void setExperiment(Experiment experimentByServerId) {
    this.experiment = experiment;

  }

  @Override
  public Experiment getExperiment() {
    return experiment;
  }

  @Override
  public void setExperimentGroup(ExperimentGroup groupByName) {
    // TODO Auto-generated method stub

  }



}
