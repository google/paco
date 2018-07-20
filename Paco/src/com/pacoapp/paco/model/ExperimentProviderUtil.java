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
package com.pacoapp.paco.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pacoapp.paco.UserPreferences;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.EventInterface;
import com.pacoapp.paco.shared.model2.EventStore;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentValidator;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.ValidationMessage;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;
import com.pacoapp.paco.shared.util.TimeUtil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.select.Select;

public class ExperimentProviderUtil implements EventStore {
  private static Logger Log = LoggerFactory.getLogger(ExperimentProviderUtil.class);

  private Context context;
  private ContentResolver contentResolver;
  public static final String AUTHORITY = "com.google.android.apps.paco.ExperimentProvider";
  private static final String PUBLIC_EXPERIMENTS_FILENAME = "experiments";
  private static final String MY_EXPERIMENTS_FILENAME = "my_experiments";
  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";
  // The next semaphore is used to make sure that all event inserts/retrievals happen atomically
  // with regards to each other, to ensure that no incomplete events can get synced to the server
  // (and, by extension, to ensure that a thread trying to access an event has to wait until the
  // event has been fully inserted).
  // The lock is used in the parent insert/getEvent methods.
  private static final ReentrantReadWriteLock eventStorageDbLock = new ReentrantReadWriteLock();
  private static final Lock eventStorageReadLock = eventStorageDbLock.readLock();
  private static final Lock eventStorageWriteLock = eventStorageDbLock.writeLock();
  private static Map<String, Class> validColumnNamesDataTypeInDb = null;
  private static final String ID = "_id";

  private static final String LIMIT = " limit ";

  DateTimeFormatter endDateFormatter = DateTimeFormat.forPattern(TimeUtil.DATE_FORMAT);
  static {
    validColumnNamesDataTypeInDb = Maps.newHashMap();
    validColumnNamesDataTypeInDb.put(EventColumns._ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.EXPERIMENT_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.EXPERIMENT_SERVER_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.EXPERIMENT_NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.EXPERIMENT_VERSION, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.SCHEDULE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.RESPONSE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.GROUP_NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.ACTION_TRIGGER_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.ACTION_TRIGGER_SPEC_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventColumns.ACTION_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(OutputColumns.NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(OutputColumns.ANSWER, StringValue.class);
  }

  public ExperimentProviderUtil(Context context) {
    super();
    this.context = context;
    if (context == null) {
      throw new IllegalArgumentException("Need a context to instantiate experimentproviderutil");
    }
    this.contentResolver = context.getContentResolver();
  }

  public List<Experiment> getJoinedExperiments() {
    List<Experiment> cachedExperiments = JoinedExperimentCache.getInstance().getExperiments();
    if (cachedExperiments.size() > 0) {
      return cachedExperiments;
    }

    final List<Experiment> foundExperiments = findExperimentsBy(null, ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI);
    if (foundExperiments != null && foundExperiments.size() > 0 ) {
      JoinedExperimentCache.getInstance().insertExperiments(foundExperiments);
    }
    return foundExperiments;
  }

  public List<Long> getJoinedExperimentServerIds() {
    List<Experiment> joinedExperiments = getJoinedExperiments();

    List<Experiment> stillRunningExperiments = Lists.newArrayList();
    DateMidnight tonightMidnight = new DateMidnight().plusDays(1);
    DateTime now = DateTime.now();
    for (Experiment experiment : joinedExperiments) {

      final ExperimentDAO experimentDAO = experiment.getExperimentDAO();
      if (experimentDAO != null && !ActionScheduleGenerator.isOver(now, experimentDAO)) {
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

  private boolean isJoinedExperimentAndroidId(long id) {
    String select = ExperimentColumns._ID + "=" + id;
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(ExperimentColumns.CONTENT_URI,
          null, select, null, null);
      if (cursor != null && cursor.moveToNext()) {
        return true;
      }
    } catch (RuntimeException e) {
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return false;
  }

  public List<Experiment> getExperimentsByServerId(long id) {
    List<Experiment> cachedExperiments = JoinedExperimentCache.getInstance().getExperimentsByServerId(id);
    if (cachedExperiments.size() > 0) {
      return cachedExperiments;
    }
    String select = ExperimentColumns.SERVER_ID + "=" + id;
    final List<Experiment> foundExperiments = findExperimentsBy(select, ExperimentColumns.CONTENT_URI);

    if (foundExperiments != null && foundExperiments.size() > 0) {
      JoinedExperimentCache.getInstance().insertExperiments(foundExperiments);
    }


    return foundExperiments;
  }

  public Experiment getExperimentByServerId(long id) {
    Experiment cachedExperiment = JoinedExperimentCache.getInstance().getExperimentByServerId(id);
    if (cachedExperiment != null) {
      return cachedExperiment;
    }

    String select = ExperimentColumns.SERVER_ID + "=" + id;
    final Experiment foundExperiment = findExperimentBy(select, ExperimentColumns.CONTENT_URI);

    if (foundExperiment != null) {
      JoinedExperimentCache.getInstance().insertExperiment(foundExperiment);
    }

    return foundExperiment;
  }

  private Uri insertExperiment(Experiment experiment) {
    final Uri returnedUri = contentResolver.insert(ExperimentColumns.CONTENT_URI,
        createContentValues(experiment));
    long rowId = Long.parseLong(returnedUri.getLastPathSegment());
    experiment.setId(rowId);

    JoinedExperimentCache.getInstance().insertExperiment(experiment);
    return returnedUri;
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
    setJoinDateOnSchedules(experiment);
    return insertExperiment(experiment);
  }

  private void setJoinDateOnSchedules(Experiment experiment) {
    long joinDateMillis = getJoinDateMillis(experiment);
    for (ExperimentGroup experimentGroup : experiment.getExperimentDAO().getGroups()) {
      if (GroupTypeEnum.SYSTEM.equals(experimentGroup.getGroupType())) {
        continue;
      } else {
        List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : actionTriggers) {
          if (actionTrigger instanceof ScheduleTrigger) {
            ScheduleTrigger scheduleTrigger = (ScheduleTrigger)actionTrigger;
            List<Schedule> schedules = scheduleTrigger.getSchedules();
            for (Schedule schedule : schedules) {
              schedule.setJoinDateMillis(joinDateMillis);
            }
          }
        }
      }
    }
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

  /**
   * This uses the Android ContentProvider id for the joined experiment
   * @param experimentId
   */
  public void deleteExperiment(long experimentId) {
    if (isJoinedExperimentAndroidId(experimentId)) {
      String[] selectionArgs = new String[] {Long.toString(experimentId)};
      contentResolver.delete(ExperimentColumns.CONTENT_URI,
          "_id = ?",
          selectionArgs);
      JoinedExperimentCache.getInstance().deleteExperiment(experimentId);
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
   * @param experimentList
   * @param shouldOverrideExistingSettings downloaded (refreshed experiments should not override certain
   * local properties. Locally modified experiments should override local properties, e.g., logActions.
   */
  public void updateExistingExperiments(List<Experiment> newExperimentDefinitions,
                                        Boolean shouldOverrideExistingSettings) {
    for (Experiment experiment : newExperimentDefinitions) {
      List<Experiment> existingList = getExperimentsByServerId(experiment.getServerId());
      if (existingList.size() == 0) {
        continue;
      }
      for (Experiment existingExperiment : existingList) { // should become only 1 element if we prevent joining experiments multiple times
        copyAllPropertiesToExistingJoinedExperiment(experiment, existingExperiment, shouldOverrideExistingSettings);
        updateJoinedExperiment(existingExperiment);
      }

    }
  }

  private void copyAllPropertiesToExistingJoinedExperiment(Experiment newExperiment, Experiment existingExperiment,
                                                           Boolean shouldOverrideExistingSettings) {
    UserPreferences userPrefs = new UserPreferences(context);
    if (shouldOverrideExistingSettings || !userPrefs.experimentEdited(existingExperiment.getId())) {
      ExperimentDAO newExperimentDAO = newExperiment.getExperimentDAO();
      ExperimentDAO existingDAO = existingExperiment.getExperimentDAO();
      // TODO preserve any modified schedule settings if it is user-editable and different from the new
      newExperimentDAO.setJoinDate(existingDAO.getJoinDate());
      existingExperiment.setExperimentDAO(newExperimentDAO);
      existingExperiment.setJoinDate(existingExperiment.getJoinDate());
    }

  }

  public void updateJoinedExperiment(Experiment experiment) {
    long t1 = System.currentTimeMillis();
    int count = contentResolver.update(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI,
        createContentValues(experiment),
        ExperimentColumns._ID + "=" + experiment.getId(), null);
    JoinedExperimentCache.getInstance().insertExperiment(experiment);
    Log.info(" updated "+ count + " rows. Time: " + (System.currentTimeMillis() - t1));
  }

  public void deleteAllExperiments() {
    contentResolver.delete(ExperimentColumns.JOINED_EXPERIMENTS_CONTENT_URI, null, null);
    JoinedExperimentCache.getInstance().deleteAllExperiments();
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
      Log.warn("Caught unexpected exception.", e);
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
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return experiments;
  }


  /** This converts the old version of json (db == 22) to the new model
   *
   * @param experimentDAO
   * @param jsonOfExperiment
   * @throws JsonProcessingException
   * @throws IOException
   */
  public static void copyAllPropertiesFromJsonToExperimentDAO(ExperimentDAO experimentDAO, String jsonOfExperiment) throws JsonProcessingException, IOException {
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    JsonNode rootNode = mapper.readTree(jsonOfExperiment);
    if (rootNode.has("id")) {
      experimentDAO.setId(rootNode.path("id").getLongValue());
    }
    if (rootNode.has("title")) {
      experimentDAO.setTitle(rootNode.path("title").getTextValue());
    }
    if (rootNode.has("description")) {
      experimentDAO.setCreator(rootNode.path("description").getTextValue());
    }
    if (rootNode.has("creator")) {
      experimentDAO.setCreator(rootNode.path("creator").getTextValue());
      experimentDAO.setContactEmail(rootNode.path("creator").getTextValue());
    }
    if (rootNode.has("contactEmail")) {
      experimentDAO.setContactEmail(rootNode.path("contactEmail").getTextValue());
    }
    if (!rootNode.has("creator") && !rootNode.has("contactEmail")) {
      experimentDAO.setContactEmail(rootNode.path("").getTextValue());
    }
    if (rootNode.has("modifyDate")) {
      experimentDAO.setModifyDate(rootNode.path("modifyDate").getTextValue());
    }
    if (rootNode.has("version")) {
      experimentDAO.setVersion(rootNode.path("version").getIntValue());
    }
    if (rootNode.has("joinDate")) {
      experimentDAO.setJoinDate(rootNode.path("joinDate").getTextValue());
    }
    if (rootNode.has("informedConsentForm")) {
      experimentDAO.setInformedConsentForm(rootNode.path("informedConsent").getTextValue());
    }
    if (rootNode.has("recordPhoneDetails")) {
      experimentDAO.setRecordPhoneDetails(rootNode.path("recordPhoneDetails").getBooleanValue());
    }
    if (rootNode.has("deleted")) {
      experimentDAO.setDeleted(rootNode.path("deleted").getBooleanValue());
    }
    if (rootNode.has("extraDataCollectionDeclarations")) {
      List<Integer> edcd = Lists.newArrayList();
      List<JsonNode> edcdNodes = rootNode.findValues("extraDataCollectionDeclarations");
      for (JsonNode edcdNode : edcdNodes) {
        edcd.add(edcdNode.getIntValue());
      }
      experimentDAO.setExtraDataCollectionDeclarations(edcd);
    }

    List<String> admins = Lists.newArrayList();
    List<JsonNode> adminNodes = rootNode.findValues("admins");
    for (JsonNode adminNode : adminNodes) {
      admins.add(adminNode.getTextValue());
    }
    experimentDAO.setAdmins(admins);

    if (rootNode.has("published")) {
      experimentDAO.setPublished(rootNode.path("published").getBooleanValue());
    }
    List<String> publishedList = Lists.newArrayList();
    List<JsonNode> publishedListNodes = rootNode.findValues("publishedUsers");
    for (JsonNode publishedListNode : publishedListNodes) {
      publishedList.add(publishedListNode.getTextValue());
    }
    experimentDAO.setPublishedUsers(publishedList);


    ExperimentGroup defaultExperimentGroup = new ExperimentGroup();
    defaultExperimentGroup.setName("default");
    experimentDAO.getGroups().add(defaultExperimentGroup);

    if (rootNode.has("endDate")) {
      defaultExperimentGroup.setEndDate(rootNode.path("endDate").getTextValue());
    }
    if (rootNode.has("startDate")) {
      defaultExperimentGroup.setEndDate(rootNode.path("startDate").getTextValue());
    }
    if (rootNode.has("fixedDuration")) {
      defaultExperimentGroup.setFixedDuration(rootNode.path("fixedDuration").getBooleanValue());
    }
    if (rootNode.has("groupType")) {
      defaultExperimentGroup.setGroupType(GroupTypeEnum.valueOf(rootNode.path("groupType").getTextValue()));
    }



//    experimentDAO.setWebRecommended(experiment.isWebRecommended());
    // maybe blow up on webrecommended to let them know to unjoin and rejoin
    // TODO figure out how to handle this.

    if (rootNode.has("customRendering")) {
      defaultExperimentGroup.setCustomRendering(rootNode.path("customRendering").getBooleanValue());
    }
    if (rootNode.has("customRenderingCode")) {
      defaultExperimentGroup.setCustomRenderingCode(rootNode.path("customRenderingCode").getTextValue());
    }

    if (rootNode.has("feedbackType")) {
      //see feedback section below
      defaultExperimentGroup.setFeedbackType(rootNode.path("feedbackType").getIntValue());
    }
    if (rootNode.has("logActions")) {
      defaultExperimentGroup.setLogActions(rootNode.path("logActions").getBooleanValue());
    }
    if (rootNode.has("logShutdown")) {
      defaultExperimentGroup.setLogShutdown(rootNode.path("logShutdown").getBooleanValue());
    }
    if (rootNode.has("logNotificationEvents")) {
      defaultExperimentGroup.setLogNotificationEvents(rootNode.path("logNotificationEvents").getBooleanValue());
    }
    if (rootNode.has("rawDataAccess")) {
      defaultExperimentGroup.setRawDataAccess(rootNode.path("rawDataAccess").getBooleanValue());
    }

    if (rootNode.has("backgroundListen")) {
      defaultExperimentGroup.setBackgroundListen(rootNode.path("backgroundListen").getBooleanValue());
    }
    if (rootNode.has("backgroundListenSourceIdentifier")) {
      defaultExperimentGroup.setBackgroundListenSourceIdentifier(rootNode.path("backgroundListenSourceIdentifier").getTextValue());
    }
    if (rootNode.has("accessibilityListen")) {
      defaultExperimentGroup.setAccessibilityListen(rootNode.path("accessibilityListen").getBooleanValue());
    }
    if (rootNode.has("inputs")) {
      List<Input2> inputs = Lists.newArrayList();
      ArrayNode inputsNode = (ArrayNode)rootNode.path("inputs");

      for (int i=0; i < inputsNode.size(); i++) {
        JsonNode inputNode = inputsNode.get(i);
        Input2 input = new Input2();
        inputs.add(input);
        if (inputNode.has("name")) {
          input.setName(inputNode.path("name").getTextValue());
        }
        if (inputNode.has("required")) {
          input.setRequired(inputNode.path("required").getBooleanValue());
        }
        if (inputNode.has("mandatory")) {
          input.setRequired(inputNode.path("mandatory").getBooleanValue());
        }
        if (inputNode.has("conditional")) {
          input.setConditional(inputNode.path("conditional").getBooleanValue());
        }
        if (inputNode.has("conditionExpression")) {
          input.setConditionExpression(inputNode.path("conditionExpression").getTextValue());
        }
        if (inputNode.has("responseType")) {
          input.setResponseType(inputNode.path("responseType").getTextValue());
        }
        if (inputNode.has("text")) {
          input.setText(inputNode.path("text").getTextValue());
        }
        if (inputNode.has("likertSteps")) {
          input.setLikertSteps(inputNode.path("likertSteps").getIntValue());
        }
        if (inputNode.has("leftSideLabel")) {
          input.setLeftSideLabel(inputNode.path("leftSideLabel").getTextValue());
        }
        if (inputNode.has("rightSideLabel")) {
          input.setRightSideLabel(inputNode.path("rightSideLabel").getTextValue());
        }
        if (inputNode.has("multiselect")) {
          input.setMultiselect(inputNode.path("multiselect").getBooleanValue());
        }
        List<String> listChoices = Lists.newArrayList();
        ArrayNode listChoicesNode = (ArrayNode) inputNode.path("listChoices");
        for (int l = 0; l < listChoicesNode.size(); l++) {
          JsonNode listChoiceNode = listChoicesNode.get(l);
          listChoices.add(listChoiceNode.getTextValue());
        }

        input.setListChoices(listChoices);
      }

      defaultExperimentGroup.setInputs(inputs);
    }

    //signaling mechanisms
    if (rootNode.has("signalingMechanisms")) {
      List<ActionTrigger> actionTriggers = defaultExperimentGroup.getActionTriggers();
      ArrayNode signalingMechanismNodes = (ArrayNode) rootNode.path("signalingMechanisms");
      for (int k = 0; k< signalingMechanismNodes.size(); k++) {
        JsonNode signalingMechanismNode = signalingMechanismNodes.get(k);
          if (signalingMechanismNode.has("type")) {
          String type = signalingMechanismNode.path("type").getTextValue();

          PacoNotificationAction defaultAction = new PacoNotificationAction();
          defaultAction.setActionCode(PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE);
          defaultAction.setId(1l);


          if (type.equals("signalSchedule")) {
            com.pacoapp.paco.shared.model2.ScheduleTrigger trigger = new com.pacoapp.paco.shared.model2.ScheduleTrigger();
            trigger.setId(1l);
            trigger.getActions().add(defaultAction);
            com.pacoapp.paco.shared.model2.Schedule schedule = new com.pacoapp.paco.shared.model2.Schedule();
            schedule.setId(1l);
            defaultAction.setSnoozeCount(signalingMechanismNode.path("snoozeCount").getIntValue());
            defaultAction.setSnoozeTime(signalingMechanismNode.path("snoozeTime").getIntValue());
            defaultAction.setTimeout(signalingMechanismNode.path("timeout").getIntValue());

            schedule.setScheduleType(signalingMechanismNode.path("scheduleType").getIntValue());
            schedule.setEsmFrequency(signalingMechanismNode.path("esmFrequency").getIntValue());
            schedule.setEsmPeriodInDays(signalingMechanismNode.path("esmPeriodInDays").getIntValue());
            schedule.setEsmStartHour(signalingMechanismNode.path("esmStartHour").getLongValue());
            schedule.setEsmEndHour(signalingMechanismNode.path("esmEndHour").getLongValue());
            schedule.setRepeatRate(signalingMechanismNode.path("repeatRate").getIntValue());
            schedule.setWeekDaysScheduled(signalingMechanismNode.path("weekdaysScheduled").getIntValue());
            schedule.setNthOfMonth(signalingMechanismNode.path("nthOfMonth").getIntValue());
            schedule.setByDayOfMonth(signalingMechanismNode.path("byDayOfMonth").getBooleanValue());
            schedule.setDayOfMonth(signalingMechanismNode.path("dayOfMonth").getIntValue());
            schedule.setEsmWeekends(signalingMechanismNode.path("esmWeekends").getBooleanValue());
            schedule.setMinimumBuffer(signalingMechanismNode.path("minimumBuffer").getIntValue());

            schedule.setUserEditable(signalingMechanismNode.path("userEditable").getBooleanValue());
            schedule.setOnlyEditableOnJoin(signalingMechanismNode.path("onlyEditableOnJoin").getBooleanValue());

            List<com.pacoapp.paco.shared.model2.SignalTime> signalTimes = schedule.getSignalTimes();
            if (signalingMechanismNode.has("signalTimes")) {
              ArrayNode signalTimeNodes = (ArrayNode) signalingMechanismNode.path("signalTimes");
              for (int l = 0; l < signalTimeNodes.size(); l++) {
                JsonNode signalTimeNode  = signalTimeNodes.get(l);
                com.pacoapp.paco.shared.model2.SignalTime newSt = new com.pacoapp.paco.shared.model2.SignalTime();
                newSt.setType(signalTimeNode.path("type").getIntValue());
                newSt.setFixedTimeMillisFromMidnight(signalTimeNode.path("fixedTimeMillisFromMidnight").getIntValue());
                newSt.setBasis(signalTimeNode.path("basis").getIntValue());
                newSt.setOffsetTimeMillis(signalTimeNode.path("offsetTimeMillis").getIntValue());
                newSt.setLabel(signalTimeNode.path("label").getTextValue());
                newSt.setMissedBasisBehavior(signalTimeNode.path("missedBasisBehavior").getIntValue());
                schedule.getSignalTimes().add(newSt);
              }
            }

            trigger.getSchedules().add(schedule);
            actionTriggers.add(trigger);
          } else if (type.equals("trigger")) {
            com.pacoapp.paco.shared.model2.InterruptTrigger trigger = new com.pacoapp.paco.shared.model2.InterruptTrigger();
            trigger.setId(1l);
            trigger.getActions().add(defaultAction);

            defaultAction.setSnoozeCount(signalingMechanismNode.path("snoozeCount").getIntValue());
            defaultAction.setSnoozeTime(signalingMechanismNode.path("snoozeTime").getIntValue());
            defaultAction.setTimeout(signalingMechanismNode.path("timeout").getIntValue());

            trigger.setMinimumBuffer(signalingMechanismNode.path("minimumBuffer").getIntValue());
            InterruptCue cue = new InterruptCue();
            cue.setId(1l);
            if (signalingMechanismNode.has("eventCode")) {
              cue.setCueCode(signalingMechanismNode.path("eventCode").getIntValue());
            }
            if (signalingMechanismNode.has("delay")) {
              defaultAction.setDelay(signalingMechanismNode.path("delay").getIntValue());
            }
            if (signalingMechanismNode.has("sourceIdentifier")) {
              cue.setCueSource(signalingMechanismNode.path("sourceIdentifier").getTextValue());
            }

            trigger.getCues().add(cue);
            actionTriggers.add(trigger);
          }

        }
      }
    }

    com.pacoapp.paco.shared.model2.Feedback f = new com.pacoapp.paco.shared.model2.Feedback();
    if (rootNode.has("feedback")) {
      ArrayNode feedbackNodes = (ArrayNode) rootNode.path("feedback");
      JsonNode feedbackNode = feedbackNodes.get(0);
      if (feedbackNode.has("text")) {
        f.setText(feedbackNode.path("text").getTextValue());
      }

    }
    if (rootNode.has("feedbackType")) {
      f.setType(rootNode.path("feedbackType").getIntValue());
    }
    defaultExperimentGroup.setFeedback(f);
    ExperimentValidator validator = new ExperimentValidator();
    experimentDAO.validateWith(validator);
    List<ValidationMessage> results = validator.getResults();
    if (!results.isEmpty()) {
      if (results.size() == 1 && results.get(0).getMsg().equals("admins should be a valid list of email addresses")) {
        return; // OK to not have admins
      } else {
        Log.error("error migrating experiment: " + experimentDAO.getId() + ":\n" + Joiner.on(",").join(results));
      }
    }

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
        Log.debug("time to de-jsonify experiment (bytes: " + jsonOfExperiment.getBytes().length + ") : " + (System.currentTimeMillis() - t1));
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
    if (experiment.getExperimentDAO().getTitle() != null) {
      values.put(ExperimentColumns.TITLE, experiment.getExperimentDAO().getTitle());
    }
    if (experiment.getExperimentDAO().getJoinDate() != null) {
      values.put(ExperimentColumns.JOIN_DATE, experiment.getExperimentDAO().getJoinDate());
    } else if (experiment.getJoinDate() != null) {
      values.put(ExperimentColumns.JOIN_DATE, experiment.getJoinDate());
    }

    long t1 = System.currentTimeMillis();
    String json = getJson(experiment);
    Log.error("time to jsonify experiment (bytes: " + json.getBytes().length + "): " + (System.currentTimeMillis() - t1));
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
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      Log.error("Json generation error " + e);
    } catch (JsonMappingException e) {
      Log.error("JsonMapping error getting experiment json: " + e.getMessage());
    } catch (IOException e) {
      Log.error("IO error getting experiment: " + e.getMessage());
    }

    return null;

  }

  public static String getJson(List<Experiment> experiments) {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    try {
      return mapper.writeValueAsString(experiments);
    } catch (JsonGenerationException e) {
      Log.error("Json generation error " + e);
    } catch (JsonMappingException e) {
      Log.error("JsonMapping error getting experiment json: " + e.getMessage());
    } catch (IOException e) {
      Log.error("IO error getting experiment: " + e.getMessage());
    }

    return null;
  }

  public void loadEventsForExperiment(Experiment experiment) {
    List<Event> eventSingleEntryList = findEventsBy(EventColumns.EXPERIMENT_ID + "=" + experiment.getId(),
        EventColumns._ID +" DESC");
    experiment.setEvents(eventSingleEntryList);
  }

  public List<Event> loadEventsForExperimentByServerId(Long serverId) {
	return loadEventsForExperimentByServerId(serverId, null);
  }

  public List<Event> loadEventsForExperimentByServerId(Long serverId, Integer noOfRecords) {
	    return findEventsBy(EventColumns.EXPERIMENT_SERVER_ID + " = " + Long.toString(serverId),
	        EventColumns._ID +" DESC", noOfRecords);
  }

  public Uri insertEvent(Event event) {
    eventStorageWriteLock.lock();
    try {
      Uri uri = contentResolver.insert(EventColumns.CONTENT_URI, createContentValues(event));
      long rowId = Long.parseLong(uri.getLastPathSegment());
      event.setId(rowId);
      for (Output response : event.getResponses()) {
        response.setEventId(rowId);
        insertResponse(response);
      }
      return uri;
    } catch (Exception e) {
      Log.warn("Caught unexpected exception.", e);
      return null;
    } finally {
      // Will get called even with return statements before
      eventStorageWriteLock.unlock();
      Log.debug("Finished inserting event");
    }
  }

  public void insertEvent(EventInterface eventI) {
    if (eventI instanceof Event) {
      Event event = (Event)eventI;
      insertEvent(event);
    }
  }

  public EventQueryStatus findEventsByCriteriaQuery(SQLQuery sqlQuery, Long expId) {
    Cursor cursor = null;
    List<Event> events = Lists.newArrayList();
    Event event = null;
    Map<Long, Event> eventMap = null;
    boolean webRequest = false;
    List<String> dateColumns = Lists.newArrayList();
    dateColumns.add(EventColumns.RESPONSE_TIME);
    EventQueryStatus evQryStat = new EventQueryStatus();
    DatabaseHelper dbHelper = new DatabaseHelper(context);

    try {
      String selectSql = SearchUtil.getPlainSql(sqlQuery);
      Select selectStmt = SearchUtil.getJsqlSelectStatement(selectSql);
      // preprocessor parses the query, and identifies potential issues like invalid column name, invalid data tye, sql injection,
      // or if join is needed.
      QueryPreprocessor qProcessor = new QueryPreprocessor(selectStmt, validColumnNamesDataTypeInDb, webRequest, dateColumns);
      if (qProcessor.containExpIdClause() == false || qProcessor.getExpIdValues().size() > 1 || !qProcessor.getExpIdValues().contains(expId)) {
        evQryStat.setStatus(FAILURE);
        evQryStat.setErrorMessage(ErrorMessages.EXPERIMENT_ID_CLAUSE_EXCEPTION.getDescription());
        return evQryStat;
      }
      if (qProcessor.containWhoClause()) {
        evQryStat.setStatus(FAILURE);
        evQryStat.setErrorMessage(ErrorMessages.INVALID_COLUMN_NAME.getDescription() + qProcessor.getWhoClause());
        return evQryStat;
      }
      if (qProcessor.probableSqlInjection() != null){
        evQryStat.setStatus(FAILURE);
        evQryStat.setErrorMessage(ErrorMessages.PROBABLE_SQL_INJECTION.getDescription() + qProcessor.probableSqlInjection());
        return evQryStat;
      }
      if (qProcessor.getInvalidDataType() != null){
        evQryStat.setStatus(FAILURE);
        evQryStat.setErrorMessage(ErrorMessages.INVALID_DATA_TYPE.getDescription() + qProcessor.getInvalidDataType());
        return evQryStat;
      }
      if (qProcessor.getInvalidColumnName() != null){
        evQryStat.setStatus(FAILURE);
        evQryStat.setErrorMessage(ErrorMessages.INVALID_COLUMN_NAME.getDescription() + qProcessor.getInvalidColumnName());
        return evQryStat;
      }
      // change date params to long.
      String[] origCriValue = sqlQuery.getCriteriaValue();
      String[] modCriValue = new String[origCriValue.length];
      System.arraycopy(origCriValue, 0, modCriValue, 0, origCriValue.length);
      Map<String, Long> dateMap = qProcessor.getDateParamWithLong();
      for (int i=0 ;i<origCriValue.length; i++ ) {
        Long dateAsLong =  dateMap.get(origCriValue[i]);
        if ( dateAsLong != null) {
          modCriValue[i] = dateAsLong.toString();
        }
      }

      if (qProcessor.isOutputColumnsPresent() || sqlQuery.isFullEventAndOutputs()) {
        cursor = dbHelper.query(ExperimentProvider.OUTPUTS_DATATYPE, sqlQuery.getProjection(), sqlQuery.getCriteriaQuery(), modCriValue,
                                sqlQuery.getSortOrder(), sqlQuery.getGroupBy(), sqlQuery.getHaving(), sqlQuery.getLimit());
      } else {
        cursor = dbHelper.query(ExperimentProvider.EVENTS_DATATYPE, sqlQuery.getProjection(), sqlQuery.getCriteriaQuery(), modCriValue,
                                sqlQuery.getSortOrder(), sqlQuery.getGroupBy(), sqlQuery.getHaving(), sqlQuery.getLimit());
      }
      //to maintain the insertion order
      eventMap = Maps.newLinkedHashMap();

      if (cursor != null) {
        events = Lists.newArrayList();
        boolean withOutputs = qProcessor.isOutputColumnsPresent() || sqlQuery.isFullEventAndOutputs();
        while (cursor.moveToNext()) {
          //no need to coalesce, we just add it to the list and send the collection to the client.
          event = createEvent(cursor, false, withOutputs);
          Event oldEvent = eventMap.get(event.getId());
          if(oldEvent == null){
            event.setResponses(findResponsesFor(event));
            eventMap.put(event.getId(), event);
          }
        }
      }
    } catch (JSQLParserException e) {
      evQryStat.setStatus(FAILURE);
      evQryStat.setErrorMessage(ErrorMessages.JSQL_PARSER_EXCEPTION.getDescription() + e);
      closeResources(cursor, dbHelper);
      return evQryStat;
    } catch(SQLiteException sqle) {
      evQryStat.setStatus(FAILURE);
      evQryStat.setErrorMessage(ErrorMessages.SQL_EXCEPTION.getDescription() + sqle);
      closeResources(cursor, dbHelper);
      return evQryStat;
    } catch (Exception e){
      evQryStat.setStatus(FAILURE);
      evQryStat.setErrorMessage(ErrorMessages.GENERAL_EXCEPTION.getDescription() + e);
      closeResources(cursor, dbHelper);
      return evQryStat;
    } finally {
      closeResources(cursor, dbHelper);
    }
    events = Lists.newArrayList(eventMap.values());
    evQryStat.setEvents(events);
    evQryStat.setStatus(SUCCESS);
    return evQryStat;
  }

  private void closeResources(Cursor cursor, DatabaseHelper dbHelper){
    if (cursor != null) {
      cursor.close();
    }
    dbHelper.close();
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

    if (event.getExperimentGroupName() != null) {
      values.put(EventColumns.GROUP_NAME, event.getExperimentGroupName());
    }
    if (event.getActionTriggerId() != null) {
      values.put(EventColumns.ACTION_TRIGGER_ID, event.getActionTriggerId());
    }
    if (event.getActionTriggerSpecId() != null) {
      values.put(EventColumns.ACTION_TRIGGER_SPEC_ID, event.getActionTriggerSpecId());
    }
    if (event.getActionId() != null) {
      values.put(EventColumns.ACTION_ID, event.getActionId());
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
    if (response.getAnswer() != null) {
      values.put(OutputColumns.ANSWER, response.getAnswer());
    }
    if (response.getName() != null) {
      values.put(OutputColumns.NAME, response.getName());
    }
    return values;
  }


  private Event findEventBy(String select, String[] selectionArgs, String sortOrder) {
    Cursor cursor = null;
    eventStorageReadLock.lock();
    try {
      cursor = contentResolver.query(EventColumns.CONTENT_URI,
          null, select, selectionArgs, sortOrder);
      if (cursor != null && cursor.moveToNext()) {
        Event event = createEvent(cursor);
        event.setResponses(findResponsesFor(event));
        return event;
      }
    } catch (RuntimeException e) {
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      eventStorageReadLock.unlock();
    }
    return null;
  }

  private List<Event> findEventsBy(String select, String sortOrder) {
    List<Event> events = new ArrayList<Event>();
    Cursor cursor = null;
    eventStorageReadLock.lock();
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
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      eventStorageReadLock.unlock();
    }
    return events;
  }

  //This is a hack, but should improve the performance for now.
  private List<Event> findEventsBy(String select, String sortOrder, Integer limitNoOfRecords) {
	  if (limitNoOfRecords != null) {
		  return findEventsBy(select, sortOrder + LIMIT  + limitNoOfRecords);
	  } else {
		  return  findEventsBy(select, sortOrder);
	  }
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
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return responses;
  }

  private Event createEvent(Cursor cursor){
    boolean cursorWithOutputColumns = false;
    return createEvent(cursor, true, cursorWithOutputColumns);
  }

  private Event createEvent(Cursor cursor, boolean requiredFieldsFlag, boolean withOutputColumns) {
    int idIndex, experimentIdIndex;
    // If output table columns are present in cursor, we will have duplicate column names for _id. One will be wrt the events table
    // which is the correct one and the other will be wrt outputs table, which is not the intended id.
    // When we have output columns, we get event_id from outputs table, otherwise _id from events table
    if(requiredFieldsFlag) {
      if (withOutputColumns) {
        idIndex = cursor.getColumnIndexOrThrow(OutputColumns.EVENT_ID);
      } else {
        idIndex = cursor.getColumnIndexOrThrow(EventColumns._ID);
      }
      experimentIdIndex = cursor.getColumnIndexOrThrow(EventColumns.EXPERIMENT_ID);
    } else {
      if (withOutputColumns) {
        idIndex = cursor.getColumnIndex(OutputColumns.EVENT_ID);
      } else {
        idIndex = cursor.getColumnIndex(EventColumns._ID);
      }
      experimentIdIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_ID);
    }
    
    int experimentServerIdIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_SERVER_ID);
    int experimentVersionIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_VERSION);
    int experimentNameIndex = cursor.getColumnIndex(EventColumns.EXPERIMENT_NAME);
    int scheduleTimeIndex = cursor.getColumnIndex(EventColumns.SCHEDULE_TIME);
    int responseTimeIndex = cursor.getColumnIndex(EventColumns.RESPONSE_TIME);
    int uploadedIndex = cursor.getColumnIndex(EventColumns.UPLOADED);
    int groupNameIndex = cursor.getColumnIndex(EventColumns.GROUP_NAME);
    int actionTriggerIndex = cursor.getColumnIndex(EventColumns.ACTION_TRIGGER_ID);
    int actionTriggerSpecIndex = cursor.getColumnIndex(EventColumns.ACTION_TRIGGER_SPEC_ID);
    int actionIdIndex = cursor.getColumnIndex(EventColumns.ACTION_ID);
    Event event = new Event();

    if (!cursor.isNull(idIndex)) {
      event.setId(cursor.getLong(idIndex));
    }

    if (!cursor.isNull(experimentIdIndex)) {
      event.setExperimentId(cursor.getLong(experimentIdIndex));
    }

    if (!cursor.isNull(experimentServerIdIndex)) {
      event.setServerExperimentId(cursor.getLong(experimentServerIdIndex));
    }
    if (!cursor.isNull(experimentVersionIndex)) {
      event.setExperimentVersion(cursor.getInt(experimentVersionIndex));
    }
    if (!cursor.isNull(experimentNameIndex)) {
      event.setExperimentName(cursor.getString(experimentNameIndex));
    }

    if (!cursor.isNull(responseTimeIndex)) {
      event.setResponseTime(new DateTime(cursor.getLong(responseTimeIndex)));
    }
    if (!cursor.isNull(scheduleTimeIndex)) {
      event.setScheduledTime(new DateTime(cursor.getLong(scheduleTimeIndex)));
    }
    if (!cursor.isNull(uploadedIndex)) {
      event.setUploaded(cursor.getInt(uploadedIndex) == 1);
    }

    if (!cursor.isNull(groupNameIndex)) {
      event.setExperimentGroupName(cursor.getString(groupNameIndex));
    }
    if (!cursor.isNull(actionTriggerIndex)) {
      event.setActionTriggerId(cursor.getLong(actionTriggerIndex));
    }
    if (!cursor.isNull(actionTriggerSpecIndex)) {
      event.setActionTriggerSpecId(cursor.getLong(actionTriggerSpecIndex));
    }
    if (!cursor.isNull(actionIdIndex)) {
      event.setActionId(cursor.getLong(actionIdIndex));
    }
    return event;
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

  public void updateEvent(EventInterface eventI) {
    if (eventI instanceof Event) {
      Event event = (Event)eventI;

      eventStorageWriteLock.lock();
      try {
        contentResolver.update(EventColumns.CONTENT_URI,
                createContentValues(event), "_id=" + event.getId(), null);
      } catch (Exception e) {
        Log.error("Unexpected exception when updating event: " + e);
      } finally {
        eventStorageWriteLock.unlock();
      }
    } else {
      throw new IllegalArgumentException("I only know how to deal with Android objects!");
    }
  }

  public List<Event> getEventsNeedingUpload() {
    String select = EventColumns.UPLOADED + " != 1";
    return findEventsBy(select, null);
  }

  public List<Event> getAllEvents() {
    return findEventsBy(null, null);
  }

  public NotificationHolder getNotificationForSource(long experimentId, String source) {
    String[] selectionArgs = new String[] {Long.toString(experimentId), source};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ? and " + NotificationHolderColumns.NOTIFICATION_SOURCE + " = ?";
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

  public NotificationHolder getNotificationForAction(long experimentId, Long actionId) {
    String[] selectionArgs = new String[] {Long.toString(experimentId), Long.toString(actionId)};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ? and " + NotificationHolderColumns.ACTION_ID + " = ?";
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

  public List<NotificationHolder> getNotificationsFor(long experimentId, String experimentGroupName) {
    List<NotificationHolder> holders = new ArrayList<NotificationHolder>();
    String[] selectionArgs = new String[] {Long.toString(experimentId), experimentGroupName};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ? and " + NotificationHolderColumns.EXPERIMENT_GROUP_NAME + " = ?";
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
    int snoozeCountIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.SNOOZE_COUNT);
    int snoozeTimeIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.SNOOZE_TIME);
    int groupNameIndexIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.EXPERIMENT_GROUP_NAME);
    int actionTriggerIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.ACTION_TRIGGER_ID);
    int actionTriggerSpecIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.ACTION_TRIGGER_SPEC_ID);
    int actionIdIndex = cursor.getColumnIndexOrThrow(NotificationHolderColumns.ACTION_ID);


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
    if (!cursor.isNull(snoozeCountIndex)) {
      notification.setSnoozeCount(cursor.getInt(snoozeCountIndex));
    }

    if (!cursor.isNull(snoozeTimeIndex)) {
      notification.setSnoozeTime(cursor.getInt(snoozeTimeIndex));
    }

    if (!cursor.isNull(groupNameIndexIndex)) {
      notification.setExperimentGroupName(cursor.getString(groupNameIndexIndex));
    }
    if (!cursor.isNull(actionTriggerIdIndex)) {
      notification.setActionTriggerId(cursor.getLong(actionTriggerIdIndex));
    }

    if (!cursor.isNull(actionTriggerSpecIdIndex)) {
      notification.setActionTriggerSpecId(cursor.getLong(actionTriggerSpecIdIndex));
    }

    if (!cursor.isNull(actionIdIndex)) {
      notification.setActionId(cursor.getLong(actionIdIndex));
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
    if (holder != null) {
      Log.info("found notificationHolder: " + holder.getId());
      deleteNotification(holder);
    }

  }

  private void deleteNotification(NotificationHolder holder) {
    if (holder != null) {
      String[] selectionArgs = new String[] {Long.toString(holder.getId())};
      String selectionClause = NotificationHolderColumns._ID + " = ?";
      int res = contentResolver.delete(NotificationHolderColumns.CONTENT_URI,
          selectionClause, selectionArgs);
      Log.info("resultof deleting notificationHolder: " + holder.getId() +" = " + res);
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
    if (notification.getSnoozeCount() != null) {
      values.put(NotificationHolderColumns.SNOOZE_COUNT, notification.getSnoozeCount());
    }
    if (notification.getSnoozeTime() != null) {
      values.put(NotificationHolderColumns.SNOOZE_TIME, notification.getSnoozeTime());
    }
    if (notification.getExperimentGroupName() != null) {
      values.put(NotificationHolderColumns.EXPERIMENT_GROUP_NAME, notification.getExperimentGroupName());
    }
    if (notification.getActionTriggerId() != null) {
      values.put(NotificationHolderColumns.ACTION_TRIGGER_ID, notification.getActionTriggerId());
    }
    if (notification.getActionTriggerSpecId() != null) {
      values.put(NotificationHolderColumns.ACTION_TRIGGER_SPEC_ID, notification.getActionTriggerSpecId());
    }
    if (notification.getActionId() != null) {
      values.put(NotificationHolderColumns.ACTION_ID, notification.getActionId());
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
      Log.info("IOException, experiments file does not exist. May be first launch.");
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
      Log.info("IOException, experiments file does not exist. May be first launch.");
    }
    return ensureExperiments(experiments);
  }

  public static void deleteExperimentCachesOnDisk(Context context2) {
    context2.deleteFile(MY_EXPERIMENTS_FILENAME);
    context2.deleteFile(PUBLIC_EXPERIMENTS_FILENAME);
  }

  public void addExperimentToExperimentsOnDisk(String contentAsString) {
    List<Experiment> existing = loadExperimentsFromDisk(false);
    List<Experiment> newEx;
    try {
      newEx = (List<Experiment>) fromDownloadedEntitiesJson(contentAsString).get("results");
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
      ObjectMapper mapper = JsonConverter.getObjectMapper();
      return mapper.readValue(fis, new TypeReference<List<Experiment>>() {});
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Lists.newArrayList();
  }

  private List<Experiment> createObjectsFromJsonStream(String fis) throws IOException, JsonParseException,
                                                                           JsonMappingException {
    try {
      ObjectMapper mapper = JsonConverter.getObjectMapper();
      return mapper.readValue(fis, new TypeReference<List<Experiment>>() {
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Lists.newArrayList();
  }

  public void loadLastEventForExperiment(Experiment experiment) {
    String select = EventColumns.EXPERIMENT_ID + "=" + experiment.getId();
    String sortOrder = EventColumns._ID +" DESC";

    List<Event> events = new ArrayList<Event>();
    Cursor cursor = null;
    try {
      eventStorageReadLock.lock();
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
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      eventStorageReadLock.unlock();
    }
  }

  public Experiment getExperimentFromDisk(Uri uri, boolean myExperimentsFile) {
    return getExperimentFromDisk(getExperimentServerIdFromUri(uri), myExperimentsFile);
  }

  public Long getExperimentServerIdFromUri(Uri uri) {
    return new Long(uri.getLastPathSegment());
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
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    Map<String, Object> resultObjects = mapper.readValue(resultsJson, new TypeReference<Map<String, Object>>() {
    });
    Object experimentResults = resultObjects.get("results");
    String experimentJson = mapper.writeValueAsString(experimentResults);
    List<ExperimentDAO> experimentDAOs = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {});

    List<Experiment> experiments = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : experimentDAOs) {
      Experiment newExperiment = new Experiment();
      newExperiment.setExperimentDAO(experimentDAO);
      newExperiment.setServerId(experimentDAO.getId());
      experiments.add(newExperiment);
    }


    resultObjects.put("results", experiments);
    return resultObjects;
  }

  /**
   * this one wraps the downlaoded DAOs with Android Experiment objects
   * @param resultsJson
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static Map<String, Object> fromDownloadedEntitiesJson(String resultsJson) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = JsonConverter.getObjectMapper();

    Map<String, Object> resultObjects = mapper.readValue(resultsJson, new TypeReference<Map<String, Object>>() {});
    Object experimentResults = resultObjects.get("results");
    String experimentJson = mapper.writeValueAsString(experimentResults);
    List<ExperimentDAO> experiments = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {
    });
    List<Experiment> experimentsWithDAOs = Lists.newArrayList();
    for (ExperimentDAO experimentDAO : experiments) {
      Experiment experiment = new Experiment();
      experiment.setExperimentDAO(experimentDAO);
      experimentsWithDAOs.add(experiment);
    }
    resultObjects.put("results", experimentsWithDAOs);
    return resultObjects;
  }

  public static List<Experiment> getExperimentsFromJson(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    Map<String, Object> results = fromDownloadedEntitiesJson(contentAsString);
    return (List<Experiment>) results.get("results");
  }

  public static Experiment getSingleExperimentFromJson(String contentAsString) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    return mapper.readValue(contentAsString, Experiment.class);
  }

  @Override
  public EventInterface getEvent(Long experimentServerId, DateTime scheduledTime,
                                 String groupName, Long actionTriggerId, Long scheduleId) {
    if (scheduledTime == null) {
      return null;
    }
    String selectionClause = EventColumns.EXPERIMENT_SERVER_ID + " = ? "
                              + " AND " + EventColumns.SCHEDULE_TIME + " = ? "
                              + " AND " + EventColumns.GROUP_NAME + " = ? "
                              + " AND " + EventColumns.ACTION_TRIGGER_ID + " = ? "
                              + " AND " + EventColumns.ACTION_TRIGGER_SPEC_ID + " = ? ";

    String[] selectionArgs = new String[] { Long.toString(experimentServerId),
                                          Long.toString(scheduledTime.getMillis()),
                                          groupName,
                                          Long.toString(actionTriggerId),
                                          Long.toString(scheduleId)};

    Event event = findEventBy(selectionClause, selectionArgs, EventColumns._ID +" DESC");
    if (event != null) {
      Log.info("Found event for experimentId: " + experimentServerId +", st = " + scheduledTime);
    } else {
      Log.info("DID NOT Find event for experimentId: " + experimentServerId +", st = " + scheduledTime);
    }
    return event;
  }

  public List<NotificationHolder> getAllNotificationsFor(Long experimentId) {
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

  public List<NotificationHolder> getAllNotificationsFor(Long experimentId, String groupName) {
    List<NotificationHolder> holders = new ArrayList<NotificationHolder>();
    String[] selectionArgs = new String[] {Long.toString(experimentId), groupName};
    String selectionClause = NotificationHolderColumns.EXPERIMENT_ID + " = ? and "
            + NotificationHolderColumns.EXPERIMENT_GROUP_NAME + " = ?";
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

  public void loadEventsForExperimentGroup(Experiment experiment, ExperimentGroup experimentGroup) {
    final Long id = experiment.getId();
    final String name = experimentGroup.getName();
    experiment.setEvents(loadEventsForExperimentGroup(id, name));
  }

  public List<Event> loadEventsForExperimentGroup(final Long experimentId, final String experimentGroupName) {
    String[] args = new String[]{ Long.toString(experimentId), experimentGroupName};
    final String select = EventColumns.EXPERIMENT_ID + " = ? and " + EventColumns.GROUP_NAME + " = ?";
    return findEventsBy(select, args, EventColumns._ID + " DESC");
  }

  private List<Event> findEventsBy(String select, String[] args, String sortOrder) {
    List<Event> events = new ArrayList<Event>();
    Cursor cursor = null;
    try {
      eventStorageReadLock.lock();
      cursor = contentResolver.query(EventColumns.CONTENT_URI,
          null, select, args, sortOrder);
      if (cursor != null) {
        while (cursor.moveToNext()) {
          Event event = createEvent(cursor);
          event.setResponses(findResponsesFor(event));
          events.add(event);
        }
      }
      return events;
    } catch (RuntimeException e) {
      Log.warn("Caught unexpected exception.", e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      eventStorageReadLock.unlock();
    }
    return events;
  }
}
