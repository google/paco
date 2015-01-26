package com.google.sampling.experiential.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.paco.shared.model.ExperimentDAO;
import com.google.paco.shared.model.FeedbackDAO;
import com.google.paco.shared.model.InputDAO;
import com.google.paco.shared.model.SignalScheduleDAO;
import com.google.paco.shared.model.SignalTimeDAO;
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.SignalTime;
import com.google.sampling.experiential.model.Trigger;
import com.google.sampling.experiential.shared.TimeUtil;

public class DAOConverter {

  private DAOConverter() {
    super();
  }

  static List<ExperimentDAO> createDAOsFor(List<Experiment> experiments) {
    List<ExperimentDAO> experimentDAOs = Lists.newArrayList();
    if (experiments == null) {
      return experimentDAOs;
    }
    for (Experiment experiment : experiments) {
      experimentDAOs.add(createDAO(experiment));
    }
    return experimentDAOs;
  }

  public static ExperimentDAO createDAO(Experiment experiment) {
    Long id = experiment.getId();
    String title = experiment.getTitle();
    String description = experiment.getDescription();
    String informedConsentForm = experiment.getInformedConsentFormText();
    String email = experiment.getCreator().getEmail();

    Boolean published = experiment.getPublished();

    SignalingMechanismDAO[] signalingMechanisms = new SignalingMechanismDAO[1];

    SignalSchedule schedule = experiment.getSchedule();
    Trigger trigger = experiment.getTrigger();

    SignalScheduleDAO signalScheduleDAO = null;
    TriggerDAO triggerDAO = null;
    // BACKWard compatibility friendliness - create a schedule for this
    // experiment
    if (schedule == null && trigger == null) {
      signalScheduleDAO = new SignalScheduleDAO();
      signalScheduleDAO.setScheduleType(SignalScheduleDAO.SELF_REPORT);
      signalingMechanisms[0] = triggerDAO;
      published = Boolean.FALSE;
    } else if (trigger != null) {
      triggerDAO = createTriggerDAO(trigger);
      signalingMechanisms[0] = triggerDAO;
    } else {
      signalScheduleDAO = createSignalScheduleDAO(schedule);
      signalingMechanisms[0] = signalScheduleDAO;
    }

    Boolean fixedDuration = experiment.getFixedDuration();
    Boolean questionsChange = experiment.getQuestionsChange();
    Boolean deleted = experiment.getDeleted();
    String startDate = experiment.getStartDate();
    String endDate = experiment.getEndDate();
    String hash = experiment.getHash();
    String joinDate = experiment.getJoinDate();
    String modifyDate = experiment.getModifyDate();

    List<String> admins = experiment.getAdmins();
    String[] adminStrArray = new String[admins.size()];
    adminStrArray = admins.toArray(adminStrArray);

    List<String> userEmails = experiment.getPublishedUsers();
    String[] userEmailsStrArray = new String[userEmails.size()];
    userEmailsStrArray = userEmails.toArray(userEmailsStrArray);

    Integer version = experiment.getVersion();

    String customRenderingCode = experiment.getCustomRenderingCode();
    Integer feedbackType = experiment.getFeedbackType();

    Boolean recordPhoneDetails = experiment.isRecordPhoneDetails();

    List<Integer> extraDataCollectionDeclarations = experiment.getExtraDataCollectionDeclarations();
    if (extraDataCollectionDeclarations != null) {
      List<Integer> jdoDetachedList = Lists.newArrayList(extraDataCollectionDeclarations);
      extraDataCollectionDeclarations = jdoDetachedList;
    }
    ExperimentDAO dao = new ExperimentDAO(id, title, description, informedConsentForm, email, signalingMechanisms,
                                          fixedDuration, questionsChange, startDate, endDate, hash, joinDate,
                                          modifyDate, published, adminStrArray, userEmailsStrArray, deleted, null,
                                          version, experiment.isCustomRendering(), customRenderingCode, feedbackType,
                                          experiment.isBackgroundListen(),
                                          experiment.getBackgroundListenSourceIdentifier(),
                                          experiment.shouldLogActions(), recordPhoneDetails,
                                          extraDataCollectionDeclarations);
    List<Input> inputs = experiment.getInputs();

    InputDAO[] inputDAOs = new InputDAO[inputs.size()];
    for (int i = 0; i < inputs.size(); i++) {
      inputDAOs[i] = createDAO(inputs.get(i));
    }
    dao.setInputs(inputDAOs);

    FeedbackDAO[] fbDAOs = new FeedbackDAO[experiment.getFeedback().size()];
    for (int i = 0; i < experiment.getFeedback().size(); i++) {
      fbDAOs[i] = createDAO(experiment.getFeedback().get(i));
    }
    dao.setFeedback(fbDAOs);
    return dao;
  }

  private static TriggerDAO createTriggerDAO(Trigger trigger) {
    return new TriggerDAO(trigger.getId().getId(), trigger.getEventCode(), trigger.getSourceIdentifier(),
                          trigger.getDelay(), trigger.getTimeout(), trigger.getMinimumBuffer(),
                          trigger.getSnoozeCount(), trigger.getSnoozeTime());
  }

  /**
   * @param schedule
   * @return
   */
  static SignalScheduleDAO createSignalScheduleDAO(SignalSchedule schedule) {
    return new SignalScheduleDAO(schedule.getId().getId(), schedule.getScheduleType(), schedule.getByDayOfMonth(),
                                 schedule.getDayOfMonth(), schedule.getEsmEndHour(), schedule.getEsmFrequency(),
                                 schedule.getEsmPeriodInDays(), schedule.getEsmStartHour(), schedule.getNthOfMonth(),
                                 schedule.getRepeatRate(), createDAOsForSignalTimes(schedule.getSignalTimes()),
                                 schedule.getWeekDaysScheduled(), schedule.getEsmWeekends(),
                                 schedule.getUserEditable(), schedule.getTimeout(), schedule.getMinimumBuffer(),
                                 schedule.getSnoozeCount(), schedule.getSnoozeTime(), schedule.getOnlyEditableOnJoin());
  }

  private static List<SignalTimeDAO> createDAOsForSignalTimes(List<SignalTime> times) {
    List<SignalTimeDAO> signalTimeDAOs = Lists.newArrayList();
    if (times == null) {
      return signalTimeDAOs;
    }
    for (SignalTime signalTime : times) {
      signalTimeDAOs.add(createDAO(signalTime));
    }
    return signalTimeDAOs;
  }

  private static SignalTimeDAO createDAO(SignalTime signalTime) {
    Key signalTimeKey = signalTime.getKey();
    Long id = null;
    if (signalTimeKey != null) {
      id = signalTimeKey.getId();
    }
    return new SignalTimeDAO(id, signalTime.getType(), signalTime.getBasis(),
                             signalTime.getFixedTimeMillisFromMidnight(), signalTime.getMissedBasisBehavior(),
                             signalTime.getOffsetTimeMillis(), signalTime.getLabel());
  }

  public static FeedbackDAO createDAO(Feedback feedback) {
    Key feedbackKey = feedback.getId();
    Long id = null;
    if (feedbackKey != null) {
      id = feedbackKey.getId();
    }
    return new FeedbackDAO(id, feedback.getLongText());
  }

  public static InputDAO createDAO(Input input) {
    return new InputDAO(input.getId().getId(), input.getName(), input.getQuestionType(), input.getResponseType(),
                        input.getText(), input.getMandatory(),
                        input.getScheduleDate() != null ? input.getScheduleDate().getTime() : null,
                        input.getLikertSteps(), input.getConditional(), input.getConditionalExpression(),
                        input.getLeftSideLabel(), input.getRightSideLabel(), toStringArray(input.getListChoices()),
                        input.isMultiselect());
  }

  /**
   * @param listChoices
   * @return
   */
  static String[] toStringArray(List<String> listChoices) {
    if (listChoices == null) {
      return new String[0];
    }
    String[] res = new String[listChoices.size()];
    return listChoices.toArray(res);
  }

  /**
   * @param times
   * @return
   */
  static Long[] toArray(List<Long> times) {
    Long[] res = new Long[times.size()];
    return times.toArray(res);
  }

  public static Experiment fromExperimentDAO(ExperimentDAO experimentDAO, Experiment experiment, User whoFromLogin) {
    experiment.setId(experimentDAO.getId()); // still neccessary in the case of
                                             // retrieval from cache
    experiment.setTitle(experimentDAO.getTitle());
    experiment.setDescription(experimentDAO.getDescription());
    if (experiment.getCreator() == null && whoFromLogin != null) { // it is only
                                                                   // null when
                                                                   // we are
                                                                   // recreating
                                                                   // it from
                                                                   // cache.
      experiment.setCreator(whoFromLogin);
    }
    experiment.setInformedConsentFormText(experimentDAO.getInformedConsentForm());
    experiment.setQuestionsChange(experimentDAO.getQuestionsChange());
    experiment.setFixedDuration(experimentDAO.getFixedDuration());

    String startDate = experimentDAO.getStartDate();
    experiment.setStartDate(startDate);

    String endDate = experimentDAO.getEndDate();
    experiment.setEndDate(endDate);

    experiment.setModifyDate(experimentDAO.getModifyDate() != null ? experimentDAO.getModifyDate() : getTodayAsString());

    Key key = null;
    if (experiment.getId() != null) {
      key = KeyFactory.createKey(Experiment.class.getSimpleName(), experiment.getId());
    }

    SignalingMechanismDAO[] signalingMechanisms = experimentDAO.getSignalingMechanisms();
    SignalingMechanismDAO signalingMechanismDAO = signalingMechanisms[0];
    if (signalingMechanismDAO instanceof SignalScheduleDAO) {
      experiment.setSchedule(fromScheduleDAO(key, (SignalScheduleDAO) signalingMechanismDAO, experiment.getSchedule()));
      experiment.setTrigger(null);
    } else {
      experiment.setTrigger(fromTriggerDAO(key, (TriggerDAO) signalingMechanismDAO));
      experiment.setSchedule(null);
    }

    experiment.setInputs(fromInputDAOs(key, experimentDAO.getInputs(), experiment.getQuestionsChange()));
    experiment.setFeedback(fromFeedbackDAOs(key, experimentDAO.getFeedback()));
    experiment.setFeedbackType(experimentDAO.getFeedbackType());

    experiment.setPublished(experimentDAO.getPublished());
    experiment.setPublishedUsers(lowerCaseEmailAddresses(experimentDAO.getPublishedUsers()));
    List<String> lowerCaseEmailAddressesForAdmins = lowerCaseEmailAddresses(experimentDAO.getAdmins());
    String whoFromLoginEmail = whoFromLogin.getEmail().toLowerCase();
    if (!lowerCaseEmailAddressesForAdmins.contains(whoFromLoginEmail)) {
      lowerCaseEmailAddressesForAdmins.add(whoFromLoginEmail);
    }
    experiment.setAdmins(lowerCaseEmailAddressesForAdmins);
    experiment.setDeleted(experimentDAO.getDeleted());

    experiment.setCustomRendering(experimentDAO.isCustomRendering());
    experiment.setCustomRenderingCode(experimentDAO.getCustomRenderingCode());

    experiment.setLogActions(experimentDAO.isLogActions());
    experiment.setRecordPhoneDetails(experimentDAO.isRecordPhoneDetails());
    experiment.setBackgroundListen(experimentDAO.isBackgroundListen());
    experiment.setBackgroundListenSourceIdentifier(experimentDAO.getBackgroundListenSourceIdentifier());

    experiment.setExtraDataCollectionDeclarations(experimentDAO.getExtraDataCollectionDeclarations());

    return experiment;
  }

  private static String getTodayAsString() {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(TimeUtil.DATE_FORMAT);
    return new DateTime().toString(formatter);
  }

  private static Trigger fromTriggerDAO(Key key, TriggerDAO signalingMechanismDAO) {
    Trigger trigger = new Trigger(key, signalingMechanismDAO.getId(), signalingMechanismDAO.getEventCode(),
                                  signalingMechanismDAO.getSourceIdentifier(), signalingMechanismDAO.getDelay(),
                                  signalingMechanismDAO.getTimeout(), signalingMechanismDAO.getMinimumBuffer(),
                                  signalingMechanismDAO.getSnoozeCount(), signalingMechanismDAO.getSnoozeTime());
    return trigger;
  }

  private static ArrayList<String> lowerCaseEmailAddresses(String[] publishedUsers) {
    ArrayList<String> publishedUsersLowercase = Lists.newArrayList();
    if (publishedUsers == null) {
      return publishedUsersLowercase;
    }
    for (String user : publishedUsers) {
      publishedUsersLowercase.add(user.toLowerCase());
    }
    return publishedUsersLowercase;
  }

  /**
   * @param key
   * @param signalSchedule
   * @param schedule
   * @return
   */
  public static SignalSchedule fromScheduleDAO(Key key, SignalScheduleDAO scheduleDAO, SignalSchedule signalSchedule) {
    Long id = scheduleDAO.getId();
    Key scheduleKey = null;
    if (id != null) {
      scheduleKey = KeyFactory.createKey(key, SignalSchedule.class.getSimpleName(), id);
    }
    List<SignalTime> fromSignalTimeDAOs = fromSignalTimeDAOs(scheduleKey, scheduleDAO.getSignalTimes());
    if (signalSchedule == null) {
      SignalSchedule schedule = new SignalSchedule(key, id, scheduleDAO.getScheduleType(),
                                                   scheduleDAO.getEsmFrequency(), scheduleDAO.getEsmPeriodInDays(),
                                                   scheduleDAO.getEsmStartHour(), scheduleDAO.getEsmEndHour(),
                                                   fromSignalTimeDAOs, scheduleDAO.getRepeatRate(),
                                                   scheduleDAO.getWeekDaysScheduled(), scheduleDAO.getNthOfMonth(),
                                                   scheduleDAO.getByDayOfMonth(), scheduleDAO.getDayOfMonth(),
                                                   scheduleDAO.getEsmWeekends(), scheduleDAO.getUserEditable(),
                                                   scheduleDAO.getTimeout(), scheduleDAO.getMinimumBuffer(),
                                                   scheduleDAO.getSnoozeCount(), scheduleDAO.getSnoozeTime(),
                                                   scheduleDAO.getOnlyEditableOnJoin());
      return schedule;
    } else {
      signalSchedule.setScheduleType(scheduleDAO.getScheduleType());
      signalSchedule.setEsmFrequency(scheduleDAO.getEsmFrequency());
      signalSchedule.setEsmPeriodInDays(scheduleDAO.getEsmPeriodInDays());
      signalSchedule.setEsmStartHour(scheduleDAO.getEsmStartHour());
      signalSchedule.setEsmEndHour(scheduleDAO.getEsmEndHour());
      signalSchedule.setSignalTimes(fromSignalTimeDAOs);
      signalSchedule.setRepeatRate(scheduleDAO.getRepeatRate());
      signalSchedule.setWeekDaysScheduled(scheduleDAO.getWeekDaysScheduled());
      signalSchedule.setNthOfMonth(scheduleDAO.getNthOfMonth());
      signalSchedule.setByDayOfMonth(scheduleDAO.getByDayOfMonth());
      signalSchedule.setDayOfMonth(scheduleDAO.getDayOfMonth());
      signalSchedule.setEsmWeekends(scheduleDAO.getEsmWeekends());
      signalSchedule.setUserEditable(scheduleDAO.getUserEditable());
      signalSchedule.setTimeout(scheduleDAO.getTimeout());
      signalSchedule.setMinimumBuffer(scheduleDAO.getMinimumBuffer());
      signalSchedule.setSnoozeCount(scheduleDAO.getSnoozeCount());
      signalSchedule.setSnoozeTime(scheduleDAO.getSnoozeTime());
      signalSchedule.setOnlyEditableOnJoin(scheduleDAO.getOnlyEditableOnJoin());
      return signalSchedule;
    }
  }

  private static List<SignalTime> fromSignalTimeDAOs(Key scheduleKey, List<SignalTimeDAO> list) {

    List<SignalTime> signalTimes = Lists.newArrayList();
    for (SignalTimeDAO signalTimeDAO : list) {
      Key signalTimeKey = null;
      if (scheduleKey != null && signalTimeDAO.getId() != null) {
        signalTimeKey = KeyFactory.createKey(scheduleKey, SignalTime.class.getSimpleName(), signalTimeDAO.getId());
      }
      signalTimes.add(new SignalTime(signalTimeKey, signalTimeDAO.getType(), signalTimeDAO.getBasis(),
                                     signalTimeDAO.getFixedTimeMillisFromMidnight(),
                                     signalTimeDAO.getMissedBasisBehavior(), signalTimeDAO.getOffsetTimeMillis(),
                                     signalTimeDAO.getLabel()));

    }
    return signalTimes;
  }

  /**
   * @param experimentKey
   *          TODO
   * @param feedback
   * @return
   */
  private static List<Feedback> fromFeedbackDAOs(Key experimentKey, FeedbackDAO[] feedbackDAOs) {
    List<Feedback> feedback = Lists.newArrayList();
    for (FeedbackDAO feedbackDAO : feedbackDAOs) {
      feedback.add(new Feedback(experimentKey, feedbackDAO.getId(), feedbackDAO.getText()));
    }
    return feedback;
  }

  /**
   * @param questionsChange
   * @param inputs
   * @return
   */
  private static List<Input> fromInputDAOs(Key experimentKey, InputDAO[] inputDAOs, boolean questionsChange) {
    List<Input> inputs = Lists.newArrayList();
    for (InputDAO input : inputDAOs) {
      Date scheduleDate = null;
      if (questionsChange) {
        scheduleDate = new Date(input.getScheduleDate());
      } else {
        scheduleDate = null;
      }
      inputs.add(new Input(experimentKey, input.getId(), input.getName(), input.getText(), scheduleDate,
                           input.getQuestionType(), input.getResponseType(), input.getLikertSteps(),
                           input.getMandatory(), input.getConditional(), input.getConditionExpression(),
                           input.getLeftSideLabel(), input.getRightSideLabel(),
                           Arrays.asList(input.getListChoices() != null ? input.getListChoices() : new String[0]),
                           input.getMultiselect()));
    }
    return inputs;
  }

  // public static Experiment createExperiment(ExperimentDAO experimentDAO) {
  // Experiment experiment = new Experiment();
  // fromExperimentDAO(experimentDAO, experiment, null);
  // return experiment;
  // }

  // static List<Experiment> createExperimentsFrom(List<ExperimentDAO>
  // experimentDAOs) {
  // List<Experiment> experiments = Lists.newArrayList();
  // for (ExperimentDAO experimentDAO : experimentDAOs) {
  // experiments.add(createExperiment(experimentDAO));
  // }
  // return experiments;
  // }

}
