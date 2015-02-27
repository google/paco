package com.google.android.apps.paco;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.common.collect.Lists;

public class JavascriptExperimentLoader {
  /**
   *
   */
  private Experiment experiment;
  private String json;
  private ExperimentProviderUtil experimentProvider;
  private Context context;

  public JavascriptExperimentLoader(Context context, ExperimentProviderUtil experimentProvider, Experiment experiment) {
    this.context = context;
      this.experimentProvider = experimentProvider;
      this.experiment = experiment;
  }

  public String getExperiment() {
    long t1 = System.currentTimeMillis();
    if (this.json == null) {
      json = ExperimentProviderUtil.getJson(experiment);
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
  public String saveExperiment(final String experimentJson) {
    this.json = experimentJson;
    new Thread(new Runnable() {


      @Override
      public void run() {
        try {
          long t1 = System.currentTimeMillis();
          Experiment experiment = ExperimentProviderUtil.getSingleExperimentFromJson(experimentJson);
          long t2= System.currentTimeMillis();
          Log.e(PacoConstants.TAG, "time to load from json : " + (t2 - t1));
          experimentProvider.updateExistingExperiments(Lists.newArrayList(experiment), true);
          long t3= System.currentTimeMillis();
          Log.e(PacoConstants.TAG, "time to update: " + (t3 - t2));
          context.startService(new Intent(context, BeeperService.class));
          if (experiment.shouldWatchProcesses()) {
            BroadcastTriggerReceiver.initPollingAndLoggingPreference(context);
            BroadcastTriggerReceiver.startProcessService(context);
          } else {
            BroadcastTriggerReceiver.stopProcessService(context);
          }
          long t4 = System.currentTimeMillis();
          Log.e(PacoConstants.TAG, "total time in saveExperiment: " + (t4 - t1));
        } catch (JsonParseException e) {
          e.printStackTrace();
          //return "{ \"status\" : 0, \"error_message\" : \"json parse error: " + e.getMessage() + "\" }";
        } catch (JsonMappingException e) {
          e.printStackTrace();
          //return "{ \"status\" : 0, \"error_message\" : \"json mapping error: " + e.getMessage() + "\" }";
        } catch (IOException e) {
          e.printStackTrace();
          //return "{ \"status\" : 0, \"error_message\" : \"io error: " + e.getMessage() + "\" }";
        }
        //return "{ \"status\" : 1, \"error_message\" : \"\" }";
      }

    }).start();
    return null;
  }
}