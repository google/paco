package com.google.sampling.experiential.server.migration.jobs;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Experiment;
import com.google.sampling.experiential.model.Input;
import com.google.sampling.experiential.model.SignalSchedule;
import com.google.sampling.experiential.model.Trigger;
import com.google.sampling.experiential.server.ExperimentRetrieverOld;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.Pair;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.pacoapp.paco.shared.model.SignalScheduleDAO;
import com.pacoapp.paco.shared.model2.ActionTrigger;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.Feedback;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.InterruptCue;
import com.pacoapp.paco.shared.model2.InterruptTrigger;
import com.pacoapp.paco.shared.model2.PacoAction;
import com.pacoapp.paco.shared.model2.PacoNotificationAction;
import com.pacoapp.paco.shared.model2.Schedule;
import com.pacoapp.paco.shared.model2.ScheduleTrigger;
import com.pacoapp.paco.shared.model2.SignalTime;
import com.pacoapp.paco.shared.model2.ValidationMessage;

public class ExperimentJDOToDatastoreMigration implements MigrationJob {

  public static final Logger log = Logger.getLogger(ExperimentJDOToDatastoreMigration.class.getName());

  @Override
  public boolean doMigration() {
    UserService userService;
    DateTimeZone timezone = DateTimeZone.getDefault();
    final int limit = 30;
    // read experiments
    ExperimentRetrieverOld erOld = ExperimentRetrieverOld.getInstance();
    ExperimentService newEs = ExperimentServiceFactory.getExperimentService();

    String cursor = null;
    String lastCursor = null;
    boolean firstPass = true;
    List<Long> unsuccessful = Lists.newArrayList();

    Map<Long, Long> idMap = Maps.newHashMap();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    loadIdMap(idMap, ds);

    while (firstPass || (cursor != null && cursor != lastCursor)) {
      firstPass = false;
      Pair<String, List<Experiment>> queryResult = erOld.getExperiments(cursor, limit);

      List<Experiment> currentBatchExperiments = queryResult.second;
      for (Experiment oe : currentBatchExperiments) {

        final Long oldExperimentId = oe.getId();
        Long newId = idMap.get(oldExperimentId);
        if (newId == null) {
          ExperimentDAO ne = new ExperimentDAO();
          // don't set Id
          // set all properties on new by copying from the old
          // note: we will need to reverse this for backward compatibility
          // and android will need to do this too.
          // ne props
          ne.setAdmins(oe.getAdmins());
          User currentOwner = oe.getCreator();
          ne.setContactEmail(currentOwner.getEmail().toLowerCase());
          ne.setCreator(currentOwner.getEmail().toLowerCase());
          ne.setDeleted(oe.getDeleted());
          ne.setDescription(oe.getDescription());
          // ne.setEarliestStartDate(oe.getStartDate());
          ne.setExtraDataCollectionDeclarations(oe.getExtraDataCollectionDeclarations());
          List<ExperimentGroup> groups = ne.getGroups();
          ExperimentGroup eg = new ExperimentGroup("default group");
          groups.add(eg);
          // experimentgroup props
          eg.setFixedDuration(oe.getFixedDuration());
          if (eg.getFixedDuration()) {
            eg.setStartDate(oe.getStartDate());
            eg.setEndDate(oe.getEndDate());
          }

          eg.setLogActions(oe.shouldLogActions());
          eg.setBackgroundListen(oe.isBackgroundListen());
          eg.setBackgroundListenSourceIdentifier(oe.getBackgroundListenSourceIdentifier());

          eg.setCustomRendering(oe.isCustomRendering());
          eg.setCustomRenderingCode(oe.getCustomRenderingCode());

          // TODO fix the end of day experiments
          // eg.setEndOfDayGroup(null);
          // eg.setEndOfDayReferredGroupName(lastCursor);

          final Feedback feedback = new Feedback();
          feedback.setText(oe.getFeedback().get(0).getText());
          feedback.setType(oe.getFeedbackType());
          eg.setFeedback(feedback);

          List<Input2> newInputs = Lists.newArrayList();
          List<Input> oldInputs = oe.getInputs();
          for (Input oi : oldInputs) {
            Input2 ni = new Input2();
            newInputs.add(ni);
            ni.setName(oi.getName());
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
              ni.setLikertSteps(oi.getLikertSteps());
            } else if (ni.getResponseType().equals(Input2.LIST)) {
              ni.setMultiselect(oi.isMultiselect());
              ni.setListChoices(oi.getListChoices());
            }
          }
          eg.setInputs(newInputs);

          // actiontrigger props
          List<ActionTrigger> actionTriggers = Lists.newArrayList();
          eg.setActionTriggers(actionTriggers);

          // action props
          List<PacoAction> actions = Lists.newArrayList();
          PacoNotificationAction notificationAction = new PacoNotificationAction();
          notificationAction.setId(1l);
          notificationAction.setActionCode(PacoAction.NOTIFICATION_TO_PARTICIPATE_ACTION_CODE);
          actions.add(notificationAction);

          ActionTrigger actionTrigger = null;
          SignalSchedule oldSchedule = oe.getSchedule();
          if (oldSchedule == null) {
            Trigger oldTrigger = oe.getTrigger();
            if (oldTrigger == null) {
              log.severe("There is no schedule or trigger for old experiment: " + oldExperimentId);
            } else {
              actionTrigger = new InterruptTrigger();
              // interrupt trigger props
              // interrupt cue props
              InterruptCue ic = new InterruptCue();
              ic.setCueCode(oldTrigger.getEventCode());
              ic.setCueSource(oldTrigger.getSourceIdentifier());
              ((InterruptTrigger) actionTrigger).setCues(Lists.newArrayList(ic));

              // interrupt action props
              notificationAction.setDelay(oldTrigger.getDelay());
              notificationAction.setSnoozeCount(oldTrigger.getSnoozeCount());
              notificationAction.setSnoozeTimeInMinutes(oldTrigger.getSnoozeTime());
              notificationAction.setTimeout(oldTrigger.getTimeout());
              ((InterruptTrigger) actionTrigger).setMinimumBuffer(oldTrigger.getMinimumBuffer());
            }
          } else {
            // schedule trigger props
            actionTrigger = new ScheduleTrigger();
            actionTrigger.setOnlyEditableOnJoin(oldSchedule.getOnlyEditableOnJoin());
            actionTrigger.setUserEditable(oldSchedule.getUserEditable());

            // schedule props
            final Integer oldScheduleType = oldSchedule.getScheduleType();
            if (oldScheduleType != SignalScheduleDAO.SELF_REPORT) {
              com.pacoapp.paco.shared.model2.Schedule schedule = new Schedule();
              ((ScheduleTrigger) actionTrigger).setSchedules(Lists.newArrayList(schedule));

              schedule.setScheduleType(oldScheduleType);
              schedule.setMinimumBuffer(oldSchedule.getMinimumBuffer());
              schedule.setId(1l);

              if (schedule.getScheduleType().equals(Schedule.ESM)) {
                schedule.setEsmEndHour(oldSchedule.getEsmEndHour());
                schedule.setEsmFrequency(oldSchedule.getEsmFrequency());
                schedule.setEsmPeriodInDays(oldSchedule.getEsmPeriodInDays());
                schedule.setEsmStartHour(oldSchedule.getEsmStartHour());
                schedule.setEsmWeekends(oldSchedule.getEsmWeekends());
              } else if (schedule.getScheduleType().equals(Schedule.DAILY)
                         || schedule.getScheduleType().equals(Schedule.WEEKDAY)) {
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
                  SignalTime newSt = new SignalTime();
                  signalTimes.add(newSt);
                  newSt.setBasis(oldSt.getBasis());
                  newSt.setFixedTimeMillisFromMidnight(oldSt.getFixedTimeMillisFromMidnight());
                  newSt.setLabel(oldSt.getLabel());
                  newSt.setMissedBasisBehavior(oldSt.getMissedBasisBehavior());
                  newSt.setOffsetTimeMillis(oldSt.getOffsetTimeMillis());
                  newSt.setType(oldSt.getType());

                }
                schedule.setSignalTimes(signalTimes);
              }

              // action props
              notificationAction.setSnoozeCount(oldSchedule.getSnoozeCount());
              notificationAction.setSnoozeTimeInMinutes(oldSchedule.getSnoozeTime());
              notificationAction.setTimeout(oldSchedule.getTimeout());
            }
          }
          if (actionTrigger != null) { // self report is no longer a schedule,
                                       // or an actiontrigger. it is the absence
                                       // of that.
            actionTrigger.setActions(actions);

            actionTriggers.add(actionTrigger);
            actionTrigger.setId(1l);
          }

          // todo add all expeirment stuff from oe that goes in groups

          //
          ne.setInformedConsentForm(oe.getInformedConsentForm());
          // TODO check these. Are they synthetic?
          // ne.setLatestEndDate(oe.getEndDateAsDate());
          ne.setModifyDate(oe.getModifyDate());
          ne.setPublished(oe.getPublished());
          ne.setPublishedUsers(oe.getPublishedUsers());
          ne.setRecordPhoneDetails(oe.isRecordPhoneDetails());
          ne.setTitle(oe.getTitle());
          ne.setVersion(oe.getVersion());

          List<ValidationMessage> results = newEs.saveExperiment(ne,
                                                                 currentOwner.getEmail().toLowerCase(),
                                                                 timezone);
          if (results.size() > 0) {
            log.severe("Errors saving New experiment: " + Joiner.on(",").join(results));
            unsuccessful.add(oldExperimentId);
          } else {
            idMap.put(oldExperimentId, ne.getId());
          }
        }
      }
      if (unsuccessful.size() > 0) {
        logUnsuccesful(unsuccessful);
        persistUnsuccessful(unsuccessful, ds);
        unsuccessful.clear();
      }
      if (idMap.keySet().size() > 0) {
        addToIdMap(idMap, ds);
        idMap.clear();
      }
      lastCursor = cursor;
      cursor = queryResult.first;
    }
    log.severe("Done processing experiments");

    // create json from experiments
    // store json experiments in new experiment_lt table as Entities
    // Experiment, title, creator, start, end, admin list, published list?, blob
    // test that we can read those experiments and that they are equal to the
    // existing experiments
    // repair all versions to make jdoExperiemntId be the new experiment_entity
    // id.
    if (unsuccessful.size() == 0) {
      return true;
    } else {
      return false;
    }
  }

  public void persistUnsuccessful(List<Long> unsuccessfulIds, DatastoreService ds) {
    List<Entity> newMappings = Lists.newArrayList();
    for (Long oldId : unsuccessfulIds) {
      Entity expEntity = new Entity("experiment_unsuccessful");
      expEntity.setProperty("oldId", oldId);
      newMappings.add(expEntity);
    }

    Transaction tx = null;
    try {
      tx = ds.beginTransaction();
      ds.put(tx, newMappings);
      tx.commit();
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
    }
  }

  public void addToIdMap(Map<Long, Long> idMap, DatastoreService ds) {
    List<Entity> newMappings = Lists.newArrayList();
    for (Long oldId : idMap.keySet()) {
      Entity mapEntity = new Entity("experiment_id_map");
      mapEntity.setProperty("oldId", oldId);
      mapEntity.setProperty("newId", idMap.get(oldId));
      newMappings.add(mapEntity);
    }

    Transaction tx = null;
    try {
      tx = ds.beginTransaction();
      ds.put(tx, newMappings);
      tx.commit();
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
    }
  }

  public void loadIdMap(Map<Long, Long> idMap, DatastoreService ds) {
    Query query = new com.google.appengine.api.datastore.Query("experiment_id_map");
    PreparedQuery preparedQuery = ds.prepare(query);
    List<Entity> idMapResults = preparedQuery.asList(FetchOptions.Builder.withDefaults());

    for (Entity entity : idMapResults) {
      idMap.put((Long) entity.getProperty("oldId"), (Long) entity.getProperty("newId"));
    }
  }

  private void logUnsuccesful(List<Long> unsuccessful) {
    log.severe("Unsuccessful batch of old experiment ids: " + Joiner.on(",").join(unsuccessful));
  }

}
