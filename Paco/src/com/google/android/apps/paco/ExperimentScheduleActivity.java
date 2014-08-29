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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.paco.shared.model.SignalTimeDAO;
import com.pacoapp.paco.R;

public class ExperimentScheduleActivity extends Activity {

  private static final String TIME_FORMAT_STRING = "hh:mm aa";

  private Uri uri;
  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private TimePicker timePicker;
  private TextView startHourField;
  private TextView endHourField;
  private Spinner repeatRate;
  private boolean[] selections;
  private Button dowButton;

  private Spinner domSpinner;

  private Spinner nthOfMonthSpinner;

  private TextView nthOfMonthText;

  private TextView dayOfMonthText;

  private RadioGroup radioGroup;

  private ListView timeList;

  private LayoutInflater inflater;

  private LinearLayout timesScheduleLayout;

  private boolean fromInformedConsentPage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Intent intent = getIntent();
    uri = intent.getData();
    if (uri != null) {
      fromInformedConsentPage = intent.getExtras() != null ? intent.getExtras().getBoolean(InformedConsentActivity.INFORMED_CONSENT_PAGE_EXTRA_KEY) : false;
      // showingJoinedExperiments = uri.getPathSegments().get(0)
      // .equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI.getPathSegments().get(0));

      // branch out into a different view to include based on the type of
      // schedule
      // in the experiment.
      experimentProviderUtil = new ExperimentProviderUtil(this);
      // if (showingJoinedExperiments) {
      experiment = experimentProviderUtil.getExperiment(uri);
      // } else {
      // experiment = experimentProviderUtil.getExperimentFromDisk(uri);
      // }

      setUpSchedulingLayout();
    }
  }

  private void setUpSchedulingLayout() {
    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      // setup ui pieces for times lists and esm start/end timepickers
      inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
      timesScheduleLayout = (LinearLayout) inflater.inflate(R.layout.times_schedule, null);
      timePicker = (TimePicker) timesScheduleLayout.findViewById(R.id.DailyScheduleTimePicker);
      timePicker.setIs24HourView(false);
      // end setup ui pieces

      SignalingMechanism signalingMechanism = experiment.getSignalingMechanisms().get(0);
      if (signalingMechanism != null && signalingMechanism.getType().equals(SignalingMechanism.SIGNAL_SCHEDULE_TYPE)) {
        createSelections();
      }

      if (signalingMechanism == null
          || signalingMechanism.getType().equals(SignalingMechanism.TRIGGER_TYPE)
          || (signalingMechanism.getType().equals(SignalSchedule.SIGNAL_SCHEDULE_TYPE) && ((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.SELF_REPORT))) {
        setContentView(R.layout.self_report_schedule);
        save();
        return;
      } else if (((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.WEEKDAY)
                 || ((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.DAILY)) {
        showDailyScheduleConfiguration();
      } else if (((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.WEEKLY)) {
        showWeeklyScheduleConfiguration();
      } else if (((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.MONTHLY)) {
        showMonthlyScheduleConfiguration();
      } else if (((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.ESM)) {
        showEsmScheduleConfiguration();
      }
      setupScheduleSaving();
    }
  }

  // Visible for testing
  public void setActivityProperties(Experiment experiment, ExperimentProviderUtil experimentProviderUtil) {
    this.experiment = experiment;
    this.experimentProviderUtil = experimentProviderUtil;

    // TODO: Uncomment this to do true instrumentation testing.
    // setUpSchedulingLayout();
  }

  private void setupScheduleSaving() {
    if (userCannotConfirmSchedule()) {
      save();
    } else {
      setupSaveButton();
    }
  }

  private Boolean userCannotConfirmSchedule() {
    if ((experiment.getSignalingMechanisms().get(0)) != null) {
      if (((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getUserEditable() != null
          && ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getUserEditable() == Boolean.FALSE) {
        return true;
      }
      boolean userCanOnlyEditOnJoin = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getOnlyEditableOnJoin() != null
                                     && ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getOnlyEditableOnJoin() == Boolean.TRUE;
      if (userCanOnlyEditOnJoin && !fromInformedConsentPage) {
        return true;
      }

    }
    return false;
  }

  private void showEsmScheduleConfiguration() {
    setContentView(R.layout.esm_schedule);
    TextView title = (TextView) findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getTitle());

    startHourField = (Button) findViewById(R.id.startHourTimePickerLabel);
    startHourField.setText(new DateMidnight().toDateTime()
                                             .withMillisOfDay(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmStartHour().intValue())
                                             .toString(TIME_FORMAT_STRING));

    endHourField = (Button) findViewById(R.id.endHourTimePickerLabel);
    endHourField.setText(new DateMidnight().toDateTime()
                                           .withMillisOfDay(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmEndHour().intValue())
                                           .toString(TIME_FORMAT_STRING));

    // TODO (bobevans): get rid of this duplication

    startHourField.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
        unsetTimesViewParent();
        dialogBuilder.setView(timesScheduleLayout);
        final AlertDialog dialog = dialogBuilder.setTitle(R.string.start_time_title).create();

        Long offset = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmStartHour();
        DateTime startHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue());
        timePicker.setCurrentHour(startHour.getHourOfDay());
        timePicker.setCurrentMinute(startHour.getMinuteOfHour());

        dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button),
                         new DialogInterface.OnClickListener() {

                           public void onClick(DialogInterface dialog, int which) {
                             ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setEsmStartHour(getHourOffsetFromPicker());
                             startHourField.setText(getTextFromPicker(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmStartHour()
                                                                                .intValue()));
                           }

                         });
        dialog.show();
      }
    });

    //

    endHourField.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        final AlertDialog.Builder endHourDialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
        unsetTimesViewParent();
        endHourDialogBuilder.setView(timesScheduleLayout);
        final AlertDialog endHourDialog = endHourDialogBuilder.setTitle(R.string.end_time_title).create();

        Long offset = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmEndHour();
        DateTime endHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue());
        timePicker.setCurrentHour(endHour.getHourOfDay());
        timePicker.setCurrentMinute(endHour.getMinuteOfHour());

        endHourDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button),
                                new DialogInterface.OnClickListener() {

                                  public void onClick(DialogInterface dialog, int which) {
                                    ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setEsmEndHour(getHourOffsetFromPicker());
                                    endHourField.setText(getTextFromPicker(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getEsmEndHour()
                                                                                     .intValue()));
                                  }

                                });
        endHourDialog.show();
      }
    });

  }

  private void unsetTimesViewParent() {
    if (timesScheduleLayout.getParent() != null) {
      ((ViewGroup) timesScheduleLayout.getParent()).removeView(timesScheduleLayout);
    }
  }

  private void showDailyScheduleConfiguration() {
    setContentView(R.layout.daily_schedule);
    if (((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getScheduleType().equals(SignalSchedule.DAILY)) {
      createRepeatRate(getString(R.string.days));
    } else {
      hideRepeatRate();
    }
    createTimesList();
  }

  private void hideRepeatRate() {
    LinearLayout repeatRateLayout = (LinearLayout) findViewById(R.id.RepeatPeriodLayout);
    repeatRateLayout.setVisibility(View.GONE);
  }

  private void showWeeklyScheduleConfiguration() {
    setContentView(R.layout.weekly_schedule);
    createRepeatRate(getString(R.string.weeks));

    createDaysOfWeekPicker();
    createTimesList();
  }

  private void showMonthlyScheduleConfiguration() {
    setContentView(R.layout.monthly_schedule);
    createRepeatRate(getString(R.string.months));

    createDayOfMonth();

    createNthOfMonth();
    createDaysOfWeekPicker();

    radioGroup = (RadioGroup) findViewById(R.id.RadioGroup01);

    if (((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getByDayOfMonth()) {
      ((RadioButton) findViewById(R.id.domRadio)).setChecked(true);
    } else {
      ((RadioButton) findViewById(R.id.dowRadio)).setChecked(true);
    }
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        toggleByDayOfMonth_DayOfWeekWidgets(checkedId == R.id.domRadio);
      }

    });
    toggleByDayOfMonth_DayOfWeekWidgets(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getByDayOfMonth());
    createTimesList();
  }

  private void createDayOfMonth() {
    dayOfMonthText = (TextView) findViewById(R.id.dayOfMonthText);
    domSpinner = (Spinner) findViewById(R.id.dayOfMonthSpinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.repeat_range,
                                                                         android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    domSpinner.setAdapter(adapter);
    domSpinner.setSelection(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getDayOfMonth() - 1);
    domSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setDayOfMonth(arg2 + 1);
      }

      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
  }

  private void createNthOfMonth() {
    nthOfMonthSpinner = (Spinner) findViewById(R.id.NthOfMonthSpinner);
    nthOfMonthText = (TextView) findViewById(R.id.NthOfMonthText);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.nth_of_month,
                                                                         android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    nthOfMonthSpinner.setAdapter(adapter);
    nthOfMonthSpinner.setSelection(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getNthOfMonth());
    nthOfMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setNthOfMonth(arg2);
      }

      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

  }

  private void toggleByDayOfMonth_DayOfWeekWidgets(boolean isByDayOfMonth) {
    if (isByDayOfMonth) {
      ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setByDayOfMonth(Boolean.TRUE);
      nthOfMonthText.setVisibility(View.GONE);
      nthOfMonthSpinner.setVisibility(View.GONE);
      dayOfMonthText.setVisibility(View.VISIBLE);
      dowButton.setVisibility(View.GONE);
      domSpinner.setVisibility(View.VISIBLE);
    } else {
      ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setByDayOfMonth(Boolean.FALSE);
      nthOfMonthText.setVisibility(View.VISIBLE);
      nthOfMonthSpinner.setVisibility(View.VISIBLE);
      dayOfMonthText.setVisibility(View.GONE);
      dowButton.setVisibility(View.VISIBLE);
      domSpinner.setVisibility(View.GONE);
    }
  }

  private void createDaysOfWeekPicker() {
    dowButton = (Button) findViewById(R.id.dow_button);
    dowButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        showDialog(0);
      }
    });
  }

  private void createRepeatRate(String period) {
    repeatRate = (Spinner) findViewById(R.id.RepeatRate);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.repeat_range,
                                                                         android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    repeatRate.setAdapter(adapter);
    repeatRate.setSelection(((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getRepeatRate() - 1);

    repeatRate.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).setRepeatRate(position + 1);
      }

      public void onNothingSelected(AdapterView<?> parent) {
        // Nothing to be done here.
      }
    });

    TextView repeatPeriodLabel = (TextView) findViewById(R.id.RepeatPeriodLabel);
    repeatPeriodLabel.setText(" " + period);
  }

  private void createTimesList() {
    TextView title = (TextView) findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getTitle());

    timeList = (ListView) findViewById(R.id.timesList);
    final List<SignalTime> times = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getSignalTimes();
    setArrayAdapter(times);

    // timeList.setOnItemClickListener(new OnItemClickListener() {
    // public void onItemClick(AdapterView<?> listview, View textview, final int
    // position, long id) {
    // final AlertDialog.Builder dialogBuilder = new
    // AlertDialog.Builder(ExperimentScheduleActivity.this);
    // unsetTimesViewParent();
    // dialogBuilder.setView(timesScheduleLayout);
    // final AlertDialog dialog =
    // dialogBuilder.setTitle("Modify Time").create();
    //
    // DateTime selectedDateTime = new
    // DateTime().withMillisOfDay(times.get(position).intValue());
    // timePicker.setCurrentHour(selectedDateTime.getHourOfDay());
    // timePicker.setCurrentMinute(selectedDateTime.getMinuteOfHour());
    //
    // dialog.setButton(Dialog.BUTTON_POSITIVE, "Save", new
    // DialogInterface.OnClickListener() {
    //
    // public void onClick(DialogInterface dialog, int which) {
    // long offsetMillis = timePicker.getCurrentHour() * 60 * 60 * 1000 +
    // timePicker.getCurrentMinute() * 60 * 1000;
    // times.set(position, offsetMillis);
    // //timeAdapter.notifyDataSetChanged();
    // setArrayAdapter(times);
    // }
    //
    // });
    // dialog.show();
    // }
    // });
  }

  private void setArrayAdapter(final List<SignalTime> times) {
    List<String> timeStrs = new ArrayList<String>();
    for (SignalTime time : times) {
      timeStrs.add(getStringForTime(time));
    }

    final ArrayAdapter<String> timeAdapter = new ButtonArrayAdapter(this, R.layout.timelist_item, timeStrs);
    timeList.setAdapter(timeAdapter);
  }

  private String getStringForTime(SignalTime time) {
    if (time.getType() == SignalTimeDAO.FIXED_TIME) {
      return new DateTime().withMillisOfDay(time.getFixedTimeMillisFromMidnight()).toString("hh:mm a");
    } else {
      return "+" + time.getOffsetTimeMillis() / 1000 / 60 + " mins";
    }
  }

  class ButtonArrayAdapter extends ArrayAdapter<String> {
    public ButtonArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
      super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      String text = getItem(position);
      if (null == convertView) {
        convertView = inflater.inflate(R.layout.timelist_item, null);
      }

      TextView label = (TextView) convertView.findViewById(R.id.textView1);
      String labelText = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getSignalTimes().get(position).getLabel();
      if (Strings.isNullOrEmpty(labelText)) {
        labelText = "Time " + Integer.toString(position + 1);
      }
      label.setText(labelText + ": ");
      Button btn = (Button) convertView.findViewById(R.id.timePickerLabel);
      btn.setText(text);
      if (text.startsWith("+")) {
        btn.setEnabled(false);
      } else {
        btn.setEnabled(true);
      }
      // set listener for the whole row
      // convertView.setOnClickListener(new OnItemClickListener(position));
      btn.setOnClickListener(new OnItemClickListener(position));
      return convertView;
    }
  }

  private class OnItemClickListener implements OnClickListener {
    private int position;

    OnItemClickListener(int position) {
      this.position = position;
    }

    public void onClick(View arg0) {
      final List<SignalTime> times = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getSignalTimes();
      final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
      unsetTimesViewParent();
      dialogBuilder.setView(timesScheduleLayout);
      final AlertDialog dialog = dialogBuilder.setTitle(R.string.modify_time_title).create();

      DateTime selectedDateTime = new DateTime().withMillisOfDay(times.get(position).getFixedTimeMillisFromMidnight());
      timePicker.setCurrentHour(selectedDateTime.getHourOfDay());
      timePicker.setCurrentMinute(selectedDateTime.getMinuteOfHour());

      dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button), new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
          timePicker.clearFocus(); // Maybe this will save whatever value is
          // there,
          // so that the current hour will work when the user is editing it
          // directly.
          int offsetMillis = timePicker.getCurrentHour() * 60 * 60 * 1000 + timePicker.getCurrentMinute() * 60 * 1000;
          times.get(position).setFixedTimeMillisFromMidnight(offsetMillis);
          // timeAdapter.notifyDataSetChanged();
          setArrayAdapter(times);
        }

      });
      dialog.show();
    }
  }

  private boolean[] createSelections() {
    selections = new boolean[7];
    int weekDaysScheduled = ((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getWeekDaysScheduled();
    for (int i = 0; i < SignalSchedule.DAYS_OF_WEEK.length; i++) {
      selections[i] = (weekDaysScheduled & SignalSchedule.DAYS_OF_WEEK[i]) == SignalSchedule.DAYS_OF_WEEK[i];
    }
    return selections;
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
    SignalingMechanism sm = experiment.getSignalingMechanisms().get(0);
    if (sm != null && sm.getType().equals(SignalingMechanism.SIGNAL_SCHEDULE_TYPE) && ((SignalSchedule)sm).getScheduleType().equals(SignalSchedule.ESM)) {
      AlarmStore alarmStore = new AlarmStore(this);
      alarmStore.deleteAllSignalsForSurvey(experiment.getId());
      experimentProviderUtil.deleteNotificationsForExperiment(experiment.getId());
    }
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
    event.setExperimentName(experiment.getTitle());
    event.setExperimentVersion(experiment.getVersion());
    event.setResponseTime(new DateTime());

    event.addResponse(createOutput("joined", "true"));

    SignalingMechanism schedule = experiment.getSignalingMechanisms().get(0);
    event.addResponse(createOutput("schedule", schedule.toString()));

    if (experiment.isRecordPhoneDetails()) {
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

  private Output createOutput(String key, String answer) {
    Output responseForInput = new Output();
    responseForInput.setAnswer(answer);
    responseForInput.setName(key);
    return responseForInput;
  }

  private void save() {
    Validation valid = isValid();
    if (!valid.ok()) {
      Toast.makeText(this, valid.errorMessage(), Toast.LENGTH_LONG).show();
      return;
    }
    scheduleExperiment();
    if (fromInformedConsentPage) {
      Toast.makeText(this, getString(R.string.successfully_joined_experiment), Toast.LENGTH_LONG).show();
    } else if (userCannotConfirmSchedule()) {
      Toast.makeText(this, getString(R.string.this_experiment_schedule_is_not_editable), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show();
    }
  }

  // Visible for testing
  public void scheduleExperiment() {
    saveExperimentRegistration();
    setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
    if (uri != null) {
      startService(new Intent(ExperimentScheduleActivity.this, BeeperService.class));
      if (experiment.shouldWatchProcesses()) {
        BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
        BroadcastTriggerReceiver.startProcessService(this);
      }
    }
    finish();
  }

  private Validation isValid() {
    Validation validation = new Validation();
    SignalingMechanism signalingMechanism = experiment.getSignalingMechanisms().get(0);
    if (signalingMechanism != null && signalingMechanism.getType().equals(SignalingMechanism.SIGNAL_SCHEDULE_TYPE) && ((SignalSchedule)signalingMechanism).getScheduleType().equals(SignalSchedule.ESM)) {
      if (((SignalSchedule)signalingMechanism).getEsmStartHour() >= ((SignalSchedule)signalingMechanism).getEsmEndHour()) {
        validation.addMessage(getString(R.string.start_hour_must_be_before_end_hour_warning));
      }
    }
    return validation;
  }

  private Long getHourOffsetFromPicker() {
    return new Long(new DateMidnight().toDateTime().withHourOfDay(timePicker.getCurrentHour())
                                      .withMinuteOfHour(timePicker.getCurrentMinute()).getMillisOfDay());
  }

  private String getTextFromPicker(int esmOffset) {
    return new DateMidnight().toDateTime().withMillisOfDay(esmOffset).toString(TIME_FORMAT_STRING);
  }

  protected Dialog onCreateDialog(int id, Bundle args) {
    return getDaysOfWeekDialog();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return super.onCreateDialog(id);
  }

  private AlertDialog getDaysOfWeekDialog() {
    AlertDialog.Builder dialogBldr = new AlertDialog.Builder(this).setTitle(R.string.days_of_week_title);

    if (((SignalSchedule)experiment.getSignalingMechanisms().get(0)).getScheduleType().equals(SignalSchedule.WEEKLY)) {
      dialogBldr.setMultiChoiceItems(R.array.days_of_week, selections, new OnMultiChoiceClickListener() {
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
          selections[which] = isChecked;
        }
      });
    } else {
      int selected = 0;
      for (int i = 0; i < selections.length; i++) {
        if (selections[i]) {
          selected = i;
          break;
        }
      }
      dialogBldr.setSingleChoiceItems(R.array.days_of_week, selected, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          for (int i = 0; i < selections.length; i++) {
            selections[i] = (i == which);
          }
        }
      });
    }

    dialogBldr.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        int selected = 0;

        for (int i = 0; i < 7; i++) {
          if (selections[i]) {
            selected |= SignalSchedule.DAYS_OF_WEEK[i];
          }
        }
        getSchedule().setWeekDaysScheduled(selected);
      }

      private SignalSchedule getSchedule() {
        return (SignalSchedule)experiment.getSignalingMechanisms().get(0);
      }
    });
    return dialogBldr.create();

  }

  // Visible for testing
  public Experiment getExperiment() {
    return experiment;
  }

}
