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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class FeedbackActivity extends Activity {

  private static final String TEMP_URL = null;
  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;
  private WebView webView;
  private Button rawDataButton;
  boolean showDialog = true;
  
  private class Environment {

    
    private HashMap<String, String> map;

    public Environment(Map<String,String> map) {
      super();
      this.map = new HashMap<String, String>();
      this.map.putAll(map);
    }

    public String getValue(String key) {
      return map.get(key);
    }
    
  }
  
  private class Email {
    public void sendEmail(String body, String subject, String userEmail) {
      FeedbackActivity.this.sendEmail(body, subject, userEmail);
    }
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    experimentProviderUtil = new ExperimentProviderUtil(this);
    experiment = getExperimentFromIntent();
    if (experiment == null) {
      displayNoExperimentMessage();
    } else {
      setContentView(R.layout.feedback);
      experimentProviderUtil.loadFeedbackForExperiment(experiment);
      experimentProviderUtil.loadInputsForExperiment(experiment);
      experimentProviderUtil.loadLatestEventForExperiment(experiment);
      final Feedback feedback = experiment.getFeedback().get(0);
      
      final Map<String,String> map = new HashMap<String, String>();      
      map.put("experimentalData", convertExperimentResultsToJsonString(feedback));
      map.put("title", experiment.getTitle());
      
      rawDataButton = (Button)findViewById(R.id.rawDataButton);
      rawDataButton.setOnClickListener(new OnClickListener() {        
        public void onClick(View v) {
          Intent rawDataIntent = new Intent(FeedbackActivity.this, RawDataActivity.class);
          rawDataIntent.setData(getIntent().getData());
          startActivity(rawDataIntent);
        }
      });
      webView = (WebView)findViewById(R.id.feedbackText);
      webView.getSettings().setJavaScriptEnabled(true);

      final Environment env = new Environment(map);
      webView.addJavascriptInterface(env, "env");
      
      String text = experiment.getFeedback().get(0).getText();                
      webView.addJavascriptInterface(text, "additions");      
      webView.addJavascriptInterface(new Email(), "email");      
            
      setWebChromeClientThatHandlesAlertsAsDialogs();
      
      WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts(feedback);      
      webView.setWebViewClient(webViewClient);
      
      if (experiment.getFeedback().size() > 0 && !experiment.getFeedback().get(0).isDefaultFeedback()) {
        loadCustomFeedbackIntoWebView();
      } else {
        loadDefaultFeedbackIntoWebView();  
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

  private void loadDefaultFeedbackIntoWebView() {
    webView.loadUrl("file:///android_asset/default_feedback.html");
  }

  private void loadCustomFeedbackIntoWebView() {
    //        webView.loadData(text, "text/html", "utf-8");
    //webView.loadDataWithBaseURL("file:///", text, "text/html", "utf-8", "data:"+text);
    webView.loadUrl("file:///android_asset/skeleton.html");
//        webView.loadUrl("file:///android_asset/shell.html");
  }

  private WebViewClient createWebViewClientThatHandlesFileLinksForCharts(final Feedback feedback) {
    WebViewClient webViewClient = new WebViewClient() {

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        if (uri.getScheme().startsWith("http")) {
          return false;
        }
        
        String inputIdStr = uri.getQueryParameter("inputId");
        long inputId = Long.valueOf(inputIdStr);
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
            if (response.getInputServerId() == inputId ) {
              Input inputById = experiment.getInputById(inputId);
              if (!inputById.isInvisible() && inputById.isNumeric()) {               
                eventJson.put(feedback.getDisplayOfAnswer(response, 
                    inputById));
                results.put(eventJson);
                continue;
              }
            }
          }
          
        }
        Map<String, String> map2 = new HashMap();
        Environment chartEnv = new Environment(map2);
        map2.put("data", results.toString());
        
        view.addJavascriptInterface(chartEnv, "chartEnv");
        view.loadUrl(url);
        return true;
      }
      
    };
    return webViewClient;
  }

  private void setWebChromeClientThatHandlesAlertsAsDialogs() {
    webView.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

        new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).setPositiveButton("OK", new Dialog.OnClickListener() {

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
            new AlertDialog.Builder(view.getContext()).setMessage(message).setCancelable(true).setPositiveButton("OK", new Dialog.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                showDialog = false;
                dialog.dismiss();
                result.confirm();
              }
            }).setNegativeButton("Cancel", new Dialog.OnClickListener() {
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
    });
  }

  private String convertExperimentResultsToJsonString(final Feedback feedback) {
    final JSONArray experimentData = new JSONArray();
    for (Event event : experiment.getEvents()) {
      try {
        JSONObject eventObject = new JSONObject();
        boolean missed = event.getResponseTime() == null;
        eventObject.put("isMissedSignal", missed);
        if (!missed) {
          eventObject.put("responseTime", event.getResponseTime().getMillis());
        }
        
        boolean selfReport = event.getScheduledTime() == null;
        eventObject.put("isSelfReport", selfReport);
        if (!selfReport) {
          eventObject.put("scheduleTime", event.getScheduledTime().getMillis());
        }
        
        
        
        JSONArray responses = new JSONArray();
        for (Output response : event.getResponses()) {
          JSONObject responseJson = new JSONObject();
          Input input = experiment.getInputById(response.getInputServerId());     
          if (input == null) {
            continue;
          }
          responseJson.put("inputId", input.getServerId());
          responseJson.put("inputName", input.getName());
          responseJson.put("responseType", input.getResponseType());
          responseJson.put("isMultiselect", input.isMultiselect());
          responseJson.put("prompt", feedback.getTextOfInputForOutput(experiment, response));
          responseJson.put("answer", feedback.getDisplayOfAnswer(response, input));
          responseJson.put("answerOrder", response.getAnswer());  
          responses.put(responseJson);
        }          
        
        eventObject.put("responses", responses);
        if (responses.length() > 0) {
          experimentData.put(eventObject);
        }
      } catch (JSONException jse) {
        // skip this event and do the next event. 
      }
    }
    String experimentDataAsJson = experimentData.toString();
    return experimentDataAsJson;
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

  private Experiment getExperimentFromIntent() {
    Uri uri = getIntent().getData();    
    if (uri == null) {
      return null;
    }
    Experiment experiment = experimentProviderUtil.getExperiment(uri);
    experimentProviderUtil.loadInputsForExperiment(experiment);
    return experiment;
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
  
}
