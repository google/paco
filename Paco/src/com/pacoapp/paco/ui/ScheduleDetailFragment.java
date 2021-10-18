package com.pacoapp.paco.ui;

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
import androidx.fragment.app.Fragment;
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
import com.pacoapp.paco.utils.IntentExtraHelper;

/**
 * A fragment representing a single Schedule detail screen. This fragment is
 * either contained in a {@link ScheduleListActivity} in two-pane mode (on
 * tablets) or a {@link ScheduleDetailActivity} on handsets.
 */
public class ScheduleDetailFragment extends Fragment implements ExperimentLoadingActivity {

  private static final String TIME_FORMAT_STRING = "hh:mm aa";

  public static final String EXTRA_SCHEDULE_ID = "schedule_extra_id";

  private Experiment experiment;
  private ExperimentGroup experimentGroup;
  private Schedule schedule;
  private ScheduleTrigger scheduleTrigger;

  private LinearLayout timesScheduleLayout;
  private TimePicker timePicker;
  private boolean[] selections;
  private Button startHourField;
  private Button endHourField;
  private RadioGroup radioGroup;
  private TextView dayOfMonthText;
  private Spinner domSpinner;
  private Spinner nthOfMonthSpinner;
  private TextView nthOfMonthText;
  private View dowButton;
  private Spinner repeatRate;
  private ListView timeList;
  public LayoutInflater inflater;
  private View rootView;

  private Callbacks callbacks;

  /**
   * A callback interface that all activities containing this fragment must
   * implement. This mechanism allows activities to be notified of item
   * selections.
   */
  public interface Callbacks {
    public Schedule getSchedule();
    public void saveSchedule();
  }

  private Callbacks sDummyCallbacks = new Callbacks() {
    @Override
    public void saveSchedule() {
    }

    @Override
    public Schedule getSchedule() {
      // TODO Auto-generated method stub
      return null;
    }
  };

  private AlertDialog endTimeBeforeStartDialog;

  public static final String SCHEDULE_TRIGGER_ID = "schedule_trigger";

  public static final String SCHEDULE_ID = "schedule_id";

  public static final String USER_EDITABLE_SCHEDULE = "UserEditableSchedule";

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ScheduleDetailFragment() {
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }
    callbacks = (Callbacks) activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    callbacks = sDummyCallbacks;
    if (endTimeBeforeStartDialog != null) {
      endTimeBeforeStartDialog.dismiss();
      endTimeBeforeStartDialog = null;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ExperimentProviderUtil experimentProviderUtil = new ExperimentProviderUtil(getActivity());
    IntentExtraHelper.loadExperimentInfoFromBundle(this, getArguments(), experimentProviderUtil);
    schedule = callbacks.getSchedule();
    //loadScheduleFromIntent();
    if (schedule == null) {
      Toast.makeText(getActivity(), R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      getActivity().finish();
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.inflater = inflater;
    timesScheduleLayout = (LinearLayout) inflater.inflate(R.layout.times_schedule, null);
    timePicker = (TimePicker) timesScheduleLayout.findViewById(R.id.DailyScheduleTimePicker);
    timePicker.setIs24HourView(false);

    if (schedule.getScheduleType().equals(Schedule.WEEKDAY) || schedule.getScheduleType().equals(Schedule.DAILY)) {
      showDailyScheduleConfiguration(container);
    } else if (schedule.getScheduleType().equals(Schedule.WEEKLY)) {
      createSelections();
      showWeeklyScheduleConfiguration(container);
    } else if (schedule.getScheduleType().equals(Schedule.MONTHLY)) {
      showMonthlyScheduleConfiguration(container);
    } else if (schedule.getScheduleType().equals(Schedule.ESM)) {
      showEsmScheduleConfiguration(container);
    } else {
      return inflater.inflate(R.layout.self_report_schedule, container, false);
    }
    return rootView;
  }

  private void unsetTimesViewParent() {
    if (timesScheduleLayout.getParent() != null) {
      ((ViewGroup) timesScheduleLayout.getParent()).removeView(timesScheduleLayout);
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

  private void showEsmScheduleConfiguration(ViewGroup container) {
    rootView = inflater.inflate(R.layout.esm_schedule, container, false);
    TextView title = (TextView) rootView.findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getExperimentDAO().getTitle());

    startHourField = (Button) rootView.findViewById(R.id.startHourTimePickerLabel);
    startHourField.setText(new DateMidnight().toDateTime()
                                             .withMillisOfDay(schedule.getEsmStartHour().intValue())
                                             .toString(TIME_FORMAT_STRING));

    endHourField = (Button) rootView.findViewById(R.id.endHourTimePickerLabel);
    endHourField.setText(new DateMidnight().toDateTime()
                                           .withMillisOfDay(schedule.getEsmEndHour().intValue())
                                           .toString(TIME_FORMAT_STRING));

    startHourField.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
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
                             final Long hourOffsetFromPicker = getHourOffsetFromPicker();
                             if (hourOffsetFromPicker >= schedule.getEsmEndHour()) {
                               alertUserToInvertedTimes();
                               return;
                             }
                             if (windowIsTooShortForSignals(hourOffsetFromPicker, schedule.getEsmEndHour())) {
                               alertUserToTooShortScheduleWindow();
                               return;
                             }
                            schedule.setEsmStartHour(hourOffsetFromPicker);
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
        final AlertDialog.Builder endHourDialogBuilder = new AlertDialog.Builder(getActivity());
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
                                    Long endHourOffsetFromPicker = getHourOffsetFromPicker();
                                    if (timePicker.getCurrentHour() == 0 && timePicker.getCurrentMinute() == 0) {
                                      setToElevenFiftyNine();
                                      endHourOffsetFromPicker = getHourOffsetFromPicker();
                                    } else if (endHourOffsetFromPicker <= schedule.getEsmStartHour()) {
                                      alertUserToInvertedTimes();
                                      return;
                                    }
                                    if (windowIsTooShortForSignals(schedule.getEsmStartHour(), endHourOffsetFromPicker)) {
                                      alertUserToTooShortScheduleWindow();
                                      return;
                                    }
                                    schedule.setEsmEndHour(endHourOffsetFromPicker);
                                    endHourField.setText(getTextFromPicker(schedule.getEsmEndHour()
                                                                                     .intValue()));
                                  }




                                });
        endHourDialog.show();
      }
    });

  }

  private boolean windowIsTooShortForSignals(Long esmStartHour, Long esmEndHour) {
    if (schedule.getEsmPeriodInDays().equals(Schedule.ESM_PERIOD_DAY)) {
      long duration = esmEndHour - esmStartHour ;
      long minimunTime = schedule.getMinimumBuffer() * 60 * 1000 * schedule.getEsmFrequency();
      return duration < minimunTime;
    }
    return false;
  }

  private void alertUserToTooShortScheduleWindow() {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
    alertDialogBuilder.setMessage(R.string.start_and_end_time_too_short);
    alertDialogBuilder.setCancelable(true);

    alertDialogBuilder.setPositiveButton(
        R.string.ok,
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
        });
    endTimeBeforeStartDialog = alertDialogBuilder.create();
    endTimeBeforeStartDialog.show();

  }

  private void alertUserToInvertedTimes() {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
    alertDialogBuilder.setMessage(R.string.end_time_must_be_after_start_time_warning_label);
    alertDialogBuilder.setCancelable(true);

    alertDialogBuilder.setPositiveButton(
        R.string.ok,
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
        });
    endTimeBeforeStartDialog = alertDialogBuilder.create();
    endTimeBeforeStartDialog.show();

  }

  private void setToElevenFiftyNine() {
    timePicker.setCurrentHour(23);
    timePicker.setCurrentMinute(59);
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


  private AlertDialog getDaysOfWeekDialog() {
    AlertDialog.Builder dialogBldr = new AlertDialog.Builder(getActivity()).setTitle(R.string.days_of_week_title);

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

    dialogBldr.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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


  private void showDailyScheduleConfiguration(ViewGroup container) {
    rootView = inflater.inflate(R.layout.daily_schedule, container, false);
    if (schedule.getScheduleType().equals(Schedule.DAILY)) {
      createRepeatRate(getString(R.string.days));
    } else {
      hideRepeatRate();
    }
    createTimesList();
  }

  private void hideRepeatRate() {
    LinearLayout repeatRateLayout = (LinearLayout) rootView.findViewById(R.id.RepeatPeriodLayout);
    repeatRateLayout.setVisibility(View.GONE);
  }

  private void showWeeklyScheduleConfiguration(ViewGroup container) {
    rootView = inflater.inflate(R.layout.weekly_schedule, container, false);
    createRepeatRate(getString(R.string.weeks));

    createDaysOfWeekPicker();
    createTimesList();
  }

  private void showMonthlyScheduleConfiguration(ViewGroup container) {
    rootView = inflater.inflate(R.layout.monthly_schedule, container, false);
    createRepeatRate(getString(R.string.months));

    createDayOfMonth();

    createNthOfMonth();
    createDaysOfWeekPicker();

    radioGroup = (RadioGroup) rootView.findViewById(R.id.RadioGroup01);

    if (schedule.getByDayOfMonth()) {
      ((RadioButton) rootView.findViewById(R.id.domRadio)).setChecked(true);
    } else {
      ((RadioButton) rootView.findViewById(R.id.dowRadio)).setChecked(true);
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
    dayOfMonthText = (TextView) rootView.findViewById(R.id.dayOfMonthText);
    domSpinner = (Spinner) rootView.findViewById(R.id.dayOfMonthSpinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.repeat_range,
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
    nthOfMonthSpinner = (Spinner) rootView.findViewById(R.id.NthOfMonthSpinner);
    nthOfMonthText = (TextView) rootView.findViewById(R.id.NthOfMonthText);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.nth_of_month,
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
    dowButton = rootView.findViewById(R.id.dow_button);
    dowButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        getActivity().showDialog(0);
      }
    });
  }

  private void createRepeatRate(String period) {
    repeatRate = (Spinner) rootView.findViewById(R.id.RepeatRate);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.repeat_range,
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

    TextView repeatPeriodLabel = (TextView) rootView.findViewById(R.id.RepeatPeriodLabel);
    repeatPeriodLabel.setText(" " + period);
  }

  private void createTimesList() {
    TextView title = (TextView) rootView.findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getExperimentDAO().getTitle());

    timeList = (ListView) rootView.findViewById(R.id.timesList);
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

    final ArrayAdapter<String> timeAdapter = new ButtonArrayAdapter(getActivity(), R.layout.timelist_item, timeStrs);
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
        labelText = getContext().getString(R.string.time_schedule_edit_label) + " " + Integer.toString(position + 1);
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
      final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
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


  private void save() {
    Validation valid = isValid();
    if (!valid.ok()) {
      Toast.makeText(getActivity(), valid.errorMessage(), Toast.LENGTH_LONG).show();
      return;
    } else {
      getActivity().setResult(getActivity().RESULT_OK);
      callbacks.saveSchedule();
      getActivity().finish();
    }
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

  private Intent serialize(Schedule schedule2) {
    Intent intent = new Intent();
    intent.putExtra(Experiment.ACTION_TRIGGER_SPEC_ID, schedule2.getId());
    return intent;
  }


  private void loadScheduleFromIntent() {
    long scheduleTriggerId = getArguments().getLong(ScheduleDetailFragment.SCHEDULE_TRIGGER_ID);
    Long scheduleId = getArguments().getLong(ScheduleDetailFragment.SCHEDULE_ID);
    scheduleTrigger = (ScheduleTrigger) experimentGroup.getActionTriggerById(scheduleTriggerId);
    schedule = scheduleTrigger.getSchedulesById(scheduleId);
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
}
