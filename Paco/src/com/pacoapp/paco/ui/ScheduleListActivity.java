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
import android.widget.Toast;

import com.google.android.apps.paco.AndroidEsmSignalStore;
import com.google.android.apps.paco.BeeperService;
import com.google.android.apps.paco.BroadcastTriggerReceiver;
import com.google.android.apps.paco.Event;
import com.google.android.apps.paco.Experiment;
import com.google.android.apps.paco.ExperimentLoadingActivity;
import com.google.android.apps.paco.ExperimentProviderUtil;
import com.google.android.apps.paco.FindExperimentsActivity;
import com.google.android.apps.paco.InformedConsentActivity;
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
 * An activity representing a list of Schedules. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ScheduleDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ScheduleListFragment} and the item details (if present) is a
 * {@link ScheduleDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ScheduleListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ScheduleListActivity extends ActionBarActivity implements ScheduleListFragment.Callbacks,
                                                           ExperimentLoadingActivity {

  private static final int SCHEDULE_DETAIL_REQUEST = 998;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private boolean fromInformedConsentPage;
  private ExperimentGroup experimentGroup;

  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet
   * device.
   */
  private boolean twoPane;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    experimentProviderUtil = new ExperimentProviderUtil(this);

    fromInformedConsentPage = getIntent().getExtras() != null ? getIntent().getExtras()
                                                                           .getBoolean(InformedConsentActivity.INFORMED_CONSENT_PAGE_EXTRA_KEY)
                                                             : false;

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);

    if (!getUserEditableFromIntent()) {
      save();
      finish();
    } else {
      if (experiment == null) {
        Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
        finish();
      } else {
        setupScheduleSaving();
      }
    }

    setContentView(R.layout.activity_schedule_list);

    if (findViewById(R.id.schedule_detail_container) != null) {
      // The detail container view will be present only in the
      // large-screen layouts (res/values-large and
      // res/values-sw600dp). If this view is present, then the
      // activity should be in two-pane mode.
      twoPane = true;

      // In two-pane mode, list items should be given the
      // 'activated' state when touched.
      ScheduleListFragment scheduleListFragment = (ScheduleListFragment) getSupportFragmentManager().findFragmentById(R.id.schedule_list);
      scheduleListFragment.setActivateOnItemClick(true);
    }
  }

  /**
   * Callback method from {@link ScheduleListFragment.Callbacks} indicating that
   * the item with the given ID was selected.
   */
  @Override
  public void onItemSelected(ScheduleBundle chosenSchedule) {
    if (twoPane) {
      // In two-pane mode, show the detail view in this activity by
      // adding or replacing the detail fragment using a
      // fragment transaction.
      Bundle arguments = new Bundle();
      arguments.putLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
      arguments.putString(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, chosenSchedule.group.getName());
      arguments.putLong(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID, chosenSchedule.trigger.getId());
      arguments.putLong(ScheduleDetailFragment.SCHEDULE_ID, chosenSchedule.schedule.getId());

      ScheduleDetailFragment fragment = new ScheduleDetailFragment();
      fragment.setArguments(arguments);
      getSupportFragmentManager().beginTransaction().replace(R.id.schedule_detail_container, fragment).commit();

    } else {
      // In single-pane mode, simply start the detail activity
      // for the selected item ID.
      Intent detailIntent = new Intent(this, ScheduleDetailActivity.class);
      detailIntent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
      detailIntent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, chosenSchedule.group.getName());
      detailIntent.putExtra(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID, chosenSchedule.trigger.getId());
      detailIntent.putExtra(ScheduleDetailFragment.SCHEDULE_ID, chosenSchedule.schedule.getId());
      startActivityForResult(detailIntent, SCHEDULE_DETAIL_REQUEST);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SCHEDULE_DETAIL_REQUEST && resultCode != RESULT_CANCELED) {
      save();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      // See the Navigation pattern on Android Design:
      // http://developer.android.com/design/patterns/navigation.html#up-vs-back
      final Intent intent = new Intent(this, MyExperimentsActivity.class);
      NavUtils.navigateUpTo(this, intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  private boolean getUserEditableFromIntent() {
    if (getIntent().getExtras() != null) {
      return getIntent().getBooleanExtra(ScheduleDetailFragment.USER_EDITABLE_SCHEDULE, true);
    }
    return true;
  }

  private void setupScheduleSaving() {
    if (!userCanEditAtLeastOneSchedule()) {
      save();
    } else {
      // setupSaveButton();
    }
  }

  private Boolean userCanEditAtLeastOneSchedule() {
    List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
    for (ExperimentGroup experimentGroup : groups) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : actionTriggers) {
        if (actionTrigger.getUserEditable()) {
          boolean userCanOnlyEditOnJoin = actionTrigger.getOnlyEditableOnJoin();
          if (!userCanOnlyEditOnJoin || (userCanOnlyEditOnJoin && fromInformedConsentPage)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void saveExperimentRegistration() {
    boolean hasEsm = false;
    for (ExperimentGroup experimentGroup : experiment.getExperimentDAO().getGroups()) {
      List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
      for (ActionTrigger actionTrigger : actionTriggers) {
        if (actionTrigger instanceof ScheduleTrigger) {
          ScheduleTrigger scheduleTrigger = (ScheduleTrigger) actionTrigger;
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
    createJoinEvent();
    startService(new Intent(this, SyncService.class));

    setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
    startService(new Intent(ScheduleListActivity.this, BeeperService.class));
    if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
      BroadcastTriggerReceiver.startProcessService(this);
    }

    finish();
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
    this.experimentGroup = groupByName;
  }

  @Override
  public void saveExperiment() {
    save();

  }

}
