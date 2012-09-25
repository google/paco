package com.google.sampling.experiential.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Feedback;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.shared.ExperimentDAO;
import com.google.sampling.experiential.shared.FeedbackDAO;
import com.google.sampling.experiential.shared.InputDAO;
import com.google.sampling.experiential.shared.SignalScheduleDAO;

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

    SignalSchedule schedule = experiment.getSchedule();

    SignalScheduleDAO signalScheduleDAO = null;
    // BACKWard compatibility friendliness - create a schedule for this
    // experiment
    if (schedule == null) {
      signalScheduleDAO = new SignalScheduleDAO();
      signalScheduleDAO.setScheduleType(SignalScheduleDAO.SELF_REPORT);
      published = Boolean.FALSE;
    } else {
      signalScheduleDAO = createSignalScheduleDAO(schedule);
    }
    Boolean fixedDuration = experiment.getFixedDuration();
    Boolean questionsChange = experiment.getQuestionsChange();
    Boolean deleted = experiment.getDeleted();
    Long startDate = experiment.getStartDate() != null ? experiment.getStartDate().getTime() : null;
    Long endDate = experiment.getEndDate() != null ? experiment.getEndDate().getTime() : null;
    String hash = experiment.getHash();
    Long joinDate = experiment.getJoinDate() != null ? experiment.getJoinDate().getTime() : null;
    Long modifyDate = experiment.getModifyDate() != null ? experiment.getModifyDate().getTime() : null;

    List<String> admins = experiment.getAdmins();
    String[] adminStrArray = new String[admins.size()];
    adminStrArray = admins.toArray(adminStrArray);

    List<String> userEmails = experiment.getPublishedUsers();
    String[] userEmailsStrArray = new String[userEmails.size()];
    userEmailsStrArray = userEmails.toArray(userEmailsStrArray);

    ExperimentDAO dao = new ExperimentDAO(id, title, description, informedConsentForm, email, signalScheduleDAO,
            fixedDuration, questionsChange, startDate, endDate, hash, joinDate, modifyDate, published, adminStrArray,
            userEmailsStrArray, deleted, null);
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

  /**
   * @param schedule
   * @return
   */
  static SignalScheduleDAO createSignalScheduleDAO(SignalSchedule schedule) {
    return new SignalScheduleDAO(schedule.getId().getId(), schedule.getScheduleType(), schedule.getByDayOfMonth(),
            schedule.getDayOfMonth(), schedule.getEsmEndHour(), schedule.getEsmFrequency(),
            schedule.getEsmPeriodInDays(), schedule.getEsmStartHour(), schedule.getNthOfMonth(),
            schedule.getRepeatRate(), toArray(schedule.getTimes()), schedule.getWeekDaysScheduled(),
            schedule.getEsmWeekends(), schedule.getUserEditable());
  }

  public static FeedbackDAO createDAO(Feedback feedback) {
    return new FeedbackDAO(feedback.getId().getId(), feedback.getFeedbackType(), feedback.getLongText());
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

  static Experiment fromExperimentDAO(ExperimentDAO experimentDAO, Experiment experiment, User whoFromLogin) {
    experiment.setId(experimentDAO.getId()); // still neccessary in the case of retrieval from cache
    experiment.setTitle(experimentDAO.getTitle());
    experiment.setDescription(experimentDAO.getDescription());
    if (experiment.getCreator() == null && whoFromLogin != null) { // it is only null when we are recreating it from cache.
      experiment.setCreator(whoFromLogin);
    }
    experiment.setInformedConsentFormText(experimentDAO.getInformedConsentForm());
    experiment.setQuestionsChange(experimentDAO.getQuestionsChange());
    experiment.setFixedDuration(experimentDAO.getFixedDuration());
    Long startDateDAO = experimentDAO.getStartDate();
    Date startDate = null;
    if (startDateDAO != null) {
      startDate = new DateTime(startDateDAO).toDate();
    }
    experiment.setStartDate(startDate);
    
    Long endDateDAO = experimentDAO.getEndDate();
    Date endDate = null;
    if (endDateDAO != null) {
      endDate = new DateTime(endDateDAO).toDate();
    }
    experiment.setEndDate(endDate);
    
    experiment.setModifyDate(experimentDAO.getModifyDate() != null ? new Date(experimentDAO
        .getModifyDate()) : new Date());
    
    Key key = null;
    if (experiment.getId() != null) {
      key = KeyFactory.createKey(Experiment.class.getSimpleName(), experiment.getId());
    }
  
    experiment.setSchedule(fromScheduleDAO(key, experimentDAO.getSchedule()));
    experiment.setInputs(fromInputDAOs(key, experimentDAO.getInputs(), 
        experiment.getQuestionsChange()));
    experiment.setFeedback(fromFeedbackDAOs(key, experimentDAO.getFeedback()));
    
    experiment.setPublished(experimentDAO.getPublished());
    experiment.setPublishedUsers(lowerCaseEmailAddresses(experimentDAO.getPublishedUsers()));
    experiment.setAdmins(lowerCaseEmailAddresses(experimentDAO.getAdmins()));
    experiment.setDeleted(experimentDAO.getDeleted());
    return experiment;
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
        scheduleDAO.getEsmWeekends(), scheduleDAO.getUserEditable());
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
      feedback.add(new Feedback(experimentKey, feedbackDAO.getId(), feedbackDAO.getFeedbackType(), 
          feedbackDAO.getText()));      
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
