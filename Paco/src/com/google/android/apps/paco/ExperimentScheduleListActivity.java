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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.paco.shared.model2.ActionTrigger;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.R;

public class ExperimentScheduleListActivity extends Activity implements ExperimentLoadingActivity {

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;

  private LayoutInflater inflater;
  private boolean fromInformedConsentPage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);

    fromInformedConsentPage = getIntent().getExtras() != null
            ? getIntent().getExtras().getBoolean(InformedConsentActivity.INFORMED_CONSENT_PAGE_EXTRA_KEY)
            : false;

    if (fromInformedConsentPage) {
      experiment = experimentProviderUtil.getExperimentFromDisk(getIntent().getExtras().getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY), fromInformedConsentPage);
    } else {
      IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    }

    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      showAllSchedulesForAllGroups();
      setupScheduleSaving();
    }
  }

  private void showAllSchedulesForAllGroups() {
    // TODO iterate each group, each scheduletrigger,
    // show details, and if editable, create link to ExperimentScheduleActivity
    // for that schedule with a onActivityResult call to update that schedule.

  }

  private void setupScheduleSaving() {
    if (!userCanEditAtLeastOneSchedule()) {
      save();
    } else {
      setupSaveButton();
    }
  }

  private Boolean userCanEditAtLeastOneSchedule() {
    List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : actionTriggers) {
        if (actionTrigger.getUserEditable()) {
          boolean userCanOnlyEditOnJoin = actionTrigger.getOnlyEditableOnJoin();
          if (userCanOnlyEditOnJoin && fromInformedConsentPage) {
            return true;
          }
        }
      }
    }
    return false;
  }


  private void setupSaveButton() {
    Button saveScheduleButton = (Button) findViewById(R.id.SetDailyScheduleButton);
    saveScheduleButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        save();
      }
    });
  }

  private void saveExperimentRegistration() {
    boolean hasEsm = false;
    for (ExperimentGroup experimentGroup : experiment.getExperimentDAO().getGroups()) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : actionTriggers) {
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
            for (Schedule schedule : scheduleTrigger.getSchedules()) {
              if (schedule != null && schedule.getScheduleType().equals(Schedule.ESM)) {
                hasEsm = true;
              }
            }
          }
        }
    }
    if (hasEsm) {
      AndroidEsmSignalStore alarmStore = new AndroidEsmSignalStore(this);
      alarmStore.deleteAllSignalsForSurvey(experiment.getId());
    }
    experimentProviderUtil.deleteNotificationsForExperiment(experiment.getId());
    experimentProviderUtil.updateJoinedExperiment(experiment);
    createJoinEvent();
    startService(new Intent(this, SyncService.class));
  }

  /**
   * Creates a pacot for a newly registered experiment
   */
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

    event.addResponse(createOutput("schedule", createSchedulesString()));

    if (experiment.getExperimentDAO().getRecordPhoneDetails()) {
      Display defaultDisplay = getWindowManager().getDefaultDisplay();
      String size = Integer.toString(defaultDisplay.getHeight()) + "x" +
              Integer.toString(defaultDisplay.getWidth());
      event.addResponse(createOutput("display", size));

      event.addResponse(createOutput("make", Build.MANUFACTURER));
      event.addResponse(createOutput("model", Build.MODEL));
      event.addResponse(createOutput("android", Build.VERSION.RELEASE));
      TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
      String carrierName = manager.getNetworkOperatorName();
      event.addResponse(createOutput("carrier", carrierName));
    }

    experimentProviderUtil.insertEvent(event);
  }

  public String createSchedulesString() {
    return "TODO Calculate all individual schedules";
  }

  private Output createOutput(String key, String answer) {
    Output responseForInput = new Output();
    responseForInput.setAnswer(answer);
    responseForInput.setName(key);
    return responseForInput;
  }

  private void save() {
    if (!userCanEditAtLeastOneSchedule()) {
      finish();
    } else {
      scheduleExperiment();

      if (fromInformedConsentPage) {
        Toast.makeText(this, getString(R.string.successfully_joined_experiment), Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show();
      }
    }
  }

  // Visible for testing
  public void scheduleExperiment() {
    saveExperimentRegistration();
    setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
    startService(new Intent(ExperimentScheduleListActivity.this, BeeperService.class));
    if (experiment.shouldWatchProcesses()) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
      BroadcastTriggerReceiver.startProcessService(this);
    }

    finish();
  }

  private Validation isValid() {
    Validation validation = new Validation();
    // iterate all schedules in all groups and actiontriggers and call validate on them.
    return validation;
  }

  // Visible for testing
  public Experiment getExperiment() {
    return experiment;
  }

  @Override
  public void setExperiment(Experiment experimentByServerId) {
    this.experiment = experimentByServerId;

  }

  @Override
  public void setExperimentGroup(ExperimentGroup groupByName) {
    // no-op for this activity
  }

}
