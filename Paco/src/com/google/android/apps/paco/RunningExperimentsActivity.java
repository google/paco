/*
* Copyright 2011 Google Inc. All Rights Reserved.
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.ui.ExperimentExecutor;
import com.pacoapp.paco.ui.ExperimentExecutorCustomRendering;
import com.pacoapp.paco.ui.ExperimentGroupPicker;
import com.pacoapp.paco.ui.FindExperimentsActivity;

import static com.pacoapp.paco.ui.ExperimentGroupPicker.getOnlySurveyGroups;


/**
 *
 */
public class RunningExperimentsActivity extends Activity {


  private static final int DATA_EXPERIMENT_OPTION = 3;
  private static final int STOP_EXPERIMENT_OPTION = 2;
  private static final int EDIT_EXPERIMENT_OPTION = 1;
  static final int JOIN_REQUEST_CODE = 1;
  static final int JOINED_EXPERIMENT = 1;

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;

  private List<Experiment> experiments = Lists.newArrayList();
  protected AvailableExperimentsListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.find_experiments, null);
    setContentView(mainLayout);

    userPrefs = new UserPreferences(this);
    list = (ListView) findViewById(R.id.find_experiments_list);
    list.setBackgroundColor(333);

    experimentProviderUtil = new ExperimentProviderUtil(this);
    reloadAdapter();
  }

  protected Dialog onCreateDialog(int id, Bundle args) {
    switch (id) {
      case NetworkUtil.INVALID_DATA_ERROR: {
          return getUnableToJoinDialog(getString(R.string.invalid_data));
      } case NetworkUtil.SERVER_ERROR: {
        return getUnableToJoinDialog(getString(R.string.ok));
      } case NetworkUtil.NO_NETWORK_CONNECTION: {
        return getNoNetworkDialog();
      } default: {
        return null;
      }
    }
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(this);
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                           setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                           finish();
                         }
                       });
    return unableToJoinBldr.create();
  }

  private AlertDialog getNoNetworkDialog() {
    AlertDialog.Builder noNetworkBldr = new AlertDialog.Builder(this);
    noNetworkBldr.setTitle(R.string.network_required)
                 .setMessage(getString(R.string.need_network_connection))
                 .setPositiveButton(R.string.go_to_network_settings, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                           showNetworkConnectionActivity();
                         }
                       })
                 .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                            setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
                            finish();
                          }
                    });
    return noNetworkBldr.create();
  }

  private void showNetworkConnectionActivity() {
    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NetworkUtil.ENABLED_NETWORK);
  }


  public void reloadAdapter() {
    adapter = new AvailableExperimentsListAdapter(this,
                                                  R.id.find_experiments_list,
                                                  experiments);
    list.setAdapter(adapter);
  }



  private class AvailableExperimentsListAdapter extends ArrayAdapter<Experiment> {

    private LayoutInflater mInflater;

    AvailableExperimentsListAdapter(Context context, int resourceId, List<Experiment> experiments) {
      super(context, resourceId, experiments);
      mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        view = mInflater.inflate(R.layout.experiment_list_row, null);
      }

      Experiment experiment = getItem(position);

      TextView tv = (TextView) view.findViewById(R.id.experimentListRowTitle);
      tv.setText(experiment != null ? experiment.getExperimentDAO().getTitle() : "ERROR");
      tv.setOnClickListener(myButtonListener);

      tv.setTag(experiment.getExperimentDAO().getId());

//      ImageButton editButton = (ImageButton) view.findViewById(R.id.editExperimentButton);
//      editButton.setOnClickListener(myButtonListener);
//      editButton.setTag(experiment.getExperimentDAO().getId());
//
//      editButton.setEnabled(ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO()));
//
//      ImageButton quitButton = (ImageButton) view.findViewById(R.id.quitExperimentButton);
//      quitButton.setOnClickListener(myButtonListener);
//      quitButton.setTag(experiment.getExperimentDAO().getId());
//
//      ImageButton exploreButton = (ImageButton) view.findViewById(R.id.exploreDataExperimentButton);
//      exploreButton.setOnClickListener(myButtonListener);
//      exploreButton.setTag(experiment.getExperimentDAO().getId());
      // show icon
      // ImageView iv = (ImageView) view.findViewById(R.id.explore_data_icon);
      // iv.setImageResource();
      return view;
    }

    private OnClickListener myButtonListener = new OnClickListener() {
      @Override
      public void onClick(final View v) {
        final int position = list.getPositionForView(v);
        if (position == ListView.INVALID_POSITION) {
          return;
        } else {
          final Long experimentServerId = (Long) v.getTag();
          final Experiment experiment = experiments.get(position);
          final List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
          List<ExperimentGroup> surveyGroups = ExperimentGroupPicker.getOnlySurveyGroups(groups);
/*          if (v.getId() == R.id.editExperimentButton) {
            editExperiment(experiment, groups);
          } else if (v.getId() == R.id.exploreDataExperimentButton) {
            showDataForExperiment(experiment, groups);
          } else if (v.getId() == R.id.quitExperimentButton) {
            new AlertDialog.Builder(RunningExperimentsActivity.this).setCancelable(true)
              .setTitle(R.string.stop_the_experiment_dialog_title)
              .setMessage(R.string.stop_experiment_dialog_body)
              .setPositiveButton(R.string.yes,
                                 new Dialog.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog,
                                                       int which) {
                                     deleteExperiment(experimentServerId);
                                   }
                                 })
              .setNegativeButton(R.string.no,
                                 new Dialog.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog,
                                                       int which) {
                                     dialog.dismiss();
                                   }
                                 }).create().show();

          } else*/ if (v.getId() == R.id.experimentListRowTitle) {
            Intent experimentIntent = null;
            // TODO grouptype must be survey and more than one
            if (surveyGroups.size() > 1) {
              experimentIntent = new Intent(RunningExperimentsActivity.this, ExperimentGroupPicker.class);
              experimentIntent.putExtra(ExperimentGroupPicker.SHOULD_GO_TO_RENDER_NEXT, ExperimentGroupPicker.RENDER_NEXT);
            } else if (surveyGroups.size() == 1) {
              Class clazz = null;
              final ExperimentGroup experimentGroup = surveyGroups.get(0);
              if (experimentGroup.getCustomRendering()) {
                clazz = ExperimentExecutorCustomRendering.class;
              } else {
                clazz = ExperimentExecutor.class;
              }
              experimentIntent = new Intent(RunningExperimentsActivity.this, clazz);
              experimentIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, experimentGroup.getName());

            }
            experimentIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experimentServerId);
            startActivity(experimentIntent);
            finish();
          }
        }
      }
    };
  }

}
