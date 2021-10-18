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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import com.pacoapp.paco.js.bridge.*;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.apps.paco.questioncondparser.Binding;
import com.google.android.apps.paco.questioncondparser.ExpressionEvaluator;
import com.google.common.base.Strings;
import com.pacoapp.paco.R;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.NotificationHolder;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.net.SyncService;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.shared.model.FeedbackDAO;
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
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
import androidx.core.app.ActivityCompat;

import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class ExperimentExecutorCustomRendering extends AppCompatActivity implements ChangeListener, LocationListener, ExperimentLoadingActivity {

  public static final int LOCATION_REQUEST_CODE = 1010101;
  private static Logger Log = LoggerFactory.getLogger(ExperimentExecutorCustomRendering.class);

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

  public static final int RESULT_SPEECH = 3001;
  public static final int RESULT_CAMERA = 3002;
  public static final int RESULT_GALLERY = 3003;

  private WebView webView;
  private Environment env;


  private final int IMAGE_MAX_SIZE = 600;
  private File photoFile;

  boolean showDialog = true;

  private String notificationMessage;
  private String notificationSource;

  private DateTime formOpenTime;

  private Long timeoutMillis;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.debug("ExperimentExecutorCustomRendering onCreate");
    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));

    experimentProviderUtil = new ExperimentProviderUtil(this);
    if (experiment == null || experimentGroup == null) {
      IntentExtraHelper.loadExperimentInfoFromIntent(this, getIntent(), experimentProviderUtil);
    }
    loadNotificationData();
    if (experiment == null || experimentGroup == null) {
      displayNoExperimentMessage();
    } else {

      actionBar.setTitle(experiment.getExperimentDAO().getTitle());

      inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      optionsMenu = new OptionsMenu(this, experiment.getExperimentDAO().getId(), scheduledTime != null && scheduledTime != 0L);


      mainLayout = (LinearLayout) inflater.inflate(R.layout.experiment_executor_custom_rendering, null);
      setContentView(mainLayout);

      // webRecommended layout pieces
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

      showForm(savedInstanceState);
    }
  }

  private void renderWebRecommendedMessage() {
    final ScrollView scrollView = (ScrollView) findViewById(R.id.ScrollView01);
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
        experimentGroup = experiment.getExperimentDAO().getGroupByName(notificationHolder.getExperimentGroupName());
        actionTriggerId = notificationHolder.getActionTriggerId();
        actionId = notificationHolder.getActionId();
        actionTriggerSpecId = notificationHolder.getActionTriggerSpecId();
        scheduledTime = notificationHolder.getAlarmTime();
        Log.info("Starting experimentExecutor from signal: " + experiment.getExperimentDAO().getTitle() + ". alarmTime: " + new DateTime(scheduledTime).toString());
        timeoutMillis = notificationHolder.getTimeoutMillis();
      } else {
        scheduledTime = null;
      }

      if (isExpiredEsmPing(timeoutMillis)) {
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
    final long experimentId = getExperiment().getExperimentDAO().getId().longValue();
    List<NotificationHolder> notificationHolders = experimentProviderUtil.getNotificationsFor(experimentId, experimentGroup.getName());
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
        Log.info("ExperimentExecutor: Self report, but found signal still active : " + experiment.getExperimentDAO().getTitle() + ". alarmTime: " + new DateTime(scheduledTime).toString());
      } else {
        NotificationCreator.create(this).timeoutNotification(notificationHolder);
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.debug("ExperimentExecutorCustomRendering onResume");
    registerLocationListenerIfNecessary();
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.debug("ExperimentExecutorCustomRendering onPause");
    for (InputLayout inputLayout : inputs) {
      inputLayout.onPause();
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
// TODO remove redundant permission check
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding

      ActivityCompat
              .requestPermissions(
                      ExperimentExecutorCustomRendering.this,
                      new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                      LOCATION_REQUEST_CODE);

    } else {
      registerLocationManagerOncePermitted();
    }
  }

  private void registerLocationManagerOncePermitted() {
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

  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    super.onRequestPermissionsResult(requestCode,
                    permissions,
                    grantResults);
    if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      registerLocationManagerOncePermitted();
    }

  }

  void launchGpsSettings() {
    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
  }


  private void getBestProvider(LocationManager lm) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding

      ActivityCompat
              .requestPermissions(
                      ExperimentExecutorCustomRendering.this,
                      new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                      LOCATION_REQUEST_CODE);

    } else {
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

  private void showForm(Bundle savedInstanceState) {
    renderWebView(savedInstanceState);
  }

  private void renderWebView(Bundle savedInstanceState) {
    //webView = (WebView)findViewById(R.id.experimentExecutorView);
    webView = new CustomInputWebview(this);
    mainLayout.addView(webView);
    webView.getSettings().setJavaScriptEnabled(true);

    injectObjectsIntoJavascriptEnvironment();
    setWebChromeClientThatHandlesAlertsAsDialogs();
    WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts();
    webView.setWebViewClient(webViewClient);
    loadCustomRendererIntoWebView();
    if (savedInstanceState != null){
      webView.loadUrl((String) savedInstanceState.get("url"));
      String showDialogString =  (String) savedInstanceState.get("showDialog");
      if (showDialogString == null || showDialogString.equals("false")){
        showDialog = false;
      }else{
        showDialog = true;
      }
    }
    formOpenTime = DateTime.now();
  }

private void injectObjectsIntoJavascriptEnvironment() {
  final Map<String,String> map = new HashMap<String, String>();
  map.put("test", "false");

  map.put("title", experiment.getExperimentDAO().getTitle());
  if (scheduledTime != null) {
    map.put("scheduledTime", Long.toString(scheduledTime));
  }
  map.put("notificationLabel", notificationMessage);
  map.put("notificationSource", notificationSource);
  map.put("experimentGroupName", experimentGroup.getName());
  if (actionTriggerId != null) {
    map.put("actionTriggerId", Long.toString(actionTriggerId));
  }
  if (actionId != null) {
    map.put("actionId", Long.toString(actionId));
  }
  if (actionTriggerSpecId != null) {
    map.put("actionTriggerSpecId", Long.toString(actionTriggerSpecId));
  }


  env = new Environment(map);
  webView.addJavascriptInterface(env, "env");

  String text = experimentGroup.getCustomRenderingCode();

  webView.addJavascriptInterface(new JavascriptStringHolder(text), "additions");

  webView.addJavascriptInterface(new JavascriptExperimentLoader(this, experimentProviderUtil, experiment.getExperimentDAO(), experiment, experimentGroup), "experimentLoader");

  JavascriptEventLoader javascriptEventLoader = new JavascriptEventLoader(experimentProviderUtil, experiment, experiment.getExperimentDAO(), experimentGroup);
  webView.addJavascriptInterface(javascriptEventLoader, "db");
  // deprecated name - use "db" in all new experiments
  webView.addJavascriptInterface(javascriptEventLoader, "eventLoader");

  webView.addJavascriptInterface(new JavascriptCalendarManager(this), "calendar");
  webView.addJavascriptInterface(new JavascriptEmail(this), "email");
  webView.addJavascriptInterface(new JavascriptNotificationService(this, experiment.getExperimentDAO(), experimentGroup, actionTriggerSpecId, actionTriggerId, actionId), "notificationService");
  webView.addJavascriptInterface(new JavascriptPhotoService(this), "photoService");
  webView.addJavascriptInterface(new JavascriptExecutorListener(experiment), "executor");
  webView.addJavascriptInterface(new JavascriptPackageManager(this), "packageManager");
  webView.addJavascriptInterface(new JavascriptSensorManager(this), "sensors");
  webView.addJavascriptInterface(new JavascriptStringResources(getApplicationContext()), "strings");

}

private void loadCustomRendererIntoWebView() {
  if (experimentGroup.getEndOfDayGroup()) {
    loadEndOfDayUrl();
  } else if (!experimentGroup.getEndOfDayGroup()) {
    loadUrl();
  } /*else {
    loadData();
  }*/
}

private void loadEndOfDayUrl() {
  webView.loadUrl("file:///android_asset/eod_skeleton.html");

}

public void loadUrl() {
  webView.loadUrl("file:///android_asset/custom_skeleton.html");
  // polymer experimentation
  // webView.loadUrl("file:///android_asset/skeleton2.html");
  //webView.loadUrl("file:///android_asset/polymer.html");
  //webView.loadUrl("file:///android_asset/empty.html");
  //webView.loadUrl("file:///android_asset/vulcanized-simple.html");
  //webView.loadUrl("file:///android_asset/vulcanized-paper.html");
}

public void loadData() {
  // data-based loading of webview
  BufferedReader r = null;
  try {
    StringBuffer data = new StringBuffer();
    InputStream in = this.getClassLoader().getResourceAsStream("file:///android_asset/polymer.html");
    r = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = r.readLine()) != null) {
      data.append(line);
    }
    webView.loadData(data.toString(), "text/html", "UTF-8");
  } catch (IOException e) {
    e.printStackTrace();
  } finally {
    if (r != null) {
      try {
        r.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

private WebViewClient createWebViewClientThatHandlesFileLinksForCharts() {
  WebViewClient webViewClient = new WebViewClient() {

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      Uri uri = Uri.parse(url);
      if (uri.getScheme().startsWith("http")) {
        return true; // throw away http requests - we don't want 3rd party javascript sending url requests due to security issues.
      }

      String inputName = uri.getQueryParameter("inputName");
      if (inputName == null) {
        return true;
      }
      JSONArray results = new JSONArray();
      for (Event event : experiment.getEvents()) {
        JSONArray eventJson = new JSONArray();
        DateTime responseTime = event.getResponseTime();
        if (responseTime == null) {
          continue; // missed signal;
        }
        eventJson.put(responseTime.getMillis());
        // in this case we are looking for one input from the responses that we are charting.
        for (Output response : event.getResponses()) {
          if (response.getAnswer().equals(inputName)) {
            Input2 input = ExperimentHelper.getInputWithName(experiment.getExperimentDAO(),inputName,null);
            if (input.isNumeric()) {
              eventJson.put(response.getDisplayOfAnswer(input));
              results.put(eventJson);
              continue;
            }
          }
        }

      }
      env.put("data", results.toString());
      env.put("inputName", inputName);

      view.loadUrl(stripQuery(url));
      return true;
    }

  };
  return webViewClient;
}

public static String stripQuery(String url) {
  String urlWithoutQuery = url;
  int indexOfQuery = url.indexOf('?');
  if (indexOfQuery != -1) {
    urlWithoutQuery = urlWithoutQuery.substring(0, indexOfQuery);
  }
  return urlWithoutQuery;
}

private void setWebChromeClientThatHandlesAlertsAsDialogs() {
  webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

      new AlertDialog.Builder(view.getContext())
      .setMessage(message)
      .setTitle("Alert!")
      .setCancelable(true)
      .setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      })
      .create()
      .show();
      result.confirm();
      return true;
    }

    public boolean onJsConfirm (WebView view, String url, String message, final JsResult result){

        if (showDialog == false){
          result.confirm();
          return true;
        } else {
          new AlertDialog.Builder(view.getContext())
          .setMessage(message)
          .setTitle("Confirm")
          .setCancelable(true)
          .setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              showDialog = false;
              dialog.dismiss();
              result.confirm();
            }
          })
          .setNegativeButton(R.string.cancel_button, new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              result.cancel();
            }
          })
          .create()
          .show();
          return true;
        }

     // return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.debug(message + " -- From line "
                             + lineNumber + " of "
                             + sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      Log.debug( consoleMessage.message() + " -- From line "
          + consoleMessage.lineNumber() + " of "
          + consoleMessage.sourceId() );
      return true;
    }

  });
}

public static String convertExperimentResultsToJsonString(final Experiment experiment) {
  List<Event> events = experiment.getEvents();
  return convertEventsToJsonString(experiment, events);
}

public static String convertLastEventToJsonString(final Experiment experiment) {
  List<Event> events = experiment.getEvents();
  if (events.isEmpty()) {
    return "[]";
  }
  return convertEventsToJsonString(experiment, events.subList(0,1));
}

private static String convertEventsToJsonString(final Experiment experiment,
                                                List<Event> events) {
  return FeedbackActivity.convertEventsToJsonString(events);
}

static String getTextOfInputForOutput(ExperimentGroup experiment, Output output) {
  for (Input2 input : experiment.getInputs()) {

    if (input.getName().equals(output.getName())) {
      if (!input.isInvisible()) {
        return input.getText();
      } else {
        return input.getResponseType();
      }
    }
  }
  return output.getName();
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
        webView.goBack();
        return true;
    }
    return super.onKeyDown(keyCode, event);
}



/// saving and external service callouts
  private void save() {
    Log.debug("ExperimentExecutorCustomRendering save");
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
      durationResponse.setName(ExperimentExecutor.FORM_DURATION_IN_SECONDS);
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
      notificationManager.cancel(new Long(notificationHolderId).intValue());  //This should cancel the notification even if it's non-dismissible
      shouldExpireNotificationHolder = false;
    }
  }

  private void notifySyncService() {
    startService(new Intent(this, SyncService.class));
  }

  void showFeedback() {
    Intent intent = new Intent(this, FeedbackActivity.class);
    final Bundle extras = getIntent().getExtras();

    if (!extras.containsKey(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY)) {
      extras.putLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY, experiment.getExperimentDAO().getId());
    }
    if (!extras.containsKey(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY)) {
      extras.putString(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY, experimentGroup.getName());
    }
    intent.putExtras(extras);
    startActivity(intent);
  }

  private void gatherResponses(Event event) throws IllegalStateException {
    com.google.android.apps.paco.questioncondparser.Environment interpreter = updateInterpreter(null);
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

  private void displayNoExperimentMessage() {
    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mainLayout = (LinearLayout) inflater.inflate(R.layout.could_not_load_experiment, null);
    setContentView(mainLayout);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return optionsMenu.init(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return optionsMenu.onOptionsItemSelected(item);
  }

  public void onChange(InputLayout input) {
    synchronized (updateLock) {
      com.google.android.apps.paco.questioncondparser.Environment interpreter = updateInterpreter(input);
      ExpressionEvaluator main = new ExpressionEvaluator(interpreter);
      for (InputLayout inputLayout : inputs) {
        inputLayout.checkConditionalExpression(main);
      }
    }
  }

  private com.google.android.apps.paco.questioncondparser.Environment updateInterpreter(InputLayout input) {
    com.google.android.apps.paco.questioncondparser.Environment interpreter = null;
    // todo make interpreter a field to optimize updates.
    if (interpreter == null) {
      interpreter = new com.google.android.apps.paco.questioncondparser.Environment();

      for (InputLayout inputLayout : inputs) {
        interpreter.addInput(createBindingFromInputView(inputLayout));
      }
    }
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
    if (resultCode == RESULT_OK) {
      if (requestCode == RESULT_SPEECH) {
        handleSpeechRecognitionActivityResult(resultCode, data);
      } else if (requestCode == RESULT_CAMERA) {
        cameraPictureTaken();
      } else if (requestCode == RESULT_GALLERY) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        try {
          Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
          cursor.moveToFirst();

          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          String filePath = cursor.getString(columnIndex);
          cursor.close();
          galleryPicturePicked(filePath);
        } catch (Exception e) {
          Log.info("Exception in gallery picking: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }

  private void handleSpeechRecognitionActivityResult(int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK && null != data) {
      ArrayList<String> guesses = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      notifySpeechRecognitionListeners(guesses);
    }
  }

  private void notifySpeechRecognitionListeners(ArrayList<String> guesses) {
    for (SpeechRecognitionListener listener : speechRecognitionListeners) {
      listener.speechRetrieved(guesses);
    }
  }

  public void removeSpeechRecognitionListener(SpeechRecognitionListener listener) {
    speechRecognitionListeners.remove(listener);
  }

  public void startSpeechRecognition(SpeechRecognitionListener listener) {
    speechRecognitionListeners .add(listener);
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault().getDisplayName());

    try {
      startActivityForResult(intent, RESULT_SPEECH);
    } catch (ActivityNotFoundException a) {
      Toast t = Toast.makeText(getApplicationContext(), R.string.oops_your_device_doesn_t_support_speech_to_text, Toast.LENGTH_SHORT);
      t.show();
    }
  }

  private class JavascriptExecutorListener {
    private Experiment experiment;
    public JavascriptExecutorListener(Experiment experiment) {
        this.experiment = experiment;
    }
    @JavascriptInterface
    public void done() {
      deleteNotification();

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        extras.clear();
      }
      updateAlarms();
      notifySyncService();

      if (experimentGroup.getFeedback().getType() != FeedbackDAO.FEEDBACK_TYPE_HIDE_FEEDBACK) {
        showFeedback();
      }
      finish();
    }
  }



//start duplicate from inputlayout for photo service

  public void renderCameraOrGalleryChooser() {
    String title = getString(R.string.please_choose_the_source_of_your_image);
    Dialog chooserDialog = new AlertDialog.Builder(this).setTitle(title)
            .setNegativeButton(getString(R.string.camera), new Dialog.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                startCameraForResult();
              }
            })
            .setPositiveButton(getString(R.string.gallery), new Dialog.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                startGalleryPicker();

              }

            }).create();
    chooserDialog.show();
  }

  private void startGalleryPicker() {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, RESULT_GALLERY);
  }

  void galleryPicturePicked(String filepath) {
    if (!Strings.isNullOrEmpty(filepath)) {
      photoFile = new File(filepath);
      setBitmapToHtmlInput(decodeFileAndScaleToThumb(photoFile));
    } else if (Strings.isNullOrEmpty(filepath)) {
      photoFile = null;
    } // otherwise leave as it was previously
  }

  private void startCameraForResult() {
    try {
      Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
      String dateString = createTimeStamp();
      photoFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
      i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
      startActivityForResult(i, RESULT_CAMERA);
    } catch (Exception e) {
      e.printStackTrace();
      new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.cannot_open_camera_warning)
          .setMessage("Error: \n" + e.getMessage()).setNegativeButton(R.string.ok, null).create().show();
    }
  }

  public void cameraPictureTaken() {
    setBitmapToHtmlInput(decodeFileAndScaleToThumb(photoFile));
  }

  private void setBitmapToHtmlInput(Bitmap bitmap) {
    String encodedBitmap = base64Encode( bitmap);
    encodedBitmap = encodedBitmap.replaceAll("\n", ""); // todo figure out why it is adding newlines and why this isn't a problem for uploaded but is for rendering.
    //String fakeBitmap = "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";

    webView.loadUrl("javascript:paco.photoService.photoResult('" + encodedBitmap + "')");
  }


  private String base64Encode(Bitmap bitmap) {
    return encodeBitmap(bitmap);
  }

  private String getPhotoValue() {
    if (photoFile != null) {
      Bitmap bitmap = decodeFile(photoFile);
      if (bitmap == null) {
        return "";
      }

      return encodeBitmap(bitmap);
    }
    return "";
  }

  private String encodeBitmap(Bitmap bitmap) {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    BufferedOutputStream out = new BufferedOutputStream(bytesOut);
    bitmap.compress(CompressFormat.JPEG, 50, out);
    return Base64.encodeToString(bytesOut.toByteArray(), Base64.DEFAULT);
  }

  private Bitmap decodeFile(File f) {
    return decodeBitmapFromFileWithMaxDimension(f, IMAGE_MAX_SIZE);
  }

  private Bitmap decodeFileAndScaleToThumb(File f) {
    return decodeBitmapFromFileWithMaxDimension(f, IMAGE_MAX_SIZE);
  }

  private Bitmap decodeBitmapFromFileWithMaxDimension(File f, int maxDimension) {
    Bitmap b = null;
    try {
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(f), null, o);
      int scale = 1;
      if (o.outHeight > maxDimension || o.outWidth > maxDimension) {
        int longestDimension = Math.max(o.outHeight, o.outWidth);
        scale = (int) Math.pow(2, (int) Math.round(Math.log(maxDimension / (double) longestDimension) / Math.log(0.5)));
      }

      // Decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
    } catch (FileNotFoundException e) {
      Toast.makeText(this, R.string.missing_image_warning, Toast.LENGTH_LONG);
    }
    return b;
  }

  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;
  /** Create a file Uri for saving an image or video */
  private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
  }

  /** Create a File for saving an image or video */
  private File getOutputMediaFile(int type){
      // To be safe, you should check that the SDCard is mounted
      // using Environment.getExternalStorageState() before doing this.

      File mediaStorageDir = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "PacoApp");
      // This location works best if you want the created images to be shared
      // between applications and persist after your app has been uninstalled.

      // Create the storage directory if it does not exist
      if (! mediaStorageDir.exists()){
          if (! mediaStorageDir.mkdirs()){
              Log.debug("failed to create directory");
              return null;
          }
      }

      // Create a media file name
      String timeStamp = createTimeStamp();
      File mediaFile;
      if (type == MEDIA_TYPE_IMAGE){
          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "IMG_"+ timeStamp + ".jpg");
      } else if(type == MEDIA_TYPE_VIDEO) {
          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
          "VID_"+ timeStamp + ".mp4");
      } else {
          return null;
      }

      return mediaFile;
  }

  private String createTimeStamp() {
    SimpleDateFormat df = new SimpleDateFormat("MMddyyyyhhmmsss");
    return (df.format(new Date()));
  }

// end duplicate from inputlayout
  public void setExperimentGroup(ExperimentGroup group) {
    this.experimentGroup = group;

  }

  public Experiment getExperiment() {
    return experiment;
  }


  public void setExperiment(Experiment experiment) {
    this.experiment = experiment;

  }

  public void stopExperiment() {
    Log.debug("ExperimentExecutorCustomRendering stopExperiment");
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



}
