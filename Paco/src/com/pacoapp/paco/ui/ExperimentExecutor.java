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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.apps.paco.questioncondparser.Binding;
import com.google.android.apps.paco.questioncondparser.Environment;
import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.NotificationHolder;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.triggering.AndroidEsmSignalStore;
import com.pacoapp.paco.triggering.BeeperService;
import com.pacoapp.paco.triggering.ExperimentExpirationManagerService;
import com.pacoapp.paco.triggering.NotificationCreator;
import com.pacoapp.paco.utils.IntentExtraHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import androidx.appcompat.app.ActionBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ExperimentExecutor extends AppCompatActivity implements ChangeListener, LocationListener, ExperimentLoadingActivity  {

  private static Logger Log = LoggerFactory.getLogger(ExperimentExecutor.class);

  public static final String FORM_DURATION_IN_SECONDS = "Form Duration";
  private Experiment experiment;
  private ExperimentGroup experimentGroup;
  private Long actionTriggerId;
  private Long actionId;
  private Long actionTriggerSpecId;

  private Long notificationHolderId;
  private boolean shouldExpireNotificationHolder;
  private Long scheduledTime = 0L;

  private ExperimentProviderUtil experimentProviderUtil;
  private List<InputLayout> inputs = new ArrayList<InputLayout>();
  private LayoutInflater inflater;
  private LinearLayout mainLayout;
  private OptionsMenu optionsMenu;

  private Object updateLock = new Object();

  private ArrayList<InputLayout> locationInputs;
  private Location location;


  private View buttonView;
  private Button doOnPhoneButton;
  private Button doOnWebButton;

  private List<SpeechRecognitionListener> speechRecognitionListeners = new ArrayList<SpeechRecognitionListener>();
  public static final int RESULT_SPEECH = 3;


  private LinearLayout inputsScrollPane;
  private DateTime formOpenTime;

  private Long timeoutMillis;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.debug("ExperimentExecutor onCreate");
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    // actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    experimentProviderUtil = new ExperimentProviderUtil(this);
    if (experiment == null || experimentGroup == null) {
      Log.debug("ExperimentExecutor experiment or group null. Loading from Intent");
      IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    }
    loadNotificationData();

    if (experiment == null || experimentGroup == null) {
      displayNoExperimentMessage();
    } else {

      actionBar.setTitle(experiment.getExperimentDAO().getTitle());
      if (scheduledTime == null || scheduledTime == 0l) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
      inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      optionsMenu = new OptionsMenu(this, getExperiment().getExperimentDAO().getId(), scheduledTime != null
                                                                                      && scheduledTime != 0L);

      mainLayout = (LinearLayout) inflater.inflate(R.layout.experiment_executor, null);
      setContentView(mainLayout);

      inputsScrollPane = (LinearLayout) findViewById(R.id.ScrollViewChild);
      displayExperimentGroupTitle();

      ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.experiment_web_recommended_buttons,
                                                                           mainLayout, true);
      buttonView = findViewById(R.id.ExecutorButtonLayout);
      buttonView.setVisibility(View.GONE);

      doOnPhoneButton = (Button) findViewById(R.id.DoOnPhoneButton);
      doOnPhoneButton.setVisibility(View.GONE);
      // doOnPhoneButton.setOnClickListener(new OnClickListener() {
      // public void onClick(View v) {
      // buttonView.setVisibility(View.GONE);
      // //mainLayout.removeView(buttonView);
      // scrollView.setVisibility(View.VISIBLE);
      // showForm();
      // }
      // });

      doOnWebButton = (Button) findViewById(R.id.DoOnWebButtonButton);
      doOnWebButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          deleteNotification();
          finish();
        }
      });

      // if (experimentGroup.getEndOfDayGroup()) {
      // renderWebRecommendedMessage();
      // } else {
      if (experimentGroup.getEndOfDayGroup() ||
              (experimentGroup.getCustomRendering() != null && experimentGroup.getCustomRendering())) {
        Intent customExecutorIntent = new Intent(this, ExperimentExecutorCustomRendering.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
          customExecutorIntent.putExtras(extras);
        }

        startActivity(customExecutorIntent);
        finish();
      } else {
        showForm();
      }
      // }
    }

  }

  private void renderWebRecommendedMessage() {
    final ScrollView scrollView = (ScrollView)findViewById(R.id.ScrollView01);
    scrollView.setVisibility(View.GONE);
    buttonView.setVisibility(View.VISIBLE);

  }

  private void loadNotificationData() {
    Bundle extras = getIntent().getExtras();
    NotificationHolder notificationHolder = null;
    if (extras != null) {
      notificationHolderId = extras.getLong(NotificationCreator.NOTIFICATION_ID);
      notificationHolder = experimentProviderUtil.getNotificationById(notificationHolderId);
      timeoutMillis = null;
      if (notificationHolder != null) {
        experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
        experimentGroup = getExperiment().getExperimentDAO().getGroupByName(notificationHolder.getExperimentGroupName());
        actionTriggerId = notificationHolder.getActionTriggerId();
        actionId = notificationHolder.getActionId();
        actionTriggerSpecId = notificationHolder.getActionTriggerSpecId();
        scheduledTime = notificationHolder.getAlarmTime();
        Log.info("Starting experimentExecutor from signal: " + getExperiment().getExperimentDAO().getTitle() +". alarmTime: " + new DateTime(scheduledTime).toString());
        timeoutMillis = notificationHolder.getTimeoutMillis();
      } else {
        scheduledTime = null;
      }

      if (isExpiredEsmPing(timeoutMillis)) {
        Log.debug("ExperimentExecutor loadNotificationData ping is alread expired");
        Toast.makeText(this, R.string.survey_expired, Toast.LENGTH_LONG).show();
        finish();
      }
    }
    if (notificationHolder == null) {
      lookForActiveNotificationForExperiment();
    }
  }

  /**
   * If the user is self-reporting there might still be an active notification for this experiment. If so, we should
   * add its scheduleTime into this response. There should only ever be one.
   */
  private void lookForActiveNotificationForExperiment() {
    NotificationHolder notificationHolder = null;
    if (getExperiment() == null) {
      return;
    }
    final long experimentServerId = getExperiment().getExperimentDAO().getId().longValue();
    List<NotificationHolder> notificationHolders = experimentProviderUtil.getNotificationsFor(experimentServerId, experimentGroup.getName());
    if (notificationHolders != null && !notificationHolders.isEmpty()) {
      notificationHolder = notificationHolders.get(0); // TODO can we ever have more than one for a group?
    }

    if (notificationHolder != null) {
      experiment = experimentProviderUtil.getExperimentByServerId(notificationHolder.getExperimentId());
      experimentGroup = getExperiment().getExperimentDAO().getGroupByName(notificationHolder.getExperimentGroupName());

      if (notificationHolder.isActive(new DateTime())) {
        notificationHolderId = notificationHolder.getId();
        scheduledTime = notificationHolder.getAlarmTime();
        shouldExpireNotificationHolder = true;
        Log.info("ExperimentExecutor: Self reporting but found active notification: " + getExperiment().getExperimentDAO().getTitle() +". alarmTime: " + new DateTime(scheduledTime).toString());
      } else {
        Log.debug("Timing out notification that has expired.");
        NotificationCreator.create(this).timeoutNotification(notificationHolder);
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.debug("ExperimentExecutor onResume");
    registerLocationListenerIfNecessary();
    if (mainLayout != null) {
      mainLayout.clearFocus();
      if (inputs != null && inputs.size() > 0) {
        InputLayout firstInput = inputs.get(0);
        if (firstInput.getInput().getResponseType().equals(Input2.OPEN_TEXT)) {
          firstInput.requestFocus();
        }

      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.debug("ExperimentExecutor onPause");
    for (InputLayout  layout : inputs) {
      layout.onPause();
    }
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
      if (input.getInput().getResponseType().equals(Input2.LOCATION)) {
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
      .setMessage(R.string.gps_message)
      .setCancelable(true)
      .setPositiveButton(R.string.enable_button, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          launchGpsSettings();
          dialog.dismiss();
        }
      })
      .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
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

  public void launchGpsSettings() {
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
      .setMessage(R.string.need_location)
      .setCancelable(true)
      .setPositiveButton(R.string.enable_button, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          launchGpsSettings();
          dialog.dismiss();
        }
      })
      .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
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

  private boolean isExpiredEsmPing(Long timeoutMillis) {
    return (scheduledTime != null && scheduledTime != 0L && timeoutMillis != null) &&
        (new DateTime(scheduledTime)).plus(timeoutMillis).isBefore(new DateTime());
  }

  private void showForm() {
    renderInputs();
    renderSaveButton();
    formOpenTime = DateTime.now();
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
    Log.debug("ExperimentExecutor save");
    try {
      if (notificationHolderId == null || isExpiredEsmPing(timeoutMillis)) {
        // workaround the bug with re-launching and stale scheduleTime.
        // How - if there isn't a notificationHolder waiting, then this is not a response
        // to a notification.
        scheduledTime = 0L;
      }
      Event event = EventUtil.createEvent(getExperiment(), experimentGroup.getName(),
                                          actionTriggerId, actionId, actionTriggerSpecId, scheduledTime);
      gatherResponses(event);
      addTiming(event);
      experimentProviderUtil.insertEvent(event);

      deleteNotification();

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        extras.clear();
      }
      updateAlarms();
      notifySyncService();
      showFeedback();
      finish();
    } catch (IllegalStateException ise) {
      new AlertDialog.Builder(this)
      .setIcon(R.drawable.paco64)
      .setTitle(R.string.required_answers_missing)
      .setMessage(ise.getMessage()).show();
    }
  }

  private void addTiming(Event event) {
    if (formOpenTime != null) {
      DateTime formFinishTime = DateTime.now();
      Seconds duration = Seconds.secondsBetween(formOpenTime, formFinishTime);

      Output durationResponse = new Output();
      durationResponse.setAnswer(Integer.toString(duration.getSeconds()));
      durationResponse.setName(FORM_DURATION_IN_SECONDS);
      event.addResponse(durationResponse);
    }
  }

  private void updateAlarms() {
    startService(new Intent(this, BeeperService.class));
  }

  private void deleteNotification() {
    if (notificationHolderId != null && notificationHolderId.longValue() != 0l) {
      experimentProviderUtil.deleteNotification(notificationHolderId);
    }
    if (shouldExpireNotificationHolder) {
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(new Long(notificationHolderId).intValue()); //This should cancel the notification even if it's non-dismissible
      shouldExpireNotificationHolder = false;
    }
  }

  private void notifySyncService() {
    startService(new Intent(this, SyncService.class));
  }

  public void showFeedback() {
    Intent intent = new Intent(this, FeedbackActivity.class);
    intent.putExtras(getIntent().getExtras());
    intent.putExtra(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    intent.putExtra(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, experimentGroup.getName());
    startActivity(intent);
  }

  private void gatherResponses(Event event) throws IllegalStateException {
    Environment interpreter = updateInterpreter(null);
    ExpressionEvaluator main = new ExpressionEvaluator(interpreter);
    for (InputLayout inputView : inputs) {
      Input2 input = inputView.getInput();
      try {
        if (input.getConditional() != null && input.getConditional() == true && !main.parse(input.getConditionExpression())) {
          continue;
        }
      } catch (IllegalArgumentException iae) {
        Log.error("Parsing problem: " + iae.getMessage());
        continue;
      }
      Output responseForInput = new Output();
      String answer = inputView.getValueAsString();
      if (input.getRequired() && (answer == null || answer.length() == 0 || answer.equals("-1") /*||
          (input.getResponseType().equals(Input.LIST) && answer.equals("0"))*/)) {
        throw new IllegalStateException(getString(R.string.must_answer) + input.getText());
      }
      responseForInput.setAnswer(answer);
      responseForInput.setName(input.getName());
      event.addResponse(responseForInput);
    }
  }

  private void renderInputs() {
    for (Input2 input : experimentGroup.getInputs()) {
      final InputLayout inputView = renderInput(input);
      inputs.add(inputView);
      inputsScrollPane.addView(inputView);
      inputView.addChangeListener(this);
      if (input.getResponseType().equals(Input2.OPEN_TEXT)) {
        final TextView componentWithValue = (TextView)inputView.getComponentWithValue();
        componentWithValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
        componentWithValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {

          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              // hide virtual keyboard
              InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(inputView.getComponentWithValue().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
              return true;
          }
          return false;
          }
        });
      }
    }
  }

//  private void setNextActionOnOpenTexts() {
//    int size = inputs.size() - 1;
//    for (int i = 0; i < size; i++) {
//      InputLayout inputLayout = inputs.get(i);
//      if (inputLayout.getInput().getResponseType().equals(Input.OPEN_TEXT)) {
//        EditText openText = ((EditText)inputLayout.getComponentWithValue());
//        openText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//        openText.setImeActionLabel("Next", EditorInfo.IME_ACTION_NEXT);
//      }
//    }
//
//  }

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
  private InputLayout renderInput(Input2 input) {
    return createInputViewGroup(input);
  }

  private InputLayout createInputViewGroup(Input2 input) {
    return new InputLayout(this, input);
  }

  private void displayExperimentGroupTitle() {
    final TextView groupNameTextView = (TextView)findViewById(R.id.experiment_title);
    String name = experimentGroup.getName();
    if (name == null || experiment.getExperimentDAO().getGroups().size() == 1) {
      groupNameTextView.setVisibility(View.GONE);
    } else {
      groupNameTextView.setText(name);
    }
  }

  private void displayNoExperimentMessage() {
    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mainLayout = (LinearLayout) inflater.inflate(R.layout.could_not_load_experiment, null);
    setContentView(mainLayout);

  }

  public void setExperimentGroup(ExperimentGroup group) {
    this.experimentGroup = group;

  }

  public Experiment getExperiment() {
    return experiment;
  }


  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (optionsMenu != null) {
      return optionsMenu.init(menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }

  public void stopExperiment() {
    deleteExperiment();
    finish();
  }

  public void deleteExperiment() {
    NotificationCreator nc = NotificationCreator.create(this);
    nc.timeoutNotificationsForExperiment(experiment.getExperimentDAO().getId());

    createStopEvent(experiment);

    experimentProviderUtil.deleteExperiment(experiment.getId());
    if (ExperimentHelper.shouldWatchProcesses(experiment.getExperimentDAO())) {
      BroadcastTriggerReceiver.initPollingAndLoggingPreference(this);
    }

    new AndroidEsmSignalStore(this).deleteAllSignalsForSurvey(experiment.getExperimentDAO().getId());

    startService(new Intent(this, BeeperService.class));
    startService(new Intent(this, ExperimentExpirationManagerService.class));
  }

  /**
   * Creates a pacot for stopping an experiment
   *
   * @param experiment
   */
  private void createStopEvent(Experiment experiment) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getExperimentDAO().getId());
    event.setExperimentName(experiment.getExperimentDAO().getTitle());
    event.setExperimentVersion(experiment.getExperimentDAO().getVersion());
    event.setResponseTime(new DateTime());

    Output responseForInput = new Output();
    responseForInput.setAnswer("false");
    responseForInput.setName("joined");
    event.addResponse(responseForInput);

    experimentProviderUtil.insertEvent(event);
    startService(new Intent(this, SyncService.class));
  }




  public void onChange(InputLayout input) {
    System.out.println("onChange outside sync Thread: " + Thread.currentThread().getName());
    //synchronized (updateLock) {
      System.out.println("onChange inside sync Thread: " + Thread.currentThread().getName());
      Environment interpreter = updateInterpreter(input);
      ExpressionEvaluator main = new ExpressionEvaluator(interpreter);
      for (InputLayout inputLayout : inputs) {
        inputLayout.checkConditionalExpression(main);
      }
    //
    //
    //
    //
    //
    // }
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_SPEECH) {
      if (resultCode == RESULT_OK) {
        handleSpeechRecognitionActivityResult(resultCode, data);
      } else {
        speechRecognitionListeners.clear();
      }
    } else if (requestCode >= InputLayout.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) { // camera picture
      for (InputLayout inputLayout : inputs) {
        inputLayout.cameraPictureTaken(requestCode);
      }
    } else if (resultCode == RESULT_OK) {   //gallery picture
      Uri selectedImage = data.getData();
      String[] filePathColumn = { MediaStore.Images.Media.DATA };

      try {
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        for (InputLayout inputLayout : inputs) {
          inputLayout.galleryPicturePicked(filePath, requestCode);
        }
      } catch (Exception e) {
        Log.info("Exception in gallery picking: " + e.getMessage());
        e.printStackTrace();
      }

    }
  }

  private void handleSpeechRecognitionActivityResult(int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK && null != data) {
      ArrayList<String> guesses = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      notifySpeechRecognitionListeners(guesses);
    }
  }

  private void notifySpeechRecognitionListeners(List<String> guesses) {
    List<SpeechRecognitionListener> copyOfSpeechListeners = new ArrayList<SpeechRecognitionListener>(speechRecognitionListeners);
    for (SpeechRecognitionListener listener : copyOfSpeechListeners) {
      listener.speechRetrieved(guesses);
    }
  }

  public void removeSpeechRecognitionListener(SpeechRecognitionListener listener) {
    speechRecognitionListeners.remove(listener);
  }

  public void startSpeechRecognition(SpeechRecognitionListener listener) {
    //speechRecognitionListeners.clear(); // just in case they canceled the last recognition (android gives us no feedback)
    speechRecognitionListeners.add(listener);
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault().getDisplayName());

    try {
      startActivityForResult(intent, RESULT_SPEECH);
    } catch (ActivityNotFoundException a) {
      Toast t = Toast.makeText(getApplicationContext(), R.string.oops_your_device_doesn_t_support_speech_to_text, Toast.LENGTH_SHORT);
      t.show();
    }
  }

  private Map<Integer, InputLayout> recordingInputLayouts = Maps.newHashMap();

  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode >= 10 && requestCode <= 20) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        InputLayout recordingInputLayout = recordingInputLayouts.get(requestCode);
        recordingInputLayout.startRecording();
      } else {
        //User denied Permission.
      }
    }
  }

  public void addAudioRecordingPermissionRequester(int i, InputLayout inputLayout) {
    recordingInputLayouts.put(i, inputLayout);
  }
}
