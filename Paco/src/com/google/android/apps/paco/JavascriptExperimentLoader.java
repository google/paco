package com.google.android.apps.paco;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentDAO;
import com.google.paco.shared.model2.JsonConverter;
import com.google.paco.shared.util.ExperimentHelper;

public class JavascriptExperimentLoader {
  /**
   *
   */
  private ExperimentDAO experiment;
  private String json;
  private ExperimentProviderUtil experimentProvider;
  private Context context;
  private Experiment androidExperiment;

  public JavascriptExperimentLoader(Context context, ExperimentProviderUtil experimentProvider, ExperimentDAO experiment2 ,Experiment androidExperiment) {
    this.context = context;
      this.experimentProvider = experimentProvider;
      this.experiment = experiment2;
      this.androidExperiment = androidExperiment;
  }

  @JavascriptInterface
  public String getExperiment() {
    long t1 = System.currentTimeMillis();
    if (this.json == null) {
      json = JsonConverter.jsonify(experiment);
    }
    long t2= System.currentTimeMillis();
    Log.e(PacoConstants.TAG, "time to load experiment in getExperiment(): " + (t2 - t1));
    return json;
  }
  /**
   * Takes the json of an experiment.
   *
   * @param experimentJson
   * @return json object of an outcome { status: [1|0], error_message : [nil|errorstring] }
   */
  @JavascriptInterface
  public String saveExperiment(final String experimentJson) {
    this.json = experimentJson;
    new Thread(new Runnable() {


      @Override
      public void run() {
        long t1 = System.currentTimeMillis();
        ExperimentDAO experiment = JsonConverter.fromSingleEntityJson(experimentJson);
        long t2 = System.currentTimeMillis();
        Log.e(PacoConstants.TAG, "time to load from json : " + (t2 - t1));
        experimentProvider.updateExistingExperiments(Lists.newArrayList(androidExperiment), true);
        long t3 = System.currentTimeMillis();
        Log.e(PacoConstants.TAG, "time to update: " + (t3 - t2));
        context.startService(new Intent(context, BeeperService.class));
        if (ExperimentHelper.shouldWatchProcesses(experiment)) {
          BroadcastTriggerReceiver.initPollingAndLoggingPreference(context);
          BroadcastTriggerReceiver.startProcessService(context);
        } else {
          BroadcastTriggerReceiver.stopProcessService(context);
        }
        long t4 = System.currentTimeMillis();
        Log.e(PacoConstants.TAG, "total time in saveExperiment: " + (t4 - t1));
      }

    }).start();
    return null;
  }
}