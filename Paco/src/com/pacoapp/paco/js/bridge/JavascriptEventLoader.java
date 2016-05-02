package com.pacoapp.paco.js.bridge;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.PacoConstants;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.ui.FeedbackActivity;

public class JavascriptEventLoader {
  private ExperimentProviderUtil experimentProviderUtil;
  private ExperimentDAO experiment;
  private ExperimentGroup experimentGroup;
  private Experiment androidExperiment;

  /**
   * @param androidExperiment
   *
   */
  public JavascriptEventLoader(ExperimentProviderUtil experimentProviderUtil, Experiment androidExperiment, ExperimentDAO experiment, ExperimentGroup experimentGroup) {
    this.experimentProviderUtil = experimentProviderUtil;
    this.androidExperiment = androidExperiment;
    this.experiment = experiment;
    this.experimentGroup = experimentGroup;
  }

  @JavascriptInterface
  public String getAllEvents() {
    return loadAllEvents();
  }

  @JavascriptInterface
  public String loadAllEvents() {
    long t1 = System.currentTimeMillis();
    List<Event> events = experimentProviderUtil.loadEventsForExperimentByServerId(experiment.getId());
    String convertExperimentResultsToJsonString = FeedbackActivity.convertEventsToJsonString(events);
    long t2= System.currentTimeMillis();
    Log.e(PacoConstants.TAG, "time for loadAllEvents: " + (t2 - t1));
    return convertExperimentResultsToJsonString;
  }

  @JavascriptInterface
  public String getLastEvent() {
    // TODO make this class manage retrieval better so that we aren't pulling tons of data into the webview.
    List<Event> events = experimentProviderUtil.loadEventsForExperimentByServerId(experiment.getId());
    return FeedbackActivity.convertLastEventToJsonString(events);
  }

  @JavascriptInterface
  public String getEventsForExperimentGroup() {
    List<Event> events = experimentProviderUtil.loadEventsForExperimentGroup(experiment.getId(), experimentGroup.getName());
    return FeedbackActivity.convertEventsToJsonString(events);
  }

  /**
   * Backward compatible alias for saveEvent
   * @param json
   */
  @JavascriptInterface
  public void saveResponse(String json) {
     saveEvent(json);
  }

  @JavascriptInterface
  public void saveEvent(String json) {
    try {
      JSONObject eventJson = new JSONObject(json);

      Long scheduledTime = null;
      if (eventJson.has("scheduledTime")) {
        String scheduledTimeString = eventJson.getString("scheduledTime");
        if (!Strings.isNullOrEmpty(scheduledTimeString) && !scheduledTimeString.equals("null")) {
          scheduledTime = Long.parseLong(scheduledTimeString);
        }
      }
      String experimentGroupName = null;
      if (eventJson.has("experimentGroupName")) {
        experimentGroupName = eventJson.getString("experimentGroupName");
      }

      Long actionTriggerId = null;
      Long actionTriggerSpecId = null;
      Long actionId = null;

      if (eventJson.has("actionTriggerId")) {
        String actionTriggerIdStr = eventJson.getString("actionTriggerId");
        if (!Strings.isNullOrEmpty(actionTriggerIdStr) && !actionTriggerIdStr.equals("null")) {
          actionTriggerId = Long.parseLong(actionTriggerIdStr);
        }

      }

      if (eventJson.has("actionTriggerSpecId")) {
        String actionTriggerSpecIdStr = eventJson.getString("actionTriggerSpecId");
        if (!Strings.isNullOrEmpty(actionTriggerSpecIdStr) && !actionTriggerSpecIdStr.equals("null")) {
          actionTriggerSpecId = Long.parseLong(actionTriggerSpecIdStr);
        }
      }

      if (eventJson.has("actionId")) {
        String actionIdStr = eventJson.getString("actionId");
        if (!Strings.isNullOrEmpty(actionIdStr) && !actionIdStr.equals("null")) {
          actionId = Long.parseLong(actionIdStr);
        }
      }


      Event event = EventUtil.createEvent(androidExperiment, experimentGroupName,
                                          actionTriggerId, actionId, actionTriggerSpecId, scheduledTime);

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

        responses.add(output);
      }

      event.setResponses(responses);
      experimentProviderUtil.insertEvent(event);
    } catch (NumberFormatException e) {
      Log.e(PacoConstants.TAG, "NumberFormatException: ", e);
      e.printStackTrace();
    } catch (JSONException e) {
      Log.e(PacoConstants.TAG, "JSONException: ", e);
    }
  }
}