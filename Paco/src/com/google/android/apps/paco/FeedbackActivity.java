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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.json.JSONArray;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.ExperimentGroup;
import com.google.paco.shared.model2.Feedback;
import com.google.paco.shared.model2.Input2;
import com.google.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.R;

public class FeedbackActivity extends Activity {

  private static final String TEMP_URL = null;
  ExperimentProviderUtil experimentProviderUtil;
  Experiment experiment;
  private WebView webView;
  private Button rawDataButton;
  boolean showDialog = true;
  private Environment env;
  private ExperimentGroup experimentGroup;

  private class JavascriptEmail {
    public void sendEmail(String body, String subject, String userEmail) {
      FeedbackActivity.this.sendEmail(body, subject, userEmail);
    }
  }

  private class JavascriptExperimentLoader {
    private Experiment experiment;
    public JavascriptExperimentLoader(Experiment experiment) {
        this.experiment = experiment;
    }
    public String getExperiment() {
      return ExperimentProviderUtil.getJson(experiment);
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    loadExperimentInfoFromIntent();

    if (experiment == null || experimentGroup == null) {
      displayNoExperimentMessage();
    } else {
      setContentView(R.layout.feedback);

      // TODO revamp this to deal with null experimentGroup (do we give a list of groups? the exploredata button in runningexperiments needs this)
      experimentProviderUtil.loadLastEventForExperiment(experiment);


      rawDataButton = (Button)findViewById(R.id.rawDataButton);
      rawDataButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          Intent rawDataIntent = new Intent(FeedbackActivity.this, RawDataActivity.class);
          rawDataIntent.putExtras(getIntent().getExtras());
          startActivity(rawDataIntent);
        }
      });
      webView = (WebView)findViewById(R.id.feedbackText);
      webView.getSettings().setJavaScriptEnabled(true);

      final com.google.paco.shared.model2.Feedback feedback = experimentGroup.getFeedback();

      injectObjectsIntoJavascriptEnvironment(feedback);


      setWebChromeClientThatHandlesAlertsAsDialogs();

      WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts(feedback);
      webView.setWebViewClient(webViewClient);

      if (experimentGroup.getFeedbackType() == com.google.paco.shared.model2.Feedback.FEEDBACK_TYPE_RETROSPECTIVE) {
        // TODO get rid of this and just use the customFeedback view
        loadOldDefaultFeedbackIntoWebView();
      } else {
        loadCustomFeedbackIntoWebView();
      }
      if (savedInstanceState != null){
        webView.loadUrl((String) savedInstanceState.get("url"));
        String showDialogString =  (String) savedInstanceState.get("showDialog");
        if (showDialogString.equals("false")){
          showDialog = false;
        }else{
          showDialog = true;
        }
      }
    }

  }

  private void injectObjectsIntoJavascriptEnvironment(final com.google.paco.shared.model2.Feedback feedback) {
    final Map<String,String> map = new HashMap<String, String>();
    map.put("lastResponse", convertLastEventToJsonString(feedback, experiment));
    map.put("title", experiment.getExperimentDAO().getTitle());
    map.put("experiment", ExperimentProviderUtil.getJson(experiment));
    map.put("test", "false");

    String text = experimentGroup.getFeedback().getText();
    webView.addJavascriptInterface(text, "additions");

    webView.addJavascriptInterface(new JavascriptEmail(), "email");
    webView.addJavascriptInterface(new JavascriptExperimentLoader(experiment), "experimentLoader");

    JavascriptEventLoader javascriptEventLoader = new JavascriptEventLoader(experimentProviderUtil, experiment, experimentGroup);
    webView.addJavascriptInterface(javascriptEventLoader, "db");
    // deprecated name - use "db" in all new experiments
    webView.addJavascriptInterface(javascriptEventLoader, "eventLoader");

    env = new Environment(map);
    webView.addJavascriptInterface(env, "env");

  }

  private void loadOldDefaultFeedbackIntoWebView() {
    webView.loadUrl("file:///android_asset/default_feedback.html");
  }

  private void loadCustomFeedbackIntoWebView() {
    webView.loadUrl("file:///android_asset/skeleton.html");
  }

  private WebViewClient createWebViewClientThatHandlesFileLinksForCharts(final com.google.paco.shared.model2.Feedback feedback) {
    WebViewClient webViewClient = new WebViewClient() {

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        if (uri.getScheme().startsWith("http")) {
          return true; // throw away http requests - we don't want 3rd party javascript sending url requests due to security issues.
        }

        String inputIdStr = uri.getQueryParameter("inputName");
        if (inputIdStr == null) {
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
            if (response.getName().equals(inputIdStr)) {
              Input2 inputById = experiment.getInputByName(inputIdStr);
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
          Log.d(PacoConstants.TAG, message + " -- From line "
                               + lineNumber + " of "
                               + sourceID);
      }

      @Override
      public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.d(PacoConstants.TAG,  "*" + consoleMessage.message() + " -- From line "
            + consoleMessage.lineNumber() + " of "
            + consoleMessage.sourceId() );
        return true;
      }

    });
  }

  public static String convertExperimentResultsToJsonString(final com.google.paco.shared.model2.Feedback feedback, final Experiment experiment) {
    List<Event> events = experiment.getEvents();
    return convertEventsToJsonString(experiment, events);
  }

  public static String convertLastEventToJsonString(final com.google.paco.shared.model2.Feedback feedback, final Experiment experiment) {
    List<Event> events = experiment.getEvents();
    if (events.isEmpty()) {
      return "[]";
    }
    return convertEventsToJsonString(experiment, events.subList(0,1));
  }

  public static String convertEventsToJsonString(final Experiment experiment,
                                                  List<Event> events) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
//    mapper.getDeserializationConfig().addMixInAnnotations(ActionTrigger.class, ActionTriggerMixIn.class);
//    mapper.getDeserializationConfig().addMixInAnnotations(PacoAction.class, PacoActionMixIn.class);

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
  }


  @Override
  protected void onStop() {
    super.onStop();
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

  private void sendEmail(String body, String subject, String userEmail) {
    userEmail = findAccount(userEmail);
    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    String aEmailList[] = { userEmail};
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    emailIntent.setType("plain/text");

    startActivity(emailIntent);
  }

  private String findAccount(String userEmail) {
    String domainName = null;
    if (userEmail.startsWith("@")) {
      domainName = userEmail.substring(1);
    }
    Account[] accounts = AccountManager.get(this).getAccounts();
    for (Account account : accounts) {
      if (userEmail == null || userEmail.length() == 0) {
        return account.name; // return first
      }

      if (domainName != null) {
        int atIndex = account.name.indexOf('@');
        if (atIndex != -1) {
          String accountDomain = account.name.substring(atIndex + 1);
          if (accountDomain.equals(domainName)) {
            return account.name;
          }
        }
      }
    }
    return "";
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString("url", webView.getUrl());
    outState.putString("showDialog", showDialog+"");
 }

  private boolean isOldDefaultFeedback(Feedback feedback) {
    return Feedback.DEFAULT_FEEDBACK_MSG.equals(feedback.getText());
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


}
