package com.pacoapp.paco.shared.model2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model.FeedbackDAO;
import com.pacoapp.paco.shared.model.InputDAO;
import com.pacoapp.paco.shared.model.SignalScheduleDAO;
import com.pacoapp.paco.shared.model.SignalTimeDAO;
import com.pacoapp.paco.shared.model.SignalingMechanismDAO;
import com.pacoapp.paco.shared.model.TriggerDAO;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.TimeUtil;

public class JsonConverter {

  public static final Logger log = Logger.getLogger(JsonConverter.class.getName());

  /**
   * @param experiments
   * @param pacoProtocol
   *          TODO
   * @param printWriter
   * @return
   */
  public static String jsonify(List<? extends ExperimentDAOCore> experiments, Integer limit, String cursor,
                               String pacoProtocol) {
    try {
      ObjectMapper mapper = getObjectMapper();

      Float pacoProtocolFloat = null;
      if (pacoProtocol != null) {
        try {
          pacoProtocolFloat = Float.parseFloat(pacoProtocol);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
      if (pacoProtocolFloat == null) {
        if (experiments == null) {
          experiments = Collections.EMPTY_LIST;
        }
        return mapper.writeValueAsString(experiments);
      } else if (pacoProtocolFloat >= 4.0) {
        Map<String, Object> preJsonObject = buildV4ProtocolJson(experiments, limit, cursor);
        return mapper.writeValueAsString(preJsonObject);
      } else if (pacoProtocolFloat >= 3.0 && pacoProtocolFloat < 4.0) {
        Map<String, Object> preGroupObject = buildV3ProtocolJson(experiments, limit, cursor);
        return mapper.writeValueAsString(preGroupObject);
      }

    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null;
  }

  private static Map<String, Object> buildV3ProtocolJson(List<? extends ExperimentDAOCore> experiments, Integer limit,
                                                         String cursor) {
    if (experiments == null) {
      experiments = Collections.EMPTY_LIST;
    }
    List<? extends com.pacoapp.paco.shared.model.ExperimentDAOCore> backwardCompatibleExperiments = createBackwardCompatibleExperiments(experiments);

    Map<String, Object> preJsonObject = new HashMap();
    preJsonObject.put("results", backwardCompatibleExperiments);
    if (limit != null) {
      preJsonObject.put("limit", limit);
    }

    if (cursor != null) {
      preJsonObject.put("cursor", cursor);
    }
    return preJsonObject;
  }

  public static List<? extends com.pacoapp.paco.shared.model.ExperimentDAOCore> createBackwardCompatibleExperiments(List<? extends ExperimentDAOCore> experiments) {
    List<com.pacoapp.paco.shared.model.ExperimentDAOCore> backwardCompatibleExperiments = Lists.newArrayList();
    for (ExperimentDAOCore experimentDAOCore : experiments) {
      if (!isExperimentBackwardCompatible(experimentDAOCore) ) {
        log.info("Experiment is not backward compat: "+ experimentDAOCore.getTitle() + ", " + experimentDAOCore.getId());
        continue;
      }

      if (experimentDAOCore instanceof ExperimentDAO) {
        ExperimentDAO experimentDAO = (ExperimentDAO)experimentDAOCore;
        final ExperimentGroup experimentGroup = experimentDAO.getGroups().get(0);
        com.pacoapp.paco.shared.model.ExperimentDAO bcExperiment = new com.pacoapp.paco.shared.model.ExperimentDAO(experimentDAO.getId(),
                                                          experimentDAO.getTitle(),
                                                          experimentDAO.getDescription(),
                                                          experimentDAO.getInformedConsentForm(),
                                                          experimentDAO.getCreator(),
                                                          getSignalingMechanismsBC(experimentDAO),
                                                          experimentGroup.getFixedDuration(),
                                                          false,
                                                          experimentGroup.getStartDate(),
                                                          experimentGroup.getEndDate(),
                                                          null,
                                                          null,
                                                          experimentDAO.getModifyDate(),
                                                          experimentDAO.getPublished(),
                                                          getAdminsBC(experimentDAO),
                                                          getPublishedUsersBC(experimentDAO),
                                                          experimentDAO.getDeleted(),
                                                          getWebRecommendedBC(experimentDAO),
                                                          experimentDAO.getVersion(),
                                                          experimentGroup.getCustomRendering(),
                                                          experimentGroup.getCustomRenderingCode(),
                                                          experimentGroup.getFeedbackType(),
                                                          experimentGroup.getBackgroundListen(),
                                                          experimentGroup.getBackgroundListenSourceIdentifier(),
                                                          experimentGroup.getAccessibilityListen(),
                                                          experimentGroup.getLogActions(),
                                                          experimentDAO.getRecordPhoneDetails(),
                                                          experimentDAO.getExtraDataCollectionDeclarations());
        final List<Input2> model2Inputs = experimentGroup.getInputs();
        InputDAO[] inputs = new InputDAO[model2Inputs.size()];
        // convert model2Inputs to inputDAOs
        long inputId = 1;
        for (int i=0; i < model2Inputs.size(); i++) {
          Input2 model2Input = model2Inputs.get(i);
          final List<java.lang.String> listChoices = model2Input.getListChoices();
          String[] listChoicesArray = null;
          if (listChoices == null) {
            listChoicesArray = new String[0];
          } else {
            listChoicesArray = new String[listChoices.size()];
            listChoices.toArray(listChoicesArray);
          }
          InputDAO oldInput = new InputDAO(inputId++,
                                           model2Input.getName(),
                                           InputDAO.QUESTION,
                                           model2Input.getResponseType(),
                                           model2Input.getText(),
                                           model2Input.getRequired(),
                                           (Long)null,
                                           model2Input.getLikertSteps(),
                                           model2Input.getConditional(),
                                           model2Input.getConditionExpression(),
                                           model2Input.getLeftSideLabel(),
                                           model2Input.getRightSideLabel(),
                                           listChoicesArray,
                                           model2Input.getMultiselect());
          inputs[i] = oldInput;
        }
        bcExperiment.setInputs(inputs);
        backwardCompatibleExperiments.add(bcExperiment);

        FeedbackDAO[] feedbacks = new FeedbackDAO[1];
        Feedback model2Feedback = experimentGroup.getFeedback();
        FeedbackDAO oldFeedback = new FeedbackDAO(1l, model2Feedback.getText());
        feedbacks[0] = oldFeedback;
        bcExperiment.setFeedback(feedbacks);
      } else {
        com.pacoapp.paco.shared.model.ExperimentDAOCore bcExperiment =
                new com.pacoapp.paco.shared.model.ExperimentDAOCore(experimentDAOCore.getId(),
                                                                           experimentDAOCore.getTitle(),
                                                                           experimentDAOCore.getDescription(),
                                                                           experimentDAOCore.getInformedConsentForm(),
                                                                           experimentDAOCore.getCreator(),
                                                                           experimentDAOCore.getEarliestStartDate() != null,
                                                                           TimeUtil.formatDate(experimentDAOCore.getEarliestStartDate().getTime()),
                                                                           TimeUtil.formatDate(experimentDAOCore.getLatestEndDate().getTime()),
                                                                           null,
                                                                           getBackgroundListen(experimentDAOCore),
                                                                           getBackgroundListenSourceId(experimentDAOCore),
                                                                           getAccessibilityListen(experimentDAOCore),
                                                                           getLogActions(experimentDAOCore),
                                                                           experimentDAOCore.getRecordPhoneDetails(),
                                                                           experimentDAOCore.getExtraDataCollectionDeclarations());
        backwardCompatibleExperiments.add(bcExperiment);
      }

    }
    return backwardCompatibleExperiments;
  }

  private static String getBackgroundListenSourceId(ExperimentDAOCore experimentDAOCore) {
    // TODO populate this until the new clients are out.
    return null;
  }

  private static Boolean getBackgroundListen(ExperimentDAOCore experimentDAOCore) {
 // TODO populate this until the new clients are out.
    return false; // almost certainly false
  }

  private static Boolean getAccessibilityListen(ExperimentDAOCore experimentDAOCore) {
    return false; // TODO following the examples before
  }

  private static Boolean getLogActions(ExperimentDAOCore experimentDAOCore) {
 // TODO populate this until the new clients are out.
    return false; // TODO fix this
  }

  private static String[] getPublishedUsersBC(ExperimentDAO experimentDAO) {
    String[] pubUsers = new String[experimentDAO.getPublishedUsers().size()];
    return experimentDAO.getPublishedUsers().toArray(pubUsers);
  }

  public static boolean getWebRecommendedBC(ExperimentDAO experimentDAO) {
   // TODO populate this until the new clients are out.
    return false;
  }

  private static java.lang.String[] getAdminsBC(ExperimentDAO experimentDAO) {
    String[] adminUsers = new String[experimentDAO.getAdmins().size()];
    return experimentDAO.getAdmins().toArray(adminUsers);
  }

  private static com.pacoapp.paco.shared.model.SignalingMechanismDAO[] getSignalingMechanismsBC(ExperimentDAO experimentDAO) {
    final List<ActionTrigger> actionTriggers = experimentDAO.getGroups().get(0).getActionTriggers();
    SignalingMechanismDAO[] daos = new SignalingMechanismDAO[1];
    if (actionTriggers.size() == 0) {

      SignalScheduleDAO schedule = new SignalScheduleDAO(1l, Schedule.SELF_REPORT, false,
                                                         null, null, null, null,
                                                         null, null, null, null,
                                                         null, false, true, null,
                                                         null, null, null, false);
      daos[0] = schedule;
      return daos;
    }

    ActionTrigger at = actionTriggers.get(0);
    if (at instanceof ScheduleTrigger) {
      ScheduleTrigger scheduledTrigger = (ScheduleTrigger)at;
      Schedule schedule = scheduledTrigger.getSchedules().get(0);
      PacoNotificationAction a = (PacoNotificationAction) scheduledTrigger.getActions().get(0);

      //
//      long id, Integer scheduleType, Boolean byDayOfMonth,
//      Integer dayOfMonth, Long esmEndHour, Integer esmFrequency, Integer esmPeriodInDays,
//      Long esmStartHour, Integer nthOfMonth, Integer repeatRate, List<SignalTimeDAO> times,
//      Integer weekDaysScheduled, Boolean esmWeekends, Boolean userEditable, Integer timeout,
//      Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime, Boolean onlyEditableOnJoin
      //

      Preconditions.checkNotNull(scheduledTrigger, "scheduledTrigger is null");
      Preconditions.checkNotNull(schedule, "schedule is null");
      List<SignalTimeDAO> signalTimesBC = null;
      if (schedule.getScheduleType() != null && !schedule.getScheduleType().equals(Schedule.ESM)) {
        signalTimesBC = getSignalTimesBC(schedule);
      }
      //Preconditions.checkArgument(signalTimesBC != null && signalTimesBC.size() > 0, "signalTimes is null or empty");
      SignalScheduleDAO newS = new SignalScheduleDAO(
                                                     makePrimitive(scheduledTrigger.getId()),
                                                     makePrimitive(schedule.getScheduleType()),
                                                     schedule.getByDayOfMonth(),
                                                     schedule.getDayOfMonth(),
                                                     schedule.getEsmEndHour(),
                                                     schedule.getEsmFrequency(),
                                                     schedule.getEsmPeriodInDays(),
                                                     schedule.getEsmStartHour(),
                                                     schedule.getNthOfMonth(),
                                                     schedule.getRepeatRate(),
                                                     signalTimesBC,
                                                     schedule.getWeekDaysScheduled(),
                                                     schedule.getEsmWeekends(),
                                                     schedule.getUserEditable(),
                                                     a.getTimeout(),
                                                     schedule.getMinimumBuffer(),
                                                     a.getSnoozeCount(),
                                                     a.getSnoozeTime(),
                                                     schedule.getOnlyEditableOnJoin()
                                                  );
      daos[0] = newS;
    } else {
      InterruptTrigger it = (InterruptTrigger)at;
      final InterruptCue cue = it.getCues().get(0);
      final PacoNotificationAction pacoNotificationAction = (PacoNotificationAction)it.getActions().get(0);
      TriggerDAO t = new TriggerDAO(it.getId(),
                                    cue.getCueCode(),
                                    cue.getCueSource(),
                                    pacoNotificationAction.getDelay(),
                                    pacoNotificationAction.getTimeout(),
                                    it.getMinimumBuffer(),
                                    pacoNotificationAction.getSnoozeCount(),
                                    pacoNotificationAction.getSnoozeTime()
                                    );
      daos[0] = t;
    }
    return daos;
  }

  private static Boolean makePrimitive(Boolean byDayOfMonth) {
    // TODO Auto-generated method stub
    return null;
  }

  private static Integer makePrimitive(Integer scheduleType) {
    if (scheduleType == null) {
      return 0;
    } else {
      return scheduleType;
    }
  }

  public static long makePrimitive(Long id) {
    if (id == null) {
      return 0;
    } else {
      return id;
    }
  }

  public static List<SignalTimeDAO> getSignalTimesBC(Schedule s) {
    List<SignalTime> times = s.getSignalTimes();
    if (times == null || times.isEmpty()) {
      return Lists.newArrayList();
    }
    List<SignalTimeDAO> res = Lists.newArrayList();
    long id = 1l;
    for (SignalTime signalTime : times) {
      //Preconditions.checkNotNull(signalTime, "signalTime is null");
      Integer type = signalTime.getType();
      if (type == null) {
        type = SignalTimeDAO.FIXED_TIME;
      }
      Integer basis = signalTime.getBasis();
      if (basis == null) {
        basis = SignalTimeDAO.OFFSET_BASIS_SCHEDULED_TIME;
      }
      Integer fixedTimeMillisFromMidnight = signalTime.getFixedTimeMillisFromMidnight();
      if (fixedTimeMillisFromMidnight == null) {
        fixedTimeMillisFromMidnight = 0;
      }

      Integer missedBasisBehavior = signalTime.getMissedBasisBehavior();
      if (missedBasisBehavior == null) {
        missedBasisBehavior = SignalTimeDAO.MISSED_BEHAVIOR_USE_SCHEDULED_TIME;
      }
      Integer offsetTimeMillis = signalTime.getOffsetTimeMillis();
      if (offsetTimeMillis == null) {
        offsetTimeMillis = SignalTimeDAO.OFFSET_TIME_DEFAULT;
      }
      String label = signalTime.getLabel();

      SignalTimeDAO oldSignalTime = new SignalTimeDAO(id++,
                                          type,
                                          basis,
                                          fixedTimeMillisFromMidnight,
                                          missedBasisBehavior,
                                          offsetTimeMillis,
                                          label);
      res.add(oldSignalTime);
    }
    return res;
  }

  public static boolean isExperimentBackwardCompatible(ExperimentDAOCore experimentDAOCore) {
    if (experimentDAOCore instanceof ExperimentDAO) {
      ExperimentDAO experimentDAO = (ExperimentDAO) experimentDAOCore;
      if (experimentDAO.getGroups().size() != 1) {
        log.info("group size != 1");
        return false;
      }
      ExperimentGroup group = experimentDAO.getGroups().get(0);
      if (group.getCustomRendering()) {
        return false;
      }
      List<ActionTrigger> actionTriggers = group.getActionTriggers();
      if (actionTriggers.size() > 1) {
        log.info("actionTriggers size > 1");
        return false;
      }
      if (actionTriggers.size() == 1) {
        ActionTrigger at = actionTriggers.get(0);
        List<PacoAction> actions = at.getActions();
        if (actions.size() != 1 || actions.get(0).getActionCode() != PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE) {
          log.info("actions size > 1 or action != notification");
          return false;
        }
        if (at instanceof ScheduleTrigger) {
          ScheduleTrigger st = (ScheduleTrigger) at;
          final List<Schedule> schedules = st.getSchedules();
          if (schedules.size() > 1) {
            log.info("schedule size > 1");
            return false;
          }
          final List<SignalTime> signalTimes = schedules.get(0).getSignalTimes();
          if (signalTimes != null) {
            for (SignalTime signalTime : signalTimes) {
              if (signalTime.getBasis() != null && signalTime.getBasis() == SignalTime.OFFSET_TIME) {
                return false;
              }
            }
          }
        } else if (at instanceof InterruptTrigger) {
          InterruptTrigger it = (InterruptTrigger) at;
          if (it.getCues().size() != 1) {
            log.info("cue size != 1");
            return false;
          }
        }
      }
    }
    return true;
  }

  private static Map<String, Object> buildV4ProtocolJson(List<? extends ExperimentDAOCore> experiments, Integer limit,
                                                         String cursor) {
    if (experiments == null) {
      experiments = Collections.EMPTY_LIST;
    }
    Map<String, Object> preJsonObject = new HashMap();
    preJsonObject.put("results", experiments);
    if (limit != null) {
      preJsonObject.put("limit", limit);
    }

    if (cursor != null) {
      preJsonObject.put("cursor", cursor);
    }
    return preJsonObject;
  }

  public static String shortJsonify(List<ExperimentDAO> experiments, Integer limit, String cursor, String pacoProtocol) {
    //List<ExperimentDAOCore> shortExperiments = getShortExperiments(experiments);
    return jsonify(experiments, limit, cursor, pacoProtocol);
  }

  private static List<ExperimentDAOCore> getShortExperiments(List<ExperimentDAO> experiments) {
    List<ExperimentDAOCore> shortExperiments = new ArrayList<ExperimentDAOCore>();
    for (ExperimentDAO experiment : experiments) {
      shortExperiments.add(experimentDAOCoreFromExperimentDAO(experiment));
    }
    return shortExperiments;

  }

  private static ExperimentDAOCore experimentDAOCoreFromExperimentDAO(ExperimentDAO experiment) {

    Date earliestStartDate = null;
    DateMidnight earliestStartDate2 = ActionScheduleGenerator.getEarliestStartDate(experiment);
    if (earliestStartDate2 != null) {
      earliestStartDate = earliestStartDate2.toDate();
    }
    Date endDate = null;
    DateTime lastEndTime = ActionScheduleGenerator.getLastEndTime(experiment);
    if (lastEndTime != null) {
      endDate = lastEndTime.toDateMidnight().toDate();
    }
    return new ExperimentDAOCore(experiment.getId(), experiment.getTitle(), experiment.getDescription(),
                                 experiment.getInformedConsentForm(), experiment.getCreator(),
                                 experiment.getJoinDate(), experiment.getRecordPhoneDetails(), experiment.getDeleted(),
                                 experiment.getExtraDataCollectionDeclarations(), experiment.getOrganization(),
                                 experiment.getContactPhone(), experiment.getContactEmail(), earliestStartDate, endDate);
  }

  public static String jsonify(ExperimentDAOCore experiment) {
    ObjectMapper mapper = getObjectMapper();
    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null;
  }

  public static String jsonify(ExperimentGroup experiment) {
    ObjectMapper mapper = getObjectMapper();
    try {
      return mapper.writeValueAsString(experiment);
    } catch (JsonGenerationException e) {
      log.severe("Json generation error " + e);
    } catch (JsonMappingException e) {
      log.severe("JsonMapping error getting experiments: " + e.getMessage());
    } catch (IOException e) {
      log.severe("IO error getting experiments: " + e.getMessage());
    }
    return null;
  }

  // public static String jsonify(ExperimentDAO experiment) {
  // ObjectMapper mapper = getObjectMapper();
  // try {
  // return mapper.writeValueAsString(experiment);
  // } catch (JsonGenerationException e) {
  // log.severe("Json generation error " + e);
  // } catch (JsonMappingException e) {
  // log.severe("JsonMapping error getting experiments: " + e.getMessage());
  // } catch (IOException e) {
  // log.severe("IO error getting experiments: " + e.getMessage());
  // }
  // return null;
  // }

  public static List<ExperimentDAO> fromEntitiesJsonUpload(String experimentJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      List<ExperimentDAO> experiments = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {
      });
      return experiments;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return new ArrayList();
  }

  public static Map<String, Object> fromEntitiesJson(String resultsJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      Map<String, Object> resultObjects = mapper.readValue(resultsJson, new TypeReference<Map<String, Object>>() {
      });
      Object experimentResults = resultObjects.get("results");
      String experimentJson = mapper.writeValueAsString(experimentResults);
      List<ExperimentDAO> experiments = mapper.readValue(experimentJson, new TypeReference<List<ExperimentDAO>>() {
      });
      resultObjects.put("results", experiments);
      return resultObjects;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }

  public static ExperimentDAO fromSingleEntityJson(String experimentJson) {
    ObjectMapper mapper = getObjectMapper();
    try {
      ExperimentDAO experiment = mapper.readValue(experimentJson, new TypeReference<ExperimentDAO>() {
      });
      return experiment;
    } catch (JsonParseException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (JsonMappingException e) {
      log.severe("Could not parse json. " + e.getMessage());
    } catch (IOException e) {
      log.severe("Could not parse json. " + e.getMessage());
    }
    return null;
  }

  public static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    mapper.getDeserializationConfig().addMixInAnnotations(ActionTrigger.class, ActionTriggerMixIn.class);
    mapper.getDeserializationConfig().addMixInAnnotations(PacoAction.class, PacoActionMixIn.class);
    return mapper;
  }

  /**
   * Helper function that converts any object to a JSON string
   * @param object The object we want to convert
   * @return A string containing a JSON representation of the object
   */
  public static String convertToJsonString(Object object) {
    ObjectMapper mapper = getObjectMapper();
    String json = null;
    try {
      json = mapper.writeValueAsString(object);
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return json;
  }


  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
  @JsonSubTypes({ @Type(value = ScheduleTrigger.class, name = "scheduleTrigger"),
                 @Type(value = InterruptTrigger.class, name = "interruptTrigger") })
  private class ActionTriggerMixIn {

    // Nothing to be done here. This class exists for the sake of its
    // annotations.
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
  @JsonSubTypes({ @Type(value = PacoActionAllOthers.class, name = "pacoActionAllOthers"),
                 @Type(value = PacoNotificationAction.class, name = "pacoNotificationAction") })
  private class PacoActionMixIn {
    // Nothing to be done here. This class exists for the sake of its
    // annotations.
  }

}
