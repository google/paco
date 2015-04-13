/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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
import android.os.Bundle;
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
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.ui.ExperimentLoadingActivity;
import com.pacoapp.paco.ui.ScheduleDetailFragment;
import com.pacoapp.paco.ui.Validation;
import com.pacoapp.paco.utils.IntentExtraHelper;

public class ExperimentScheduleActivity extends Activity implements ExperimentLoadingActivity {

  private static final String TIME_FORMAT_STRING = "hh:mm aa";

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

  private Schedule schedule;

  private boolean userEditable = true;

  private ExperimentGroup experimentGroup;

  private ScheduleTrigger scheduleTrigger;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Intent intent = getIntent();

    experimentProviderUtil = new ExperimentProviderUtil(this);
    IntentExtraHelper.loadExperimentInfoFromIntent(this, intent, experimentProviderUtil);

    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      loadScheduleFromIntent();
      if (schedule == null) {
        Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
        finish();
      } else {
        userEditable = getUserEditableFromIntent();
        setUpSchedulingLayout();
      }
    }

  }

  private boolean getUserEditableFromIntent() {
    if (getIntent().getExtras() != null) {
      return getIntent().getBooleanExtra(ScheduleDetailFragment.USER_EDITABLE_SCHEDULE, true);
    }
    return false;
  }

  private void loadScheduleFromIntent() {
    if (getIntent().getExtras() != null) {
      long scheduleTriggerId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID);
      Long scheduleId = getIntent().getExtras().getLong(ScheduleDetailFragment.SCHEDULE_ID);
      scheduleTrigger = (ScheduleTrigger)experimentGroup.getActionTriggerById(scheduleTriggerId);
      schedule = scheduleTrigger.getSchedulesById(scheduleId);
    }
  }

  private void setUpSchedulingLayout() {
    // setup ui pieces for times lists and esm start/end timepickers
    inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    timesScheduleLayout = (LinearLayout) inflater.inflate(R.layout.times_schedule, null);
    timePicker = (TimePicker) timesScheduleLayout.findViewById(R.id.DailyScheduleTimePicker);
    timePicker.setIs24HourView(false);
    // end setup ui pieces

    if (schedule == null) {
      setContentView(R.layout.self_report_schedule);
      save();
      return;
    }

    createSelections();

    if (schedule.getScheduleType().equals(Schedule.WEEKDAY)
               || schedule.getScheduleType().equals(Schedule.DAILY)) {
      showDailyScheduleConfiguration();
    } else if (schedule.getScheduleType().equals(Schedule.WEEKLY)) {
      showWeeklyScheduleConfiguration();
    } else if (schedule.getScheduleType().equals(Schedule.MONTHLY)) {
      showMonthlyScheduleConfiguration();
    } else if (schedule.getScheduleType().equals(Schedule.ESM)) {
      showEsmScheduleConfiguration();
    }
    setupScheduleSaving();
  }

  // Visible for testing
  public void setActivityProperties(Experiment experiment, ExperimentProviderUtil experimentProviderUtil) {
    this.experiment = experiment;
    this.experimentProviderUtil = experimentProviderUtil;

    // TODO: Uncomment this to do true instrumentation testing.
    // setUpSchedulingLayout();
  }

  private void setupScheduleSaving() {
    if (userEditable) {
      save();
    } else {
      setupSaveButton();
    }
  }

  private void showEsmScheduleConfiguration() {
    setContentView(R.layout.esm_schedule);
    TextView title = (TextView) findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getExperimentDAO().getTitle());

    startHourField = (Button) findViewById(R.id.startHourTimePickerLabel);
    startHourField.setText(new DateMidnight().toDateTime()
                                             .withMillisOfDay(schedule.getEsmStartHour().intValue())
                                             .toString(TIME_FORMAT_STRING));

    endHourField = (Button) findViewById(R.id.endHourTimePickerLabel);
    endHourField.setText(new DateMidnight().toDateTime()
                                           .withMillisOfDay(schedule.getEsmEndHour().intValue())
                                           .toString(TIME_FORMAT_STRING));

    // TODO (bobevans): get rid of this duplication

    startHourField.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
        unsetTimesViewParent();
        dialogBuilder.setView(timesScheduleLayout);
        final AlertDialog dialog = dialogBuilder.setTitle(R.string.start_time_title).create();

        Long offset = schedule.getEsmStartHour();
        DateTime startHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue());
        timePicker.setCurrentHour(startHour.getHourOfDay());
        timePicker.setCurrentMinute(startHour.getMinuteOfHour());

        dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button),
                         new DialogInterface.OnClickListener() {

                           public void onClick(DialogInterface dialog, int which) {
                             schedule.setEsmStartHour(getHourOffsetFromPicker());
                             startHourField.setText(getTextFromPicker(schedule.getEsmStartHour()
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

        Long offset = schedule.getEsmEndHour();
        DateTime endHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue());
        timePicker.setCurrentHour(endHour.getHourOfDay());
        timePicker.setCurrentMinute(endHour.getMinuteOfHour());

        endHourDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button),
                                new DialogInterface.OnClickListener() {

                                  public void onClick(DialogInterface dialog, int which) {
                                    schedule.setEsmEndHour(getHourOffsetFromPicker());
                                    endHourField.setText(getTextFromPicker(schedule.getEsmEndHour()
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
    if (schedule.getScheduleType().equals(Schedule.DAILY)) {
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

    if (schedule.getByDayOfMonth()) {
      ((RadioButton) findViewById(R.id.domRadio)).setChecked(true);
    } else {
      ((RadioButton) findViewById(R.id.dowRadio)).setChecked(true);
    }
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        toggleByDayOfMonth_DayOfWeekWidgets(checkedId == R.id.domRadio);
      }

    });
    toggleByDayOfMonth_DayOfWeekWidgets(schedule.getByDayOfMonth());
    createTimesList();
  }

  private void createDayOfMonth() {
    dayOfMonthText = (TextView) findViewById(R.id.dayOfMonthText);
    domSpinner = (Spinner) findViewById(R.id.dayOfMonthSpinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.repeat_range,
                                                                         android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    domSpinner.setAdapter(adapter);
    domSpinner.setSelection(schedule.getDayOfMonth() - 1);
    domSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        schedule.setDayOfMonth(arg2 + 1);
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
    nthOfMonthSpinner.setSelection(schedule.getNthOfMonth());
    nthOfMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        schedule.setNthOfMonth(arg2);
      }

      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

  }

  private void toggleByDayOfMonth_DayOfWeekWidgets(boolean isByDayOfMonth) {
    if (isByDayOfMonth) {
      schedule.setByDayOfMonth(Boolean.TRUE);
      nthOfMonthText.setVisibility(View.GONE);
      nthOfMonthSpinner.setVisibility(View.GONE);
      dayOfMonthText.setVisibility(View.VISIBLE);
      dowButton.setVisibility(View.GONE);
      domSpinner.setVisibility(View.VISIBLE);
    } else {
      schedule.setByDayOfMonth(Boolean.FALSE);
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
    repeatRate.setSelection(schedule.getRepeatRate() - 1);

    repeatRate.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        schedule.setRepeatRate(position + 1);
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
    title.setText(experiment.getExperimentDAO().getTitle());

    timeList = (ListView) findViewById(R.id.timesList);
    final List<SignalTime> times = schedule.getSignalTimes();
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
    if (time.getType() == SignalTime.FIXED_TIME) {
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
      String labelText = schedule.getSignalTimes().get(position).getLabel();
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
      final List<SignalTime> times = schedule.getSignalTimes();
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
    int weekDaysScheduled = schedule.getWeekDaysScheduled();
    for (int i = 0; i < Schedule.DAYS_OF_WEEK.length; i++) {
      selections[i] = (weekDaysScheduled & Schedule.DAYS_OF_WEEK[i]) == Schedule.DAYS_OF_WEEK[i];
    }
    return selections;
  }

  private void setupSaveButton() {
  }

  private void save() {
    Validation valid = isValid();
    if (!valid.ok()) {
      Toast.makeText(this, valid.errorMessage(), Toast.LENGTH_LONG).show();
      return;
    } else {
      setResult(RESULT_OK, serialize(schedule));
    }
  }

  private Intent serialize(Schedule schedule2) {
    Intent intent = new Intent();
    intent.putExtra(Experiment.ACTION_TRIGGER_SPEC_ID, schedule2.getId());
    //intent.putExtra(Experiment.ACTION_TRIGGER_SPEC, JsonConverter.schedule2);
    return intent;
  }

  private Validation isValid() {
    Validation validation = new Validation();
    if (schedule != null && schedule.getScheduleType().equals(Schedule.ESM)) {
      if (schedule.getEsmStartHour() >= schedule.getEsmEndHour()) {
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

    if (schedule.getScheduleType().equals(Schedule.WEEKLY)) {
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
            selected |= Schedule.DAYS_OF_WEEK[i];
          }
        }
        schedule.setWeekDaysScheduled(selected);
      }

    });
    return dialogBldr.create();

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

}
