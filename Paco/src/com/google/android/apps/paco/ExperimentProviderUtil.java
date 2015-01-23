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
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.text.TextUtils.StringSplitter;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
    List<SignalingMechanism> signalingMechanisms = experiment.getSignalingMechanisms();
    Long joinDateMillis = getJoinDateMillis(experiment);
    for (SignalingMechanism signalingMechanism : signalingMechanisms) {
      if (signalingMechanism instanceof SignalSchedule) {
        SignalSchedule schedule = (SignalSchedule) signalingMechanism;
        schedule.setBeginDate(joinDateMillis);
      }
    }
    Uri uri = contentResolver.insert(ExperimentColumns.CONTENT_URI,
        createContentValues(experiment));

    long rowId = Long.parseLong(uri.getLastPathSegment());

    experiment.setId(rowId);
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

  public void deleteExperiment(long experimentId) {
    Experiment experiment = getExperiment(experimentId);
    if (experiment != null) {
      String[] selectionArgs = new String[] {Long.toString(experimentId)};
      contentResolver.delete(ExperimentColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
    }
  }

  /**
   * This is used by refresh downloaders only
   *
   * @param contentAsString
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public void updateExistingExperiments(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    Map<String, Object> results = fromEntitiesJson(contentAsString);
    List<Experiment> experimentList = (List<Experiment>) results.get("results");
    updateExistingExperiments(experimentList, false);
  }

  /**
   * Used when refreshing experiment list from the server.
   * If the experiment server id is already in the database,
   * then update it, otherwise, add it.
   * @param experiments
   * @param shouldOverrideExistingSettings downloaded (refreshed experiments should not override certain
   * local properties. Locally modified experiments should override local properties, e.g., logActions.
   */
  public void updateExistingExperiments(List<Experiment> experiments, Boolean shouldOverrideExistingSettings) {

    for (Experiment experiment : experiments) {
      long t1 = System.currentTimeMillis();

      List<Experiment> existingList = getExperimentsByServerId(experiment.getServerId());

      long t2 = System.currentTimeMillis();
      Log.e(PacoConstants.TAG, "time to load existing experiments (count: " + existingList.size() + " : " + (t2 - t1));

      if (existingList.size() == 0) {
        continue;
      }
      for (Experiment existingExperiment : existingList) {
        long startTime = System.currentTimeMillis();
        existingExperiment.setInputs(experiment.getInputs());
        existingExperiment.setFeedback(experiment.getFeedback());
        if (false /* TODO test if the user has modified any of the signaling mechanisms and update if they have not created custom times */) {
          existingExperiment.setSignalingMechanisms(experiment.getSignalingMechanisms());
        }
        copyAllPropertiesToExistingJoinedExperiment(experiment, existingExperiment, shouldOverrideExistingSettings);
        updateJoinedExperiment(existingExperiment);
        Log.i(PacoConstants.TAG, "Time to update one existing joined experiment: " + (System.currentTimeMillis() - startTime));
      }

    }
  }

  private void copyAllPropertiesToExistingJoinedExperiment(Experiment experiment, Experiment existingExperiment, Boolean shouldOverrideExistingSettings) {
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
    existingExperiment.setFeedbackType(experiment.getFeedbackType());
    // for now, because we can modify the experiment in the js at runtime, we do not want to update this.
    // however, this creates a problem for admins who want to update experiments.
    // TODO find a way to merge current state and updates correctly
    if (shouldOverrideExistingSettings) {
      existingExperiment.setLogActions(experiment.isLogActions());
    }

    existingExperiment.setRecordPhoneDetails(experiment.isRecordPhoneDetails());
    existingExperiment.setBackgroundListen(experiment.isBackgroundListen());
    existingExperiment.setBackgroundListenSourceIdentifier(experiment.getBackgroundListenSourceIdentifier());
  }

  private void deleteFullExperiment(Experiment experiment2) {
    deleteExperiment(experiment2.getId());
  }

  public void updateJoinedExperiment(Experiment experiment) {
    long t1 = System.currentTimeMillis();
    int count = contentResolver.update(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
        createContentValues(experiment),
        ExperimentColumns._ID + "=" + experiment.getId(), null);
    Log.i(ExperimentProviderUtil.class.getSimpleName(), " updated "+ count + " rows. Time: " + (System.currentTimeMillis() - t1));
  }

  public void deleteAllExperiments() {
    contentResolver.delete(ExperimentColumns.CONTENT_URI, null, null);
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


  static Experiment createExperiment(Cursor cursor) {
    int idIndex = cursor.getColumnIndex(ExperimentColumns._ID);
    int jsonIndex = cursor.getColumnIndex(ExperimentColumns.JSON);

    if (!cursor.isNull(jsonIndex)) {
      String jsonOfExperiment = cursor.getString(jsonIndex);
      try {
        long t1 = System.currentTimeMillis();
        Experiment experiment = ExperimentProviderUtil.getSingleExperimentFromJson(jsonOfExperiment);
        if (experiment.getId() == null) {
          if (!cursor.isNull(idIndex)) {
            experiment.setId(cursor.getLong(idIndex));
          }
        }
        Log.e(PacoConstants.TAG, "time to de-jsonify experiment (bytes: " + jsonOfExperiment.getBytes().length + ") : " + (System.currentTimeMillis() - t1));
        return experiment;
      } catch (JsonParseException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    return null;

  }

  static ContentValues createContentValues(Experiment experiment) {
    ContentValues values = new ContentValues();
    // Values id < 0 indicate no id is available:
    if (experiment.getId() != null) {
      values.put(ExperimentColumns._ID, experiment.getId());
    }
    if (experiment.getServerId() != null) {
      values.put(ExperimentColumns.SERVER_ID, experiment.getServerId());
    }
    if (experiment.getJoinDate() != null) {
      values.put(ExperimentColumns.JOIN_DATE, experiment.getJoinDate());
    }

    long t1 = System.currentTimeMillis();
    String json = getJson(experiment);
    Log.e(PacoConstants.TAG, "time to jsonify experiment (bytes: " + json.getBytes().length + "): " + (System.currentTimeMillis() - t1));
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

  public static String getJson(Experiment experiment) {
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

  public static String getJson(List<Experiment> experiments) {
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

  public void loadEventsForExperiment(Experiment experiment) {
    List<Event> eventSingleEntryList = findEventsBy(EventColumns.EXPERIMENT_ID + "=" + experiment.getId(),
        EventColumns._ID +" DESC");
    experiment.setEvents(eventSingleEntryList);
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
    int notificationSourceIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.NOTIFICATION_SOURCE);
    int customMessageIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.CUSTOM_MESSAGE);

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

    if (!cursor.isNull(notificationSourceIndex)) {
      notification.setNotificationSource(cursor.getString(notificationSourceIndex));
    }
    if (!cursor.isNull(customMessageIndex)) {
      notification.setMessage(cursor.getString(customMessageIndex));
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
    if (notification.getNotificationSource() != null) {
      values.put(NotificationHolderColumns.NOTIFICATION_SOURCE, notification.getNotificationSource());
    }
    if (notification.getMessage() != null) {
      values.put(NotificationHolderColumns.CUSTOM_MESSAGE, notification.getMessage());
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

  public Event getEvent(Long experimentServerId, DateTime scheduledTime) {
    if (scheduledTime == null) {
      return null;
    }
    Event event = findEventBy(EventColumns.EXPERIMENT_SERVER_ID + "=" + experimentServerId + " AND " + EventColumns.SCHEDULE_TIME + "=" + scheduledTime.getMillis(),
                                                    EventColumns._ID +" DESC");
    if (event != null) {
      Log.i(PacoConstants.TAG, "Found event for experimentId: " + experimentServerId +", st = " + scheduledTime);
    } else {
      Log.i(PacoConstants.TAG, "DID NOT Find event for experimentId: " + experimentServerId +", st = " + scheduledTime);
    }
    return event;
  }

  // legacy for 21->22 db upgrade
  public void loadInputsForExperiment(Experiment experiment) {
    String select = "_id" + " = " + experiment.getId();
    experiment.setInputs(findInputsBy(select));
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

  static class InputColumns implements BaseColumns {
    public static final String EXPERIMENT_ID = "experiment_id";
    public static final String SERVER_ID = "question_id";
    public static final String NAME = "name";
    public static final String TEXT = "text";
    public static final String MANDATORY = "mandatory";
    public static final String QUESTION_TYPE = "question_type";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String LIKERT_STEPS = "likert_steps";
    public static final String LEFT_SIDE_LABEL = "left_side_label";
    public static final String RIGHT_SIDE_LABEL = "right_side_label";
    public static final String LIST_CHOICES_JSON = "list_choices";
    public static final String SCHEDULED_DATE = "scheduledDate";
    public static final String CONDITIONAL = "conditional";
    public static final String CONDITIONAL_EXPRESSION = "condition_expression";
    public static final String MULTISELECT = "multiselect";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.input";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.input";

    public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/inputs");



  }
  public static Input createInput(Cursor cursor) {
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

  public static class FeedbackColumns implements BaseColumns {

    public static final String EXPERIMENT_ID = "experiment_id";
    public static final String SERVER_ID = "server_id";
    public static final String TEXT = "text";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.feedback";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.feedback";

    public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/feedback");

  }
//  public static void loadFeedbackForExperiment(Experiment experiment) {
//    String select = FeedbackColumns.EXPERIMENT_ID + " = " + experiment.getId();
//    experiment.setFeeback(ExperimentProviderUtil.findFeedbackBy(select));
//  }

//  private static List<Feedback> findFeedbackBy(String select) {
//    List<Feedback> feedback = new ArrayList<Feedback>();
//    Cursor cursor = null;
//    try {
//      cursor = contentResolver.query(FeedbackColumns.CONTENT_URI,
//          null, select, null, null);
//      if (cursor != null) {
//        while (cursor.moveToNext()) {
//          feedback.add(createFeedback(cursor));
//        }
//      }
//    } catch (RuntimeException e) {
//      Log.w(ExperimentProvider.TAG, "Caught unexpected exception.", e);
//    } finally {
//      if (cursor != null) {
//        cursor.close();
//      }
//    }
//    return feedback;
//  }

  public static Feedback createFeedback(Cursor cursor) {
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

  public static class SignalScheduleColumns implements BaseColumns {
    public static final String EXPERIMENT_ID = "experiment_id";
    public static final String SERVER_ID = "server_id";
    public static final String SCHEDULE_TYPE = "schedule_type";
    public static final String TIMES_CSV = "times";
    public static final String ESM_FREQUENCY = "esm_frequency";
    public static final String ESM_PERIOD = "esm_period";

    public static final String ESM_START_HOUR = "esm_start_hour";
    public static final String ESM_END_HOUR = "esm_end_hour";
    public static final String ESM_WEEKENDS = "esm_weekends";

    public static final String REPEAT_RATE =  "repeat_rate";
    public static final String WEEKDAYS_SCHEDULED  = "weekdays_scheduled";
    public static final String NTH_OF_MONTH  = "nth_of_month";
    public static final String BY_DAY_OF_MONTH = "by_day_of_month";
    public static final String DAY_OF_MONTH  = "day_of_month";
    public static final String BEGIN_DATE  = "begin_date";
    public static final String USER_EDITABLE = "user_editable";
    public static final String TIME_OUT = "timeout";
    public static final String MINIMUM_BUFFER = "minimum_buffer";
    public static final String SNOOZE_COUNT = "snooze_count";
    public static final String SNOOZE_TIME = "snooze_time";
    public static final String SIGNAL_TIMES = "signalTimesJson";
    public static final String ONLY_EDITABLE_ON_JOIN = "only_editable_on_join";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.paco.schedule";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.paco.schedule";

    public static final Uri CONTENT_URI = Uri.parse("content://"+ExperimentProviderUtil.AUTHORITY+"/schedules");







  }
  public static SignalingMechanism createSchedule(Cursor cursor) {
    int idIndex = cursor.getColumnIndexOrThrow(SignalScheduleColumns._ID);
    int serverIdIndex = cursor.getColumnIndexOrThrow(SignalScheduleColumns.SERVER_ID);
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
    int onlyEditableOnJoinIndex = cursor.getColumnIndex(SignalScheduleColumns.ONLY_EDITABLE_ON_JOIN);
    int timeoutIndex = cursor.getColumnIndex(SignalScheduleColumns.TIME_OUT);
    int minBufferIndex = cursor.getColumnIndex(SignalScheduleColumns.MINIMUM_BUFFER);
    int snoozeCountIndex = cursor.getColumnIndex(SignalScheduleColumns.SNOOZE_COUNT);
    int snoozeTimeIndex = cursor.getColumnIndex(SignalScheduleColumns.SNOOZE_TIME);
    int signalTimesJsonIndex = cursor.getColumnIndex(SignalScheduleColumns.SIGNAL_TIMES);

    SignalSchedule schedule = new SignalSchedule();
    if (!cursor.isNull(idIndex)) {
      schedule.setId(cursor.getLong(idIndex));
    }
    if (!cursor.isNull(serverIdIndex)) {
      schedule.setServerId(cursor.getLong(serverIdIndex));
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
    if (!cursor.isNull(signalTimesJsonIndex)) {
      List<SignalTime> signalTimes = fromJson(cursor.getString(signalTimesJsonIndex));
      schedule.setSignalTimes(signalTimes);
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
    if (!cursor.isNull(onlyEditableOnJoinIndex)) {
      schedule.setOnlyEditableOnJoin(cursor.getInt(onlyEditableOnJoinIndex) == 1? Boolean.TRUE : Boolean.FALSE);
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

  private static List<SignalTime> fromJson(String string) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(string, new TypeReference<List<SignalTime>>() {});
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Lists.newArrayList();
  }


  // end 21-> 22 backward compat code.

}
