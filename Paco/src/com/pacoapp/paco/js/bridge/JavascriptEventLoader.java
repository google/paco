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
import com.pacoapp.paco.model.EventUtil;
import com.pacoapp.paco.model.Experiment;
import com.pacoapp.paco.model.ExperimentProviderUtil;
import com.pacoapp.paco.model.Output;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.ui.FeedbackActivity;

import android.webkit.JavascriptInterface;

public class JavascriptEventLoader {

  private Logger Log = LoggerFactory.getLogger(JavascriptEventLoader.class);

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
    Log.error("time for loadAllEvents: " + (t2 - t1));
    return convertExperimentResultsToJsonString;
  }

  //TODO: check if method has references
  @JavascriptInterface
  public String getLastEvent() {
	  return getLastNEvent("1");
  }
  
  @JavascriptInterface
  public String getLastNEvent(String numberOfRecords) {
	// TODO: Should this be 10; Adding a default value of 10
	int noOfRecords=10;
	try{  
		noOfRecords = Integer.parseInt(numberOfRecords);
	}catch(NumberFormatException nfe){
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
  
  @JavascriptInterface
  public String getEventsByQuery(String criteriaQuery) {
	  List<Event> events = null;
	  try{
    	String criteriaColumns = null;
    	String[] projectionColumns =null;
    	String groupBy = null;
    	String[] criteriaValue = null;
     	String sortOrder = null;
    	String limitRecords = null;
    	JSONObject criteriaQueryObj = new JSONObject(criteriaQuery);
    	
    	if (criteriaQueryObj.has("select")){
    		JSONArray selectAr = criteriaQueryObj.getJSONArray("select");
    		if(selectAr!=null){
	    		projectionColumns = new String[selectAr.length()];
	    		for(int j=0; j<selectAr.length();j++){
	    			projectionColumns[j]=selectAr.getString(j);
	    		}
    		}
    	}
    	
    	if(criteriaQueryObj.has("query")){
    		JSONObject queryCriteria = criteriaQueryObj.getJSONObject("query");
    		if(queryCriteria!=null){
		    	if (queryCriteria.has("criteria")){
		    		criteriaColumns = queryCriteria.getString("criteria");
		    	}
		    	
		    	if (queryCriteria.has("values")){
		    		JSONArray cv = queryCriteria.getJSONArray("values");
		    		criteriaValue = new String[cv.length()];
		    		for(int i=0; i<cv.length();i++){
		    			criteriaValue[i]=cv.getString(i);
		    		}
		    	}
    		}
    	}
    	
    	if (criteriaQueryObj.has("order")){    	
    		sortOrder = criteriaQueryObj.getString("order");
    	}
    	
    	if (criteriaQueryObj.has("limit")){
    		limitRecords = criteriaQueryObj.getString("limit");
    	}
    	
    	if (criteriaQueryObj.has("group")){
    		groupBy = criteriaQueryObj.getString("group");
    	}
    	
    	events = experimentProviderUtil.findEventsByQuery(projectionColumns,  criteriaColumns, criteriaValue, sortOrder, limitRecords, groupBy);
    	
    }catch(JSONException je){
    	Log.error("Invalid JSON in criteria query" + je);
    }
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
      Log.error("NumberFormatException: ", e);
      e.printStackTrace();
    } catch (JSONException e) {
      Log.error("JSONException: ", e);
    }
  }
}