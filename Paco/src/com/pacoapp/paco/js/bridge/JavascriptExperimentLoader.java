package com.pacoapp.paco.js.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.sensors.android.BroadcastTriggerReceiver;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.triggering.BeeperService;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;


public class JavascriptExperimentLoader {

  private static Logger Log = LoggerFactory.getLogger(JavascriptExperimentLoader.class);

  private ExperimentDAO experiment;
  private String json;
  private ExperimentProviderUtil experimentProvider;
  private Context context;
  private Experiment androidExperiment;
  private ExperimentGroup experimentGroup;
  private String experimentGroupJson;
  private String experimentGroupReferredJson;

  public JavascriptExperimentLoader(Context context, ExperimentProviderUtil experimentProvider,
                                    ExperimentDAO experiment2, Experiment androidExperiment, ExperimentGroup experimentGroup2) {
    this.context = context;
      this.experimentProvider = experimentProvider;
      this.experiment = experiment2;
      this.androidExperiment = androidExperiment;
      this.experimentGroup = experimentGroup2;
  }


  @JavascriptInterface
  public String getExperimentGroup() {
    long t1 = System.currentTimeMillis();
    if (this.experimentGroupJson == null) {
      experimentGroupJson = JsonConverter.jsonify(experimentGroup);
    }
    long t2= System.currentTimeMillis();
    Log.error("time to load experiment group: " + (t2 - t1));
    return experimentGroupJson;
  }

  @JavascriptInterface
  public String getEndOfDayReferredExperimentGroup() {
    long t1 = System.currentTimeMillis();
    if (this.experimentGroupReferredJson == null) {
      String referredGroupName = experimentGroup.getEndOfDayReferredGroupName();
      if (referredGroupName == null || referredGroupName.length() == 0) {
        return null;
      }
      ExperimentGroup referredGroup = experiment.getGroupByName(referredGroupName);
      if (referredGroup == null) {
        return null;
      }
      experimentGroupReferredJson = JsonConverter.jsonify(referredGroup);
    }
    long t2= System.currentTimeMillis();
    Log.error("time to load referred experiment group: " + (t2 - t1));
    return experimentGroupReferredJson;
  }


  @JavascriptInterface
  public String getExperiment() {
    long t1 = System.currentTimeMillis();
    if (this.json == null) {
      json = JsonConverter.jsonify(experiment);
    }
    long t2= System.currentTimeMillis();
    Log.error("time to load experiment in getExperiment(): " + (t2 - t1));
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
        androidExperiment.setExperimentDAO(experiment);
        long t2 = System.currentTimeMillis();
        Log.error("time to load from json : " + (t2 - t1));
        experimentProvider.updateExistingExperiments(Lists.newArrayList(androidExperiment), true);
        UserPreferences userprefs = new UserPreferences(context);
        userprefs.setExperimentEdited(androidExperiment.getId());
        long t3 = System.currentTimeMillis();
        Log.error("time to update: " + (t3 - t2));
        context.startService(new Intent(context, BeeperService.class));
        if (ExperimentHelper.shouldWatchProcesses(experiment)) {
          BroadcastTriggerReceiver.initPollingAndLoggingPreference(context);
          BroadcastTriggerReceiver.startProcessService(context);
        } else {
          BroadcastTriggerReceiver.stopProcessService(context);
        }
        long t4 = System.currentTimeMillis();
        Log.error("total time in saveExperiment: " + (t4 - t1));
      }

    }).start();
    return null;
  }
}