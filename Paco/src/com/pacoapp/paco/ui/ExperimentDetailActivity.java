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

import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.pacoapp.paco.R;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.net.ExperimentUrlBuilder;
import com.pacoapp.paco.net.NetworkClient;
import com.pacoapp.paco.net.NetworkUtil;
import com.pacoapp.paco.net.PacoForegroundService;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.utils.IntentExtraHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExperimentDetailActivity extends AppCompatActivity implements ExperimentLoadingActivity, NetworkClient {

  public static final String ID_FROM_MY_EXPERIMENTS_FILE = "my_experimentsFile";

  private static Logger Log = LoggerFactory.getLogger(ExperimentDetailActivity.class);

  private static final int REFRESHING_EXPERIMENTS_DIALOG_ID = 1001;


  static final String EXPERIMENT_NAME = "com.google.android.apps.paco.Experiment";
  static DateTimeFormatter df = DateTimeFormat.shortDate();
  private Button joinButton;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private UserPreferences userPrefs;
  private ProgressDialog p;
  private boolean useMyExperimentsDiskFile;


  private Uri uri;
  protected Long receivedExperimentId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.debug("ExperimentDetailActivity onCreate");
    setContentView(R.layout.experiment_detail);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));


    final Intent intent = getIntent();
    this.useMyExperimentsDiskFile = intent.getExtras() != null ? intent.getExtras().getBoolean(ID_FROM_MY_EXPERIMENTS_FILE) : false;
    this.userPrefs = new UserPreferences(this);
    this.experimentProviderUtil = new ExperimentProviderUtil(this);
    this.uri = intent.getData();
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


  private boolean isLaunchedFromLink(){
	  return uri != null && uri.getScheme().equalsIgnoreCase("pacoapp");
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

    TextView organizationView = (TextView)findViewById(R.id.organization);
    String organization = experimentDAO.getOrganization();
    if (!Strings.isNullOrEmpty(organization) && !organization.equals("null")) {
      organizationView.setText(organization);
    } else {
      organizationView.setVisibility(View.GONE);
    }
    TextView verifiedView = (TextView)findViewById(R.id.verified);
    verifiedView.setVisibility(View.GONE);

    TextView contactView = (TextView)findViewById(R.id.contact);
    String contact = experimentDAO.getContactEmail();
    if (Strings.isNullOrEmpty(contact) || contact.equals("null")) {
      contact = experimentDAO.getCreator();
    }
    contactView.setText(contact);
//    TextView creatorView = (TextView)findViewById(R.id.creator);
//    String creatorEmail = experimentDAO.getCreator();
//    creatorView.setText(creatorEmail);



//    String startDate = getString(R.string.ongoing_duration);
//    String endDate = getString(R.string.ongoing_duration);
//    ((TextView)findViewById(R.id.startDate)).setText(startDate);
//    ((TextView)findViewById(R.id.endDate)).setText(endDate);

//    if (ActionScheduleGenerator.areAllGroupsFixedDuration(experimentDAO)) {
//      startDate = TimeUtil.formatDateTime(ActionScheduleGenerator.getEarliestStartDate(experimentDAO).toDateTime());
//      endDate = TimeUtil.formatDateTime(ActionScheduleGenerator.getLastEndTime(experimentDAO).toDateMidnight().toDateTime());
//      ((TextView)findViewById(R.id.startDate)).setText(startDate);
//      ((TextView)findViewById(R.id.endDate)).setText(endDate);
//    } else {
//      findViewById(R.id.durationPanel).setVisibility(View.GONE);
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
          Log.debug("Already joined experiment");
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
    return experiment != null && experiment.getJoinDate() != null;
  }


  protected void retrieveExperimentFromServer(final String realServerId) {
    if (!NetworkUtil.isConnected(this)) {
      Log.debug("Cannot retrieve experiment from server. No Network connection");
      showDialog(NetworkUtil.NO_NETWORK_CONNECTION, null);
    } else {
      //      progressBar.setVisibility(View.VISIBLE);
      receivedExperimentId = Long.parseLong(realServerId);
      final String myExperimentsUrl = ExperimentUrlBuilder.buildUrlForFullExperiment(new UserPreferences(this),
                                                                                   receivedExperimentId);
      new PacoForegroundService(this, myExperimentsUrl).execute();
    }
  }


  private void showFailureDialog(String status) {
    if (status.equals(NetworkUtil.CONTENT_ERROR) ||
        status.equals(NetworkUtil.RETRIEVAL_ERROR)) {
      showDialog(NetworkUtil.INVALID_DATA_ERROR, null);
    } else {
      showDialog(NetworkUtil.SERVER_ERROR, null);
    }
  }


  @Override
  protected void onResume() {
    Log.debug("ExperimentDetailActivity onResume");
    if ((isLaunchedFromLink() || isLaunchedFromQRCode()) && userPrefs.getAccessToken() == null) {
      Log.debug("redirect to Login");
      Intent splash = new Intent(this, SplashActivity.class);
      this.startActivity(splash);
    } else {
      if (!isLaunchedFromQRCode() && !isLaunchedFromLink()) {
        IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
        Long experimentId = getIntent().getExtras().getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
        experiment = experimentProviderUtil.getExperimentFromDisk(experimentId, useMyExperimentsDiskFile);
      }

      if (isLaunchedFromLink()) {
        // Get experiment id from link/uri
        // example: pacoapp://experiment/1234567
        String[] uriSegments = this.uri.getSchemeSpecificPart().toString().replace("//", "").split("/");
        if (uriSegments.length == 2 && uriSegments[0].equalsIgnoreCase("experiment")) {
          // Got the right URI format, check whether to download experiment
          this.receivedExperimentId = Long.parseLong(uriSegments[1]);
          this.experiment = this.experimentProviderUtil.getExperimentFromDisk(this.receivedExperimentId,
                                                                              this.useMyExperimentsDiskFile);

          if (this.experiment == null) { // Experiment NOT found locally, load  from server
            Log.debug("No local experiment. Loading from server");
            this.retrieveExperimentFromServer(uriSegments[1]);
          } else { // experiment found, show it
            this.showExperiment();
          }
        } else {
          // If the URI is wrong, show message and then go to MyExperimentsActivity
          new AlertDialog.Builder(this).setMessage(R.string.link_wrong)
                                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int which) {
                                           startActivity(new Intent(getContext(), MyExperimentsActivity.class));
                                         }
                                       }).show();
        }
      } else if (isLaunchedFromQRCode() && experiment == null) {
        new AlertDialog.Builder(this).setMessage(R.string.selected_experiment_not_on_phone_refresh)
                                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int which) {
                                         String realServerId = uri.getLastPathSegment().substring(4);
                                         retrieveExperimentFromServer(realServerId);
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
        Toast.makeText(ExperimentDetailActivity.this, msg, Toast.LENGTH_LONG);
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
          // TODO this is a slow, roundabout way to do this. Just get back the experiment directly.
          experimentProviderUtil.addExperimentToExperimentsOnDisk(msg);
          List<Experiment> experiments = experimentProviderUtil.loadExperimentsFromDisk(false);
          for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
            Experiment currentExperiment = (Experiment) iterator.next();
            if (currentExperiment.getServerId().equals(receivedExperimentId)) {
              experiment = currentExperiment;
            }
          }
          if (experiment != null) {
            showExperiment();
          } else {
            ExperimentDetailActivity.this.finish();
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
        Toast.makeText(ExperimentDetailActivity.this, "Exception: " + exception.getMessage(), Toast.LENGTH_LONG);
      }
    });
  }
}

