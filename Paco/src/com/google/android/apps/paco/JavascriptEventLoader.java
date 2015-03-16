package com.google.android.apps.paco;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.paco.shared.model2.ExperimentGroup;

public class JavascriptEventLoader {
  private ExperimentProviderUtil experimentProviderUtil;
  private Experiment experiment;
  private ExperimentGroup experimentGroup;

  /**
   *
   */
  public JavascriptEventLoader(ExperimentProviderUtil experimentProviderUtil, Experiment experiment, ExperimentGroup experimentGroup) {
    this.experimentProviderUtil = experimentProviderUtil;
    this.experiment = experiment;
    this.experimentGroup = experimentGroup;
  }

  public String getAllEvents() {
    return loadAllEvents();
  }

  public String loadAllEvents() {
    long t1 = System.currentTimeMillis();
    experimentProviderUtil.loadEventsForExperiment(experiment);
    String convertExperimentResultsToJsonString = FeedbackActivity.convertExperimentResultsToJsonString( experimentGroup.getFeedback(), experiment);
    long t2= System.currentTimeMillis();
    Log.e(PacoConstants.TAG, "time for loadAllEvents: " + (t2 - t1));
    return convertExperimentResultsToJsonString;
  }

  public String getLastEvent() {
    final com.google.paco.shared.model2.Feedback feedback = experimentGroup.getFeedback();
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
      String experimentGroupName = null;
      if (eventJson.has("experimentGroup")) {
        experimentGroupName = eventJson.getString("experimentGroup");
      }

      Long actionTriggerId = null;
      Long actionTriggerSpecId = null;
      Long actionId = null;

      if (eventJson.has("actionTriggerId")) {
        String actionTriggerIdStr = eventJson.getString("actionTriggerId");
        if (!Strings.isNullOrEmpty(actionTriggerIdStr)) {
          actionTriggerId = Long.parseLong(actionTriggerIdStr);
        }

      }

      if (eventJson.has("actionTriggerSpecId")) {
        String actionTriggerSpecIdStr = eventJson.getString("actionTriggerSpecId");
        if (!Strings.isNullOrEmpty(actionTriggerSpecIdStr)) {
          actionTriggerSpecId = Long.parseLong(actionTriggerSpecIdStr);
        }
      }

      if (eventJson.has("actionId")) {
        String actionIdStr = eventJson.getString("actionId");
        if (!Strings.isNullOrEmpty(actionIdStr)) {
          actionId = Long.parseLong(actionIdStr);
        }
      }


      Event event = EventUtil.createEvent(experiment, experimentGroupName, scheduledTime, actionTriggerId, actionTriggerSpecId, actionId);

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