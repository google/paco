package com.google.android.apps.paco;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class JavascriptEventLoader {
  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;

  /**
   *
   */
  JavascriptEventLoader(ExperimentProviderUtil experimentProviderUtil, Experiment experiment) {
    this.experimentProviderUtil = experimentProviderUtil;
    this.experiment = experiment;
  }

  public String getAllEvents() {
    return loadAllEvents();
  }

  public String loadAllEvents() {
    long t1 = System.currentTimeMillis();
    experimentProviderUtil.loadEventsForExperiment(experiment);
    final Feedback feedback = experiment.getFeedback().get(0);
    String convertExperimentResultsToJsonString = FeedbackActivity.convertExperimentResultsToJsonString(feedback, experiment);
    long t2= System.currentTimeMillis();
    Log.e(PacoConstants.TAG, "time for loadAllEvents: " + (t2 - t1));
    return convertExperimentResultsToJsonString;
  }

  public String getLastEvent() {
    final Feedback feedback = experiment.getFeedback().get(0);
    return FeedbackActivity.convertLastEventToJsonString(feedback, experiment);
  }

  /**
   * Backward compatible alias for saveEvent
   * @param json
   */
  public void saveResponse(String json) {
     saveEvent(json);
  }

  public void saveEvent(String json) {
    try {
      JSONObject eventJson = new JSONObject(json);

      Long scheduledTime = null;
      if (eventJson.has("scheduledTime")) {
        String scheduledTimeString = eventJson.getString("scheduledTime");
        if (!Strings.isNullOrEmpty(scheduledTimeString)) {
          scheduledTime = Long.parseLong(scheduledTimeString);
        }
      }
      Event event = ExperimentExecutor.createEvent(experiment, scheduledTime);

      JSONArray jsonResponses = eventJson.getJSONArray("responses");
      List<Output> responses = Lists.newArrayList();
      for (int i = 0; i <  jsonResponses.length(); i++) {
        JSONObject jsonOutput = jsonResponses.getJSONObject(i);
        Output output = new Output();

        if (jsonOutput.has("answer")) {
          output.setAnswer(jsonOutput.getString("answer"));
        }

        if (jsonOutput.has("name")) {
          output.setName(jsonOutput.getString("name"));
        }

        if (jsonOutput.has("inputId")) {
          output.setInputServerId(jsonOutput.getLong("inputId"));
        }

        responses.add(output);
      }
      event.setResponses(responses);
      experimentProviderUtil.insertEvent(event);
    } catch (NumberFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}