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
package com.pacoapp.paco.ui;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.NetworkClient;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.sensors.android.AndroidInstalledApplications;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.SchedulePrinter;
import com.pacoapp.paco.shared.util.TimeUtil;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;
import com.pacoapp.paco.triggering.PacoExperimentActionBroadcaster;
import com.pacoapp.paco.utils.IntentExtraHelper;

public class InformedConsentActivity extends AppCompatActivity implements ExperimentLoadingActivity, NetworkClient {

  public static final String INFORMED_CONSENT_PAGE_EXTRA_KEY = "InformedConsentPage";

  public static final int REFRESHING_JOINED_EXPERIMENT_DIALOG_ID = 1002;

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private Button icButton;

  private ProgressBar progressBar;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.informed_consent);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    final Intent intent = getIntent();

    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, intent, experimentProviderUtil);

    if (experiment == null) {
      boolean myExperiments = intent.getExtras() != null
              ? intent.getExtras().getBoolean(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE)
              : false;

      Long experimentServerId = intent.getExtras().getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
      experiment = experimentProviderUtil.getExperimentFromDisk(experimentServerId, myExperiments);
    }
    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      // TextView title = (TextView)findViewById(R.id.experimentNameIc);
      // title.setText(experiment.getTitle());
      progressBar = (ProgressBar)findViewById(R.id.findExperimentsProgressBar);

      boolean logsActions = ExperimentHelper.isLogActions(experiment.getExperimentDAO());
      if (logsActions || ExperimentHelper.declaresLogAppUsageAndBrowserCollection(experiment.getExperimentDAO())) {
        TextView appBrowserUsageView = (TextView) findViewById(R.id.dataCollectedAppAndBrowserUsageView);
        appBrowserUsageView.setVisibility(TextView.VISIBLE);
      }
      if (experiment.getExperimentDAO().getRecordPhoneDetails()
          || experiment.getExperimentDAO().getRecordPhoneDetails()) {
        TextView phoneDetailsView = (TextView) findViewById(R.id.dataCollectedPhoneDetailsView);
        phoneDetailsView.setVisibility(TextView.VISIBLE);
      }
      if (ExperimentHelper.declaresInstalledAppDataCollection(experiment.getExperimentDAO())) {
        TextView appInstallLogView = (TextView) findViewById(R.id.dataCollectedInstalledAppsView);
        appInstallLogView.setVisibility(TextView.VISIBLE);
      }
      // Show the user if accessibility services are used by this experiment
      if (ExperimentHelper.declaresAccessibilityLogging(experiment.getExperimentDAO())) {
        TextView accessibilityView = (TextView) findViewById(R.id.dataCollectedAccessibilityView);
        accessibilityView.setVisibility(TextView.VISIBLE);
      }
      TextView ic = (TextView) findViewById(R.id.InformedConsentTextView);
      ic.setText(experiment.getExperimentDAO().getInformedConsentForm());

      if (experiment.getJoinDate() == null) {
        icButton = (Button) findViewById(R.id.InformedConsentAgreementButton);
        icButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            requestFullExperimentForJoining();
          }
        });
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
      setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
      finish();
    }
  }

  private void requestFullExperimentForJoining() {
    if (!NetworkUtil.isConnected(this)) {
      showDialog(NetworkUtil.NO_NETWORK_CONNECTION, null);
    } else {
      // WE are currently already downloading the full experiment.
            progressBar.setVisibility(View.VISIBLE);
//      final List<ExperimentGroup> groups = experiment.getGroups();
//      if (groups == null || groups.size() == 0) {
//        final String myExperimentsUrl = ExperimentUrlBuilder.buildUrlForFullExperiment(new UserPreferences(this),
//                                                                                   experiment.getServerId());
//        new PacoForegroundService(this, myExperimentsUrl).execute();
//      } else {
     try {
       saveDownloadedExperimentBeforeScheduling(experiment);
     } catch (IllegalStateException ise) {
       showDialog(NetworkUtil.JOIN_ERROR);
     }
//      }
    }
  }

  private List<Experiment> getDownloadedExperimentsList(String contentAsString) {
    List<Experiment> experimentList;
    try {
      experimentList = ExperimentProviderUtil.getExperimentsFromJson(contentAsString);
    } catch (JsonParseException e) {
      e.printStackTrace();
      showDialog(NetworkUtil.SERVER_ERROR, null);
      return null;
    } catch (JsonMappingException e) {
      e.printStackTrace();
      showDialog(NetworkUtil.SERVER_ERROR, null);
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      showDialog(NetworkUtil.SERVER_ERROR, null);
      return null;
    }
    return experimentList;
  }

  // Visible for testing
  public void saveDownloadedExperimentBeforeScheduling(Experiment fullExperiment) {
    experiment = fullExperiment;
    joinExperiment();
    createJoinEvent();
    PacoExperimentActionBroadcaster.sendJoinExperiment(getApplicationContext(),  experiment);
    startService(new Intent(this, SyncService.class));
    startService(new Intent(this, BeeperService.class));
    if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
      BroadcastTriggerReceiver.startProcessService(this);
    }
    startService(new Intent(this, ExperimentExpirationManagerService.class));
    if (ExperimentHelper.declaresInstalledAppDataCollection(experiment.getExperimentDAO())) {
      // Cache installed app names at the start of the experiment
      (new AndroidInstalledApplications(getContext())).cacheApplicationNames();
    }
    progressBar.setVisibility(View.GONE);
    runPostJoinInstructionsActivity();
  }

  private void runPostJoinInstructionsActivity() {
    Intent instructionsIntent = new Intent(this, PostJoinInstructionsActivity.class);
    instructionsIntent.putExtras(getIntent().getExtras());
    instructionsIntent.putExtra(INFORMED_CONSENT_PAGE_EXTRA_KEY, true);
    instructionsIntent.putExtra(ScheduleDetailFragment.USER_EDITABLE_SCHEDULE, ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO()));
    startActivityForResult(instructionsIntent, FindExperimentsActivity.JOIN_REQUEST_CODE);
  }

  private void createJoinEvent() {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentGroupName(null);
    event.setActionTriggerId(null);
    event.setActionTriggerSpecId(null);
    event.setActionId(null);
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    event.addResponse(createOutput("joined", "true"));

    event.addResponse(createOutput("schedule", SchedulePrinter.createStringOfAllSchedules(experiment.getExperimentDAO())));

    if (experiment.getExperimentDAO().getRecordPhoneDetails()) {
      Display defaultDisplay = getWindowManager().getDefaultDisplay();
      String size = Integer.toString(defaultDisplay.getHeight()) + "x" + Integer.toString(defaultDisplay.getWidth());
      event.addResponse(createOutput("display", size));

      event.addResponse(createOutput("make", Build.MANUFACTURER));
      event.addResponse(createOutput("model", Build.MODEL));
      event.addResponse(createOutput("android", Build.VERSION.RELEASE));
      TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
      String carrierName = manager.getNetworkOperatorName();
      event.addResponse(createOutput("carrier", carrierName));
    }

    experimentProviderUtil.insertEvent(event);
  }

  private Output createOutput(String key, String answer) {
    Output responseForInput = new Output();
    responseForInput.setAnswer(answer);
    responseForInput.setName(key);
    return responseForInput;
  }




  private void joinExperiment() {
    experiment.setJoinDate(getTodayAsStringWithZone());
    experimentProviderUtil.insertFullJoinedExperiment(experiment);
  }

  protected Dialog onCreateDialog(int id, Bundle args) {
    switch (id) {
    case REFRESHING_JOINED_EXPERIMENT_DIALOG_ID: {
      return getRefreshJoinedDialog();
    }
    case NetworkUtil.INVALID_DATA_ERROR: {
      return getUnableToJoinDialog(getString(R.string.invalid_data));
    }
    case NetworkUtil.SERVER_ERROR: {
      return getUnableToJoinDialog(getString(R.string.ok));
    }
    case NetworkUtil.NO_NETWORK_CONNECTION: {
      return getNoNetworkDialog();
    }
    case NetworkUtil.JOIN_ERROR: {
      return getUnableToJoinDialog(getString(R.string.unable_to_save_experiment));
    }

    default: {
      return null;
    }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        finish();
        return true;
      }
      return super.onOptionsItemSelected(item);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return super.onCreateDialog(id);
  }

  private ProgressDialog getRefreshJoinedDialog() {
    return ProgressDialog.show(this, getString(R.string.experiment_retrieval),
                               getString(R.string.retrieving_your_joined_experiment_from_the_server), true, true);
  }

  private AlertDialog getUnableToJoinDialog(String message) {
    AlertDialog.Builder unableToJoinBldr = new AlertDialog.Builder(this);
    unableToJoinBldr.setTitle(R.string.experiment_could_not_be_retrieved).setMessage(message)
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
    noNetworkBldr.setTitle(R.string.network_required).setMessage(getString(R.string.need_network_connection))
                 .setPositiveButton(R.string.go_to_network_settings, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                     showNetworkConnectionActivity();
                   }
                 }).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
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

  private void showFailureDialog(String status) {
    if (status.equals(NetworkUtil.CONTENT_ERROR) || status.equals(NetworkUtil.RETRIEVAL_ERROR)) {
      showDialog(NetworkUtil.INVALID_DATA_ERROR, null);
    } else if (status.equals(NetworkUtil.JOIN_ERROR)) {
      showDialog(NetworkUtil.JOIN_ERROR);
    } else {
      showDialog(NetworkUtil.SERVER_ERROR, null);
    }
  }

  private String getTodayAsStringWithZone() {
    return TimeUtil.formatDateWithZone(new DateTime());
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
    // no-op for this networkClient
  }


  @Override
  public Context getContext() {
    return this;
  }

  @Override
  public void show(final String msg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(InformedConsentActivity.this, msg, Toast.LENGTH_LONG);
      }
    });
  }

  @Override
  public void showAndFinish(final String msg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        //progressBar.setVisibility(View.GONE);
        if (msg != null) {
          List<Experiment> experimentList = getDownloadedExperimentsList(msg);
          if (experimentList == null || experimentList.isEmpty()) {
            showFailureDialog(getString(R.string.could_not_load_full_experiment_));
          } else {
            saveDownloadedExperimentBeforeScheduling(experimentList.get(0));
          }
        } else {
          showFailureDialog(getString(R.string.could_not_successfully_join_try_again_));
        }
      }
    });
  }

  @Override
  public void handleException(final Exception exception) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        //progressBar.setVisibility(View.GONE);
        Toast.makeText(InformedConsentActivity.this, "Exception: " + exception.getMessage(), Toast.LENGTH_LONG);
      }
    });
  }

}
