/*
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.pacoapp.paco.R;

public class InformedConsentActivity extends Activity {

  public static final String INFORMED_CONSENT_PAGE_EXTRA_KEY = "InformedConsentPage";

  public static final int REFRESHING_JOINED_EXPERIMENT_DIALOG_ID = 1002;

  private Uri uri;
  private Experiment experiment;
  private boolean showingJoinedExperiments;

  private ExperimentProviderUtil experimentProviderUtil;
  private DownloadFullExperimentsTask experimentDownloadTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.informed_consent);
    final Intent intent = getIntent();
    uri = intent.getData();

    boolean myExperiments = intent.getExtras() != null ? intent.getExtras().getBoolean(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE) : false;

    if (uri != null) {
      showingJoinedExperiments = intent.getData().equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
      experimentProviderUtil = new ExperimentProviderUtil(this);
      if (showingJoinedExperiments) {
        experiment = experimentProviderUtil.getExperiment(uri);
      } else {
        experiment = experimentProviderUtil.getExperimentFromDisk(uri, myExperiments);
      }
      if (experiment == null) {
        Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
        finish();
      } else {
        // TextView title = (TextView)findViewById(R.id.experimentNameIc);
        // title.setText(experiment.getTitle());
        if (experiment.isLogActions() || experiment.declaresLogAppUsageAndBrowserCollection()) {
          TextView appBrowserUsageView = (TextView) findViewById(R.id.dataCollectedAppAndBrowserUsageView);
          appBrowserUsageView.setVisibility(TextView.VISIBLE);
        }
        if (experiment.isRecordPhoneDetails() || experiment.declaresPhoneDetailsCollection()) {
          TextView phoneDetailsView = (TextView) findViewById(R.id.dataCollectedPhoneDetailsView);
          phoneDetailsView.setVisibility(TextView.VISIBLE);
        }

        TextView ic = (TextView) findViewById(R.id.InformedConsentTextView);
        ic.setText(experiment.getInformedConsentForm());

        if (experiment.getJoinDate() == null) {
          final CheckBox icCheckbox = (CheckBox) findViewById(R.id.InformedConsentAgreementCheckBox);
          icCheckbox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              if (icCheckbox.isChecked()) {
                requestFullExperimentForJoining();
              }
            }
          });
        }
      }
    }
  }

  // Visible for testing
  public void setActivityProperties(Experiment experiment, ExperimentProviderUtil experimentProviderUtil) {
    this.experiment = experiment;
    this.experimentProviderUtil = experimentProviderUtil;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FindExperimentsActivity.JOIN_REQUEST_CODE) {
      if (resultCode == FindExperimentsActivity.JOINED_EXPERIMENT) {
        setResult(resultCode);
        finish();
      }
    }
  }

  private void requestFullExperimentForJoining() {
    if (!NetworkUtil.isConnected(this)) {
      showDialog(DownloadHelper.NO_NETWORK_CONNECTION, null);
    } else {
      DownloadFullExperimentsTaskListener listener = new DownloadFullExperimentsTaskListener() {

        @Override
        public void done(String resultCode) {
          dismissDialog(REFRESHING_JOINED_EXPERIMENT_DIALOG_ID);
          if (resultCode.equals(DownloadHelper.SUCCESS)) {
            List<Experiment> experimentList = getDownloadedExperimentsList();
            if (experimentList == null || experimentList.isEmpty()) {
              showFailureDialog(getString(R.string.could_not_load_full_experiment_));
            } else {
              saveDownloadedExperimentBeforeScheduling(experimentList.get(0));
            }
          } else {
            showFailureDialog(resultCode);
          }
        }
      };
      showDialog(REFRESHING_JOINED_EXPERIMENT_DIALOG_ID, null);

      List<Long> experimentServerIds = Arrays.asList(experiment.getServerId());
      experimentDownloadTask = new DownloadFullExperimentsTask(this, listener, new UserPreferences(this),
                                                               experimentServerIds);
      experimentDownloadTask.execute();
    }
  }

  private List<Experiment> getDownloadedExperimentsList() {
    String contentAsString = experimentDownloadTask.getContentAsString();
    List<Experiment> experimentList;
    try {
      experimentList = ExperimentProviderUtil.getExperimentsFromJson(contentAsString);
    } catch (JsonParseException e) {
      e.printStackTrace();
      showDialog(DownloadHelper.SERVER_ERROR, null);
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      showDialog(DownloadHelper.SERVER_ERROR, null);
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      showDialog(DownloadHelper.SERVER_ERROR, null);
      return null;
    }
    return experimentList;
  }

  // Visible for testing
  public void saveDownloadedExperimentBeforeScheduling(Experiment fullExperiment) {
    experiment = fullExperiment;
    joinExperiment();
    runScheduleActivity();
  }

  private void runScheduleActivity() {
    Intent intent = new Intent(InformedConsentActivity.this,
                               ExperimentScheduleActivity.class);
    intent.setAction(Intent.ACTION_EDIT);
    intent.setData(uri);
    intent.putExtra(INFORMED_CONSENT_PAGE_EXTRA_KEY, true);
    startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
  }

  private void joinExperiment() {
    experiment.setJoinDate(getTodayAsStringWithZone());
    // Set the uri to refer to the experiment's new saved location.
    uri = experimentProviderUtil.insertFullJoinedExperiment(experiment);
  }

    protected Dialog onCreateDialog(int id, Bundle args) {
    switch (id) {
    case REFRESHING_JOINED_EXPERIMENT_DIALOG_ID: {
      return getRefreshJoinedDialog();
    } case DownloadHelper.INVALID_DATA_ERROR: {
      return getUnableToJoinDialog(getString(R.string.invalid_data));
    } case DownloadHelper.SERVER_ERROR: {
      return getUnableToJoinDialog(getString(R.string.dialog_dismiss));
    } case DownloadHelper.NO_NETWORK_CONNECTION: {
      return getNoNetworkDialog();
    } default: {
      return null;
    }
    }
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return super.onCreateDialog(id);
  }

  private ProgressDialog getRefreshJoinedDialog() {
    return ProgressDialog.show(this, getString(R.string.experiment_retrieval),
                               getString(R.string.retrieving_your_joined_experiment_from_the_server),
                               true, true);
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(this);
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved)
    .setMessage(message)
    .setPositiveButton(R.string.dialog_dismiss, new DialogInterface.OnClickListener() {
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
    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), DownloadHelper.ENABLED_NETWORK);
  }

  private void showFailureDialog(String status) {
    if (status.equals(DownloadHelper.CONTENT_ERROR) ||
        status.equals(DownloadHelper.RETRIEVAL_ERROR)) {
      showDialog(DownloadHelper.INVALID_DATA_ERROR, null);
    } else {
      showDialog(DownloadHelper.SERVER_ERROR, null);
    }
  }

  private String getTodayAsStringWithZone() {
    return TimeUtil.formatDateWithZone(new DateTime());
  }

  // Visible for testing
  public Uri getExperimentUri() {
    return uri;
  }


}
