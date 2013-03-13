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

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ExperimentDetailActivity extends Activity {
  
  private static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;


  static final String EXPERIMENT_NAME = "com.google.android.apps.paco.Experiment";
  static DateTimeFormatter df = DateTimeFormat.shortDate();
  private Uri uri;
  private Button joinButton;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private UserPreferences userPrefs;
  private ProgressDialog p;
  private boolean showingJoinedExperiments;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.experiment_detail);
    final Intent intent = getIntent();
    uri = intent.getData();
    showingJoinedExperiments = intent.getData().equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
    userPrefs = new UserPreferences(this);
    
    experimentProviderUtil = new ExperimentProviderUtil(this);
    
    
    if (uri.getLastPathSegment().startsWith("0000")) {
      String realServerId = uri.getLastPathSegment().substring(4);
      List<Experiment> experiments = experimentProviderUtil.getExperimentsByServerId(new Long(realServerId));
      if (experiments != null && experiments.size() > 0) {
        experiment = experiments.get(0);
        uri= Uri.withAppendedPath(ExperimentColumns.CONTENT_URI, Long.toString(experiment.getId()));
      }
    } else {
      if (showingJoinedExperiments) {
        experiment = experimentProviderUtil.getExperiment(uri);
      } else {
        experiment = experimentProviderUtil.getExperimentFromDisk(uri);
        uri= Uri.withAppendedPath(ExperimentColumns.CONTENT_URI, Long.toString(experiment.getServerId()));
      }
    }
    
    if (experiment == null) {
      new AlertDialog.Builder(this).setMessage(R.string.selected_experiment_not_on_phone_refresh).setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
        
        public void onClick(DialogInterface dialog, int which) {
          refreshList();          
        }
      }).show();
      
    } else {
      showExperiment();
    }
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
    if (isJoinedExperiment()) {
      joinButton.setVisibility(View.GONE);
    }

    ((TextView)findViewById(R.id.experiment_name)).setText(experiment.getTitle());
    ((TextView)findViewById(R.id.description)).setText(experiment.getDescription());
    ((TextView)findViewById(R.id.creator)).setText(experiment.getCreator());
    //    Schedule scheduleDetails = experiment.getScheduleDetails();
    ((TextView)findViewById(R.id.schedule)).setText(SignalSchedule.SCHEDULE_TYPES_NAMES[experiment.getSchedule().getScheduleType()]);
    String startDate = getString(R.string.ongoing_duration);
    String endDate = getString(R.string.ongoing_duration);
    if (experiment.isFixedDuration()) {
      startDate = df.print(experiment.getStartDate());
      endDate = df.print(experiment.getEndDate());
      ((TextView)findViewById(R.id.startDate)).setText(startDate);
      ((TextView)findViewById(R.id.endDate)).setText(endDate);
    } else {
      findViewById(R.id.startDatePanel).setVisibility(View.GONE);
      findViewById(R.id.endDatePanel).setVisibility(View.GONE);
    }
    ((TextView)findViewById(R.id.startDate)).setText(startDate);
    ((TextView)findViewById(R.id.endDate)).setText(endDate);

    String esm_frequency = experiment.getSchedule().getEsmFrequency() != null 
      ? experiment.getSchedule().getEsmFrequency().toString() 
      : null;
    if (experiment.getSchedule().getScheduleType() == SignalSchedule.ESM && esm_frequency != null && esm_frequency.length() > 0) {
      findViewById(R.id.esmPanel).setVisibility(View.VISIBLE); 
      ((TextView)findViewById(R.id.esm_frequency)).setText(esm_frequency+ "/" + getString(SignalSchedule.ESM_PERIODS_NAMES[experiment.getSchedule().getEsmPeriodInDays()]));
    }
    // TODO (bobevans): Update to show all the new shceduling types in a succinct readonly way
    if (isJoinedExperiment()) {
      findViewById(R.id.timePanel).setVisibility(View.VISIBLE); 
      ((TextView)findViewById(R.id.time)).setText(toCommaSeparatedString(experiment.getSchedule().getTimes()));
    }
    if (!isJoinedExperiment()) {
      joinButton.setOnClickListener(new OnClickListener() {    	 
        public void onClick(View v) {
          Intent intent = new Intent(ExperimentDetailActivity.this, InformedConsentActivity.class);
          intent.setAction(Intent.ACTION_EDIT);
          intent.setData(uri);
          startActivityForResult(intent, FindExperimentsActivity.JOIN_REQUEST_CODE);
        }
      });
    }
  }

  
  private String toCommaSeparatedString(List<Long> times) {
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

  
  protected void refreshList() {
    DownloadExperimentsTaskListener listener = new DownloadExperimentsTaskListener() {

      @Override
      public void done() {          
          experiment = experimentProviderUtil.getExperimentFromDisk(new Long(uri.getLastPathSegment().substring(4)));
          dismissDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
          if (experiment != null) {
            uri= Uri.withAppendedPath(ExperimentColumns.CONTENT_URI, Long.toString(experiment.getServerId()));
            showExperiment();
          } else {
            ExperimentDetailActivity.this.finish();
          }
          
      }
    };
    showDialog(REFRESHING_EXPERIMENTS_DIALOG_ID);
    new DownloadExperimentsTask(this, listener, userPrefs, experimentProviderUtil, null).execute();

  }
  

    

  
}

