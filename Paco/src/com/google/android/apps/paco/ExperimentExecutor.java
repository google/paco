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
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.paco.questioncondparser.Binding;
import com.google.android.apps.paco.questioncondparser.Environment;
import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;

public class ExperimentExecutor extends Activity implements ChangeListener, LocationListener  {

  private Experiment experiment;
  private ExperimentProviderUtil experimentProviderUtil;
  private List<InputLayout> inputs = new ArrayList<InputLayout>();
  private LayoutInflater inflater;
  private LinearLayout mainLayout;
  private Button refreshButton;
  private OptionsMenu optionsMenu;
  private Long scheduledTime = 0L;
  private LinearLayout inputsScrollPane;
  private Object updateLock = new Object();
  private UserPreferences userPrefs;
  private ProgressDialog p;
  
  private ArrayList<InputLayout> locationInputs;
  private Location location;
  
  private Long notificationHolderId;
  private boolean shouldExpireNotificationHolder;

  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    userPrefs = new UserPreferences(this);
    experiment = getExperimentFromIntent();
    if (experiment == null) {
      displayNoExperimentMessage();
    } else {       
      getSignallingData();

      inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      optionsMenu = new OptionsMenu(this, getIntent().getData(), scheduledTime != null && scheduledTime != 0L);
      
      experimentProviderUtil.loadInputsForExperiment(experiment);
      experimentProviderUtil.loadFeedbackForExperiment(experiment);      

      mainLayout = (LinearLayout) inflater.inflate(R.layout.experiment_executor, null);                  
      setContentView(mainLayout);
      
      inputsScrollPane = (LinearLayout)findViewById(R.id.ScrollViewChild);
      displayExperimentTitle();

      refreshButton = (Button)findViewById(R.id.RefreshQuestionsButton);
      if (!experiment.hasFreshInputs()) {
        refreshButton.setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.SaveResponseButton)).setVisibility(View.GONE);
        refreshButton.setOnClickListener(new OnClickListener() {          
          public void onClick(View v) {
            refreshExperiment();
          }
        });
      } else {
        showForm();
      }
    }
  }

  private void getSignallingData() {
    Bundle extras = getIntent().getExtras();
    if (extras != null) {        
      notificationHolderId = extras.getLong(NotificationCreator.NOTIFICATION_ID);
      NotificationHolder notificationHolder = experimentProviderUtil.getNotificationById(notificationHolderId);
      if (notificationHolder != null) {
        scheduledTime = notificationHolder.getAlarmTime();
        Log.i(PacoConstants.TAG, "Starting experimentExecutor from signal: " + experiment.getTitle() +". alarmTime: " + new DateTime(scheduledTime).toString());
      } else {
        scheduledTime = null;          
      }
      
      if (isExpiredEsmPing()) {
        Toast.makeText(this, "This survey request has expired. No need to enter a response", Toast.LENGTH_LONG).show();
        finish();
      }
    } 
    if (notificationHolderId == null) {
      lookForActiveNotificationForExperiment();
    }
  }

  /**
   * If the user is self-reporting there might still be an active notification for this experiment. If so, we should
   * add its scheduleTime into this response. There should only ever be one.
   */
  private void lookForActiveNotificationForExperiment() {
    NotificationHolder notificationHolder = experimentProviderUtil.getNotificationFor(experiment.getId().longValue());
    if (notificationHolder != null) {
      if (notificationHolder.isActive(new DateTime())) {
        notificationHolderId = notificationHolder.getId();
        scheduledTime = notificationHolder.getAlarmTime();
        shouldExpireNotificationHolder = true;
        Log.i(PacoConstants.TAG, "ExperimentExecutor: Self report, but found signal still active : " + experiment.getTitle() +". alarmTime: " + new DateTime(scheduledTime).toString());
      } else {
        NotificationCreator.create(this).timeoutNotification(notificationHolder);
      }
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    registerLocationListenerIfNecessary();
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterLocationListenerIfNecessary();
  }
 
  private void unregisterLocationListenerIfNecessary() {
    if (locationInputs.size() > 0) {
      locationInputs.clear();
      unregisterLocationListener();
    }
  }

  private void registerLocationListenerIfNecessary() {
    locationInputs = new ArrayList<InputLayout>();
    for (InputLayout input : inputs) {
      if (input.getInput().getResponseType().equals(Input.LOCATION)) {
        locationInputs.add(input);
      }
    }
    if (locationInputs.size() > 0) {
      registerLocationListener();
    }
  }
  
  // Location
  
  private void registerLocationListener() {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (!lm.isProviderEnabled("gps")) {
      new AlertDialog.Builder(this)
        .setMessage("Use GPS for improved location (outside only)? ")
        .setCancelable(true)
        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {          
          public void onClick(DialogInterface dialog, int which) {
            launchGpsSettings();
            dialog.dismiss();
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {          
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        })
        .create()
        .show();
    }
    if (lm != null) {
      getBestProvider(lm);
    }
  }

  void launchGpsSettings() {
    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
  }
  

  private void getBestProvider(LocationManager lm) {
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    criteria.setAccuracy(Criteria.ACCURACY_FINE);

    String bestProvider = lm.getBestProvider(criteria, true);
    if (bestProvider != null) {
      lm.requestLocationUpdates(bestProvider, 0, 0, this);
      location = lm.getLastKnownLocation(bestProvider);
      for (InputLayout input : locationInputs) {
        input.setLocation(location);  
      }
      
    } else {
      new AlertDialog.Builder(this)
      .setMessage("You must enable some form of Location Service if you want to include location data. ")
      .setCancelable(true)
      .setPositiveButton("Enable", new DialogInterface.OnClickListener() {          
        public void onClick(DialogInterface dialog, int which) {
          launchGpsSettings();
          dialog.dismiss();
        }
      })
      .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {          
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
        }
      })
      .create()
      .show();
    }
  }

  public void onLocationChanged(Location location) {
    this.location = location;
    for (InputLayout input : locationInputs) {
      input.setLocation(location);  
    }           
  }

  public void onProviderDisabled(String provider) {
    unregisterLocationListener();
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    getBestProvider(lm);
  }

  public void onProviderEnabled(String provider) {
    unregisterLocationListener();
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    getBestProvider(lm);    
  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
    unregisterLocationListener();
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    getBestProvider(lm);
  }


  private void unregisterLocationListener() {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (lm != null) {
      lm.removeUpdates(this);
    }
  }  
  //End Location

  private boolean isExpiredEsmPing() {
    return (scheduledTime != null && scheduledTime != 0L) && 
      (new DateTime(scheduledTime)).plus(experiment.getExpirationTimeInMillis()).isBefore(new DateTime());
  }

  private void showForm() {
    renderInputs();
    renderSaveButton();
   
    
  }

  private void renderSaveButton() {
    View saveButtonView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.experiment_save_buttons, 
                                                                                              inputsScrollPane, 
                                                                                              true);
//    inputsScrollPane.removeView(saveButtonView);
    //LinearLayout saveButtonLayout = (LinearLayout)findViewById(R.id.ExecutorButtonLayout);
    Button saveButton = (Button)findViewById(R.id.SaveResponseButton);
    //saveButtonLayout.removeView(saveButton);
//    inputsScrollPane.addView(saveButtonView);
    saveButton.setOnClickListener(new OnClickListener() {        
      public void onClick(View v) {
        save();
      }
    });   

  }

  private void save() {
    try {
      if (notificationHolderId == null) {
        // workaround the bug with re-launching and stale scheduleTime.
        // How - if there isn't a notificationHolder waiting, then this is not a response
        // to a notification.
        scheduledTime = 0L;
      }
      Event event = createEvent();
      gatherResponses(event);
      experimentProviderUtil.insertEvent(event);
      
      
      if (notificationHolderId != null) {
        experimentProviderUtil.deleteNotification(notificationHolderId);
      }
      if (shouldExpireNotificationHolder) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(new Long(notificationHolderId).intValue());
        shouldExpireNotificationHolder = false;
      }

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        extras.clear();
      }

      notifySyncService();
      showFeedback();
      // TODO(bobevans): trying to make it so that reopening from the home longpress doesn't
      // cause the same scheduleTime for a previously signalled experiment.
      finish();
    } catch (IllegalStateException ise) {
      new AlertDialog.Builder(this)
        .setIcon(R.drawable.paco64)
        .setTitle("Required Answers Missing")
        .setMessage(ise.getMessage()).show();
    }
  }

  private void notifySyncService() {
    startService(new Intent(this, SyncService.class));
  }

  void showFeedback() {
    Intent intent = new Intent(this, FeedbackActivity.class);
    intent.setData(getIntent().getData());
    startActivity(intent);
  }

  private void gatherResponses(Event event) throws IllegalStateException {
    Environment interpreter = updateInterpreter(null);
    ExpressionEvaluator main = new ExpressionEvaluator(interpreter);
    for (InputLayout inputView : inputs) {
      Input input = inputView.getInput();
      try {
        if (input.getConditional() != null && input.getConditional() == true && !main.parse(input.getConditionExpression())) {
          continue;
        }
      } catch (IllegalArgumentException iae) {
        Log.e(PacoConstants.TAG, "Parsing problem: " + iae.getMessage());
        continue;
      }
      Output responseForInput = new Output();
      String answer = inputView.getValueAsString();
      if (input.isMandatory() && (answer == null || answer.length() == 0 || answer.equals("-1") /*|| 
          (input.getResponseType().equals(Input.LIST) && answer.equals("0"))*/)) {
        throw new IllegalStateException("Must answer: " + input.getText());
      }
      responseForInput.setAnswer(answer);
      responseForInput.setName(input.getName());
      responseForInput.setInputServerId(input.getServerId());
      event.addResponse(responseForInput);  
    }
  }

  private Event createEvent() {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    if (scheduledTime != null && scheduledTime != 0L) {
      event.setScheduledTime(new DateTime(scheduledTime));
    }
    event.setResponseTime(new DateTime());
    return event;
  }

  private void renderInputs() {
    if (experiment.isQuestionsChange()) {
      DateMidnight dateMidnight = new DateMidnight();
      boolean hadQuestionForToday = false; 
      for (Input input : experiment.getInputs()) {
        if (dateMidnight.isEqual(new DateMidnight(input.getScheduleDate().getTime()))) {
          hadQuestionForToday = true;
          InputLayout inputView = renderInput(input);
          inputs.add(inputView);
          inputsScrollPane.addView(inputView);
        }
      }
    } else {
      for (Input input : experiment.getInputs()) {
        InputLayout inputView = renderInput(input);        
        inputs.add(inputView);
        inputsScrollPane.addView(inputView);
        inputView.addChangeListener(this);
      }
      //setNextActionOnOpenTexts();
    }
  }

  private void setNextActionOnOpenTexts() {
    int size = inputs.size() - 1;
    for (int i = 0; i < size; i++) {
      InputLayout inputLayout = inputs.get(i);
      if (inputLayout.getInput().getResponseType().equals(Input.OPEN_TEXT)) {
        EditText openText = ((EditText)inputLayout.getComponentWithValue());
        openText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        openText.setImeActionLabel("Next", EditorInfo.IME_ACTION_NEXT);
      }
    }
    
  }

  /**
   * Note this is a simple version geared towards questions
   * with text and then some input type for the response.
   * In the future, it might make sense to create an InputView class
   * that has control of displaying the prompt (Question text),
   * and the input type. It just has an api that allows you to 
   * retrieve the value that was selected. This could make it
   * easy to create interesting media inputs, or sensor inputs with 
   * no display.
   * 
   * @param input
   * @return
   */
  private InputLayout renderInput(Input input) {
    return createInputViewGroup(input);
  }

  private InputLayout createInputViewGroup(Input input) {
    return new InputLayout(this, input);
  }

  private void displayExperimentTitle() {
    ((TextView)findViewById(R.id.experiment_title)).setText(experiment.getTitle());
  }

  private void displayNoExperimentMessage() {
  }

  private Experiment getExperimentFromIntent() {
    Uri uri = getIntent().getData();    
    if (uri == null) {
      return null;
    }
    return experimentProviderUtil.getExperiment(uri);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return optionsMenu.init(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }

  public void stopExperiment() {
    experimentProviderUtil.deleteFullExperiment(getIntent().getData());
    startService(new Intent(this, BeeperService.class));  
    finish();
  }

  public void onChange(InputLayout input) {
    synchronized (updateLock) {
      Environment interpreter = updateInterpreter(input);
      ExpressionEvaluator main = new ExpressionEvaluator(interpreter);
      for (InputLayout inputLayout : inputs) {
        inputLayout.checkConditionalExpression(main);
      }
    }
  }

  private Environment updateInterpreter(InputLayout input) {
    Environment interpreter = null;
    // todo make interpreter a field to optimize updates.
    if (interpreter == null) {
      interpreter = new Environment();
    
      for (InputLayout inputLayout : inputs) {
        interpreter.addInput(createBindingFromInputView(inputLayout));
      }
    } 
//    else {
//      if (input != null) {
//        interpreter.addInput(createBindingFromInputView(input));
//      }
//    }
    return interpreter;
  }

  private Binding createBindingFromInputView(InputLayout inputLayout) {
    String inputName = inputLayout.getInputName();
    Class responseType = inputLayout.getResponseType();
    Object value = inputLayout.getValue();
    Binding binding = new Binding(inputName, responseType, value);
    return binding;
  }

  public void refreshExperiment() {
    Runnable runnable = new Runnable() {

      public void run() {
        experimentProviderUtil.loadInputsForExperiment(experiment);
        experimentProviderUtil.loadFeedbackForExperiment(experiment);      

        if (experiment.hasFreshInputs() && experiment.isQuestionsChange()) {
          Toast.makeText(ExperimentExecutor.this, "I found new questions.", Toast.LENGTH_SHORT).show();
          refreshButton.setVisibility(View.GONE);
          ((Button)findViewById(R.id.SaveResponseButton)).setVisibility(View.VISIBLE);
          // TODO - consolidate this logic into one method, shared with onCreate.
          experiment = getExperimentFromIntent();
          experimentProviderUtil.loadInputsForExperiment(experiment);
          experimentProviderUtil.loadFeedbackForExperiment(experiment);      

          showForm();
        } else if (!experiment.hasFreshInputs() && experiment.isQuestionsChange()) {
          Toast.makeText(ExperimentExecutor.this, "I did not find new questions on the server.", Toast.LENGTH_SHORT);
        } else {
          Toast.makeText(ExperimentExecutor.this, "Experiment may be invalid now. Closing screen. Please reopen.", Toast.LENGTH_LONG);
          finish();
        }
        
      }
    };
    new DownloadExperimentsTask(this, null, userPrefs, experimentProviderUtil, runnable).execute();
  }





}
