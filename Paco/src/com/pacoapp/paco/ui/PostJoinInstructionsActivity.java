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

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.utils.IntentExtraHelper;

public class PostJoinInstructionsActivity extends AppCompatActivity implements ExperimentLoadingActivity {

  public static final int REFRESHING_JOINED_EXPERIMENT_DIALOG_ID = 1002;

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.post_install_instructions);
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
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      if (ExperimentHelper.declaresAccessibilityLogging(experiment.getExperimentDAO())) {
        Button openAccessibilitySettings = (Button) findViewById(R.id.openAccessibilitySettings);
        openAccessibilitySettings.setVisibility(TextView.VISIBLE);
        openAccessibilitySettings.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            runAccessibilityActivity();
          }
        });
      }
      if (ExperimentHelper.declaresLogAppUsageAndBrowserCollection(experiment.getExperimentDAO())) {
        Button openUsageAccessSettings = (Button) findViewById(R.id.openUsageAccessSettings);
        openUsageAccessSettings.setVisibility(TextView.VISIBLE);
        openUsageAccessSettings.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            runUsageAccessActivity();
          }
        });
      }
      boolean userEditableSchedule = ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO());
      if (userEditableSchedule) {
        Button reviewScheduleButton = (Button) findViewById(R.id.reviewSchedule);
        reviewScheduleButton.setVisibility(TextView.VISIBLE);
        reviewScheduleButton.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            runScheduleActivity();
          }
        });
      }

      TextView ic = (TextView) findViewById(R.id.instructionsTextView);
      final String postInstallInstructions = experiment.getExperimentDAO().getPostInstallInstructions();
      if (!Strings.isNullOrEmpty(postInstallInstructions)) {
        ic.setText(Html.fromHtml(postInstallInstructions));
      }

      Button closeButton = (Button) findViewById(R.id.instructionsCloseButton);
      closeButton.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
          finish();
        }
      });
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
    }
  }

  /**
   * Show the user an activity allowing to provide accessibility permissions to Paco
   */
  private void runAccessibilityActivity() {
    Intent accessibilityActivityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    startActivity(accessibilityActivityIntent);
  }

  /*
   * Show the user an activity allowing to provide app usage access to Paco
   */
  private void runUsageAccessActivity() {
    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    startActivity(intent);
  }

  private void runScheduleActivity() {
    if (ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO())) {
      Intent experimentIntent = new Intent(this, ScheduleListActivity.class);
      experimentIntent.putExtras(getIntent().getExtras());
      experimentIntent.putExtra(InformedConsentActivity.INFORMED_CONSENT_PAGE_EXTRA_KEY, true);
      experimentIntent.putExtra(ScheduleDetailFragment.USER_EDITABLE_SCHEDULE, ExperimentHelper.hasUserEditableSchedule(experiment.getExperimentDAO()));
      startActivityForResult(experimentIntent, FindExperimentsActivity.JOIN_REQUEST_CODE);
    } else {
      setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
      finish();
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


}
