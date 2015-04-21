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

import com.google.common.collect.Lists;
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
      ExperimentDAO experimentDAO = (ExperimentDAO)experimentDAOCore;
      if (!isExperimentBackwardCompatible(experimentDAO) ) {
        continue;
      }


      com.pacoapp.paco.shared.model.ExperimentDAOCore bcExperiment = null;
      if (experimentDAO instanceof ExperimentDAO) {
      bcExperiment = new com.pacoapp.paco.shared.model.ExperimentDAO(experimentDAO.getId(),
                                                          experimentDAO.getTitle(),
                                                          experimentDAO.getDescription(),
                                                          experimentDAO.getInformedConsentForm(),
                                                          experimentDAO.getCreator(),
                                                          getSignalingMechanismsBC(experimentDAO),
                                                          experimentDAO.getGroups().get(0).getFixedDuration(),
                                                          false,
                                                          experimentDAO.getGroups().get(0).getStartDate(),
                                                          experimentDAO.getGroups().get(0).getEndDate(),
                                                          null,
                                                          null,
                                                          experimentDAO.getModifyDate(),
                                                          experimentDAO.getPublished(),
                                                          getAdminsBC(experimentDAO),
                                                          getPublishedUsersBC(experimentDAO),
                                                          experimentDAO.getDeleted(),
                                                          getWebRecommendedBC(experimentDAO),
                                                          experimentDAO.getVersion(),
                                                          experimentDAO.getGroups().get(0).getCustomRendering(),
                                                          experimentDAO.getGroups().get(0).getCustomRenderingCode(),
                                                          experimentDAO.getGroups().get(0).getFeedbackType(),
                                                          experimentDAO.getGroups().get(0).getBackgroundListen(),
                                                          experimentDAO.getGroups().get(0).getBackgroundListenSourceIdentifier(),
                                                          experimentDAO.getGroups().get(0).getLogActions(),
                                                          experimentDAO.getRecordPhoneDetails(),
                                                          experimentDAO.getExtraDataCollectionDeclarations());
      } else {
//        Long id, String title, String description, String informedConsentForm,
//        String email, Boolean fixedDuration,
//        String startDate, String endDate, String joinDate, Boolean backgroundListen,
//        String backgroundListenSourceIdentifier, Boolean logActions, Boolean recordPhoneDetails,
//        List<Integer> extraDataCollectionDeclarations
        bcExperiment = new com.pacoapp.paco.shared.model.ExperimentDAOCore(experimentDAO.getId(),
                                                                           experimentDAO.getTitle(),
                                                                           experimentDAO.getDescription(),
                                                                           experimentDAO.getInformedConsentForm(),
                                                                           experimentDAO.getCreator(),
                                                                           experimentDAO.getEarliestStartDate() != null,
                                                                           TimeUtil.formatDate(experimentDAO.getEarliestStartDate().getTime()),
                                                                           TimeUtil.formatDate(experimentDAO.getLatestEndDate().getTime()),
                                                                           null,
                                                                           getBackgroundListen(experimentDAO),
                                                                           getBackgroundListenSourceId(experimentDAO),
                                                                           getLogActions(experimentDAO),
                                                                           experimentDAO.getRecordPhoneDetails(),
                                                                           experimentDAO.getExtraDataCollectionDeclarations());
      }
      backwardCompatibleExperiments.add(bcExperiment);
    }
    return backwardCompatibleExperiments;
  }

  private static String getBackgroundListenSourceId(ExperimentDAO experimentDAO) {
    // TODO populate this until the new clients are out.
    return null;
  }

  private static Boolean getBackgroundListen(ExperimentDAO experimentDAO) {
 // TODO populate this until the new clients are out.
    return false; // almost certainly false
  }

  private static Boolean getLogActions(ExperimentDAO experimentDAO) {
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
    ActionTrigger at = experimentDAO.getGroups().get(0).getActionTriggers().get(0);
    SignalingMechanismDAO[] daos = new SignalingMechanismDAO[1];

    if (at instanceof ScheduleTrigger) {
      ScheduleTrigger st = (ScheduleTrigger)at;
      Schedule s = st.getSchedules().get(0);
      PacoNotificationAction a = (PacoNotificationAction) st.getActions().get(0);

      //
//      long id, Integer scheduleType, Boolean byDayOfMonth,
//      Integer dayOfMonth, Long esmEndHour, Integer esmFrequency, Integer esmPeriodInDays,
//      Long esmStartHour, Integer nthOfMonth, Integer repeatRate, List<SignalTimeDAO> times,
//      Integer weekDaysScheduled, Boolean esmWeekends, Boolean userEditable, Integer timeout,
//      Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime, Boolean onlyEditableOnJoin
      //

      SignalScheduleDAO newS = new SignalScheduleDAO(st.getId(), s.getScheduleType(), s.getByDayOfMonth(), s.getDayOfMonth(),
                                                  s.getEsmEndHour(), s.getEsmFrequency(), s.getEsmPeriodInDays(),
                                                  s.getEsmStartHour(), s.getNthOfMonth(), s.getRepeatRate(),
                                                  getSignalTimesBC(s), s.getWeekDaysScheduled(), s.getEsmWeekends(),
                                                  st.getUserEditable(), a.getTimeout(), s.getMinimumBuffer(),
                                                  a.getSnoozeCount(), a.getSnoozeTime(), st.getOnlyEditableOnJoin()
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

  public static List<SignalTimeDAO> getSignalTimesBC(Schedule s) {
    List<SignalTimeDAO> res = Lists.newArrayList();
    List<SignalTime> times = s.getSignalTimes();
    for (SignalTime signalTime : times) {
      res.add(new SignalTimeDAO(0l, signalTime.getType(), signalTime.getBasis(),
                                signalTime.getFixedTimeMillisFromMidnight(), signalTime.getMissedBasisBehavior(),
                                signalTime.getOffsetTimeMillis(), signalTime.getLabel()));
    }
    return res;
  }

  public static boolean isExperimentBackwardCompatible(ExperimentDAOCore experimentDAOCore) {
    if (experimentDAOCore instanceof ExperimentDAO) {
      ExperimentDAO experimentDAO = (ExperimentDAO) experimentDAOCore;
      if (experimentDAO.getGroups().size() != 1) {
        return false;
      }
      ExperimentGroup group = experimentDAO.getGroups().get(0);
      List<ActionTrigger> actionTriggers = group.getActionTriggers();
      if (actionTriggers.size() != 1) {
        return false;
      }
      ActionTrigger at = actionTriggers.get(0);
      List<PacoAction> actions = at.getActions();
      if (actions.size() != 1 || actions.get(0).getActionCode() != PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE) {
        return false;
      }
      if (at instanceof ScheduleTrigger) {
        ScheduleTrigger st = (ScheduleTrigger) at;
        if (st.getSchedules().size() != 1) {
          return false;
        }
      } else if (at instanceof InterruptTrigger) {
        InterruptTrigger it = (InterruptTrigger) at;
        if (it.getCues().size() != 1) {
          return false;
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
    List<ExperimentDAOCore> shortExperiments = getShortExperiments(experiments);
    return jsonify(shortExperiments, limit, cursor, pacoProtocol);
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
