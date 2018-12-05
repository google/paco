package com.google.sampling.experiential.server.migration.jobs;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.datastore.ExperimentJsonEntityManager;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.Trigger;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.server.ExperimentRetrieverOld;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.pacoapp.paco.shared.model.SignalScheduleDAO;
import com.pacoapp.paco.shared.model.TriggerDAO;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentValidator;
import com.pacoapp.paco.shared.model2.Feedback;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.model2.Pair;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.model2.ValidationMessage;

public class ExperimentJDOToDatastoreMigration implements MigrationJob {

  public static final Logger log = Logger.getLogger(ExperimentJDOToDatastoreMigration.class.getName());

  @Override
  public boolean doMigration(String optionalcursor, DateTime startTime, DateTime endTime) {
    System.out.println("STARTING MIGRATION");
    UserService userService;
    DateTimeZone timezone = DateTimeZone.getDefault();
    final int limit = 30;
    // read experiments
    ExperimentRetrieverOld erOld = ExperimentRetrieverOld.getInstance();
    // ExperimentService newEs =
    // ExperimentServiceFactory.getExperimentService();

    String cursor = null;
    String lastCursor = null;
    boolean firstPass = true;
    List<Long> unsuccessful = Lists.newArrayList();

    Set<Long> idMap = Sets.newConcurrentHashSet();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    loadIdMap(idMap, ds);

    while (firstPass || (cursor != null && !cursor.equals(lastCursor))) {
      firstPass = false;
      Pair<String, List<Experiment>> queryResult = erOld.getExperiments(cursor, limit);

      List<Experiment> currentBatchExperiments = queryResult.second;
      System.out.println("currentBatchExperiments.size = " + currentBatchExperiments.size());
      for (Experiment oe : currentBatchExperiments) {
        System.out.println("COPYING experiment: " + oe.getTitle() + ", " + oe.getId());
        final Long oldExperimentId = oe.getId();
        Boolean seen = idMap.contains(oldExperimentId);
        if (!seen) {
          createNewModelExperimentFromOld(timezone, unsuccessful, idMap, ds, oe, oldExperimentId);
        }
      }
      if (unsuccessful.size() > 0) {
        logUnsuccesful(unsuccessful);
        persistUnsuccessful(unsuccessful, ds);
        unsuccessful.clear();
      }
      if (idMap.size() > 0) {
        persistIdMap(idMap, ds);
        idMap.clear();
      }
      lastCursor = cursor;
      cursor = queryResult.first;
      System.out.println("Cursor check. lastCursor: " + lastCursor + "\ncursor: " + cursor);
    }

    log.severe("Done processing experiments");

    if (unsuccessful.size() == 0) {
      return true;
    } else {
      return false;
    }
  }

  public void createNewModelExperimentFromOld(DateTimeZone timezone, List<Long> unsuccessful, Set<Long> idMap,
                                              DatastoreService ds, Experiment oe, final Long oldExperimentId) {
    List<ValidationMessage> results = null;
    ExperimentDAO ne = null;
    try {
      //System.out.println("unseen experiment: " + oe.getTitle() + ", " + oe.getId());
      User currentOwner = oe.getCreator();

      ne = new ExperimentDAO();
      copyExperimentLevelProperties(oe, oldExperimentId, ne, currentOwner);

      List<ExperimentGroup> groups = ne.getGroups();
      ExperimentGroup eg = createExperimentGroup(oe, oldExperimentId, "default");
      groups.add(eg);

      String email = null;
      if (currentOwner != null) {
        email = currentOwner.getEmail();
      }
      // todo check for a referred experiment and add as a second group?
      if (email != null) {
        String lowerCase = email.toLowerCase();
        results = saveExperiment(ne, lowerCase, timezone, ds);
      }
    } catch (Exception e) {
      System.out.println("Got exception trying to copy experiment! msg: " + e.getMessage());
      StackTraceElement[] st = e.getStackTrace();
      System.out.println("error location: " + st[0].getClassName() + ":" + st[0].getMethodName() + ":" + st[0].getLineNumber());
      unsuccessful.add(oldExperimentId);
    }
    if (results != null && results.size() > 0) {
      log.severe("Errors saving New experiment: " + oe.getTitle() + ":" + oldExperimentId);
      log.severe("errors: " + Joiner.on(",").join(results));
      unsuccessful.add(oldExperimentId);
    } else {
      System.out.println("SAVED EXPERIMENT: " + ne.getTitle() + ", " + ne.getId());
      idMap.add(oldExperimentId);
    }
  }

  public void copyExperimentLevelProperties(Experiment oe, final Long oldExperimentId, ExperimentDAO ne,
                                            User currentOwner) {
    ne.setId(oldExperimentId); // set id because we are writing into the
                               // same table and the same row (id)
    // set all properties on new by copying from the old
    // note: we will need to reverse this for backward compatibility
    // and android will need to do this too.
    // ne props


    copyCreator(ne, currentOwner);
    final List<String> admins = oe.getAdmins();
    if (admins == null || admins.size() == 0) {
      admins.add(ne.getCreator());
    }
    ne.setAdmins(admins);

    ne.setDeleted(oe.getDeleted());
    ne.setDescription(oe.getDescription());
    // ne.setEarliestStartDate(oe.getStartDate());
    final List<Integer> extraDataCollectionDeclarations = oe.getExtraDataCollectionDeclarations();
    if (extraDataCollectionDeclarations != null ) {
      ne.setExtraDataCollectionDeclarations(extraDataCollectionDeclarations);
    }

    ne.setInformedConsentForm(oe.getInformedConsentForm());
    // TODO check these. Are they synthetic?
    // ne.setLatestEndDate(oe.getEndDateAsDate());
    ne.setModifyDate(oe.getModifyDate());
    ne.setPublished(oe.getPublished());
    ne.setPublishedUsers(oe.getPublishedUsers());
    final Boolean recordPhoneDetails = oe.isRecordPhoneDetails();
    if (recordPhoneDetails != null) {
      ne.setRecordPhoneDetails(recordPhoneDetails);
    }

    ne.setTitle(oe.getTitle());
    Integer version = oe.getVersion();
    if (version == null) {
      version = 1;
    }
    ne.setVersion(version);
  }

  public ExperimentGroup createExperimentGroup(Experiment oe, final Long oldExperimentId, final String defaultGroupName) {
    ExperimentGroup eg = new ExperimentGroup(defaultGroupName);

    final Boolean fixedDuration = oe.getFixedDuration();
    if (fixedDuration != null) {
      eg.setFixedDuration(fixedDuration);
      if (eg.getFixedDuration()) {
        eg.setStartDate(oe.getStartDate());
        eg.setEndDate(oe.getEndDate());
      }
    }

    final Boolean shouldLogActions = oe.shouldLogActions();
    if (shouldLogActions != null) {
      eg.setLogActions(shouldLogActions);
    }
    final Boolean backgroundListen = oe.isBackgroundListen();
    if (backgroundListen != null) {
      eg.setBackgroundListen(backgroundListen);

      String backgroundListenSourceIdentifier = oe.getBackgroundListenSourceIdentifier();
      if (backgroundListen && (backgroundListenSourceIdentifier == null || backgroundListenSourceIdentifier.length() == 0 )) {
        backgroundListenSourceIdentifier = "unknown";
      }
      eg.setBackgroundListenSourceIdentifier(backgroundListenSourceIdentifier);
    }

    final Boolean accessibilityListen = oe.isAccessibilityListen();
    if (accessibilityListen != null) {
      eg.setAccessibilityListen(accessibilityListen);
    }

    final Boolean customRendering = oe.isCustomRendering();
    if (customRendering != null) {
      eg.setCustomRendering(customRendering);
      String customRenderingCode = oe.getCustomRenderingCode();
      if (customRendering && (customRenderingCode == null || customRenderingCode.length() == 0)) {
        customRenderingCode = "<script></script>";
      }
      eg.setCustomRenderingCode(customRenderingCode);
    }

    // TODO fix the end of day experiments
    // eg.setEndOfDayGroup(null);
    // eg.setEndOfDayReferredGroupName(referredGroupName);

    final Feedback feedback = copyFeedback(oe);
    eg.setFeedback(feedback);
    eg.setFeedbackType(feedback.getType());

    List<Input2> newInputs = copyInputs(oe);
    eg.setInputs(newInputs);

    // actiontrigger props
    List<ActionTrigger> actionTriggers = createActionTriggers(oe, oldExperimentId);
    eg.setActionTriggers(actionTriggers);
    return eg;
  }

  public Feedback copyFeedback(Experiment oe) {
    final Feedback feedback = new Feedback();
    final Integer feedbackType = oe.getFeedbackType();
    feedback.setType(feedbackType);
    String text = oe.getFeedback().get(0).getLongText();
    if (text == null || text.length() == 0) {
      text = Feedback.DEFAULT_FEEDBACK_MSG;
    }
    feedback.setText(text);

    return feedback;
  }

  public List<Input2> copyInputs(Experiment oe) {
    List<Input2> newInputs = Lists.newArrayList();
    List<Input> oldInputs = oe.getInputs();
    int unknownNameCounter = 1;
    for (Input oi : oldInputs) {
      Input2 ni = new Input2();

      String name = oi.getName();
      if (name == null || name.length() == 0) {
        name = "unknown_" + unknownNameCounter++;
      }
      ni.setName(name);
      ni.setRequired(oi.getMandatory());
      ni.setText(oi.getText());

      ni.setConditional(oi.getConditional());
      if (ni.getConditional()) {
        ni.setConditionExpression(oi.getConditionalExpression());
      }
      ni.setResponseType(oi.getResponseType());
      if (ni.getResponseType().equals(Input2.LIKERT)) {
        ni.setLeftSideLabel(oi.getLeftSideLabel());
        ni.setRightSideLabel(oi.getRightSideLabel());
        Integer likertSteps = oi.getLikertSteps();
        if (likertSteps == null) {
          likertSteps = 5;
        }
        ni.setLikertSteps(likertSteps);
      } else if (ni.getResponseType().equals(Input2.LIST)) {
        final Boolean multiselect = oi.isMultiselect();
        if (multiselect != null) {
          ni.setMultiselect(multiselect);
        }
        final List<String> listChoices = oi.getListChoices();
        final List<String> fixedListChoices = Lists.newArrayList();
        int missedCounter = 0;
        for (String choice : listChoices) {
          if (choice == null || choice.length() == 0) {
            fixedListChoices.add("_"+missedCounter++);
          } else {
            fixedListChoices.add(choice);
          }
        }
        ni.setListChoices(fixedListChoices);
      }
      newInputs.add(ni);
    }
    return newInputs;
  }

  public List<ActionTrigger> createActionTriggers(Experiment oe, final Long oldExperimentId) {
    List<ActionTrigger> actionTriggers = Lists.newArrayList();

    List<PacoAction> actions = Lists.newArrayList();
    PacoNotificationAction notificationAction = new PacoNotificationAction();
    notificationAction.setId(1l);
    notificationAction.setActionCode(PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE);
    actions.add(notificationAction);

    ActionTrigger actionTrigger = null;
    SignalSchedule oldSchedule = oe.getSchedule();
    if (oldSchedule == null) {
      actionTrigger = createInterruptTrigger(oe, oldExperimentId, notificationAction);
    } else {
      actionTrigger = createScheduleTrigger(notificationAction, oldSchedule);
    }

    // self report is no longer a schedule type. It is no schedule or trigger.
    if (actionTrigger != null) {
      actionTrigger.setActions(actions);
      actionTriggers.add(actionTrigger);
      actionTrigger.setId(1l);
    }
    return actionTriggers;
  }

  public ActionTrigger createScheduleTrigger(PacoNotificationAction notificationAction, SignalSchedule oldSchedule) {
    final Integer oldScheduleType = oldSchedule.getScheduleType();
    if (oldScheduleType == SignalScheduleDAO.SELF_REPORT) {
      return null;
    }
    ActionTrigger actionTrigger = new ScheduleTrigger();
    com.pacoapp.paco.shared.model2.Schedule schedule = new Schedule();
    ((ScheduleTrigger) actionTrigger).setSchedules(Lists.newArrayList(schedule));

    final Boolean onlyEditableOnJoin = oldSchedule.getOnlyEditableOnJoin();
    if (onlyEditableOnJoin != null) {
      schedule.setOnlyEditableOnJoin(onlyEditableOnJoin);
    }

    final Boolean userEditable = oldSchedule.getUserEditable();
    if (userEditable != null) {
      schedule.setUserEditable(userEditable);
    }


    schedule.setScheduleType(oldScheduleType);
    schedule.setId(1l);

    if (schedule.getScheduleType().equals(Schedule.ESM)) {
      schedule.setEsmEndHour(oldSchedule.getEsmEndHour());
      schedule.setEsmFrequency(oldSchedule.getEsmFrequency());
      schedule.setEsmPeriodInDays(oldSchedule.getEsmPeriodInDays());
      schedule.setEsmStartHour(oldSchedule.getEsmStartHour());
      schedule.setEsmWeekends(oldSchedule.getEsmWeekends());
      final Integer minimumBuffer = oldSchedule.getMinimumBuffer();
      if (minimumBuffer != null) {
        schedule.setMinimumBuffer(minimumBuffer);
      }
    } else if (schedule.getScheduleType().equals(Schedule.DAILY) || schedule.getScheduleType().equals(Schedule.WEEKDAY)) {
      schedule.setRepeatRate(oldSchedule.getRepeatRate());
    } else if (schedule.getScheduleType().equals(Schedule.WEEKLY)) {
      schedule.setRepeatRate(oldSchedule.getRepeatRate());
      schedule.setWeekDaysScheduled(oldSchedule.getWeekDaysScheduled());
    } else if (schedule.getScheduleType().equals(Schedule.MONTHLY)) {
      schedule.setRepeatRate(oldSchedule.getRepeatRate());
      schedule.setByDayOfMonth(oldSchedule.getByDayOfMonth());

      if (schedule.getByDayOfMonth()) {
        schedule.setDayOfMonth(oldSchedule.getDayOfMonth());
      } else {
        schedule.setNthOfMonth(oldSchedule.getNthOfMonth());
        schedule.setWeekDaysScheduled(oldSchedule.getWeekDaysScheduled());
      }
    }
    if (!schedule.getScheduleType().equals(Schedule.ESM)) {
      List<SignalTime> signalTimes = Lists.newArrayList();
      List<com.google.sampling.experiential.model.SignalTime> oldTimes = oldSchedule.getSignalTimes();
      for (com.google.sampling.experiential.model.SignalTime oldSt : oldTimes) {
        SignalTime newSt = copySignalTime(oldSt);
        signalTimes.add(newSt);
      }
      schedule.setSignalTimes(signalTimes);
    }

    // action props
    final Integer snoozeCount = oldSchedule.getSnoozeCount();
    if (snoozeCount != null) {
      notificationAction.setSnoozeCount(snoozeCount);
    }
    final Integer snoozeTime = oldSchedule.getSnoozeTime();
    if (snoozeTime != null) {
      notificationAction.setSnoozeTimeInMinutes(snoozeTime);
    }
    notificationAction.setTimeout(oldSchedule.getTimeout());
    return actionTrigger;
  }

  public ActionTrigger createInterruptTrigger(Experiment oe, final Long oldExperimentId,
                                              PacoNotificationAction notificationAction) {
    Trigger oldTrigger = oe.getTrigger();
    if (oldTrigger == null) {
      log.severe("There is no schedule or trigger for old experiment: " + oldExperimentId);
      return null;
    } else {
      ActionTrigger actionTrigger = new InterruptTrigger();
      // interrupt trigger props
      // interrupt cue props
      InterruptCue ic = new InterruptCue();
      ic.setCueCode(oldTrigger.getEventCode());

      String sourceIdentifier = oldTrigger.getSourceIdentifier();
      if ((sourceIdentifier == null || sourceIdentifier.length() == 0) &&
              (oldTrigger.getEventCode() == TriggerDAO.APP_CLOSED ||
              oldTrigger.getEventCode() == TriggerDAO.APP_USAGE ||
              oldTrigger.getEventCode() == TriggerDAO.PACO_ACTION_EVENT)) {
        sourceIdentifier = "unknown";
      }
      ic.setCueSource(sourceIdentifier);
      ((InterruptTrigger) actionTrigger).setCues(Lists.newArrayList(ic));

      // interrupt action props
      notificationAction.setDelay(oldTrigger.getDelay());
      final Integer snoozeCount = oldTrigger.getSnoozeCount();
      if (snoozeCount != null) {
        notificationAction.setSnoozeCount(snoozeCount);
      }
      final Integer snoozeTime = oldTrigger.getSnoozeTime();
      if (snoozeTime != null) {
        notificationAction.setSnoozeTimeInMinutes(snoozeTime);
      }
      final Integer timeout = oldTrigger.getTimeout();
      if (timeout != null) {
        notificationAction.setTimeout(timeout);
      }
      final Integer minimumBuffer = oldTrigger.getMinimumBuffer();
      if (minimumBuffer != null) {
        ((InterruptTrigger) actionTrigger).setMinimumBuffer(minimumBuffer);
      }
      return actionTrigger;
    }

  }

  public SignalTime copySignalTime(com.google.sampling.experiential.model.SignalTime oldSt) {
    SignalTime newSt = new SignalTime();
    newSt.setType(oldSt.getType());
    newSt.setBasis(oldSt.getBasis());
    newSt.setFixedTimeMillisFromMidnight(oldSt.getFixedTimeMillisFromMidnight());
    newSt.setLabel(oldSt.getLabel());
    newSt.setMissedBasisBehavior(oldSt.getMissedBasisBehavior());
    newSt.setOffsetTimeMillis(oldSt.getOffsetTimeMillis());
    return newSt;
  }

  public void copyCreator(ExperimentDAO ne, User currentOwner) {
    if (currentOwner != null && currentOwner.getEmail() != null) {
      ne.setContactEmail(currentOwner.getEmail().toLowerCase());
      ne.setCreator(currentOwner.getEmail().toLowerCase());
    } else {
      log.severe("Unknown creator email for experiment: " + ne.getId() + ", " + ne.getTitle());
    }
  }

  public List<ValidationMessage> saveExperiment(ExperimentDAO experiment, String loggedInUserEmail,
                                                DateTimeZone timezone, DatastoreService ds) {
    System.out.println("VALIDATING EXPERIMENT: " + experiment.getTitle() + ", " + experiment.getId());
    try {
      ExperimentValidator validator = new ExperimentValidator();
      experiment.validateWith(validator);
      List<ValidationMessage> results = validator.getResults();
      if (!results.isEmpty()) {
        System.out.println("ERROR VALIDATING EXPERIMENT: " + experiment.getTitle() + ", " + experiment.getId());
        System.out.println("errors: " + Joiner.on(", ").join(results));
        return results;
      }
    }  catch (Exception e) {
      log.severe("Got exception validating!!!!!");
      log.severe(e.getMessage());
      e.printStackTrace();
      return null;
    }

    System.out.println("SAVING EXPERIMENT: " + experiment.getTitle() + ", " + experiment.getId());
//      TransactionOptions options = TransactionOptions.Builder.withXG(true);
      Transaction tx = null;//ds.beginTransaction(options);
      try {
        if (experiment.getId() == null) {
          experiment.setCreator(loggedInUserEmail);
        }
        if (!experiment.getAdmins().contains(loggedInUserEmail)) {
          experiment.getAdmins().add(loggedInUserEmail);
        }
        if (Strings.isNullOrEmpty(experiment.getContactEmail())) {
          experiment.setContactEmail(experiment.getCreator());
        }

        Key experimentKey = ExperimentJsonEntityManager.saveExperiment(ds, tx, JsonConverter.jsonify(experiment),
                                                                       experiment.getId(),
                                                                       experiment.getTitle(),
                                                                       experiment.getVersion(),
                                                                       new DateTime().getMillis(),
                                                                       experiment.getAdmins());

        experiment.setId(experimentKey.getId());
        ExperimentAccessManager.updateAccessControlEntities(ds, tx, experiment, experimentKey, timezone);
        //tx.commit();
        return null;
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      } finally {
//        if (tx.isActive()) {
//          tx.rollback();
//        }
      }

  }

  public void persistUnsuccessful(List<Long> unsuccessfulIds, DatastoreService ds) {
    if (unsuccessfulIds.size() == 0) {
      return;
    }
    System.out.println("Persisting unsuccessful ids");

//    List<Entity> newMappings = Lists.newArrayList();
//    for (Long oldId : unsuccessfulIds) {
//      Entity expEntity = new Entity("experiment_unsuccessful");
//      expEntity.setProperty("oldId", oldId);
//      newMappings.add(expEntity);
//    }
//
//    Transaction tx = null;
//    try {
//      tx = ds.beginTransaction(TransactionOptions.Builder.withXG(true));
//      ds.put(tx, newMappings);
//      tx.commit();
//    } finally {
//      if (tx != null && tx.isActive()) {
//        tx.rollback();
//      }
//    }
  }

  public void persistIdMap(Set<Long> idMap, DatastoreService ds) {
    if (idMap.size() == 0) {
      return;
    }
    List<Entity> newMappings = Lists.newArrayList();

//    for (Long oldId : idMap) {
//      Entity mapEntity = new Entity("experiment_id_map");
//      mapEntity.setProperty("oldId", oldId);
//      newMappings.add(mapEntity);
//    }
//
//    Transaction tx = null;
//    try {
//      tx = ds.beginTransaction();
//      ds.put(tx, newMappings);
//      tx.commit();
//    } finally {
//      if (tx != null && tx.isActive()) {
//        tx.rollback();
//      }
//    }
  }

  public void loadIdMap(Set<Long> idMap, DatastoreService ds) {
    Query query = new com.google.appengine.api.datastore.Query("experiment_id_map");
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> idMapResults = preparedQuery.asList(FetchOptions.Builder.withDefaults());
    if (idMapResults != null) {
      for (Entity entity : idMapResults) {
        idMap.add((Long) entity.getProperty("oldId"));
      }
    }
  }

  private void logUnsuccesful(List<Long> unsuccessful) {
    log.severe("Unsuccessful batch of old experiment ids: " + Joiner.on(",").join(unsuccessful));
  }

}
