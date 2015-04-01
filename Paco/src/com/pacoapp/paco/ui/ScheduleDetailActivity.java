package com.pacoapp.paco.ui;

import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.MenuItem;

import com.google.android.apps.paco.AndroidEsmSignalStore;
import com.google.android.apps.paco.BeeperService;
import com.google.android.apps.paco.BroadcastTriggerReceiver;
import com.google.android.apps.paco.Event;
import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentLoadingActivity;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.FindExperimentsActivity;
import com.google.android.apps.paco.Output;
import com.google.android.apps.paco.SyncService;
import com.google.android.apps.paco.utils.IntentExtraHelper;
import com.google.paco.shared.model2.ActionTrigger;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Schedule;
import com.google.paco.shared.model2.ScheduleTrigger;
import com.google.paco.shared.util.ExperimentHelper;
import com.google.paco.shared.util.SchedulePrinter;
import com.pacoapp.paco.R;

/**
 * An activity representing a single Daily Response detail screen. This activity
 * is only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link ScheduleListActivity}
 * .
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ScheduleDetailFragment}.
 */
public class ScheduleDetailActivity extends ActionBarActivity implements ScheduleDetailFragment.Callbacks,
                                                             ExperimentLoadingActivity {

  private ScheduleTrigger scheduleTrigger;
  private ExperimentGroup experimentGroup;
  private Schedule schedule;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_schedule_detail);

    // Show the Up button in the action bar.
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    // savedInstanceState is non-null when there is fragment state
    // saved from previous configurations of this activity
    // (e.g. when rotating the screen from portrait to landscape).
    // In this case, the fragment will automatically be re-added
    // to its container so we don't need to manually add it.
    // For more information, see the Fragments API guide at:
    //
    // http://developer.android.com/guide/components/fragments.html
    //
    if (savedInstanceState == null) {
      ScheduleDetailFragment fragment = new ScheduleDetailFragment();
      fragment.setArguments(getIntent().getExtras());
      getSupportFragmentManager().beginTransaction().add(R.id.schedule_detail_container, fragment).commit();
    }
    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    loadScheduleFromIntent();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      // See the Navigation pattern on Android Design:
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back
      saveSchedule();
      final Intent intent = new Intent(this, ScheduleListActivity.class);
      intent.putExtras(getIntent().getExtras());
      setResult(RESULT_OK);
      NavUtils.navigateUpTo(this, intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadScheduleFromIntent() {
    if (getIntent().getExtras() != null) {
      long scheduleTriggerId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID);
      Long scheduleId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_ID);
      scheduleTrigger = (ScheduleTrigger) experimentGroup.getActionTriggerById(scheduleTriggerId);
      schedule = scheduleTrigger.getSchedulesById(scheduleId);
    }
  }

  @Override
  public void saveSchedule() {
    if (schedule != null && schedule.getScheduleType().equals(Schedule.ESM)) {
      AndroidEsmSignalStore alarmStore = new AndroidEsmSignalStore(this);
      alarmStore.deleteAllSignalsForSurvey(experiment.getId());
    }
    experimentProviderUtil.deleteNotificationsForExperiment(experiment.getId());
    experimentProviderUtil.updateJoinedExperiment(experiment);

    createJoinEvent();
    startService(new Intent(this, SyncService.class));

    setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
    startService(new Intent(ScheduleDetailActivity.this, BeeperService.class));
    if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
      BroadcastTriggerReceiver.startProcessService(this);
    }
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

    //event.addResponse(createOutput("joined", "true"));

    event.addResponse(createOutput("schedule", createSchedulesString()));

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

  public String createSchedulesString() {
    StringBuffer buf = new StringBuffer();
    List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
    boolean firstItem = true;
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : actionTriggers) {
        if (actionTrigger instanceof ScheduleTrigger) {
          List<Schedule> schedules = ((ScheduleTrigger)actionTrigger).getSchedules();

          for (Schedule schedule : schedules) {
            if (firstItem) {
              firstItem = false;
            } else {
              buf.append("; ");
            }
            buf.append(SchedulePrinter.toString(schedule));
          }
        }
      }
    }
    return buf.toString();
  }

  private Output createOutput(String key, String answer) {
    Output responseForInput = new Output();
    responseForInput.setAnswer(answer);
    responseForInput.setName(key);
    return responseForInput;
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
    this.experimentGroup = groupByName;
  }

  @Override
  public Schedule getSchedule() {
    return schedule;
  }
}
