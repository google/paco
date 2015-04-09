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

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.R;

public class ExperimentDetailActivity extends ActionBarActivity implements ExperimentLoadingActivity {

  public static final String ID_FROM_MY_EXPERIMENTS_FILE = "my_experimentsFile";


  private static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;


  static final String EXPERIMENT_NAME = "com.google.android.apps.paco.Experiment";
  static DateTimeFormatter df = DateTimeFormat.shortDate();
  private Button joinButton;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private UserPreferences userPrefs;
  private ProgressDialog p;
  private boolean useMyExperimentsDiskFile;


  private DownloadFullExperimentsTask experimentDownloadTask;


  private Uri uri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.experiment_detail);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));


    final Intent intent = getIntent();
    useMyExperimentsDiskFile = intent.getExtras() != null ? intent.getExtras().getBoolean(ID_FROM_MY_EXPERIMENTS_FILE) : false;
    userPrefs = new UserPreferences(this);
    experimentProviderUtil = new ExperimentProviderUtil(this);
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


  private boolean isLaunchedFromQRCode() {
    return uri != null && uri.getLastPathSegment().startsWith("0000");
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    if (id == REFRESHING_EXPERIMENTS_DIALOG_ID) {
      ProgressDialog loadingDialog = ProgressDialog.show(this, getString(R.string.experiment_refresh),
                                                         getString(R.string.checking_server_for_new_and_updated_experiment_definitions),
                                                         true, true);
      return loadingDialog;
    }
    return super.onCreateDialog(id);
  }


  private void showExperiment() {
    joinButton = (Button)findViewById(R.id.JoinExperimentButton);
    if (isJoinedExperiment() || isAlreadyJoined()) {
      joinButton.setVisibility(View.GONE);
    }

    final ExperimentDAO experimentDAO = experiment.getExperimentDAO();
    ((TextView)findViewById(R.id.experiment_name)).setText(experimentDAO.getTitle());
    ((TextView)findViewById(R.id.description)).setText(experimentDAO.getDescription());
    ((TextView)findViewById(R.id.creator)).setText(experimentDAO.getCreator());

//    SignalSchedule schedule = experiment.getSchedule();
//    Trigger trigger = experiment.getTrigger();
//    if (schedule != null && trigger == null) {
//      Integer scheduleType = schedule.getScheduleType();
//      int scheduleName = SignalSchedule.SCHEDULE_TYPES_NAMES[scheduleType];
//      ((TextView) findViewById(R.id.schedule)).setText(scheduleName);
//    } else if (trigger != null) {
//      String triggerDetails = Trigger.getNameForCode(trigger.getEventCode());
//      ((TextView) findViewById(R.id.schedule)).setText(triggerDetails);
//    }

    // Hide the schedule panel for now (a short experiment load comes with no schedule info).
    findViewById(R.id.scheduleDisplayPanel).setVisibility(View.GONE);

    String startDate = getString(R.string.ongoing_duration);
    String endDate = getString(R.string.ongoing_duration);
//    if (ActionScheduleGenerator.areAllGroupsFixedDuration(experimentDAO)) {
//      startDate = TimeUtil.formatDateTime(ActionScheduleGenerator.getEarliestStartDate(experimentDAO).toDateTime());
//      endDate = TimeUtil.formatDateTime(ActionScheduleGenerator.getLastEndTime(experimentDAO).toDateMidnight().toDateTime());
//      ((TextView)findViewById(R.id.startDate)).setText(startDate);
//      ((TextView)findViewById(R.id.endDate)).setText(endDate);
//    } else {
      findViewById(R.id.startDatePanel).setVisibility(View.GONE);
      findViewById(R.id.endDatePanel).setVisibility(View.GONE);
//    }
    ((TextView)findViewById(R.id.startDate)).setText(startDate);
    ((TextView)findViewById(R.id.endDate)).setText(endDate);

//    String esm_frequency = schedule != null && schedule.getEsmFrequency() != null
//      ? schedule.getEsmFrequency().toString()
//      : null;
//    if (schedule != null && schedule.getScheduleType() == SignalSchedule.ESM && esm_frequency != null && esm_frequency.length() > 0) {
//      findViewById(R.id.esmPanel).setVisibility(View.VISIBLE);
//      ((TextView)findViewById(R.id.esm_frequency)).setText(esm_frequency+ "/" + getString(SignalSchedule.ESM_PERIODS_NAMES[schedule.getEsmPeriodInDays()]));
//    }
//    // TODO (bobevans): Update to show all the new shceduling types in a succinct readonly way
//    if (isJoinedExperiment()) {
//      findViewById(R.id.timePanel).setVisibility(View.VISIBLE);
//      ((TextView)findViewById(R.id.time)).setText(toCommaSeparatedString(schedule != null ? schedule.getTimes() : null));
//    }

    if (!isJoinedExperiment()) {
      joinButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          showInformedConsentActivity();
        }


        private void showInformedConsentActivity() {
          Intent intent = new Intent(ExperimentDetailActivity.this, InformedConsentActivity.class);
//          intent.setAction(Intent.ACTION_EDIT);
          intent.putExtras(getIntent().getExtras());
          intent.putExtra(ExperimentDetailActivity.ID_FROM_MY_EXPERIMENTS_FILE, useMyExperimentsDiskFile);
          startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
        }
      });
    }
  }

  public boolean isAlreadyJoined() {
    List<Experiment> potentialJoinedExperiment = experimentProviderUtil.getExperimentsByServerId(experiment.getServerId());
    boolean alreadyJoined = false;
    if (!potentialJoinedExperiment.isEmpty()) {
      for (Experiment experiment : potentialJoinedExperiment) {
        if (experiment.getJoinDate() != null) {
          alreadyJoined = true;
          break;
        }
      }
    }
    return alreadyJoined;
  }

  private String toCommaSeparatedString(List<Long> times) {
    if (times == null) {
      return "";
    }
    DateTimeFormatter df2 = org.joda.time.format.DateTimeFormat.shortTime();
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Long long1 : times) {
      if (!first) {
        buf.append(",");
      }
      buf.append(new DateTime(long1).toString(df2));
    }
    String bufAsString = buf.toString();
    return bufAsString.substring(0, Math.min(bufAsString.length(), 40));
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


  private boolean isJoinedExperiment() {
    return experiment.getJoinDate() != null;
  }


  protected void refreshList(final String realServerId) {
    DownloadFullExperimentsTaskListener listener = new DownloadFullExperimentsTaskListener() {

      @Override
      public void done(String resultCode) {

        if (resultCode.equals(DownloadHelper.SUCCESS)) {
          experimentProviderUtil.addExperimentToExperimentsOnDisk(experimentDownloadTask.getContentAsString());
          List<Experiment> experiments = experimentProviderUtil.loadExperimentsFromDisk(false);
          Long serverId = Long.parseLong(realServerId);
          for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
            Experiment currentExperiment = (Experiment) iterator.next();
            if (currentExperiment.getServerId().equals(serverId)) {
              experiment = currentExperiment;
            }
          }
          dismissDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
        } else {
          dismissDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
          showFailureDialog(resultCode);
        }

        if (experiment != null) {
          showExperiment();
        } else {
          ExperimentDetailActivity.this.finish();
        }

      }
    };
    showDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
    List<Long> experimentServerIds = Lists.newArrayList(Long.parseLong(realServerId));
    experimentDownloadTask = new DownloadFullExperimentsTask(this, listener, userPrefs, experimentServerIds);
    experimentDownloadTask.execute((Void)null);
  }


  private void showFailureDialog(String status) {
    if (status.equals(DownloadHelper.CONTENT_ERROR) ||
        status.equals(DownloadHelper.RETRIEVAL_ERROR)) {
      showDialog(DownloadHelper.INVALID_DATA_ERROR, null);
    } else {
      showDialog(DownloadHelper.SERVER_ERROR, null);
    }
  }


  @Override
  protected void onResume() {
    if (isLaunchedFromQRCode() && userPrefs.getSelectedAccount() == null) {
      Intent acctChooser = new Intent(this, AccountChooser.class);
      this.startActivity(acctChooser);
    } else {
      if (!isLaunchedFromQRCode()) {
        IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
        Long experimentId = getIntent().getExtras().getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
        experiment = experimentProviderUtil.getExperimentFromDisk(experimentId, useMyExperimentsDiskFile);
      }

      if (isLaunchedFromQRCode() && experiment == null) {
        new AlertDialog.Builder(this).setMessage(R.string.selected_experiment_not_on_phone_refresh).setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            String realServerId = uri.getLastPathSegment().substring(4);
            refreshList(realServerId);
          }
        }).show();

      } else {
        showExperiment();
      }
    }

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

}

