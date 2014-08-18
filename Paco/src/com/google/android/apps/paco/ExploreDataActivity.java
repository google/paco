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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.pacoapp.paco.R;


/**
 *
 */
public class ExploreDataActivity extends Activity {

  private ExperimentProviderUtil experimentProviderUtil;
  private ListView list;
  private ViewGroup mainLayout;
  public UserPreferences userPrefs;
  private List<Long> inputIds;
  private WebView webView = null;
  private Button rawDataButton;
  // Choices that have been selected on a multiselect list in a dialog.
  private HashMap<Long, List<Long>> checkedChoices = new HashMap<Long, List<Long>>();
  List<String> inpNames;
  boolean showDialog = true;
  private Environment env;
  private List<Experiment> experiments;

  @Override
  //Make the first screen with which choices of what to do: Trends, Relationships, or Distributions (TRD)
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.explore_data, null);
    setContentView(mainLayout);

    //Check for experiments. If they do not exist, disable buttons and alert the user of that.
    experimentProviderUtil = new ExperimentProviderUtil(this);
    experiments = experimentProviderUtil.getJoinedExperiments();
    if (experiments.size()<1){
      new AlertDialog.Builder(mainLayout.getContext()).setMessage(R.string.no_experimental_data_warning).setCancelable(true).setPositiveButton(R.string.ok, new Dialog.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }

      }).create().show();
      Button chooseTrends = (Button)findViewById(R.id.TrendsButton);
      Button chooseRelationships = (Button)findViewById(R.id.RelationshipsButton);
      Button chooseDistributions = (Button)findViewById(R.id.DistributionsButton);
      chooseTrends.setEnabled(false);
      chooseRelationships.setEnabled(false);
      chooseDistributions.setEnabled(false);
      return;
    }
    ////

    Button chooseTrends = (Button)findViewById(R.id.TrendsButton);
    chooseTrends.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(1);
      }
      });

    Button chooseRelationships = (Button)findViewById(R.id.RelationshipsButton);
    chooseRelationships.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(2);
      }
      });

    Button chooseDistributions = (Button)findViewById(R.id.DistributionsButton);
    chooseDistributions.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        gotoVarSelection(3);
      }
      });

  }

  //When we click on a TRD option, go to the next screen which is set up below with the options of which variables we want.
  //The screen will show you all of the experiments you are running. You can click on an experiment and decide
  //and a dialog will pop up allowing you to choose which variables you want to explore.
  //Then there is an OK button which will take you to the next screen or will tell you if you have an error.

  protected void gotoVarSelection(final int whichOption){
    mainLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.variable_choices, null);
    setContentView(mainLayout);

    final Button varOkButton = (Button) findViewById(R.id.VarOkButton);
    switch (whichOption){
      case 1: varOkButton.setText(R.string.show_trends_button); break;
      case 2: varOkButton.setText(R.string.show_relationships_button); break;
      case 3: varOkButton.setText(R.string.show_distributions_button); break;
      default: varOkButton.setText("  " + getString(R.string.ok) + "  "); break;
    }

    varOkButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showVisualizations(checkedChoices, whichOption);
      }
    });

    userPrefs = new UserPreferences(this);
    list = (ListView)findViewById(R.id.exploreable_experiments_list);
    experimentProviderUtil = new ExperimentProviderUtil(this);

    Cursor cursor = managedQuery(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
        new String[] { ExperimentColumns._ID, ExperimentColumns.TITLE},
        null, null, null);


    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
      android.R.layout.simple_list_item_2, cursor,
      new String[] { ExperimentColumns.TITLE},
      new int[] { android.R.id.text1}) {
    };

    list.setAdapter(adapter);
    list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> listview, View textview, int position,
          long id) {
        Experiment experiment = experimentProviderUtil.getExperiment(id);

        if (experiment!= null) {
         inputIds = getInputIds(experiment.getInputs());
         inpNames = getInputNames(experiment.getInputs());
         renderMultiSelectListButton(id, (TextView) ((TwoLineListItem) textview).getChildAt(1));
         varOkButton.setVisibility(View.VISIBLE);
        } else{
          Toast.makeText(ExploreDataActivity.this, R.string.experiment_choice_warning,
          Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  //Make the dialog box containing variables in the experiment that is clicked on
  private View renderMultiSelectListButton(final Long id, final TextView textview) {

    DialogInterface.OnMultiChoiceClickListener multiselectListDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked){
          if (checkedChoices.get(id) !=null){
            checkedChoices.get(id).add(inputIds.get(which));
          }
          else{
            List<Long> tempList = new ArrayList<Long>();
            tempList.add(inputIds.get(which));
            checkedChoices.put(id, tempList);
          }
        }else{
          checkedChoices.get(id).remove(inputIds.get(which));
          if (checkedChoices.get(id).isEmpty())
            checkedChoices.remove(id);
        }
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(mainLayout.getContext());
    builder.setTitle(R.string.make_selections);

    boolean[] checkedChoicesBoolArray = new boolean[inputIds.size()];
    int count = inputIds.size();

    if (checkedChoices.get(id) !=null){
      for (int i = 0; i < count; i++) {
        checkedChoicesBoolArray[i] = checkedChoices.get(id).contains(inputIds.get(i));
      }
    }
    String[] listChoices = new String[inputIds.size()];
    inpNames.toArray(listChoices);
    builder.setMultiChoiceItems(listChoices, checkedChoicesBoolArray, multiselectListDialogListener);
    builder.setPositiveButton(R.string.ok,
      new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog,
              int whichButton) {
        getLabelWithSelectedVariables(id, textview);
        }
      });
    AlertDialog multiSelectListDialog = builder.create();
    multiSelectListDialog.show();
    return multiSelectListDialog.getListView();
  }

  private void getLabelWithSelectedVariables(long id, TextView textview){
    if (checkedChoices.get(id) !=null){
      String finalString = "  ";
      List<Long> tempVals;

        tempVals = checkedChoices.get(id);
        Experiment e = getExperiment(id);
        for (Long val: tempVals){
          finalString+=(e.getInputById(val).getName()+"  ");
        }
      textview.setText(finalString);
    }else{
      textview.setText("");
    }
  }

  private void showVisualizations(HashMap<Long, List<Long>> choices, int whichOpt) {
    int choicesSize = 0;
    for (Long key : choices.keySet()) {
      choicesSize += choices.get(key).size();
    }
    if (whichOpt == 1 || whichOpt == 3) {
      if (choicesSize == 1) {
        for (Long key : choices.keySet()) {
          showTrendOrDistributionOfOneVar(key, choices.get(key).get(0), whichOpt);
        }
      } else {
        chooseOneVarToast();
      }
    } else if (whichOpt == 2) {
      if (choicesSize == 2) {
        if (choices.keySet().size() == 1) {// For data from the same experiment
          long varX, varY;
          for (Long key : choices.keySet()) {
            varX = choices.get(key).get(0);
            varY = choices.get(key).get(1);
            showRelationshipForVarsInSameExperiment(key, varX, varY);
          }
        } else if (choices.keySet().size() == 2) {// For data from two different
                                                  // experiments
          showRelationshipForVarsInDifferentExperiments(choices);
        }
      } else {
        chooseTwoVarsToast();
      }
    }
  }

  //execute trends or distributions for one variable from one experiment
  private void showTrendOrDistributionOfOneVar(Long expId, Long inpId, int whichOpt) {
    Experiment experiment = getFullyLoadedExperiment(expId);
    if (experiment == null) {
      Toast.makeText(ExploreDataActivity.this, R.string.experiment_does_not_exist_warning,
        Toast.LENGTH_SHORT).show();
    } else {
      setContentView(R.layout.feedback);
      loadRestOfExperimentInformation(experimentProviderUtil, experiment);

      final Map<String,String> map = new HashMap<String, String>();

      map.put("experimentalData", convertExperimentResultsToJsonString(experiment));

      map.put("inputId", inpId+"");

      rawDataButton = (Button)findViewById(R.id.rawDataButton);
      rawDataButton.setVisibility(View.GONE);

      webView = (WebView)findViewById(R.id.feedbackText);
      webView.getSettings().setJavaScriptEnabled(true);

      env = new Environment(map);
      webView.addJavascriptInterface(env, "env");
      webView.addJavascriptInterface(new JavascriptEventLoader(experimentProviderUtil, experiment), "eventLoader");

      setWebChromeClientThatHandlesAlertsAsDialogs();
      WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts();
      webView.setWebViewClient(webViewClient);

      if (whichOpt==1)
        webView.loadUrl("file:///android_asset/trends.html");
      else if (whichOpt==3)
        webView.loadUrl("file:///android_asset/distributions.html");

    }
  }

  //execute relationships for two variables from different experiments
  private void showRelationshipForVarsInDifferentExperiments(HashMap<Long, List<Long>> choices) {

    ArrayList<Pair<Experiment, Long>> experimentInputPairs = new ArrayList<Pair<Experiment, Long>>();

    for (Long experimentId : choices.keySet()){
      Long inputId = choices.get(experimentId).get(0);
      Experiment fullyLoadedExperiment = getFullyLoadedExperiment(experimentId);
      if (fullyLoadedExperiment == null) {
        Toast.makeText(ExploreDataActivity.this, R.string.experiment_does_not_exist_warning, Toast.LENGTH_SHORT).show();
        return;
      }
      experimentInputPairs.add(new Pair<Experiment, Long>(fullyLoadedExperiment, inputId));
    }

    setContentView(R.layout.feedback);
    rawDataButton = (Button)findViewById(R.id.rawDataButton);
    rawDataButton.setVisibility(View.INVISIBLE);

    webView = (WebView)findViewById(R.id.feedbackText);
    webView.getSettings().setJavaScriptEnabled(true);

    final Map<String,String> map = new HashMap<String, String>();

    map.put("xAxisData", convertExperimentResultsToJsonString(experimentInputPairs.get(0).first));
    map.put("yAxisData", convertExperimentResultsToJsonString(experimentInputPairs.get(1).first));

    map.put("xAxisInputId", Long.toString(experimentInputPairs.get(0).second));
    map.put("yAxisInputId", Long.toString(experimentInputPairs.get(1).second));

    setWebChromeClientThatHandlesAlertsAsDialogs();
    WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts();
    webView.setWebViewClient(webViewClient);

    final Environment env = new Environment(map);
    webView.addJavascriptInterface(env, "env");

    webView.loadUrl("file:///android_asset/relationships.html");

  }


  //execute relationships for two variables within the same experiment
  private void showRelationshipForVarsInSameExperiment(Long experimentId, long xAxisInputId, long yAxisInputId) {
    Experiment experiment = getFullyLoadedExperiment(experimentId);
    if (experiment == null) {
      Toast.makeText(ExploreDataActivity.this, R.string.experiment_does_not_exist_warning,
        Toast.LENGTH_SHORT).show();
    } else {
      setContentView(R.layout.feedback);

      final Map<String,String> map = new HashMap<String, String>();
      String experimentJsonResults = convertExperimentResultsToJsonString(experiment);
      map.put("xAxisData", experimentJsonResults);
      map.put("yAxisData", experimentJsonResults);
      map.put("xAxisInputId", Long.toString(xAxisInputId));
      map.put("yAxisInputId", Long.toString(yAxisInputId));

      rawDataButton = (Button)findViewById(R.id.rawDataButton);
      rawDataButton.setVisibility(View.INVISIBLE);

      webView = (WebView)findViewById(R.id.feedbackText);
      webView.getSettings().setJavaScriptEnabled(true);

      env = new Environment(map);
      webView.addJavascriptInterface(env, "env");

      setWebChromeClientThatHandlesAlertsAsDialogs();
      WebViewClient webViewClient = createWebViewClientThatHandlesFileLinksForCharts();
      webView.setWebViewClient(webViewClient);

      webView.loadUrl("file:///android_asset/relationships.html");
    }
  }

  public void chooseOneVarToast(){
    Toast.makeText(ExploreDataActivity.this, R.string.sorry_please_select_exactly_one_variable_warning,
        Toast.LENGTH_SHORT).show();
  }

  public void chooseTwoVarsToast(){
    Toast.makeText(ExploreDataActivity.this, R.string.sorry_please_select_exactly_two_variables_warning, Toast.LENGTH_SHORT).show();
  }

  private List<String> getInputNames(List<Input> i){
    List<String> tempInputNames = new ArrayList<String>();
    for (Input inp: i){
      tempInputNames.add(inp.getName());
    }
    return tempInputNames;
  }

  private List<Long> getInputIds(List<Input> inputs){
    List<Long> tempIds = new ArrayList<Long>();
    for (Input inp: inputs){
      tempIds.add(inp.getServerId());
    }
    return tempIds;
  }


  private Experiment getExperiment(long expId){
    return experimentProviderUtil.getExperiment(expId);
  }

  private Experiment getFullyLoadedExperiment(long expId){
    Experiment experiment = getExperiment(expId);
    loadRestOfExperimentInformation(experimentProviderUtil, experiment);
    return experiment;
  }

  private String convertExperimentResultsToJsonString(Experiment experiment) {
    return convertExperimentDataToJsonArray(experiment).toString();
  }

  private JSONArray convertExperimentDataToJsonArray(Experiment experiment) {
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
          responseJson.put("prompt", experiment.getFeedback().get(0).getTextOfInputForOutput(experiment, response));
          responseJson.put("answer", response.getDisplayOfAnswer(input));
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
    return experimentData;
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
    });
  }

  private WebViewClient createWebViewClientThatHandlesFileLinksForCharts() {
    WebViewClient webViewClient = new WebViewClient() {

      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        if (uri.getScheme().startsWith("http")) {
          return true;
        }

        view.loadUrl(FeedbackActivity.stripQuery(url));
        return true;
      }

    };
    return webViewClient;
  }

  private void loadRestOfExperimentInformation(ExperimentProviderUtil epu, Experiment exp) {
    epu.loadEventsForExperiment(exp);
  }

  /**
   * @param inputId
   * @param localExperiment
   * @return
   */
  private JSONArray getResultsForInputAsJsonString(long inputId, Experiment localExperiment) {
    JSONArray results = new JSONArray();
    for (Event event : localExperiment.getEvents()) {
      JSONArray eventJson = new JSONArray();
      DateTime responseTime = event.getResponseTime();
      if (responseTime == null) {
        continue; // missed signal;
      }
      eventJson.put(responseTime.getMillis());

      // in this case we are looking for one input from the responses that we are charting.
      for (Output response : event.getResponses()) {
        if (response.getInputServerId() == inputId ) {
          Input inputById = localExperiment.getInputById(inputId);
          if (!inputById.isInvisible() && inputById.isNumeric()) {
            eventJson.put(response.getDisplayOfAnswer(inputById));
            results.put(eventJson);
            continue;
          }
        }
      }

    }
    return results;
  }

}