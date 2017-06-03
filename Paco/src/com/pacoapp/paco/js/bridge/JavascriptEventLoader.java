package com.pacoapp.paco.js.bridge;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.model.Event;
import com.pacoapp.paco.model.EventQueryStatus;
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;
import com.pacoapp.paco.ui.FeedbackActivity;

import android.webkit.JavascriptInterface;

public class JavascriptEventLoader {

  private Logger Log = LoggerFactory.getLogger(JavascriptEventLoader.class);

  private ExperimentProviderUtil experimentProviderUtil;
  private ExperimentDAO experiment;
  private ExperimentGroup experimentGroup;
  private Experiment androidExperiment;
  private static final String FAILURE ="Failure";

  /**
   * @param androidExperiment
   *
   */
  public JavascriptEventLoader(ExperimentProviderUtil experimentProviderUtil, Experiment androidExperiment,
                               ExperimentDAO experiment, ExperimentGroup experimentGroup) {
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
    Log.error("time for loadAllEvents: " + (t2 - t1));
    return convertExperimentResultsToJsonString;
  }

  @JavascriptInterface
  public String getLastEvent() {
    return getLastNEvents("1");
  }

  @JavascriptInterface
  public String getLastNEvents(String numberOfRecords) {
    // TODO: Should this be 10; Adding a default value of 10
    int noOfRecords = 10;
    try {
      noOfRecords = Integer.parseInt(numberOfRecords);
    } catch (NumberFormatException nfe) {
      Log.error("Not a valid number of records :" + numberOfRecords);
    }
    long t1 = System.currentTimeMillis();
    List<Event> events = experimentProviderUtil.loadEventsForExperimentByServerId(experiment.getId(), noOfRecords);
    long t2 = System.currentTimeMillis();
    Log.info("Time for getLastNEvents: " + (t2 - t1));
    return FeedbackActivity.convertEventsToJsonString(events);
  }

  @JavascriptInterface
  public String getEventsForExperimentGroup() {
    List<Event> events = experimentProviderUtil.loadEventsForExperimentGroup(androidExperiment.getId(), experimentGroup.getName());
    return FeedbackActivity.convertEventsToJsonString(events);
  }

 /**
   * The query JSON should have the following format
   * Example {query: {criteria: " (group_name in (?,?) and (answer=?)) ",
   *                  values:["New Group","Exp Group", "ven"]},
   *                  limit: "100,1",
   *                  group: "group_name",
   *                  order: "response_time",
   *                  select: ["group_name", "response_time", "experiment_name", "text", "answer"]}
   * The above JSON represents the following
   *    query->criteria: String with where clause conditions and the values replaced by '?'
   *    query->values: An array of String representing the values of the '?' expressed in query->criteria (in order).
   *    query->limit: String Number of records to limit the result set, followed by "," followed by the offset integer(startPosition)
   *    query->group: String which holds the group by column. For now, this clause is disabled programmatically.
   *    query->order: String which holds the order by columns separated by commas. Default sort order will be event._id desc
   *    query->select: An array of String which holds the column names. For now, this clause is disabled programmatically. All values will be fetched by default
   *    and executes the following query
   *    Since the query requires columns from both Events and Outputs table, we do the inner join.
   *    If the query requires columns from just Events table, it will be a plain select ......from Events
   * SELECT group_name, response_time, experiment_name, text, answer FROM events INNER JOIN outputs ON events._id = event_id WHERE ( (group_name in(?,?) and (answer=?)) ) GROUP BY group_name ORDER BY response_time limit 100
   * @param criteriaQuery Query conditions and clauses in JSON format mentioned above
   * @return Json string of EventQueryStatus object, which holds boolean status - success/failure. If success, it would also include
   *         list of Events. If Failure, it would include the reason for failure.
   * @throws JSONException
   * @throws Exception
   */
  @JavascriptInterface
  public String getEventsByQuery(String criteriaQuery) {
    EventQueryStatus qryStatus = null;
    String qryOutput = null;
    boolean enableGrpByAndProjection = false;
    try {
      SQLQuery sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(criteriaQuery, enableGrpByAndProjection);
      if (sqlQueryObj != null) {
        qryStatus = experimentProviderUtil.findEventsByCriteriaQuery(sqlQueryObj, experiment.getId());
      } else {
        qryStatus = new EventQueryStatus();
        qryStatus.setStatus(FAILURE);
        qryStatus.setErrorMessage(ErrorMessages.NOT_VALID_DATA.getDescription());
      }
    } catch (JSONException jsone){
      qryStatus = new EventQueryStatus();
      qryStatus.setStatus(FAILURE);
      qryStatus.setErrorMessage(ErrorMessages.JSON_EXCEPTION.getDescription() + jsone);
    } catch (Exception ex){
      qryStatus = new EventQueryStatus();
      qryStatus.setStatus(FAILURE);
      qryStatus.setErrorMessage(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ex);
    }
    qryOutput = FeedbackActivity.convertEventQueryStatusToJsonString(qryStatus);
    return qryOutput;
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
      Log.error("NumberFormatException: ", e);
      e.printStackTrace();
    } catch (JSONException e) {
      Log.error("JSONException: ", e);
    }
  }
}