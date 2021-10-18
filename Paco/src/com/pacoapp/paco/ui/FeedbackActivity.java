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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.pacoapp.paco.R;
import com.pacoapp.paco.js.bridge.Environment;
import com.pacoapp.paco.js.bridge.JavascriptEmail;
import com.pacoapp.paco.js.bridge.JavascriptEventLoader;
import com.pacoapp.paco.js.bridge.JavascriptExperimentLoader;
import com.pacoapp.paco.js.bridge.JavascriptSensorManager;
import com.pacoapp.paco.js.bridge.JavascriptStringResources;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventQueryStatus;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ExperimentHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class FeedbackActivity extends AppCompatActivity {

  private static Logger Log = LoggerFactory.getLogger(FeedbackActivity.class);

  private static final String TEMP_URL = null;
  ExperimentProviderUtil experimentProviderUtil;
  Experiment experiment;
  private WebView webView;
  private Button rawDataButton;
  boolean showDialog = true;
  private Environment env;
  private ExperimentGroup experimentGroup;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.debug("FeedbackActivity onCreate");

    ActionBar actionBar = getSupportActionBar();
    actionBar.setLogo(R.drawable.ic_launcher);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setBackgroundDrawable(new ColorDrawable(0xff4A53B3));


    experimentProviderUtil = new ExperimentProviderUtil(this);
    loadExperimentInfoFromIntent();

    if (experiment == null || experimentGroup == null) {
      Log.error("FeedbackActivity experiment is null");
      displayNoExperimentMessage();
    } else {
      setContentView(R.layout.feedback);

      // TODO revamp this to deal with null experimentGroup (do we give a list of groups? the exploredata button in runningexperiments needs this)


      rawDataButton = (Button)findViewById(R.id.rawDataButton);

      if (experimentGroup.getRawDataAccess()) {
        rawDataButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            Intent rawDataIntent = new Intent(FeedbackActivity.this, RawDataActivity.class);
            rawDataIntent.putExtras(getIntent().getExtras());
            startActivity(rawDataIntent);
          }
        });
      } else {
        rawDataButton.setVisibility(View.GONE);
      }
      webView = (WebView)findViewById(R.id.feedbackText);
      webView.getSettings().setJavaScriptEnabled(true);

      final com.pacoapp.paco.shared.model2.Feedback feedback = experimentGroup.getFeedback();

      injectObjectsIntoJavascriptEnvironment(feedback);
      setWebChromeClientThatHandlesAlertsAsDialogs();

      WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts(feedback);
      webView.setWebViewClient(webViewClient);

      if (experimentGroup.getFeedback().getType() != null &&
              experimentGroup.getFeedback().getType().equals(com.pacoapp.paco.shared.model2.Feedback.FEEDBACK_TYPE_RETROSPECTIVE)) {
        // TODO get rid of this and just use the customFeedback view
        loadRetrospectiveFeedbackIntoWebView();
      } else {
        loadCustomFeedbackIntoWebView();
      }
      if (savedInstanceState != null){
        String savedUrl = savedInstanceState.getString("url");
        if (!Strings.isNullOrEmpty(savedUrl)) {
          webView.loadUrl(savedUrl);
        }
        showDialog = savedInstanceState.getBoolean("showDialog", false);

      }
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
        finish();
        return true;
      }
      return super.onOptionsItemSelected(item);
  }
  private void injectObjectsIntoJavascriptEnvironment(final com.pacoapp.paco.shared.model2.Feedback feedback) {
    final Map<String,String> map = new HashMap<String, String>();
    map.put("experimentGroupName", experimentGroup.getName());
    map.put("title", experiment.getExperimentDAO().getTitle());
    map.put("test", "false");
    map.put("additions", experimentGroup.getFeedback().getText());
    env = new Environment(map);
    webView.addJavascriptInterface(env, "env");

    webView.addJavascriptInterface(new JavascriptEmail(this), "email");
    webView.addJavascriptInterface(new JavascriptExperimentLoader(this, experimentProviderUtil,
                                                                  experiment.getExperimentDAO(), experiment, experimentGroup),
                                                                  "experimentLoader");

    JavascriptEventLoader javascriptEventLoader = new JavascriptEventLoader(experimentProviderUtil, experiment,
                                                                            experiment.getExperimentDAO(), experimentGroup);
    webView.addJavascriptInterface(javascriptEventLoader, "db");
    webView.addJavascriptInterface(new JavascriptSensorManager(getApplicationContext()), "sensors");
    webView.addJavascriptInterface(new JavascriptStringResources(getApplicationContext()), "strings");

  }

  private void loadRetrospectiveFeedbackIntoWebView() {
    Log.debug("FeedbackActivity loadRetrospectiveFeedbackIntoWebView");
    webView.loadUrl("file:///android_asset/retrospective_feedback.html");
  }

  private void loadCustomFeedbackIntoWebView() {
    Log.debug("FeedbackActivity loadCustomFeedbackIntoWebView");
    webView.loadUrl("file:///android_asset/skeleton.html");
  }

  private WebViewClient createWebViewClientThatHandlesFileLinksForCharts(final com.pacoapp.paco.shared.model2.Feedback feedback) {
    WebViewClient webViewClient = new WebViewClient() {

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        if (uri.getScheme().startsWith("http")) {
          return true; // throw away http requests - we don't want 3rd party javascript sending url requests due to security issues.
        }

        String inputIdStr = uri.getQueryParameter("inputId");
        if (inputIdStr == null) {
          return true;
        }

        JSONArray results = new JSONArray();
        experimentProviderUtil.loadEventsForExperiment(experiment);
        for (Event event : experiment.getEvents()) {
          JSONArray eventJson = new JSONArray();
          DateTime responseTime = event.getResponseTime();
          if (responseTime == null) {
            continue; // missed signal;
          }
          eventJson.put(responseTime.getMillis());

          // in this case we are looking for one input from the responses that we are charting.
          for (Output response : event.getResponses()) {
            if (response.getName().equals(inputIdStr)) {
              Input2 inputById = ExperimentHelper.getInputWithName(experiment.getExperimentDAO(), inputIdStr, null);
              if (inputById.isNumeric()) {
                eventJson.put(response.getDisplayOfAnswer(inputById));
                results.put(eventJson);
                continue;
              }
            }
          }

        }
        env.put("data", results.toString());
        env.put("inputId", inputIdStr);

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

        new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).setPositiveButton(R.string.ok, new Dialog.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }

        }).create().show();
        result.confirm();
        return true;
      }

      public boolean onJsConfirm (WebView view, String url, String message, final JsResult result){
        if (url.contains("file:///android_asset/map.html")){
          if (showDialog == false){
            result.confirm();
            return true;
          } else{
            new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).setPositiveButton(R.string.ok, new Dialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                showDialog = false;
                dialog.dismiss();
                result.confirm();
              }
            }).setNegativeButton(R.string.cancel_button, new Dialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                result.cancel();
              }
            }).create().show();
            return true;
          }
        }
        return super.onJsConfirm(view, url, message, result);
      }

      @Override
      public void onConsoleMessage(String message, int lineNumber, String sourceID) {
          Log.debug(message + " -- From line "
                               + lineNumber + " of "
                               + sourceID);
      }

      @Override
      public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.debug("*" + consoleMessage.message() + " -- From line "
            + consoleMessage.lineNumber() + " of "
            + consoleMessage.sourceId() );
        return true;
      }

    });
  }

  public static String convertExperimentResultsToJsonString(final com.pacoapp.paco.shared.model2.Feedback feedback, final Experiment experiment) {
    List<Event> events = experiment.getEvents();
    return convertEventsToJsonString(events);
  }

  public static String convertLastEventToJsonString(List<Event> events) {
    if (events.isEmpty()) {
      return "[]";
    }
    return convertEventsToJsonString(events.subList(0,1));
  }
  
  public static String convertEventQueryStatusToJsonString(EventQueryStatus eventQryStat) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    String eventJson = null;
    try {
      eventJson = mapper.writeValueAsString(eventQryStat);
    } catch (JsonGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return eventJson;
  }


  public static String convertEventsToJsonString(List<Event> events) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    String eventJson = null;
    try {
      eventJson = mapper.writeValueAsString(events);
    } catch (JsonGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return eventJson;

    // TODO use jackson instead. But preserve these synthesized values for backward compatibility.
//    final JSONArray experimentData = new JSONArray();
//    for (Event event : events) {
//      try {
//        JSONObject eventObject = new JSONObject();
//        boolean missed = event.getResponseTime() == null;
//        eventObject.put("isMissedSignal", missed);
//        if (!missed) {
//          eventObject.put("responseTime", event.getResponseTime().getMillis());
//        }
//
//        boolean selfReport = event.getScheduledTime() == null;
//        eventObject.put("isSelfReport", selfReport);
//        if (!selfReport) {
//          eventObject.put("scheduleTime", event.getScheduledTime().getMillis());
//        }
//
//        JSONArray responses = new JSONArray();
//        for (Output response : event.getResponses()) {
//          JSONObject responseJson = new JSONObject();
//          Input2 input = experiment.getInputByName(response.getName());
//          if (input == null) {
//            // just create the event based on all of the values in the datum
//            responseJson.put("name", response.getName());
//            responseJson.put("isMultiselect", false);
//            responseJson.put("prompt", feedback.getTextOfInputForOutput(experiment.getExperimentDAO(), response));
//            responseJson.put("answer", response.getAnswer());
//            // deprecate answerOrder for answerRaw
//            responseJson.put("answerOrder", response.getAnswer());
//            responseJson.put("answerRaw", response.getAnswer());
//            responses.put(responseJson);
//          } else {
//            responseJson.put("name", input.getName());
//            responseJson.put("responseType", input.getResponseType());
//            responseJson.put("isMultiselect", input.getMultiselect());
//            responseJson.put("prompt", feedback.getTextOfInputForOutput(experiment, response));
//            responseJson.put("answer", response.getDisplayOfAnswer(input));
//            // deprecate answerOrder for answerRaw
//            responseJson.put("answerOrder", response.getAnswer());
//            responseJson.put("answerRaw", response.getAnswer());
//            responses.put(responseJson);
//          }
//        }
//
//        eventObject.put("responses", responses);
//        if (responses.length() > 0) {
//          experimentData.put(eventObject);
//        }
//      } catch (JSONException jse) {
//        // skip this event and do the next event.
//      }
//    }
//    String experimentDataAsJson = experimentData.toString();
//    return experimentDataAsJson;
  }


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
          webView.goBack();
          return true;
      }
      return super.onKeyDown(keyCode, event);
  }

  private void displayNoExperimentMessage() {
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.could_not_load_experiment, null);
    setContentView(mainLayout);

  }


  @Override
  protected void onStop() {
    super.onStop();
    Log.debug("FeedbackActivity onStop");
    //finish();
  }

  private void loadExperimentInfoFromIntent() {
    Bundle extras = getIntent().getExtras();
    if (extras == null) {
      return;
    }
    if (extras.containsKey(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY)) {
      long experimentServerId = extras.getLong(Experiment.EXPERIMENT_SERVER_ID_EXTRA_KEY);
      experiment = experimentProviderUtil.getExperimentByServerId(experimentServerId);
      if (experiment != null && extras.containsKey(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY)) {
        String experimentGroupName = extras.getString(Experiment.EXPERIMENT_GROUP_NAME_EXTRA_KEY);
          experimentGroup = experiment.getExperimentDAO().getGroupByName(experimentGroupName);
      }
    }
  }

  String getTextOfInputForOutput(ExperimentDAO experiment, Output output) {
    for (Input2 input : ExperimentHelper.getInputs(experiment)) {
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
  protected void onSaveInstanceState(Bundle outState) {
    if (webView != null) {
      outState.putString("url", webView.getUrl());
    }
    outState.putBoolean("showDialog", showDialog);
  }

  // TODO resolve whether this should be here or in the onCreate method.
//  @Override
//  protected void onRestoreInstanceState(Bundle savedInstanceState) {
//    super.onRestoreInstanceState(savedInstanceState);
//    String url = savedInstanceState.getString("url");
//    if (url != null) {
//      if (webView != null) {
//        webView.loadUrl(url);
//      }
//    }
//    showDialog = savedInstanceState.getBoolean("showDialog", false);
//  }
//

}
