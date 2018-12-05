package com.google.sampling.experiential.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.TimeUtil;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.ExperimentHelper;

public class EventJsonUploadProcessor {

  private static final Logger log = Logger.getLogger(EventJsonUploadProcessor.class.getName());
  private ExperimentService experimentRetriever;
  private EventRetriever eventRetriever;

  public EventJsonUploadProcessor(ExperimentService experimentRetriever, EventRetriever eventRetriever) {
    this.eventRetriever = eventRetriever;
    this.experimentRetriever = experimentRetriever;
  }

  public static EventJsonUploadProcessor create() {
    ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    return new EventJsonUploadProcessor(experimentService, EventRetriever.getInstance());
  }

  public String processJsonEvents(String postBodyString, String whoFromLogin, String appIdHeader, String pacoVersion) {
    //This is the traditional event processing call to insert to data store.
    //This call is not from insertCloudSQl flow, where we will persist this info in cloud sql. So, we send the flagfalse
    boolean persistInCloudSqlOnly = false;
    return processJsonEvents(persistInCloudSqlOnly, postBodyString, whoFromLogin, appIdHeader, pacoVersion);
  }

  public String processJsonEvents(boolean persistInCloudSqlOnly, String postBodyString, String whoFromLogin, String appIdHeader, String pacoVersion) {
    String eventInJsonFormat=null;
    try {
      if (postBodyString.startsWith("[")) {
        final JSONArray events = new JSONArray(postBodyString);
        eventInJsonFormat = toJson(processJsonArray(persistInCloudSqlOnly, events, whoFromLogin, appIdHeader, pacoVersion));
      } else {
        final JSONObject currentEvent = new JSONObject(postBodyString);
        eventInJsonFormat = toJson(processSingleJsonEvent(persistInCloudSqlOnly, currentEvent, whoFromLogin, appIdHeader, pacoVersion));
      }

      return eventInJsonFormat;
    } catch (JSONException e) {
      throw new IllegalArgumentException("JSON Exception reading post data: " + e.getMessage());
    }
  }

  private String toJson(List<Outcome> outcomes) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      return mapper.writeValueAsString(outcomes);
    } catch (JsonGenerationException e) {
      log.warning("could not generate outcome json. " + e.getMessage());
      e.printStackTrace();
      throw new IllegalArgumentException(e);
    } catch (JsonMappingException e) {
      log.warning("could not map outcome to json. " + e.getMessage());
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      log.warning("io exception generating outcome json. " + e.getMessage());
      throw new IllegalArgumentException(e);
    }
  }

  private List<Outcome> processSingleJsonEvent(boolean persistInCloudSql, JSONObject currentEvent, String whoFromLogin, String appIdHeader, String pacoVersionHeader) {
    List<Outcome> results = Lists.newArrayList();
    try {
      results.add(postEvent(persistInCloudSql, currentEvent, 0, whoFromLogin, appIdHeader, pacoVersionHeader));
    } catch (Throwable e) {
      log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
      results.add(new Outcome(0, "Exception posting event: 0. "+ e.getMessage()));
    }
    return results;
  }

  private List<Outcome> processJsonArray(boolean persistInCloudSqlOnly, JSONArray events, String whoFromLogin, String appIdHeader, String pacoVersionHeader) {
    List<Outcome> results = Lists.newArrayList();
    JSONObject currentEvent = null;
    for (int i = 0; i < events.length(); i++) {
      try {
        currentEvent = events.getJSONObject(i);
        results.add(postEvent(persistInCloudSqlOnly, currentEvent, i, whoFromLogin, appIdHeader, pacoVersionHeader));
      } catch (JSONException e) {
        results.add(new Outcome(i, "JSONException posting event: " + i + ". " + e.getMessage()));
      } catch (Throwable e) {
        results.add(new Outcome(i, "Exception posting event: " + i + ". " + e.getMessage()));
      }
    }
    return results;
  }

  private Outcome postEvent(boolean persistInCloudSqlOnly, JSONObject eventJson, int eventId, String who, String appIdHeader, String pacoVersionHeader) throws Throwable {
    Outcome outcome = new Outcome(eventId);
    if (eventJson.has("experimentId") && eventJson.getString("experimentId").equals("5552926096359424")) {
      // ignore daydream experiment
      return outcome;
    }

    String pacoVersion = null;
    if (eventJson.has("pacoVersion")) {
      pacoVersion = eventJson.getString("pacoVersion");
    } else if (!Strings.isNullOrEmpty(pacoVersionHeader)) {
      pacoVersion = pacoVersionHeader;
    }

    String appId = null;
    if (eventJson.has("appId")) {
      appId = eventJson.getString("appId");
    } else if (appIdHeader != null) {
      appId = appIdHeader;
    } else {
      appId = "Unknown";
    }

    Date whenDate =  new Date();

    String experimentIdStr = null;
    String experimentName = null;
    Integer experimentVersion = null;
    DateTime responseTime = null;
    DateTime scheduledTime = null;
    String groupName = null;

    if (eventJson.has("experimentId")) {
      experimentIdStr = eventJson.getString("experimentId");
    }
    if (eventJson.has("experimentName")) {
      experimentName = eventJson.getString("experimentName");
    }

    if (eventJson.has("experimentVersion")) {
      String experimentVersionStr= eventJson.getString("experimentVersion");
      if (!Strings.isNullOrEmpty(experimentVersionStr)) {
        try {
          experimentVersion = Integer.parseInt(experimentVersionStr);
        } catch (Exception e) {

        }
      }
    }

    if (eventJson.has("experimentGroupName")) {
      groupName = eventJson.getString("experimentGroupName");
    }

    Long actionTriggerId = null;
    if (eventJson.has("actionTriggerId")) {
      String actionTriggerIdStr = eventJson.getString("actionTriggerId");
      if (!Strings.isNullOrEmpty(actionTriggerIdStr) && !actionTriggerIdStr.equals("null")) {
        actionTriggerId = Long.parseLong(actionTriggerIdStr);
      }

    }
    Long actionTriggerSpecId = null;
    if (eventJson.has("actionTriggerSpecId")) {
      String actionTriggerSpecIdStr = eventJson.getString("actionTriggerSpecId");
      if (!Strings.isNullOrEmpty(actionTriggerSpecIdStr) && !actionTriggerSpecIdStr.equals("null")) {
        actionTriggerSpecId = Long.parseLong(actionTriggerSpecIdStr);
      }
    }
    Long actionId = null;
    if (eventJson.has("actionId")) {
      String actionIdStr = eventJson.getString("actionId");
      if (!Strings.isNullOrEmpty(actionIdStr) && !actionIdStr.equals("null")) {
        actionId = Long.parseLong(actionIdStr);
      }
    }


    log.info("Retrieving experimentId, experimentName for event posting: " + experimentIdStr + ", " + experimentName);
    if (experimentIdStr == null) {
      log.info("Could not find experiment for event posting. experimentId, experimentName: " + experimentIdStr + ", " + experimentName);
      //outcome.setError("No experiment ID for this event: " + eventId);
      return outcome;
    }
    Long experimentIdLong = null;
    try {
      experimentIdLong = Long.parseLong(experimentIdStr);
    } catch (NumberFormatException e) {
      log.info("experimentId, " + experimentIdStr + ", not a number for this event: " + eventId);
      //outcome.setError("experimentId, " + experimentIdStr + ", not a number for this event: " + eventId);
      return outcome;
    }

    log.info("start retrieving experiment");
    ExperimentDAO experiment = null;
    try {
      experiment = experimentRetriever.getExperiment(experimentIdLong);
    } catch (Exception e) {
      log.severe("caught exception retrieving experiment" + e.getMessage());
      e.printStackTrace();
      throw e;
    }
    log.info("end retrieving experiment");

    if (experiment == null) {
      //outcome.setError("No existing experiment for this event: " + eventId);
      log.info("No existing experiment for this event: " + eventId + ", who = " + who + ", experimentId = " + experimentIdStr);
      //return outcome;
    } else {
      log.info("Found the experiment: " + experimentIdStr);
    }
    // We retrieve the 'who' value from json, only when it's a request coming from
    // cloud sql queue.
    if(persistInCloudSqlOnly){
      who = eventJson.getString("who");
    }

    if (!experiment.isWhoAllowedToPostToExperiment(who)) {
      // don't give differentiated error messages in case someone is trying to discover experiment ids
      log.info("User not allowed to post to this experiment " + experimentIdStr + " .Event: " + eventId + " user: " + who);
      //outcome.setError("No existing experiment for this event: " + eventId);
      return outcome;
    }

//    log.info("Starting to read responses");
    Set<What> whats = Sets.newHashSet();
    List<PhotoBlob> blobs = Lists.newArrayList();
    if (eventJson.has("responses")) {
      JSONArray responses = eventJson.getJSONArray("responses");
//      log.info("There are " + responses.length() + " response objects");

      for (int i = 0; i < responses.length(); i++) {
        JSONObject response = responses.getJSONObject(i);
        String name = response.getString("name");


        Input2 input = null;
        if (input == null) {
          input = ExperimentHelper.getInputWithName(experiment, name, groupName);
        }
//        if (input != null) {
//          log.info("Input name, responseType: " + input.getName() + ", " + input.getResponseType());
//        } else {
//          log.info("input is null for name, group: " + name +", " + groupName);
//        }

        String answer = null;
        if (response.has("answer")) {
          answer = response.getString("answer");
        }

        if (input != null && input.getResponseType() != null && input.getResponseType().equals(Input2.PHOTO) && !Strings.isNullOrEmpty(answer)) {
          PhotoBlob photoBlob = new PhotoBlob(name, Base64.decodeBase64(answer.getBytes()));
          blobs.add(photoBlob);
          answer = "blob";
        } else if (input != null && input.getResponseType() != null && input.getResponseType().equals(Input2.AUDIO) && !Strings.isNullOrEmpty(answer)) {
          // TODO Store audio in Google Cloud Storage
          PhotoBlob photoBlob = new PhotoBlob(name, Base64.decodeBase64(answer.getBytes()));
          blobs.add(photoBlob);
          answer = "audioblob";
        } else if (answer != null && answer.length() >= 500) {
//          log.info("The response was too long for: " + name + ".");
//          log.info("Response was " + answer);
          answer = answer.substring(0, 497) + "...";
        }

        whats.add(new What(name, answer));

      }
    } else {
//      log.info("There is no responses section for this event");
    }

    DateTimeFormatter df = org.joda.time.format.DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT).withOffsetParsed();

    if (eventJson.has("responseTime")) {
      String responseTimeStr = eventJson.getString("responseTime");
      if (!responseTimeStr.equals("null") && !responseTimeStr.isEmpty()) {
        responseTime = parseDate(df, responseTimeStr);
//        log.info("Response TIME check" + responseTimeStr);
//        log.info(" = " + responseTime != null ? responseTime.toString() : "");
      }
    }
    if (eventJson.has("scheduledTime")) {
      String timeStr = eventJson.getString("scheduledTime");
      if (!timeStr.equals("null") && !timeStr.isEmpty()) {
        scheduledTime = parseDate(df, timeStr);
//        log.info("Schedule TIME check" + timeStr);
//        log.info(" = " + scheduledTime != null ? scheduledTime.toString() : "");
      }
    }


//    log.info("Sanity check: who = " + who + ", when = "
//             + (new SimpleDateFormat(TimeUtil.DATETIME_FORMAT)).format(whenDate)
//             + ", what length = " + whats.size());


    eventRetriever.postEvent(persistInCloudSqlOnly, eventJson, who, null, null, whenDate, appId, pacoVersion, whats, false, experimentIdStr,
                                           experimentName, experimentVersion, responseTime, scheduledTime, blobs,
                                           groupName, actionTriggerId, actionTriggerSpecId, actionId);

    return outcome;
  }

  private DateTime parseDate(DateTimeFormatter df, String when) throws ParseException {
    return df.parseDateTime(when);
  }


}
