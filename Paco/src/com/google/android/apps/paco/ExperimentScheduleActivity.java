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

import com.google.sampling.experiential.android.lib.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ExperimentScheduleActivity extends Activity {

  private static final String TIME_FORMAT_STRING = "hh:mm aa";

  
  private Uri uri;
  private Experiment experiment;
  ExperimentProviderUtil experimentProviderUtil;
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


  private boolean showingJoinedExperiments;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// branch out into a different view to include based on the type of schedule in the experiment.
	final Intent intent = getIntent();
	uri = intent.getData();
  showingJoinedExperiments = uri.getPathSegments().get(0).equals(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI.getPathSegments().get(0));

	experimentProviderUtil = new ExperimentProviderUtil(this);
  if (showingJoinedExperiments) {
    experiment = experimentProviderUtil.getExperiment(uri);
  } else {
    experiment = experimentProviderUtil.getExperimentFromDisk(uri);
  }

    
    if (experiment == null) {
      Toast.makeText(this, R.string.cannot_find_the_experiment_warning, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      // setup ui pieces for times lists and esm start/end timepickers
      inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
      timesScheduleLayout = (LinearLayout)inflater.inflate(R.layout.times_schedule, null);
      timePicker = (TimePicker)timesScheduleLayout.findViewById(R.id.DailyScheduleTimePicker);
      timePicker.setIs24HourView(false);
      // end setup ui pieces
      createSelections();
      if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKDAY) ||
          experiment.getSchedule().getScheduleType().equals(SignalSchedule.DAILY)) {
        showDailyScheduleConfiguration();
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKLY)) {
        showWeeklyScheduleConfiguration();
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.MONTHLY)) {
        showMonthlyScheduleConfiguration();
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.ESM)) {
        showEsmScheduleConfiguration();
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.SELF_REPORT)) {
        setContentView(R.layout.self_report_schedule);
      }
      setupSaveButton();
      if (experiment.getSchedule().getUserEditable() != null && experiment.getSchedule().getUserEditable() == Boolean.FALSE) {
        save();
      }
    }
  }

  private void showEsmScheduleConfiguration() {
    setContentView(R.layout.esm_schedule);
    TextView title = (TextView)findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getTitle());
    
//    frequencyField = (EditText)findViewById(R.id.experimentEsmFrequency);
//    frequencyField.setInputType(InputType.TYPE_CLASS_NUMBER);
//    frequencyField.setText(Integer.toString(experiment.getSchedule().getEsmFrequency()));
//    
    //createPeriod();
    
//    weekendsCheckBox = (CheckBox)findViewById(R.id.weekendsCheckBox);
//    weekendsCheckBox.setChecked(experiment.getSchedule().getEsmWeekends());
//    weekendsCheckBox.setOnClickListener(new OnClickListener() {
//
//      public void onClick(View v) {
//        experiment.getSchedule().setEsmWeekends(weekendsCheckBox.isChecked());
//      }
//      
//    });    
    startHourField = (Button) findViewById(R.id.startHourTimePickerLabel);
    startHourField.setText(new DateMidnight().toDateTime().withMillisOfDay(experiment.getSchedule().getEsmStartHour().intValue()).toString(TIME_FORMAT_STRING));

    endHourField = (Button) findViewById(R.id.endHourTimePickerLabel);
    endHourField.setText(new DateMidnight().toDateTime().withMillisOfDay(experiment.getSchedule().getEsmEndHour().intValue()).toString(TIME_FORMAT_STRING));
    
    // TODO (bobevans): get rid of this duplication
    
    startHourField.setOnClickListener(new OnClickListener() { 
      
      public void onClick(View v) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
        unsetTimesViewParent();
        dialogBuilder.setView(timesScheduleLayout);
        final AlertDialog dialog = dialogBuilder.setTitle(R.string.start_time_title).create();

        Long offset = experiment.getSchedule().getEsmStartHour();
        DateTime startHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue()); 
        timePicker.setCurrentHour(startHour.getHourOfDay());
        timePicker.setCurrentMinute(startHour.getMinuteOfHour());      

        dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            experiment.getSchedule().setEsmStartHour(getHourOffsetFromPicker());
            startHourField.setText(getTextFromPicker(experiment.getSchedule().getEsmStartHour().intValue()));
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

        Long offset = experiment.getSchedule().getEsmEndHour();
        DateTime endHour = new DateMidnight().toDateTime().withMillisOfDay(offset.intValue()); 
        timePicker.setCurrentHour(endHour.getHourOfDay());
        timePicker.setCurrentMinute(endHour.getMinuteOfHour());      

        endHourDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            experiment.getSchedule().setEsmEndHour(getHourOffsetFromPicker());
            endHourField.setText(getTextFromPicker(experiment.getSchedule().getEsmEndHour().intValue()));
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
  	if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.DAILY)) {
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
    
    radioGroup = (RadioGroup)findViewById(R.id.RadioGroup01);
    
    if (experiment.getSchedule().getByDayOfMonth()) {
      ((RadioButton)findViewById(R.id.domRadio)).setChecked(true);
    } else {
      ((RadioButton)findViewById(R.id.dowRadio)).setChecked(true);
    }
    radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {     
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        toggleByDayOfMonth_DayOfWeekWidgets(checkedId == R.id.domRadio);
      }

    });
    toggleByDayOfMonth_DayOfWeekWidgets(experiment.getSchedule().getByDayOfMonth());
    createTimesList();
  }

  private void createDayOfMonth() {
    dayOfMonthText = (TextView)findViewById(R.id.dayOfMonthText);
    domSpinner = (Spinner)findViewById(R.id.dayOfMonthSpinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        this, R.array.repeat_range, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    domSpinner.setAdapter(adapter);
    domSpinner.setSelection(experiment.getSchedule().getDayOfMonth() - 1);
    domSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
          long arg3) {
        experiment.getSchedule().setDayOfMonth(arg2 + 1);
      }

      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
  }
  
  private void createNthOfMonth() {
    nthOfMonthSpinner = (Spinner)findViewById(R.id.NthOfMonthSpinner);
    nthOfMonthText = (TextView)findViewById(R.id.NthOfMonthText);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        this, R.array.nth_of_month, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    nthOfMonthSpinner.setAdapter(adapter);
    nthOfMonthSpinner.setSelection(experiment.getSchedule().getNthOfMonth());
    nthOfMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
          long arg3) {
        experiment.getSchedule().setNthOfMonth(arg2);
      }

      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

  }


  private void toggleByDayOfMonth_DayOfWeekWidgets(boolean isByDayOfMonth) {
    if (isByDayOfMonth) {
      experiment.getSchedule().setByDayOfMonth(Boolean.TRUE);
      nthOfMonthText.setVisibility(View.GONE);
      nthOfMonthSpinner.setVisibility(View.GONE);
      dayOfMonthText.setVisibility(View.VISIBLE);
      dowButton.setVisibility(View.GONE);
      domSpinner.setVisibility(View.VISIBLE);
    } else {
      experiment.getSchedule().setByDayOfMonth(Boolean.FALSE);
      nthOfMonthText.setVisibility(View.VISIBLE);
      nthOfMonthSpinner.setVisibility(View.VISIBLE);
      dayOfMonthText.setVisibility(View.GONE);
      dowButton.setVisibility(View.VISIBLE);
      domSpinner.setVisibility(View.GONE);
    }
  }

  private void createDaysOfWeekPicker() {
    dowButton = (Button)findViewById(R.id.dow_button);
    dowButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        showDialog(0);
      }      
    });
  }
  
  private void createRepeatRate(String period) {
    repeatRate = (Spinner) findViewById(R.id.RepeatRate);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        this, R.array.repeat_range, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    repeatRate.setAdapter(adapter);
    repeatRate.setSelection(experiment.getSchedule().getRepeatRate() - 1);
    TextView repeatPeriodLabel = (TextView) findViewById(R.id.RepeatPeriodLabel);      
    repeatPeriodLabel.setText(" " + period);
  }

  private void createTimesList() {
    TextView title = (TextView)findViewById(R.id.experimentNameSchedule);
    title.setText(experiment.getTitle());
    
    
    timeList = (ListView)findViewById(R.id.timesList);
    final List<Long> times = experiment.getSchedule().getTimes();
    setArrayAdapter(times);
    
    
//    timeList.setOnItemClickListener(new OnItemClickListener() {      
//      public void onItemClick(AdapterView<?> listview, View textview, final int position, long id) {        
//        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
//        unsetTimesViewParent();
//        dialogBuilder.setView(timesScheduleLayout);
//        final AlertDialog dialog = dialogBuilder.setTitle("Modify Time").create();
//        
//        DateTime selectedDateTime = new DateTime().withMillisOfDay(times.get(position).intValue());
//        timePicker.setCurrentHour(selectedDateTime.getHourOfDay());
//        timePicker.setCurrentMinute(selectedDateTime.getMinuteOfHour());      
//
//        dialog.setButton(Dialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
//
//          public void onClick(DialogInterface dialog, int which) {
//            long offsetMillis = timePicker.getCurrentHour() * 60 * 60 * 1000 + 
//            timePicker.getCurrentMinute() * 60 * 1000;            
//            times.set(position, offsetMillis);
//            //timeAdapter.notifyDataSetChanged();
//            setArrayAdapter(times);
//          }
//          
//        });
//        dialog.show();
//      }
//    });
  }

  private void setArrayAdapter(final List<Long> times) {
    List<String> timeStrs = new ArrayList<String>();
    for (Long time : times) {
      timeStrs.add(getStringForTime(time));
    }
    
    final ArrayAdapter<String> timeAdapter = new ButtonArrayAdapter(this, 
        R.layout.timelist_item, 
        timeStrs);
    timeList.setAdapter(timeAdapter);
  }

  private String getStringForTime(Long time) {
    return new DateTime().withMillisOfDay(time.intValue()).toString("hh:mm a");
  }

    
    class ButtonArrayAdapter extends ArrayAdapter<String> implements View.OnClickListener {
      public ButtonArrayAdapter(Context context, int textViewResourceId,
          List<String> objects) {
        super(context, textViewResourceId, objects);
      }

      
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        String text = getItem(position);
        if (null == convertView) {
                convertView = inflater.inflate(R.layout.timelist_item, null);
        }
        
        Button btn = (Button) convertView.findViewById(R.id.timePickerLabel);
        btn.setOnClickListener(this);
        btn.setText(text);
        // set listener for the whole row
        convertView.setOnClickListener(new OnItemClickListener(position));
        return convertView;
      }
      
      public void onClick(View v) {
        
      }
    }

    private class OnItemClickListener implements OnClickListener {           
      private int position;
      OnItemClickListener(int position){
              this.position = position;
      }

      public void onClick(View arg0) {
        final List<Long> times = experiment.getSchedule().getTimes();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentScheduleActivity.this);
        unsetTimesViewParent();
        dialogBuilder.setView(timesScheduleLayout);
        final AlertDialog dialog = dialogBuilder.setTitle(R.string.modify_time_title).create();
        
        DateTime selectedDateTime = new DateTime().withMillisOfDay(times.get(position).intValue());
        timePicker.setCurrentHour(selectedDateTime.getHourOfDay());
        timePicker.setCurrentMinute(selectedDateTime.getMinuteOfHour());      

        dialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save_button), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            timePicker.clearFocus(); // Maybe this will save whatever value is there, 
            // so that the current hour will work when the user is editing it directly.
            long offsetMillis = timePicker.getCurrentHour() * 60 * 60 * 1000 + 
            timePicker.getCurrentMinute() * 60 * 1000;            
            times.set(position, offsetMillis);
            //timeAdapter.notifyDataSetChanged();
            setArrayAdapter(times);
          }
          
        });
        dialog.show();                   
      }               
  }
    private boolean[] createSelections() {
    selections = new boolean[7];
    int weekDaysScheduled = experiment.getSchedule().getWeekDaysScheduled();
    for (int i= 0; i < SignalSchedule.DAYS_OF_WEEK.length;i++) {      
      selections[i] = (weekDaysScheduled & SignalSchedule.DAYS_OF_WEEK[i]) == SignalSchedule.DAYS_OF_WEEK[i];
    }
    return selections;
  }

  
  protected Dialog onCreateDialog(int id, Bundle args) {
    
    AlertDialog.Builder dialogBldr = new AlertDialog.Builder( this )
    .setTitle( R.string.days_of_week_title );
    
    if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKLY)) {
      dialogBldr.setMultiChoiceItems(R.array.days_of_week, selections, new OnMultiChoiceClickListener() {      
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
          selections[which] = isChecked;
        }
      });
    } else {
      int selected = 0;
      for (int i=0; i < selections.length; i++) {
        if (selections[i]) {
          selected = i;
          break;
        }
      }
      dialogBldr.setSingleChoiceItems(R.array.days_of_week, selected, new DialogInterface.OnClickListener() {        
        public void onClick(DialogInterface dialog, int which) {
          for(int i =0; i < selections.length; i++) {
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
        experiment.getSchedule().setWeekDaysScheduled(selected);
      }
    });
    return dialogBldr.create();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    return super.onCreateDialog(id);
  }

  private void setupSaveButton() {
    Button saveScheduleButton = (Button)findViewById(R.id.SetDailyScheduleButton);
    saveScheduleButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        save();
      }
    });
  }
	
    private void saveExperimentRegistration() {
      if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKDAY) ||
          experiment.getSchedule().getScheduleType().equals(SignalSchedule.DAILY)) {

        if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.DAILY)) {
          saveRepeatRate();
        }
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.WEEKLY)) {
        saveRepeatRate();
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.MONTHLY)) {
        saveRepeatRate();
//        if (radioGroup.isSelected()) {
//          experiment.getSchedule().setByDayOfMonth(Boolean.TRUE);
//          experiment.getSchedule().setDayOfMonth(Integer.parseInt((String)domSpinner.getSelectedItem()) + 1);          
//        } else {
//          experiment.getSchedule().setByDayOfMonth(Boolean.FALSE);
//          experiment.getSchedule().setNthOfMonth(Integer.parseInt((String)nthOfMonthSpinner.getSelectedItem()));          
//        }
        
      } else if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.ESM)) {            
        AlarmStore alarmStore = new AlarmStore(this);
        alarmStore.deleteAllSignalsForSurvey(experiment.getId());
        //new AlarmStore(this).deleteSignalsForPeriod(experiment.getId(), experiment.getPeriodStart(new DateTime()).getMillis());
        experimentProviderUtil.deleteNotificationsForExperiment(experiment.getId());
      }
       
      if (experiment.getJoinDate() == null) {
        experiment.setJoinDate(new DateTime());
        experimentProviderUtil.insertFullJoinedExperiment(experiment);
        createJoinEvent();
        startService(new Intent(this, SyncService.class));
      } else {
        experimentProviderUtil.updateJoinedExperiment(experiment);
      }
    }


    private void saveRepeatRate() {
      experiment.getSchedule().setRepeatRate((repeatRate.getSelectedItem() != null) 
          ? Integer.parseInt((String) repeatRate.getSelectedItem()) 
          : 1);
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

      Output responseForInput = new Output();
      responseForInput.setAnswer("true");
      responseForInput.setName("joined");
      event.addResponse(responseForInput);
      
      Output responseForSchedule = new Output();
      responseForSchedule.setAnswer(experiment.getSchedule().toString());
      responseForSchedule.setName("schedule");
      event.addResponse(responseForSchedule);
      
      experimentProviderUtil.insertEvent(event);
    }

    private void save() {
      Validation valid = isValid();
      if (!valid.ok()) {        
        Toast.makeText(this, valid.errorMessage(), Toast.LENGTH_LONG).show();
        return;
      }
      saveExperimentRegistration();
      setResult(FindExperimentsActivity.JOINED_EXPERIMENT);
      startService(new Intent(ExperimentScheduleActivity.this, BeeperService.class));        
      finish();
    }

    private Validation isValid() {
      Validation validation = new Validation();
      if (experiment.getSchedule().getScheduleType().equals(SignalSchedule.ESM))  {
        if (experiment.getSchedule().getEsmStartHour() >= experiment.getSchedule().getEsmEndHour()) {
          validation.addMessage(getString(R.string.start_hour_must_be_before_end_hour_warning));          
        }
      }
      return validation;
    }

    private Long getHourOffsetFromPicker() {
      return new Long(new DateMidnight().toDateTime().withHourOfDay(timePicker.getCurrentHour()).withMinuteOfHour(timePicker.getCurrentMinute()).getMillisOfDay());
    }

    private String getTextFromPicker(int esmOffset) {
      return new DateMidnight().toDateTime().withMillisOfDay(esmOffset).toString(TIME_FORMAT_STRING);
    }

}
