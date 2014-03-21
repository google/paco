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
import com.google.paco.shared.model.SignalingMechanismDAO;
import com.google.paco.shared.model.TriggerDAO;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
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
    Trigger trigger  = experiment.getTrigger();

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
    Boolean showFeedback = experiment.shouldShowFeedback();

    Boolean hasCustomFeedback = experiment.hasCustomFeedback();

    ExperimentDAO dao = new ExperimentDAO(id, title, description, informedConsentForm, email, signalingMechanisms,
            fixedDuration, questionsChange, startDate, endDate, hash, joinDate, modifyDate, published, adminStrArray,
            userEmailsStrArray, deleted, null, version, experiment.isCustomRendering(), customRenderingCode, showFeedback, hasCustomFeedback);
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
                          trigger.getDelay(), trigger.getTimeout(), trigger.getMinimumBuffer());
  }

  /**
   * @param schedule
   * @return
   */
  static SignalScheduleDAO createSignalScheduleDAO(SignalSchedule schedule) {
    return new SignalScheduleDAO(schedule.getId().getId(), schedule.getScheduleType(), schedule.getByDayOfMonth(),
            schedule.getDayOfMonth(), schedule.getEsmEndHour(), schedule.getEsmFrequency(),
            schedule.getEsmPeriodInDays(), schedule.getEsmStartHour(), schedule.getNthOfMonth(),
            schedule.getRepeatRate(), toArray(schedule.getTimes()), schedule.getWeekDaysScheduled(),
            schedule.getEsmWeekends(), schedule.getUserEditable(), schedule.getTimeout(), schedule.getMinimumBuffer());
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
            input.getText(), input.getMandatory(), input.getScheduleDate() != null ? input.getScheduleDate().getTime()
                    : null, input.getLikertSteps(), input.getConditional(), input.getConditionalExpression(),
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
    experiment.setId(experimentDAO.getId()); // still neccessary in the case of retrieval from cache
    experiment.setTitle(experimentDAO.getTitle());
    experiment.setDescription(experimentDAO.getDescription());
    if (experiment.getCreator() == null && whoFromLogin != null) { // it is only null when we are recreating it from cache.
      experiment.setCreator(whoFromLogin);
    }
    experiment.setInformedConsentFormText(experimentDAO.getInformedConsentForm());
    experiment.setQuestionsChange(experimentDAO.getQuestionsChange());
    experiment.setFixedDuration(experimentDAO.getFixedDuration());

    String startDate = experimentDAO.getStartDate();
    experiment.setStartDate(startDate);

    String endDate = experimentDAO.getEndDate();
    experiment.setEndDate(endDate);

    experiment.setModifyDate(experimentDAO.getModifyDate() != null ? experimentDAO
        .getModifyDate() : getTodayAsString());

    Key key = null;
    if (experiment.getId() != null) {
      key = KeyFactory.createKey(Experiment.class.getSimpleName(), experiment.getId());
    }

    SignalingMechanismDAO[] signalingMechanisms = experimentDAO.getSignalingMechanisms();
    SignalingMechanismDAO signalingMechanismDAO = signalingMechanisms[0];
    if (signalingMechanismDAO instanceof SignalScheduleDAO) {
      experiment.setSchedule(fromScheduleDAO(key, (SignalScheduleDAO) signalingMechanismDAO));
      experiment.setTrigger(null);
    } else {
      experiment.setTrigger(fromTriggerDAO(key, (TriggerDAO) signalingMechanismDAO));
      experiment.setSchedule(null);
    }

    experiment.setInputs(fromInputDAOs(key, experimentDAO.getInputs(),
        experiment.getQuestionsChange()));
    experiment.setFeedback(fromFeedbackDAOs(key, experimentDAO.getFeedback()));
    experiment.setShowFeedback(experimentDAO.shouldShowFeedback());

    experiment.setPublished(experimentDAO.getPublished());
    experiment.setPublishedUsers(lowerCaseEmailAddresses(experimentDAO.getPublishedUsers()));
    experiment.setAdmins(lowerCaseEmailAddresses(experimentDAO.getAdmins()));
    experiment.setDeleted(experimentDAO.getDeleted());

    experiment.setCustomRendering(experimentDAO.isCustomRendering());
    experiment.setCustomRenderingCode(experimentDAO.getCustomRenderingCode());

    return experiment;
  }

  private static String getTodayAsString() {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(TimeUtil.DATE_FORMAT);
    return new DateTime().toString(formatter);
  }

  private static Trigger fromTriggerDAO(Key key, TriggerDAO signalingMechanismDAO) {
    Trigger trigger = new Trigger(key, signalingMechanismDAO.getId(),
                                                 signalingMechanismDAO.getEventCode(),
                                                 signalingMechanismDAO.getSourceIdentifier(),
                                                 signalingMechanismDAO.getDelay(), signalingMechanismDAO.getTimeout(), signalingMechanismDAO.getMinimumBuffer());
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
   * @param schedule
   * @return
   */
  private static SignalSchedule fromScheduleDAO(Key key, SignalScheduleDAO scheduleDAO) {
    SignalSchedule schedule = new SignalSchedule(key, scheduleDAO.getId(),
        scheduleDAO.getScheduleType(), scheduleDAO.getEsmFrequency(),
        scheduleDAO.getEsmPeriodInDays(), scheduleDAO.getEsmStartHour(),
        scheduleDAO.getEsmEndHour(), Arrays.asList(scheduleDAO.getTimes()),
        scheduleDAO.getRepeatRate(), scheduleDAO.getWeekDaysScheduled(),
        scheduleDAO.getNthOfMonth(), scheduleDAO.getByDayOfMonth(), scheduleDAO.getDayOfMonth(),
        scheduleDAO.getEsmWeekends(), scheduleDAO.getUserEditable(), scheduleDAO.getTimeout(), scheduleDAO.getMinimumBuffer());
    return schedule;
  }

  /**
   * @param experimentKey TODO
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
  private static List<Input> fromInputDAOs(Key experimentKey, InputDAO[] inputDAOs,
      boolean questionsChange) {
    List<Input> inputs = Lists.newArrayList();
    for (InputDAO input : inputDAOs) {
      Date scheduleDate = null;
      if (questionsChange) {
        scheduleDate = new Date(input.getScheduleDate());
      } else {
        scheduleDate = null;
      }
      inputs.add(new Input(experimentKey, input.getId(), input.getName(), input.getText(),
          scheduleDate, input.getQuestionType(), input.getResponseType(), input.getLikertSteps(),
          input.getMandatory(), input.getConditional(), input.getConditionExpression(),
          input.getLeftSideLabel(), input.getRightSideLabel(),
          Arrays.asList(input.getListChoices() != null ? input.getListChoices() : new String[0]),
          input.getMultiselect()));
    }
    return inputs;
  }

//  public static Experiment createExperiment(ExperimentDAO experimentDAO) {
//    Experiment experiment = new Experiment();
//    fromExperimentDAO(experimentDAO, experiment, null);
//    return experiment;
//  }

//  static List<Experiment> createExperimentsFrom(List<ExperimentDAO> experimentDAOs) {
//    List<Experiment> experiments = Lists.newArrayList();
//    for (ExperimentDAO experimentDAO : experimentDAOs) {
//      experiments.add(createExperiment(experimentDAO));
//    }
//    return experiments;
//  }


}
