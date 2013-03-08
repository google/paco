package com.google.sampling.experiential.server;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.paco.shared.Outcome;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.TimeUtil;

public class EventJsonUploadProcessor {

  private static final Logger log = Logger.getLogger(EventJsonUploadProcessor.class.getName());
  private ExperimentRetriever experimentRetriever;
  private EventRetriever eventRetriever;
  
  public EventJsonUploadProcessor(ExperimentRetriever experimentRetriever, EventRetriever eventRetriever) {
    this.experimentRetriever = experimentRetriever;
    this.eventRetriever = eventRetriever;
  }

  public static EventJsonUploadProcessor create() {
    return new EventJsonUploadProcessor(ExperimentRetriever.getInstance(), EventRetriever.getInstance());
  }

  public String processJsonEvents(String postBodyString, String whoFromLogin) {
    try {
      if (postBodyString.startsWith("[")) {
        return toJson(processJsonArray(new JSONArray(postBodyString), whoFromLogin));
      } else {
        return toJson(processSingleJsonEvent(new JSONObject(postBodyString), whoFromLogin));
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("JSON Exception reading post data: " + e.getMessage());
    }
  }

  private String toJson(List<Outcome> outcomes) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
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

  private List<Outcome> processSingleJsonEvent(JSONObject currentEvent, String whoFromLogin) {
    List<Outcome> results = Lists.newArrayList();
    try {
      results.add(postEvent(currentEvent, 0, whoFromLogin));
    } catch (Throwable e) {
      results.add(new Outcome(0, "Exception posting event: 0. "+ e.getMessage()));
    }
    return results;
  }

  private List<Outcome> processJsonArray(JSONArray events, String whoFromLogin) {
    List<Outcome> results = Lists.newArrayList();
    JSONObject currentEvent = null;
    for (int i = 0; i < events.length(); i++) {
      try {
        currentEvent = events.getJSONObject(i);
        results.add(postEvent(currentEvent, i, whoFromLogin));
      } catch (JSONException e) {
        results.add(new Outcome(i, "JSONException posting event: " + i + ". " + e.getMessage()));
      } catch (Throwable e) {
        results.add(new Outcome(i, "Exception posting event: " + i + ". " + e.getMessage()));
      }          
    }
    return results;
  }

  private Outcome postEvent(JSONObject eventJson, int eventId, String who) throws Throwable {
    Outcome outcome = new Outcome(eventId);
    
    String pacoVersion = null;
    if (eventJson.has("pacoVersion")) {
      pacoVersion = eventJson.getString("pacoVersion");
    }
    Date whenDate =  new Date();
    
    String experimentId = null;
    String experimentName = null;
    Integer experimentVersion = null;
    Date responseTime = null;
    Date scheduledTime = null;

    if (eventJson.has("experimentId")) {
      experimentId = eventJson.getString("experimentId");
    }
    if (eventJson.has("experimentName")) {
      experimentName = eventJson.getString("experimentName");
    }
    
    if (eventJson.has("experimentVersion")) {
      experimentVersion = eventJson.getInt("experimentVersion");
    }
    log.info("Retrieving experimentId, experimentName for event posting: " + experimentId + ", " + experimentName);
    Experiment experiment = experimentRetriever.getExperiment(experimentId);

    if (experiment == null) {
      outcome.setError("No existing experiment for this event: " + eventId);
      return outcome;
    }

    if (!experiment.isWhoAllowedToPostToExperiment(who)) {
      // don't give differentiated error messages in case someone is trying to discover experiment ids
      outcome.setError("No existing experiment for this event: " + eventId);      
      return outcome;
    }

    Set<What> whats = Sets.newHashSet();
    List<PhotoBlob> blobs = Lists.newArrayList();
    if (eventJson.has("responses")) {
      JSONArray responses = eventJson.getJSONArray("responses");
      log.info("There are " + responses.length() + " response objects");
      for (int i = 0; i < responses.length(); i++) {
        JSONObject response = responses.getJSONObject(i);
        String name = response.getString("name");

        String inputId = response.getString("inputId");
        Input input = null;
        if (experiment != null) {
          input = experiment.getInputWithId(Long.valueOf(inputId));
        }
        if (input == null) {
          input = experiment.getInputWithName(name);
        }
        if (input != null) {
          log.info("Input name, responseType: " + input.getName() + ", " + input.getResponseType());
        } else {
          log.info("input is null for inputId: " + inputId);
        }

        String answer = response.getString("answer");

        if (input != null && input.getResponseType() != null && input.getResponseType().equals(InputDAO.PHOTO)) {
          PhotoBlob photoBlob = new PhotoBlob(name, Base64.decodeBase64(answer.getBytes()));
          blobs.add(photoBlob);
          answer = "blob";
        } else if (answer.length() >= 500) {
          log.info("The response was too long for: " + name + ".");
          log.info("Response was " + answer);
          answer = answer.substring(0, 497) + "...";
        }
        
        if (Strings.isNullOrEmpty(name) && (input == null || Strings.isNullOrEmpty(input.getName()))) {
          name = "unnamed_input_" + i;
          whats.add(new What(name, inputId));
        }
        whats.add(new What(name, answer));
      }
    }

    SimpleDateFormat df = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT);
    SimpleDateFormat oldDf = new SimpleDateFormat(TimeUtil.DATETIME_FORMAT_OLD);

    if (eventJson.has("responseTime")) {
      String responseTimeStr = eventJson.getString("responseTime");
      if (!responseTimeStr.equals("null") && !responseTimeStr.isEmpty()) {
        responseTime = parseDate(df, oldDf, responseTimeStr);
      }
    }
    if (eventJson.has("scheduledTime")) {
      String timeStr = eventJson.getString("scheduledTime");
      if (!timeStr.equals("null") && !timeStr.isEmpty()) {
        scheduledTime = parseDate(df, oldDf, timeStr);
      }
    }

    log.info("Sanity check: who = " + who + ", when = "
             + (new SimpleDateFormat(TimeUtil.DATETIME_FORMAT)).format(whenDate) 
             + ", what length = " + whats.size());

    eventRetriever.postEvent(who, null, null, whenDate, null, pacoVersion, whats, false, experimentId,
                                           experimentName, experimentVersion, responseTime, scheduledTime, blobs);
    return outcome;
  }

  private Date parseDate(SimpleDateFormat df, SimpleDateFormat oldDf, String when) throws ParseException {
    return df.parse(when);
  }


}
