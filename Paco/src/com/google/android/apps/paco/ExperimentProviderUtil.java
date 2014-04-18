/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.android.apps.paco;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.FeedbackDAO;

public class ExperimentProviderUtil {

  private Context context;
  private ContentResolver contentResolver;
  public static final String AUTHORITY = "com.google.android.apps.paco.ExperimentProvider";
  private static final String PUBLIC_EXPERIMENTS_FILENAME = "experiments";
  private static final String MY_EXPERIMENTS_FILENAME = "my_experiments";

  DateTimeFormatter endDateFormatter = DateTimeFormat.forPattern(TimeUtil.DATE_FORMAT);

  public ExperimentProviderUtil(Context context) {
    super();
    this.context = context;
    this.contentResolver = context.getContentResolver();
  }

  public List<Experiment> getExperiments() {
    return findExperimentsBy(null, ExperimentColumns.CONTENT_URI);

  }

  public List<Experiment> getJoinedExperiments() {
    return findExperimentsBy(null, ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
  }

  public List<Long> getJoinedExperimentServerIds() {
    List<Experiment> joinedExperiments = getJoinedExperiments();
    List<Experiment> stillRunningExperiments = Lists.newArrayList();
    DateMidnight tonightMidnight = new DateMidnight().plusDays(1);
    for (Experiment experiment : joinedExperiments) {
      String endDate = experiment.getEndDate();
      if (experiment.isFixedDuration() != null && experiment.isFixedDuration() || endDate == null || endDateFormatter.parseDateTime(endDate).isAfter(tonightMidnight)) {
        stillRunningExperiments.add(experiment);
      }
    }
    List<Long> experimentIds = Lists.transform(stillRunningExperiments, new Function<Experiment, Long>() {
      public Long apply(Experiment experiment) {
        return experiment.getServerId();
      }
    });
    return experimentIds;
  }

  public Experiment getExperiment(Uri uri) {
    return findExperimentBy(null, uri);
  }


  public Experiment getExperiment(long id) {
    String select = ExperimentColumns._ID + "=" + id;
    return findExperimentBy(select, ExperimentColumns.CONTENT_URI);
  }

  public List<Experiment> getExperimentsByServerId(long id) {
    String select = ExperimentColumns.SERVER_ID + "=" + id;
    return findExperimentsBy(select, ExperimentColumns.CONTENT_URI);
  }

  public Uri insertExperiment(Experiment experiment) {
    return contentResolver.insert(ExperimentColumns.CONTENT_URI,
        createContentValues(experiment));
  }

  /**
   * This operation takes a default experiment from the server,
   * and clones it to created an experiment record that represents
   * an experiment the user has joined. This ensures that even if the
   * experiment is no longer available on the server, or is updated for
   * future users, it remains the same for this user by default.
   * Future work could ask them if they want to upgrade their
   * joined experiment to a new version of the experiment on the
   * server, but that is out of scope for now.
   * @param experiment
   * @return
   */
  public Uri insertFullJoinedExperiment(Experiment experiment) {
    // fully load the other parts of the experiment prototype
    // before saving a cloned version
//    loadInputsForExperiment(experiment);
//    loadFeedbackForExperiment(experiment);
//    loadScheduleForExperiment(experiment);
//    experiment.unsetId();

    Uri uri = contentResolver.insert(ExperimentColumns.CONTENT_URI,
        createContentValues(experiment));

    long rowId = Long.parseLong(uri.getLastPathSegment());

    experiment.setId(rowId);
    SignalSchedule schedule = experiment.getSchedule();
    if (schedule != null) {
      schedule.setExperimentId(rowId);
      schedule.setBeginDate(getJoinDateMillis(experiment));
      insertSchedule(schedule);
    }

    for (Input input : experiment.getInputs()) {
//      input.setId(null);
      input.setExperimentId(rowId);
      insertInput(input);
    }
    for (Feedback feedback : experiment.getFeedback()) {
//      feedback.setId(null);
      feedback.setExperimentId(rowId);
      insertFeedback(feedback);
    }

    return uri;
  }

  private Long getJoinDateMillis(Experiment experiment) {
    return TimeUtil.unformatDateWithZone(experiment.getJoinDate()).getMillis();
  }

  // For testing
  public Uri insertFullJoinedExperiment(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    Experiment experiment = getSingleExperimentFromJson(contentAsString);
    experiment.setJoinDate(TimeUtil.formatDateWithZone(new DateTime()));
    return insertFullJoinedExperiment(experiment);
  }

  private void loadScheduleForExperiment(Experiment experiment) {
      String select = SignalScheduleColumns.EXPERIMENT_ID + " = " + experiment.getId();
      SignalSchedule schedule = findScheduleBy(select);
      if (schedule != null) {
        List<SignalingMechanism> signalingMechanisms = new ArrayList<SignalingMechanism>();
        signalingMechanisms.add(schedule);
        experiment.setSchedule(schedule);
        experiment.setSignalingMechanisms(signalingMechanisms);
      }
  }

  private SignalSchedule findScheduleBy(String select) {
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(SignalScheduleColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null && cursor.moveToNext()) {
        return createSchedule(cursor);
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  public void deleteExperiment(long experimentId) {
    Experiment experiment = getExperiment(experimentId);
    if (experiment != null) {
      String[] selectionArgs = new String[] {Long.toString(experimentId)};
      contentResolver.delete(ExperimentColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
    }
  }

  public void updateExistingExperiments(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    Map<String, Object> results = fromEntitiesJson(contentAsString);
    List<Experiment> experimentList = (List<Experiment>) results.get("results");
    updateExistingExperiments(experimentList);
  }

  /**
   * Used when refreshing experiment list from the server.
   * If the experiment server id is already in the database,
   * then update it, otherwise, add it.
   * @param experiments
   */
  public void updateExistingExperiments(List<Experiment> experiments) {
    for (Experiment experiment : experiments) {
      //Log.i(PacoConstants.TAG, "experiment = " + experiment.getTitle() + ", serverId = " + experiment.getServerId());
      List<Experiment> existingList = getExperimentsByServerId(experiment.getServerId());
      if (existingList.size() == 0) {
        continue;
      }
      for (Experiment existingExperiment : existingList) {
        long startTime = System.currentTimeMillis();
        deleteAllInputsForExperiment(existingExperiment.getId());
        existingExperiment.setInputs(experiment.getInputs());
        deleteAllFeedbackForExperiment(existingExperiment.getId());
        existingExperiment.setFeedback(experiment.getFeedback());
        SignalSchedule schedule = experiment.getSchedule();
        if (schedule != null) {
          schedule.setExperimentId(existingExperiment.getId());
          existingExperiment.setSchedule(schedule);
          List<SignalingMechanism> signalingMechanisms = new ArrayList<SignalingMechanism>();
          signalingMechanisms.add(schedule);
          existingExperiment.setSignalingMechanisms(signalingMechanisms);
          insertSchedule(schedule);
        } else {
          List<SignalingMechanism> signalingMechanisms = new ArrayList<SignalingMechanism>();
          signalingMechanisms.add(experiment.getTrigger());
          existingExperiment.setSignalingMechanisms(signalingMechanisms);
        }
        insertInputsForJoinedExperiment(existingExperiment);
        insertFeedbackForJoinedExperiment(existingExperiment);
        copyAllPropertiesToExistingJoinedExperiment(experiment, existingExperiment);
        updateJoinedExperiment(existingExperiment);
        Log.i(PacoConstants.TAG, "Time to update one existing joined experiment: " + (System.currentTimeMillis() - startTime));
      }

    }
  }

  private void copyAllPropertiesToExistingJoinedExperiment(Experiment experiment, Experiment existingExperiment) {
    existingExperiment.setCreator(experiment.getCreator());
    existingExperiment.setVersion(experiment.getVersion());
    existingExperiment.setDescription(experiment.getDescription());
    existingExperiment.setEndDate(experiment.getEndDate());
    existingExperiment.setFixedDuration(experiment.isFixedDuration());
    existingExperiment.setIcon(experiment.getIcon());
    existingExperiment.setInformedConsentForm(experiment.getInformedConsentForm());
    existingExperiment.setQuestionsChange(experiment.isQuestionsChange());
    existingExperiment.setStartDate(experiment.getStartDate());
    existingExperiment.setTitle(experiment.getTitle());
    existingExperiment.setWebRecommended(experiment.isWebRecommended());
    existingExperiment.setCustomRendering(experiment.isCustomRendering());
    existingExperiment.setCustomRenderingCode(experiment.getCustomRenderingCode());
    existingExperiment.setJson(getJson(experiment));
  }

  private void deleteFullExperiment(Experiment experiment2) {
    deleteScheduleForExperiment(experiment2.getId());
    deleteAllInputsForExperiment(experiment2.getId());
    deleteAllFeedbackForExperiment(experiment2.getId());
    deleteExperiment(experiment2.getId());
  }

  private int deleteScheduleForExperiment(Long id) {
    return contentResolver.delete(SignalScheduleColumns.CONTENT_URI,
        SignalScheduleColumns.EXPERIMENT_ID + " = " + id, null);
  }

  private int deleteAllFeedbackForExperiment(Long id) {
    return contentResolver.delete(FeedbackColumns.CONTENT_URI,
        FeedbackColumns.EXPERIMENT_ID + " = " + id, null);
  }

  private int deleteAllInputsForExperiment(Long id) {
    return contentResolver.delete(InputColumns.CONTENT_URI,
          InputColumns.EXPERIMENT_ID + " = " + id, null);
  }

  public void updateJoinedExperiment(Experiment experiment) {
    int count = contentResolver.update(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
        createContentValues(experiment),
        ExperimentColumns._ID + "=" + experiment.getId(), null);
    Log.i(ExperimentProviderUtil.class.getSimpleName(), "updated "+ count + " rows");
    SignalSchedule schedule = experiment.getSchedule();
    if (schedule != null) {
      updateSchedule(schedule);
    }
  }

  public void deleteAllExperiments() {
    contentResolver.delete(ExperimentColumns.CONTENT_URI, null, null);
    contentResolver.delete(SignalScheduleColumns.CONTENT_URI, null, null);
    contentResolver.delete(InputColumns.CONTENT_URI, null, null);
    contentResolver.delete(FeedbackColumns.CONTENT_URI, null, null);
  }

  public void deleteAllJoinedExperiments() {
    // TODO first select all joined_experiments ids.
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
          new String[] { ExperimentColumns._ID },
          ExperimentColumns.JOIN_DATE + " IS NOT NULL ",
          null, null);
      if (cursor != null) {
        StringBuilder idsStringBuilder = new StringBuilder();
        int lineCount = 0;
        while (cursor.moveToNext()) {
          if (lineCount > 0) {
            idsStringBuilder.append(",");
          }
          idsStringBuilder.append(cursor.getString(0));
          lineCount++;
        }
        String idsString = idsStringBuilder.toString();
        contentResolver.delete(
            ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, null, null);
        // TODO delete all from child tables where experiment_ids match
        contentResolver.delete(SignalScheduleColumns.CONTENT_URI,
            InputColumns.EXPERIMENT_ID + " in (" + idsString + ")", null);
        contentResolver.delete(InputColumns.CONTENT_URI,
            InputColumns.EXPERIMENT_ID + " in (" + idsString + ")", null);
        contentResolver.delete(FeedbackColumns.CONTENT_URI,
            FeedbackColumns.EXPERIMENT_ID + " in (" + idsString + ")", null);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

  }

  private Experiment findExperimentBy(String select, Uri contentUri) {
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(contentUri,
          null, select, null, null);
      if (cursor != null && cursor.moveToNext()) {
        Experiment experiment = createExperiment(cursor);
        if (experiment.getTrigger() == null) {
          loadScheduleForExperiment(experiment);
        }
        return experiment;
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  private List<Experiment> findExperimentsBy(String select, Uri contentUri) {
    List<Experiment> experiments = new ArrayList<Experiment>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(contentUri,
          null, select, null, null);
      if (cursor != null) {
        while (cursor.moveToNext()) {
         Experiment experiment = createExperiment(cursor);
         loadScheduleForExperiment(experiment);
         experiments.add(experiment);
        }
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return experiments;
  }


  private Experiment createExperiment(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(ExperimentColumns._ID);
    int serverIdIndex = cursor.getColumnIndexOrThrow(ExperimentColumns.SERVER_ID);
    int titleIndex = cursor.getColumnIndex(ExperimentColumns.TITLE);
    int versionIndex = cursor.getColumnIndex(ExperimentColumns.VERSION);
    int descIndex = cursor.getColumnIndex(ExperimentColumns.DESCRIPTION);
    int creatorIndex = cursor.getColumnIndex(ExperimentColumns.CREATOR);
    int icIndex = cursor.getColumnIndex(ExperimentColumns.INFORMED_CONSENT);
    int hashIndex = cursor.getColumnIndex(ExperimentColumns.HASH);
    int fixedDurationIndex = cursor.getColumnIndex(ExperimentColumns.FIXED_DURATION);
    int startDateIndex = cursor.getColumnIndex(ExperimentColumns.START_DATE);
    int endDateIndex = cursor.getColumnIndex(ExperimentColumns.END_DATE);
    int joinDateIndex = cursor.getColumnIndex(ExperimentColumns.JOIN_DATE);
    int questionsChangeIndex = cursor.getColumnIndex(ExperimentColumns.QUESTIONS_CHANGE);
    int iconIndex = cursor.getColumnIndex(ExperimentColumns.ICON);
    int webRecommendedIndex = cursor.getColumnIndex(ExperimentColumns.WEB_RECOMMENDED);
    int jsonIndex = cursor.getColumnIndex(ExperimentColumns.JSON);

    Experiment experiment = new Experiment();

    if (!cursor.isNull(idIndex)) {
      experiment.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(serverIdIndex)) {
      experiment.setServerId(cursor.getLong(serverIdIndex));
    }

    if (!cursor.isNull(titleIndex)) {
      experiment.setTitle(cursor.getString(titleIndex));
    }

    if (!cursor.isNull(versionIndex)) {
      experiment.setVersion(cursor.getInt(versionIndex));
    }

    if (!cursor.isNull(descIndex)) {
      experiment.setDescription(cursor.getString(descIndex));
    }

    if (!cursor.isNull(creatorIndex)) {
      experiment.setCreator(cursor.getString(creatorIndex));
    }

    if (!cursor.isNull(icIndex)) {
      experiment.setInformedConsentForm(cursor.getString(icIndex));
    }

    if (!cursor.isNull(hashIndex)) {
      experiment.setHash(cursor.getString(hashIndex));
    }

    if (!cursor.isNull(fixedDurationIndex)) {
      experiment.setFixedDuration(cursor.getLong(fixedDurationIndex) == 1);
    }

    if (!cursor.isNull(startDateIndex)) {
      experiment.setStartDate(cursor.getString(startDateIndex));
    }

    if (!cursor.isNull(endDateIndex)) {
      experiment.setEndDate(cursor.getString(endDateIndex));
    }

    if (!cursor.isNull(joinDateIndex)) {
      // TODO (bobevans) add the timezone from the user. The default is probably fine for now.
      experiment.setJoinDate(cursor.getString(joinDateIndex));
    }

    if (!cursor.isNull(questionsChangeIndex)) {
      experiment.setQuestionsChange(cursor.getLong(questionsChangeIndex) == 1);
    }

    if (!cursor.isNull(iconIndex)) {
      experiment.setIcon(cursor.getBlob(iconIndex));
    }

    if (!cursor.isNull(webRecommendedIndex)) {
      experiment.setWebRecommended(cursor.getLong(webRecommendedIndex) == 1);
    }

    if (!cursor.isNull(jsonIndex)) {
      String jsonOfExperiment = cursor.getString(jsonIndex);
      experiment.setJson(jsonOfExperiment);
      try {
        Experiment experimentFromJson = ExperimentProviderUtil.getSingleExperimentFromJson(jsonOfExperiment);
        Trigger trigger = experimentFromJson.getTrigger();
        if (trigger != null) {
          List<SignalingMechanism> signalingMechanisms = new ArrayList();
          signalingMechanisms.add(trigger);
          experiment.setTrigger(trigger);
          experiment.setSignalingMechanisms(signalingMechanisms);
        }
        Boolean customRendering = experimentFromJson.isCustomRendering();
        experiment.setCustomRendering(customRendering != null ? customRendering : false);
        experiment.setCustomRenderingCode(experimentFromJson.getCustomRenderingCode());
        Integer feedbackType = experimentFromJson.getFeedbackType();
        if (feedbackType == FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE) {
          if (FeedbackDAO.DEFAULT_FEEDBACK_MSG.equals(experimentFromJson.getFeedback().get(0).getText())) {
            feedbackType = FeedbackDAO.FEEDBACK_TYPE_RETROSPECTIVE;
          } else {
            feedbackType = FeedbackDAO.FEEDBACK_TYPE_CUSTOM;
          }
        }
        experiment.setFeedbackType(feedbackType);
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return experiment;
  }

  private ContentValues createContentValues(Experiment experiment) {
    ContentValues values = new ContentValues();
    // Values id < 0 indicate no id is available:
    if (experiment.getId() != null) {
      values.put(ExperimentColumns._ID, experiment.getId());
    }
    if (experiment.getServerId() != null) {
      values.put(ExperimentColumns.SERVER_ID, experiment.getServerId());
    }
    if (experiment.getTitle() != null) {
      values.put(ExperimentColumns.TITLE, experiment.getTitle() );
    }
    if (experiment.getVersion() != null) {
      values.put(ExperimentColumns.VERSION, experiment.getVersion() );
    }
    if (experiment.getDescription() != null) {
      values.put(ExperimentColumns.DESCRIPTION, experiment.getDescription() );
    }
    if (experiment.getCreator() != null) {
      values.put(ExperimentColumns.CREATOR, experiment.getCreator() );
    }
    if (experiment.getInformedConsentForm() != null) {
      values.put(ExperimentColumns.INFORMED_CONSENT, experiment.getInformedConsentForm() );
    }
    if (experiment.getHash() != null) {
      values.put(ExperimentColumns.HASH, experiment.getHash() );
    }

    values.put(ExperimentColumns.FIXED_DURATION, experiment.isFixedDuration() != null && experiment.isFixedDuration() ? 1 : 0);

    if (experiment.getStartDate() != null) {
      values.put(ExperimentColumns.START_DATE, experiment.getStartDate());
    }
    if (experiment.getEndDate() != null) {
      values.put(ExperimentColumns.END_DATE, experiment.getEndDate());
    }

    if (experiment.getJoinDate() != null) {
      values.put(ExperimentColumns.JOIN_DATE, experiment.getJoinDate());
    }
    values.put(ExperimentColumns.QUESTIONS_CHANGE, experiment.isQuestionsChange() ? 1 : 0 );

    if (experiment.getIcon() != null) {
          values.put(ExperimentColumns.ICON, experiment.getIcon() );
    }

    values.put(ExperimentColumns.WEB_RECOMMENDED, experiment.isWebRecommended() != null && experiment.isWebRecommended() ? 1 : 0);

    String json = experiment.getJson();
    if (json == null || json.length() == 0) {
      json = getJson(experiment);
    }
    values.put(ExperimentColumns.JSON, json);
    return values;
  }

  // Visible for testing
  public List<String> getJsonList(List<Experiment> experiments) {
    List<String> experimentJsons = Lists.newArrayList();
    for (Experiment experiment : experiments) {
      experimentJsons.add(getJson(experiment));
    }
    return experimentJsons;
  }

  public String getJson(Experiment experiment) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      Log.e(PacoConstants.TAG, "Json generation error " + e);
    } catch (JsonMappingException e) {
      Log.e(PacoConstants.TAG, "JsonMapping error getting experiment json: " + e.getMessage());
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "IO error getting experiment: " + e.getMessage());
    }

    return null;
  }

  public String getJson(List<Experiment> experiments) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

    try {
      return mapper.writeValueAsString(experiments);
    } catch (JsonGenerationException e) {
      Log.e(PacoConstants.TAG, "Json generation error " + e);
    } catch (JsonMappingException e) {
      Log.e(PacoConstants.TAG, "JsonMapping error getting experiment json: " + e.getMessage());
    } catch (IOException e) {
      Log.e(PacoConstants.TAG, "IO error getting experiment: " + e.getMessage());
    }

    return null;
  }


  public void loadInputsForExperiment(Experiment experiment) {
    String select = InputColumns.EXPERIMENT_ID + " = " + experiment.getId();
    experiment.setInputs(findInputsBy(select));
  }

  public void loadEventsForExperiment(Experiment experiment) {
    List<Event> eventSingleEntryList = findEventsBy(EventColumns.EXPERIMENT_ID + "=" + experiment.getId(),
        EventColumns._ID +" DESC");
    experiment.setEvents(eventSingleEntryList);
  }


  public void insertInputsForJoinedExperiment(Experiment experiment) {
    List<Input> inputs = experiment.getInputs();
    for (Input input : inputs) {
      input.setExperimentId(experiment.getId());
      insertInput(input);
    }
  }
    // The same things for Inputs

  public Input getInput(long id) {
    String select = InputColumns._ID + "=" + id;
    return findInputBy(select);
  }

  public Uri insertInput(Input input) {
    return contentResolver.insert(InputColumns.CONTENT_URI,
        createContentValues(input));
  }

  public void deleteInput(long inputId) {
    Input input = getInput(inputId);
    if (input != null) {
      String[] selectionArgs = new String[] {Long.toString(inputId)};
      contentResolver.delete(InputColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
    }
  }

  public void updateInput(Input input) {
    contentResolver.update(InputColumns.CONTENT_URI,
        createContentValues(input), "_id=" + input.getId(), null);
  }

  private Input findInputBy(String select) {
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(InputColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null && cursor.moveToNext()) {
        return createInput(cursor);
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  private List<Input> findInputsBy(String select) {
    List<Input> inputs = new ArrayList<Input>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(InputColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null) {
        while (cursor.moveToNext()) {
          inputs.add(createInput(cursor));
        }
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return inputs;
  }


  private Input createInput(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(InputColumns._ID);
    int serverIdIndex = cursor.getColumnIndexOrThrow(InputColumns.SERVER_ID);
    int experimentIndex = cursor.getColumnIndex(InputColumns.EXPERIMENT_ID);
    int quesstionTypeIndex = cursor.getColumnIndex(InputColumns.QUESTION_TYPE);
    int responseTypeIndex = cursor.getColumnIndex(InputColumns.RESPONSE_TYPE);
    int mandatoryIndex = cursor.getColumnIndex(InputColumns.MANDATORY);
    int textIndex = cursor.getColumnIndex(InputColumns.TEXT);
    int nameIndex = cursor.getColumnIndex(InputColumns.NAME);
    int scheduledDateIndex = cursor.getColumnIndex(InputColumns.SCHEDULED_DATE);
    int likertStepsIndex = cursor.getColumnIndex(InputColumns.LIKERT_STEPS);
    int leftSideLabelIndex = cursor.getColumnIndex(InputColumns.LEFT_SIDE_LABEL);
    int rightSideLabelIndex = cursor.getColumnIndex(InputColumns.RIGHT_SIDE_LABEL);
    int listChoiceIndex = cursor.getColumnIndex(InputColumns.LIST_CHOICES_JSON);
    int conditionIndex = cursor.getColumnIndex(InputColumns.CONDITIONAL);
    int conditionExpressionIndex = cursor.getColumnIndex(InputColumns.CONDITIONAL_EXPRESSION);
    int multiselectIndex = cursor.getColumnIndex(InputColumns.MULTISELECT);

    Input input = new Input();

    if (!cursor.isNull(idIndex)) {
      input.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(serverIdIndex)) {
      input.setServerId(cursor.getLong(serverIdIndex));
    }

    if (!cursor.isNull(experimentIndex)) {
      input.setExperimentId(cursor.getLong(experimentIndex));
    }

    if (!cursor.isNull(nameIndex)) {
      input.setName(cursor.getString(nameIndex));
    }

    if (!cursor.isNull(quesstionTypeIndex)) {
      input.setQuestionType(cursor.getString(quesstionTypeIndex));
    }

    if (!cursor.isNull(responseTypeIndex)) {
      input.setResponseType(cursor.getString(responseTypeIndex));
    }

    if (!cursor.isNull(scheduledDateIndex)) {
      input.setScheduleDateFromLong(cursor.getLong(scheduledDateIndex));
    }

    if (!cursor.isNull(mandatoryIndex)) {
      input.setMandatory(cursor.getInt(mandatoryIndex) == 1);
    }

    if (!cursor.isNull(textIndex)) {
      input.setText(cursor.getString(textIndex));
    }

    if (!cursor.isNull(likertStepsIndex)) {
      input.setLikertSteps(cursor.getInt(likertStepsIndex));
    }
    if (!cursor.isNull(leftSideLabelIndex)) {
      input.setLeftSideLabel(cursor.getString(leftSideLabelIndex));
    }
    if (!cursor.isNull(rightSideLabelIndex)) {
      input.setRightSideLabel(cursor.getString(rightSideLabelIndex));
    }

    List<String> listChoices = null;
    if (!cursor.isNull(listChoiceIndex)) {
      String jsonChoices = cursor.getString(listChoiceIndex);
      ObjectMapper mapper = new ObjectMapper();
      try {
        listChoices = mapper.readValue(jsonChoices, new TypeReference<List<String>>() {
        });
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (listChoices == null) {
      listChoices = new ArrayList<String>();
    }
    input.setListChoices(listChoices);

    if (!cursor.isNull(conditionIndex)) {
      input.setConditional(cursor.getInt(conditionIndex) == 1);
    }
    if (!cursor.isNull(conditionExpressionIndex)) {
      input.setConditionExpression(cursor.getString(conditionExpressionIndex));
    }
    if (!cursor.isNull(multiselectIndex)) {
      input.setMultiselect(cursor.getInt(multiselectIndex) == 1);
    }
    return input;
  }

  private ContentValues createContentValues(Input input) {
    ContentValues values = new ContentValues();
    // Values id < 0 indicate no id is available:
    if (input.getId() != null) {
      values.put(InputColumns._ID, input.getId());
    }
    if (input.getServerId() != null) {
      values.put(InputColumns.SERVER_ID, input.getServerId());
    }
    if (input.getExperimentId() != null) {
      values.put(InputColumns.EXPERIMENT_ID, input.getExperimentId());
    }
    if (input.getQuestionType() != null) {
      values.put(InputColumns.QUESTION_TYPE, input.getQuestionType());
    }
    if (input.getName() != null) {
      values.put(InputColumns.NAME, input.getName());
    }

    if (input.getResponseType() != null) {
      values.put(InputColumns.RESPONSE_TYPE, input.getResponseType());
    }

    if (input.getScheduleDate() != null) {
      values.put(InputColumns.SCHEDULED_DATE, input.getScheduleDate().getTime());
    }
    values.put(InputColumns.MANDATORY, input.isMandatory() ? 1 : 0);

    if (input.getText() != null) {
      values.put(InputColumns.TEXT, input.getText() );
    }
    if (input.getLikertSteps() != null) {
      values.put(InputColumns.LIKERT_STEPS, input.getLikertSteps());
    }
    if (input.getLeftSideLabel() != null) {
      values.put(InputColumns.LEFT_SIDE_LABEL, input.getLeftSideLabel());
    }
    if (input.getRightSideLabel() != null) {
      values.put(InputColumns.RIGHT_SIDE_LABEL, input.getRightSideLabel());
    }
    List<String> choices = input.getListChoices();

    if (choices != null && choices.size() > 0) {
      for (int i=0; i < choices.size(); i++) {
        String jsonChoices = null;
        try {
          jsonChoices = new ObjectMapper().writeValueAsString(choices);
        } catch (JsonGenerationException e) {
          e.printStackTrace();
        } catch (JsonMappingException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (jsonChoices != null) {
          values.put(InputColumns.LIST_CHOICES_JSON, jsonChoices);
        }
      }
    }

    if (input.getConditional() != null) {
      values.put(InputColumns.CONDITIONAL, input.getConditional());
    }
    if (input.getConditionExpression() != null) {
      values.put(InputColumns.CONDITIONAL_EXPRESSION, input.getConditionExpression());
    }
    values.put(InputColumns.MULTISELECT, (input.isMultiselect() != null && input.isMultiselect()) ? 1 : 0);
    return values;
  }

    //The same things for Feedback

  public void loadFeedbackForExperiment(Experiment experiment) {
    String select = FeedbackColumns.EXPERIMENT_ID + " = " + experiment.getId();
    experiment.setFeeback(findFeedbackBy(select));
  }

  public void insertFeedbackForJoinedExperiment(Experiment experiment) {
    List<Feedback> feedback = experiment.getFeedback();
    for (Feedback feedbackItem : feedback) {
      feedbackItem.setExperimentId(experiment.getId());
      insertFeedback(feedbackItem);
    }
  }
    // The same things for Inputs

  public Feedback getFeedbackItem(long id) {
    String select = FeedbackColumns._ID + "=" + id;
    return findFeedbackItemBy(select);
  }

  public Uri insertFeedback(Feedback feedback) {
    return contentResolver.insert(FeedbackColumns.CONTENT_URI,
        createContentValues(feedback));
  }

  public void deleteFeedback(long feedbackId) {
    Feedback feedback = getFeedbackItem(feedbackId);
    if (feedback != null) {
      String[] selectionArgs = new String[] {Long.toString(feedbackId)};
      contentResolver.delete(FeedbackColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
    }
  }

  public void updateFeedback(Feedback feedback) {
    contentResolver.update(FeedbackColumns.CONTENT_URI,
        createContentValues(feedback), "_id=" + feedback.getId(), null);
  }

  private Feedback findFeedbackItemBy(String select) {
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(FeedbackColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null && cursor.moveToNext()) {
        return createFeedback(cursor);
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  private List<Feedback> findFeedbackBy(String select) {
    List<Feedback> feedback = new ArrayList<Feedback>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(FeedbackColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null) {
        while (cursor.moveToNext()) {
          feedback.add(createFeedback(cursor));
        }
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return feedback;
  }


  private Feedback createFeedback(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(FeedbackColumns._ID);
    int serverIdIndex = cursor.getColumnIndexOrThrow(FeedbackColumns.SERVER_ID);
    int experimentIndex = cursor.getColumnIndex(FeedbackColumns.EXPERIMENT_ID);
    int textIndex = cursor.getColumnIndex(FeedbackColumns.TEXT);

    Feedback input = new Feedback();

    if (!cursor.isNull(idIndex)) {
      input.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(serverIdIndex)) {
      input.setServerId(cursor.getLong(serverIdIndex));
    }

    if (!cursor.isNull(experimentIndex)) {
      input.setExperimentId(cursor.getLong(experimentIndex));
    }
    if (!cursor.isNull(textIndex)) {
      input.setText(cursor.getString(textIndex));
    }
    return input;
  }

  private ContentValues createContentValues(Feedback feedback) {
    ContentValues values = new ContentValues();

    if (feedback.getId() != null) {
      values.put(FeedbackColumns._ID, feedback.getId());
    }
    if (feedback.getServerId() != null) {
      values.put(FeedbackColumns.SERVER_ID, feedback.getServerId());
    }
    if (feedback.getExperimentId() != null) {
      values.put(FeedbackColumns.EXPERIMENT_ID, feedback.getExperimentId());
    }
    if (feedback.getText() != null) {
      values.put(FeedbackColumns.TEXT, feedback.getText() );
    }
    return values;
  }

  public Uri insertEvent(Event event) {
    Uri uri = contentResolver.insert(EventColumns.CONTENT_URI,
        createContentValues(event));
    long rowId = Long.parseLong(uri.getLastPathSegment());
    event.setId(rowId);
    for (Output response : event.getResponses()) {
      response.setEventId(rowId);
      insertResponse(response);
    }
    return uri;
  }

  private SignalSchedule createSchedule(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(SignalScheduleColumns._ID);
    int serverIdIndex = cursor.getColumnIndexOrThrow(SignalScheduleColumns.SERVER_ID);
    int experimentIndex = cursor.getColumnIndex(SignalScheduleColumns.EXPERIMENT_ID);
    int scheduleTypeIndex = cursor.getColumnIndex(SignalScheduleColumns.SCHEDULE_TYPE);
    int timesCSVIndex = cursor.getColumnIndex(SignalScheduleColumns.TIMES_CSV);
    int esmFrequencyIndex = cursor.getColumnIndex(SignalScheduleColumns.ESM_FREQUENCY);
    int esmPeriodIndex = cursor.getColumnIndex(SignalScheduleColumns.ESM_PERIOD);

    int esmStartIndex = cursor.getColumnIndex(SignalScheduleColumns.ESM_START_HOUR);
    int esmEndIndex = cursor.getColumnIndex(SignalScheduleColumns.ESM_END_HOUR);
    int esmWeekendsIndex = cursor.getColumnIndex(SignalScheduleColumns.ESM_WEEKENDS);

    int repeatIndex = cursor.getColumnIndex(SignalScheduleColumns.REPEAT_RATE);
    int weekdaysIndex = cursor.getColumnIndex(SignalScheduleColumns.WEEKDAYS_SCHEDULED);
    int nthIndex = cursor.getColumnIndex(SignalScheduleColumns.NTH_OF_MONTH);
    int byDayIndex = cursor.getColumnIndex(SignalScheduleColumns.BY_DAY_OF_MONTH);
    int dayIndex = cursor.getColumnIndex(SignalScheduleColumns.DAY_OF_MONTH);
    int beginDateIndex = cursor.getColumnIndex(SignalScheduleColumns.BEGIN_DATE);
    int userEditableIndex = cursor.getColumnIndex(SignalScheduleColumns.USER_EDITABLE);
    int timeoutIndex = cursor.getColumnIndex(SignalScheduleColumns.TIME_OUT);
    int minBufferIndex = cursor.getColumnIndex(SignalScheduleColumns.MINIMUM_BUFFER);
    int snoozeCountIndex = cursor.getColumnIndex(SignalScheduleColumns.SNOOZE_COUNT);
    int snoozeTimeIndex = cursor.getColumnIndex(SignalScheduleColumns.SNOOZE_TIME);

    SignalSchedule schedule = new SignalSchedule();
    if (!cursor.isNull(idIndex)) {
      schedule.setId(cursor.getLong(idIndex));
    }
    if (!cursor.isNull(serverIdIndex)) {
      schedule.setServerId(cursor.getLong(serverIdIndex));
    }
    if (!cursor.isNull(experimentIndex)) {
      schedule.setExperimentId(cursor.getLong(experimentIndex));
    }
    if (!cursor.isNull(scheduleTypeIndex)) {
      schedule.setScheduleType(cursor.getInt(scheduleTypeIndex));
    }
    if (!cursor.isNull(timesCSVIndex)) {
      List<Long> times = new ArrayList<Long>();
      StringSplitter sp = new TextUtils.SimpleStringSplitter(',');
      sp.setString(cursor.getString(timesCSVIndex));
      for (String string : sp) {
        times.add(Long.parseLong(string));
      }
      schedule.setTimes(times);
    }
    if (!cursor.isNull(esmFrequencyIndex)) {
      schedule.setEsmFrequency(cursor.getInt(esmFrequencyIndex));
    }
    if (!cursor.isNull(esmPeriodIndex)) {
      schedule.setEsmPeriodInDays(cursor.getInt(esmPeriodIndex));
    }
    if (!cursor.isNull(esmStartIndex)) {
      schedule.setEsmStartHour(cursor.getLong(esmStartIndex));
    }
    if (!cursor.isNull(esmEndIndex)) {
      schedule.setEsmEndHour(cursor.getLong(esmEndIndex));
    }
    if (!cursor.isNull(esmWeekendsIndex)) {
      schedule.setEsmWeekends(cursor.getInt(esmWeekendsIndex) == 1 ? Boolean.TRUE : Boolean.FALSE);
    }
    if (!cursor.isNull(repeatIndex)) {
      schedule.setRepeatRate(cursor.getInt(repeatIndex));
    }
    if (!cursor.isNull(weekdaysIndex)) {
      schedule.setWeekDaysScheduled(cursor.getInt(weekdaysIndex));
    }
    if (!cursor.isNull(nthIndex)) {
      schedule.setNthOfMonth(cursor.getInt(nthIndex));
    }
    if (!cursor.isNull(byDayIndex)) {
      schedule.setByDayOfMonth(cursor.getInt(byDayIndex) == 1? Boolean.TRUE : Boolean.FALSE);
    }
    if (!cursor.isNull(dayIndex)) {
      schedule.setDayOfMonth(cursor.getInt(dayIndex));
    }
    if (!cursor.isNull(beginDateIndex)) {
      schedule.setBeginDate(cursor.getLong(beginDateIndex));
    }
    if (!cursor.isNull(userEditableIndex)) {
      schedule.setUserEditable(cursor.getInt(userEditableIndex) == 1? Boolean.TRUE : Boolean.FALSE);
    }
    if (!cursor.isNull(timeoutIndex)) {
      schedule.setTimeout(cursor.getInt(timeoutIndex));
    }
    if (!cursor.isNull(minBufferIndex)) {
      schedule.setMinimumBuffer(cursor.getInt(minBufferIndex));
    }

    if (!cursor.isNull(snoozeCountIndex)) {
      schedule.setSnoozeCount(cursor.getInt(snoozeCountIndex));
    }

    if (!cursor.isNull(snoozeTimeIndex)) {
      schedule.setSnoozeTime(cursor.getInt(snoozeTimeIndex));
    }

    return schedule;
  }


  private ContentValues createContentValues(SignalSchedule schedule) {
    ContentValues values = new ContentValues();
    if (schedule.getId() != null) {
      values.put(EventColumns._ID, schedule.getId());
    }
    if (schedule.getExperimentId() != null) {
      values.put(SignalScheduleColumns.EXPERIMENT_ID, schedule.getExperimentId());
    }
    if (schedule.getServerId() != null) {
      values.put(SignalScheduleColumns.SERVER_ID, schedule.getServerId());
    }
    if (schedule.getByDayOfMonth() != null) {
      values.put(SignalScheduleColumns.BY_DAY_OF_MONTH, schedule.getByDayOfMonth());
    }
    if (schedule.getScheduleType() != null) {
      values.put(SignalScheduleColumns.SCHEDULE_TYPE, schedule.getScheduleType());
    }
    if (schedule.getEsmFrequency() != null) {
      values.put(SignalScheduleColumns.ESM_FREQUENCY, schedule.getEsmFrequency());
    }
    if (schedule.getEsmPeriodInDays() != null) {
      values.put(SignalScheduleColumns.ESM_PERIOD, schedule.getEsmPeriodInDays());
    }
    if (schedule.getEsmStartHour() != null) {
      values.put(SignalScheduleColumns.ESM_START_HOUR, schedule.getEsmStartHour());
    }
    if (schedule.getEsmEndHour() != null) {
      values.put(SignalScheduleColumns.ESM_END_HOUR, schedule.getEsmEndHour());
    }
    if (schedule.getEsmWeekends() != null) {
      values.put(SignalScheduleColumns.ESM_WEEKENDS, schedule.getEsmWeekends() == Boolean.TRUE ? 1 : 0);
    }
    if (schedule.getUserEditable() != null) {
      values.put(SignalScheduleColumns.USER_EDITABLE, schedule.getUserEditable() == Boolean.TRUE ? 1 : 0);
    }
    if (schedule.getTimeout() != null) {
      values.put(SignalScheduleColumns.TIME_OUT, schedule.getTimeout());
    }

    if (schedule.getMinimumBuffer() != null) {
      values.put(SignalScheduleColumns.MINIMUM_BUFFER, schedule.getMinimumBuffer());
    }

    if (schedule.getSnoozeCount() != null) {
      values.put(SignalScheduleColumns.SNOOZE_COUNT, schedule.getSnoozeCount());
    }

    if (schedule.getSnoozeTime() != null) {
      values.put(SignalScheduleColumns.SNOOZE_TIME, schedule.getSnoozeTime());
    }

    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Long time : schedule.getTimes()) {
      if (!first) {
        buf.append(",");
      } else {
        first = false;
      }
      buf.append(time);
    }
    values.put(SignalScheduleColumns.TIMES_CSV, buf.toString());

    if (schedule.getRepeatRate() != null) {
      values.put(SignalScheduleColumns.REPEAT_RATE, schedule.getRepeatRate());
    }

    if (schedule.getWeekDaysScheduled() != null) {
      values.put(SignalScheduleColumns.WEEKDAYS_SCHEDULED, schedule.getWeekDaysScheduled());
    }

    if (schedule.getNthOfMonth() != null) {
      values.put(SignalScheduleColumns.NTH_OF_MONTH, schedule.getNthOfMonth());
    }

    if (schedule.getByDayOfMonth() != null) {
      values.put(SignalScheduleColumns.BY_DAY_OF_MONTH, schedule.getByDayOfMonth() == Boolean.TRUE ? 1 : 0);
    }

    if (schedule.getDayOfMonth() != null) {
      values.put(SignalScheduleColumns.DAY_OF_MONTH, schedule.getDayOfMonth());
    }
    if (schedule.getBeginDate() != null) {
      values.put(SignalScheduleColumns.BEGIN_DATE, schedule.getBeginDate());
    }
    return values;
  }

  public void deleteSchedule(long scheduleId) {
    Input input = getInput(scheduleId);
    if (input != null) {
      String[] selectionArgs = new String[] {Long.toString(scheduleId)};
      contentResolver.delete(SignalScheduleColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
    }
  }

  public void updateSchedule(SignalSchedule schedule) {
    contentResolver.update(SignalScheduleColumns.CONTENT_URI,
        createContentValues(schedule), "_id=" + schedule.getId(), null);
  }

  public Uri insertSchedule(SignalSchedule schedule) {
    return contentResolver.insert(SignalScheduleColumns.CONTENT_URI,
        createContentValues(schedule));
  }


  private ContentValues createContentValues(Event event) {
    ContentValues values = new ContentValues();

    if (event.getId() >= 0) {
      values.put(EventColumns._ID, event.getId());
    }
    if (event.getExperimentId() >= 0) {
      values.put(EventColumns.EXPERIMENT_ID, event.getExperimentId());
    }
    if (event.getExperimentServerId() >= 0) {
      values.put(EventColumns.EXPERIMENT_SERVER_ID, event.getExperimentServerId());
    }
    if (event.getExperimentVersion() != null) {
      values.put(EventColumns.EXPERIMENT_VERSION, event.getExperimentVersion());
    }
    if (event.getExperimentName() != null) {
      values.put(EventColumns.EXPERIMENT_NAME, event.getExperimentName());
    }

    if (event.getScheduledTime() != null) {
      values.put(EventColumns.SCHEDULE_TIME, event.getScheduledTime().getMillis());
    }
    if (event.getResponseTime() != null) {
      values.put(EventColumns.RESPONSE_TIME, event.getResponseTime().getMillis());
    }

    values.put(EventColumns.UPLOADED, event.isUploaded() ? 1 : 0);

    return values;
  }

  private Uri insertResponse(Output response) {
    return contentResolver.insert(OutputColumns.CONTENT_URI,
        createContentValues(response));
  }

  private ContentValues createContentValues(Output response) {
    ContentValues values = new ContentValues();

    if (response.getId() >= 0) {
      values.put(OutputColumns._ID, response.getId());
    }
    if (response.getEventId() >= 0) {
      values.put(OutputColumns.EVENT_ID, response.getEventId());
    }
    if (response.getInputServerId() >= 0) {
      values.put(OutputColumns.INPUT_SERVER_ID, response.getInputServerId());
    }
    if (response.getAnswer() != null) {
      values.put(OutputColumns.ANSWER, response.getAnswer());
    }
    if (response.getName() != null) {
      values.put(OutputColumns.NAME, response.getName());
    }
    return values;
  }

  private Event findEventBy(String select, String sortOrder) {
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(EventColumns.CONTENT_URI,
          null, select, null, sortOrder);
      if (cursor != null && cursor.moveToNext()) {
        Event event = createEvent(cursor);
        event.setResponses(findResponsesFor(event));
        return event;
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  private List<Event> findEventsBy(String select, String sortOrder) {
    List<Event> events = new ArrayList<Event>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(EventColumns.CONTENT_URI,
          null, select, null, sortOrder);
      if (cursor != null) {
        while (cursor.moveToNext()) {
          Event event = createEvent(cursor);
          event.setResponses(findResponsesFor(event));
          events.add(event);
        }
      }
      return events;
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return events;
  }

  private List<Output> findResponsesFor(Event event) {
    List<Output> responses = new ArrayList<Output>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(OutputColumns.CONTENT_URI,
          null,
          OutputColumns.EVENT_ID + "=" + event.getId(),
          null,
          OutputColumns.INPUT_SERVER_ID + " ASC"); // TODO (bobevans) module the conditional questions, this ordering should be OK to get questions in order.
      if (cursor != null) {
        while (cursor.moveToNext()) {
          responses.add(createResponse(cursor));
        }
      }
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return responses;
  }

  private Event createEvent(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(EventColumns._ID);
    int experimentIdIndex = cursor.getColumnIndexOrThrow(EventColumns.EXPERIMENT_ID);
    int experimentServerIdIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_SERVER_ID);
    int experimentVersionIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_VERSION);
    int experimentNameIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_NAME);
    int scheduleTimeIndex = cursor.getColumnIndex(EventColumns.SCHEDULE_TIME);
    int responseTimeIndex = cursor.getColumnIndex(EventColumns.RESPONSE_TIME);
    int uploadedIndex = cursor.getColumnIndex(EventColumns.UPLOADED);

    Event input = new Event();

    if (!cursor.isNull(idIndex)) {
      input.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(experimentIdIndex)) {
      input.setExperimentId(cursor.getLong(experimentIdIndex));
    }

    if (!cursor.isNull(experimentServerIdIndex)) {
      input.setServerExperimentId(cursor.getLong(experimentServerIdIndex));
    }
    if (!cursor.isNull(experimentVersionIndex)) {
      input.setExperimentVersion(cursor.getInt(experimentVersionIndex));
    }
    if (!cursor.isNull(experimentNameIndex)) {
      input.setExperimentName(cursor.getString(experimentNameIndex));
    }

    if (!cursor.isNull(responseTimeIndex)) {
      input.setResponseTime(new DateTime(cursor.getLong(responseTimeIndex)));
    }
    if (!cursor.isNull(scheduleTimeIndex)) {
      input.setScheduledTime(new DateTime(cursor.getLong(scheduleTimeIndex)));
    }
    if (!cursor.isNull(uploadedIndex)) {
      input.setUploaded(cursor.getInt(uploadedIndex) == 1);
    }
    return input;
  }

  private Output createResponse(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(OutputColumns._ID);
    int eventIdIndex = cursor.getColumnIndexOrThrow(OutputColumns.EVENT_ID);
    int inputServeridIndex = cursor.getColumnIndex(OutputColumns.INPUT_SERVER_ID);
    int answerIndex = cursor.getColumnIndex(OutputColumns.ANSWER);
    int nameIndex = cursor.getColumnIndex(OutputColumns.NAME);

    Output input = new Output();

    if (!cursor.isNull(idIndex)) {
      input.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(eventIdIndex)) {
      input.setEventId(cursor.getLong(eventIdIndex));
    }

    if (!cursor.isNull(inputServeridIndex)) {
      input.setInputServerId(cursor.getLong(inputServeridIndex));
    }
    if (!cursor.isNull(nameIndex)) {
      input.setName(cursor.getString(nameIndex));
    }
    if (!cursor.isNull(answerIndex)) {
      input.setAnswer(cursor.getString(answerIndex));
    }
    return input;
  }

  public void updateEvent(Event event) {
    contentResolver.update(EventColumns.CONTENT_URI,
        createContentValues(event), "_id=" + event.getId(), null);
  }

  public List<Event> getEventsNeedingUpload() {
    String select = EventColumns.UPLOADED + " != 1";
    return findEventsBy(select, null);
  }

  public List<Event> getAllEvents() {
    return findEventsBy(null, null);
  }

  public void deleteFullExperiment(Uri uri) {
    Experiment experiment = getExperiment(uri);
    if (experiment != null) {
      deleteFullExperiment(experiment);
    }
  }

  public void deleteAllUnJoinedExperiments() {

    Cursor cursor = null;
    try {
      cursor = contentResolver.query(ExperimentColumns.CONTENT_URI,
          new String[] { ExperimentColumns._ID },
          ExperimentColumns.JOIN_DATE + " IS NULL ",
          null, null);
      if (cursor != null) {
        StringBuilder idsStringBuilder = new StringBuilder();
        int lineCount = 0;
        while (cursor.moveToNext()) {
          if (lineCount > 0) {
            idsStringBuilder.append(",");
          }
          idsStringBuilder.append(cursor.getString(0));
          lineCount++;
        }
        String idsString = idsStringBuilder.toString();
        contentResolver.delete(
            ExperimentColumns.CONTENT_URI, ExperimentColumns._ID + " in (" + idsString + ")", null);
        // TODO delete all from child tables where experiment_ids match
        contentResolver.delete(InputColumns.CONTENT_URI,
            InputColumns.EXPERIMENT_ID + " in (" + idsString + ")", null);
        contentResolver.delete(FeedbackColumns.CONTENT_URI,
            FeedbackColumns.EXPERIMENT_ID + " in (" + idsString + ")", null);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

  }

  public NotificationHolder getNotificationFor(long experimentId) {
    String[] selectionArgs = new String[] {Long.toString(experimentId)};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ?";
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(NotificationHolderColumns.CONTENT_URI,
        null, selectionClause, selectionArgs, null);
      if (cursor.moveToFirst()) {
        return createNotification(cursor);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  public List<NotificationHolder> getNotificationsFor(long experimentId) {
    List<NotificationHolder> holders = new ArrayList<NotificationHolder>();
    String[] selectionArgs = new String[] {Long.toString(experimentId)};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ?";
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(NotificationHolderColumns.CONTENT_URI,
        null, selectionClause, selectionArgs, null);
      while (cursor.moveToNext()) {
        holders.add(createNotification(cursor));
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return holders;
  }


  private NotificationHolder createNotification(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns._ID);
    int experimentIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.EXPERIMENT_ID);
    int alarmIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.ALARM_TIME);
    int noticeCountIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.NOTICE_COUNT);
    int timeoutMillisIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.TIMEOUT_MILLIS);

    NotificationHolder notification = new NotificationHolder();

    if (!cursor.isNull(idIndex)) {
      notification.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(experimentIdIndex)) {
      notification.setExperimentId(cursor.getLong(experimentIdIndex));
    }

    if (!cursor.isNull(alarmIdIndex)) {
      notification.setAlarmTime(cursor.getLong(alarmIdIndex));
    }
    if (!cursor.isNull(noticeCountIdIndex)) {
      notification.setNoticeCount(cursor.getInt(noticeCountIdIndex));
    }

    if (!cursor.isNull(timeoutMillisIdIndex)) {
      notification.setTimeoutMillis(cursor.getLong(timeoutMillisIdIndex));
    }
    return notification;
  }

  public Uri insertNotification(NotificationHolder notification) {
    Uri uri = contentResolver.insert(NotificationHolderColumns.CONTENT_URI,
        createContentValues(notification));
    long rowId = Long.parseLong(uri.getLastPathSegment());
    notification.setId(rowId);
    return uri;
  }

  public NotificationHolder getNotificationById(long notificationId) {
    String[] selectionArgs = new String[] {Long.toString(notificationId)};
    String selectionClause = NotificationHolderColumns._ID + " = ?";
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(NotificationHolderColumns.CONTENT_URI,
        null, selectionClause, selectionArgs, null);
      if (cursor.moveToFirst()) {
        return createNotification(cursor);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  public void deleteNotification(long notificationId) {
    NotificationHolder holder = getNotificationById(notificationId);
    deleteNotification(holder);
  }

  private void deleteNotification(NotificationHolder holder) {
    if (holder != null) {
      String[] selectionArgs = new String[] {Long.toString(holder.getId())};
      String selectionClause = NotificationHolderColumns._ID + " = ?";
      contentResolver.delete(NotificationHolderColumns.CONTENT_URI,
          selectionClause, selectionArgs);
    }
  }

  public int deleteNotificationsForExperiment(Long experimentId) {
    if (experimentId == null) {
      return 0;
    }

    String[] selectionArgs = new String[] {Long.toString(experimentId)};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ?";
    return contentResolver.delete(NotificationHolderColumns.CONTENT_URI,
        selectionClause,
        selectionArgs);
  }

  public int updateNotification(NotificationHolder notification) {
    String[] selectionArgs = new String[] {Long.toString(notification.getId())};
    String selectionClause = NotificationHolderColumns._ID +" = ?";

    return contentResolver.update(NotificationHolderColumns.CONTENT_URI,
        createContentValues(notification),
        selectionClause, selectionArgs);
  }

  private ContentValues createContentValues(NotificationHolder notification) {
    ContentValues values = new ContentValues();

    if (notification.getId() != null) {
      values.put(NotificationHolderColumns._ID, notification.getId());
    }
    if (notification.getAlarmTime() != null) {
      values.put(NotificationHolderColumns.ALARM_TIME, notification.getAlarmTime() );
    }
    if (notification.getExperimentId() != null) {
      values.put(NotificationHolderColumns.EXPERIMENT_ID, notification.getExperimentId());
    }
    if (notification.getNoticeCount() != null) {
      values.put(NotificationHolderColumns.NOTICE_COUNT, notification.getNoticeCount());
    }
    if (notification.getTimeoutMillis() != null) {
      values.put(NotificationHolderColumns.TIMEOUT_MILLIS, notification.getTimeoutMillis());
    }

    return values;
  }

  public List<NotificationHolder> getNotificationsStillActive(DateTime now) {
    List<NotificationHolder> notifs = new ArrayList<NotificationHolder>();

    Cursor cursor = null;
    try {
      cursor = contentResolver.query(NotificationHolderColumns.CONTENT_URI,
        null, null, null, null);
      while (cursor.moveToNext()) {

        NotificationHolder notif = createNotification(cursor);
        if (notif.isActive(now)) {
          notifs.add(notif);
        }
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return notifs;
  }

  public List<NotificationHolder> getAllNotifications() {
    List<NotificationHolder> notifs = new ArrayList<NotificationHolder>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(NotificationHolderColumns.CONTENT_URI,
        null, null, null, null);
      while (cursor.moveToNext()) {
        notifs.add(createNotification(cursor));
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return notifs;
  }


  public void saveMyExperimentsToDisk(String contentAsString) throws IOException {
    FileOutputStream fos = context.openFileOutput(MY_EXPERIMENTS_FILENAME, Context.MODE_PRIVATE);
    fos.write(contentAsString.getBytes());
    fos.close();
  }

  public List<Experiment> loadMyExperimentsFromDisk() {
    List<Experiment> experiments = null;
    try {
      experiments = createObjectsFromJsonStream(context.openFileInput(MY_EXPERIMENTS_FILENAME));
    } catch (IOException e) {
      Log.i(PacoConstants.TAG, "IOException, experiments file does not exist. May be first launch.");
    }
    return ensureExperiments(experiments);
  }


  public void saveExperimentsToDisk(String contentAsString) throws IOException {
    FileOutputStream fos = context.openFileOutput(PUBLIC_EXPERIMENTS_FILENAME, Context.MODE_PRIVATE);
    fos.write(contentAsString.getBytes());
    fos.close();
  }

  public List<Experiment> loadExperimentsFromDisk(boolean myExperimentsFile) {
    String filename = null;
    if (myExperimentsFile) {
      filename = MY_EXPERIMENTS_FILENAME;
    } else {
      filename = PUBLIC_EXPERIMENTS_FILENAME;
    }
    List<Experiment> experiments = null;
    try {
      FileInputStream openFileInput = context.openFileInput(filename);
      experiments = createObjectsFromJsonStream(openFileInput);
    } catch (IOException e) {
      Log.i(PacoConstants.TAG, "IOException, experiments file does not exist. May be first launch.");
    }
    return ensureExperiments(experiments);
  }

  public void deleteExperimentCachesOnDisk() {
    context.deleteFile(MY_EXPERIMENTS_FILENAME);
    context.deleteFile(PUBLIC_EXPERIMENTS_FILENAME);
  }

  public void addExperimentToExperimentsOnDisk(String contentAsString) {
    List<Experiment> existing = loadExperimentsFromDisk(false);
    List<Experiment> newEx;
    try {
      newEx = (List<Experiment>) fromEntitiesJson(contentAsString).get("results");
      existing.addAll(newEx);
      String newJson = getJson(existing);
      if (newJson != null) {
        saveExperimentsToDisk(newJson);
      }
    } catch (JsonParseException e) {
    } catch (JsonMappingException e) {
    } catch (IOException e) {
    }


  }

  private List<Experiment> ensureExperiments(List<Experiment> experiments) {
    if (experiments != null) {
      return experiments;
    }
    return new ArrayList<Experiment>();
  }

  private List<Experiment> createObjectsFromJsonStream(FileInputStream fis) throws IOException, JsonParseException,
                                                                           JsonMappingException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(fis, new TypeReference<List<Experiment>>() {});
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Lists.newArrayList();
  }

  private List<Experiment> createObjectsFromJsonStream(String fis) throws IOException, JsonParseException,
                                                                           JsonMappingException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(fis, new TypeReference<List<Experiment>>() {
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Lists.newArrayList();
  }

  public boolean hasJoinedExperiments() {
    Cursor query = null;
    try {
      query = contentResolver.query(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
            new String[] {ExperimentColumns._ID}, null, null, null);
      return query.moveToFirst();
    } finally {
      if (query != null) {
        query.close();
      }
    }
  }

  public void loadLastEventForExperiment(Experiment experiment) {
    String select = EventColumns.EXPERIMENT_ID + "=" + experiment.getId();
    String sortOrder = EventColumns._ID +" DESC";

    List<Event> events = new ArrayList<Event>();
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(EventColumns.CONTENT_URI, null, select, null, sortOrder);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          Event event = createEvent(cursor);
          event.setResponses(findResponsesFor(event));
          events.add(event);
        }
      }
      experiment.setEvents(events);
    } catch (RuntimeException e) {
      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  public Experiment getExperimentFromDisk(Uri uri, boolean myExperimentsFile) {
    Long experimentServerId = new Long(uri.getLastPathSegment());
    return getExperimentFromDisk(experimentServerId, myExperimentsFile);
  }

  public Experiment getExperimentFromDisk(Long experimentServerId, boolean myExperimentsFile) {
    List<Experiment> experiments= loadExperimentsFromDisk(myExperimentsFile);
    for (Experiment experiment : experiments) {
      if (experiment.getServerId().equals(experimentServerId)) {
        return experiment;
      }
    }
    return null;
  }

  public static Map<String, Object> fromEntitiesJson(String resultsJson) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      Map<String, Object> resultObjects = mapper.readValue(resultsJson, new TypeReference<Map<String, Object>>() {});
      Object experimentResults = resultObjects.get("results");
      String experimentJson = mapper.writeValueAsString(experimentResults);
      List<Experiment> experiments = mapper.readValue(experimentJson, new TypeReference<List<Experiment>>() {});
      resultObjects.put("results", experiments);
      return resultObjects;
  }

  public static List<Experiment> getExperimentsFromJson(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    Map<String, Object> results = fromEntitiesJson(contentAsString);
    return (List<Experiment>) results.get("results");
  }

  public static Experiment getSingleExperimentFromJson(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.readValue(contentAsString, Experiment.class);
  }


}

