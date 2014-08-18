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
import java.util.Locale;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.paco.questioncondparser.Binding;
import com.google.android.apps.paco.questioncondparser.Environment;
import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.pacoapp.paco.R;

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
  private View buttonView;
  private Button doOnPhoneButton;
  private Button doOnWebButton;
  private TextView warningText;

  private List<SpeechRecognitionListener> speechRecognitionListeners = new ArrayList<SpeechRecognitionListener>();
  public static final int RESULT_SPEECH = 3;

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

      mainLayout = (LinearLayout) inflater.inflate(R.layout.experiment_executor, null);
      setContentView(mainLayout);

      inputsScrollPane = (LinearLayout)findViewById(R.id.ScrollViewChild);
      displayExperimentTitle();

      ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.experiment_web_recommended_buttons,
                                                                           mainLayout, true);
      buttonView = findViewById(R.id.ExecutorButtonLayout);
      buttonView.setVisibility(View.GONE);

      warningText = (TextView) findViewById(R.id.webRecommendedWarningText);
      warningText.setText(warningText.getText() + getString(R.string.use_browser) + "http://"
          + getString(R.string.about_weburl));

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

      if (experiment.isWebRecommended()) {
        renderWebRecommendedMessage();
      } else {
        if (experiment.isCustomRendering() != null && experiment.isCustomRendering()) {
          Intent customExecutorIntent = new Intent(this, ExperimentExecutorCustomRendering.class);
          customExecutorIntent.setData(getIntent().getData());

          Bundle extras = getIntent().getExtras();
          if (extras != null) {
            customExecutorIntent.putExtra(Experiment.SCHEDULED_TIME, scheduledTime);
            customExecutorIntent.putExtra(NotificationCreator.NOTIFICATION_ID, extras.getLong(NotificationCreator.NOTIFICATION_ID));
          }

          startActivity(customExecutorIntent);
          finish();
        } else {
          showForm();
        }
      }
    }


  }

  private void renderWebRecommendedMessage() {
    final ScrollView scrollView = (ScrollView)findViewById(R.id.ScrollView01);
    scrollView.setVisibility(View.GONE);
    buttonView.setVisibility(View.VISIBLE);

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
        Toast.makeText(this, R.string.survey_expired, Toast.LENGTH_LONG).show();
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
      Event event = createEvent(experiment, scheduledTime);
      gatherResponses(event);
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

  private void updateAlarms() {
    startService(new Intent(this, BeeperService.class));
  }

  private void deleteNotification() {
    if (notificationHolderId != null) {
      experimentProviderUtil.deleteNotification(notificationHolderId);
    }
    if (shouldExpireNotificationHolder) {
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(new Long(notificationHolderId).intValue());
      shouldExpireNotificationHolder = false;
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
        throw new IllegalStateException(getString(R.string.must_answer) + input.getText());
      }
      responseForInput.setAnswer(answer);
      responseForInput.setName(input.getName());
      responseForInput.setInputServerId(input.getServerId());
      event.addResponse(responseForInput);
    }
  }

  public static Event createEvent(Experiment experiment, Long scheduledTimeLong) {
    Event event = new Event();
    event.setExperimentId(experiment.getId());
    event.setServerExperimentId(experiment.getServerId());
    event.setExperimentName(experiment.getTitle());
    if (scheduledTimeLong != null && scheduledTimeLong != 0L) {
      event.setScheduledTime(new DateTime(scheduledTimeLong));
    }
    event.setExperimentVersion(experiment.getVersion());
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
    updateAlarms();
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RESULT_SPEECH) {
      if (resultCode == RESULT_OK) {
        handleSpeechRecognitionActivityResult(resultCode, data);
      } else {
        speechRecognitionListeners.clear();
      }
    } else if (requestCode >= InputLayout.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
      for (InputLayout inputLayout : inputs) {
        inputLayout.cameraPictureTaken(requestCode);
      }
    } else if (resultCode == RESULT_OK) {
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
        Log.i(PacoConstants.TAG, "Exception in gallery picking: " + e.getMessage());
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

}
