package com.google.sampling.experiential.server.stats.usage;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.migration.MigrationJob;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;
import com.pacoapp.paco.shared.util.TimeUtil;

/**
 * This class is called by a scheduled Task weekly and adds a new stats row into the
 * usage_stats table
 *
 *
 *
 */
public class UsageStatsBackfillJob implements MigrationJob {

  private static final Logger log = Logger.getLogger(UsageStatsBackfillJob.class.getName());
  private String adminDomainSystemSetting;

  public UsageStatsBackfillJob() {
  }

  @Override
  public boolean doMigration(String optionalCursor, DateTime startTime, DateTime endTime) {
    try {
      log.info("Starting usage stats backfill 2");
      run();
      log.info("Successfully finished usage stats backfill 2");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      log.info("Error on usage stats backfill 2");
      return false;
    }

  }


  public void run() throws IOException {
    Long numberOfEvents = 0l;//getTotalEventCount();

    //part 3 participant stats
    Long totalParticipantsJoined = 0l;//ExperimentAccessManager.getTotalJoinedParticipantsCount();

    // part 2 experiment stats
    ExperimentQueryResult experimentsQueryResults = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();
    log.info("Backfill job retrieved experiments. Count = " + experimentList.size());

    if (experimentList != null) {
      Collections.sort(experimentList, new Comparator<ExperimentDAO>() {

        @Override
        public int compare(ExperimentDAO o1, ExperimentDAO o2) {
          String o1ModifyDate = o1.getModifyDate();
          String o2ModifyDate = o2.getModifyDate();
          if (o1ModifyDate == null && o2ModifyDate == null) {
            return 0;
          } else if (o1ModifyDate == null && o2ModifyDate != null) {
            return 1;
          } else if (o1ModifyDate != null && o2ModifyDate == null) {
            return -1;
          } else {
            DateTime o1Date = DateTime.parse(o1ModifyDate, TimeUtil.dateFormatter);
            DateTime o2Date = DateTime.parse(o2ModifyDate, TimeUtil.dateFormatter);
            return o1Date.compareTo(o2Date);
          }

        }

      });
      ExperimentDAO oldestExperiment = experimentList.get(0);
      DateMidnight oldestDate = DateTime.parse(oldestExperiment.getModifyDate(), TimeUtil.dateFormatter).toDateMidnight();
      int dow = oldestDate.getDayOfWeek();
      //adjust to next Sunday
      int offset = (7 - dow) % 7;

      DateMidnight current = DateTime.parse("2016/06/03", TimeUtil.dateFormatter).toDateMidnight();

      List<ExperimentDAO> experimentsCreatedBeforeCurrentWeek = Lists.newArrayList();
      int i = 0;
      DateMidnight anyGivenSunday = oldestDate.plusDays(offset);

      while (experimentsCreatedBeforeCurrentWeek.size() < experimentList.size() && anyGivenSunday.isBefore(current)) {
        for (; i < experimentList.size(); i++) {
          ExperimentDAO candidate = experimentList.get(i);
          String modifyDate = candidate.getModifyDate();
          if (modifyDate != null && DateTime.parse(modifyDate, TimeUtil.dateFormatter).isAfter(anyGivenSunday)) {
            break;
          } else {
            experimentsCreatedBeforeCurrentWeek.add(candidate);
          }
        }
        runWeek(anyGivenSunday.toDateTime(), experimentsCreatedBeforeCurrentWeek, numberOfEvents, totalParticipantsJoined);
        anyGivenSunday = anyGivenSunday.plusWeeks(1);
      }
    }
  }

  private void runWeek(DateTime dateTime, List<ExperimentDAO> experimentList, Long numberOfEvents, Long totalParticipantsJoined) {
    log.info("writing usage stats for week: " + dateTime.toString() + ", experimentList size = " + experimentList.size());
    loadAdminDomainSetting();
    // part 1 = event stats
    if (experimentList != null) {

      List<ExperimentDAO> nonDomainExperimentsList = Lists.newArrayList();
      List<ExperimentDAO> domainExperimentsList = Lists.newArrayList();

      for (ExperimentDAO experimentDAO : experimentList) {
        boolean domainAdmin = false;

        List<String> admins = experimentDAO.getAdmins();
        for (String admin : admins) {

          if (admin.indexOf("@" + adminDomainSystemSetting) != -1) {
            domainAdmin = true;
            break;
          }
        }
        if (domainAdmin) {
          domainExperimentsList.add(experimentDAO);
        } else {
          nonDomainExperimentsList.add(experimentDAO);
        }
      }

      UsageStat nonDomainExperimentStats = computeStats(dateTime, nonDomainExperimentsList, numberOfEvents, totalParticipantsJoined);
      UsageStat domainExperimentStats = computeStats(dateTime, domainExperimentsList, 0, 0l);
      domainExperimentStats.setAdminDomainFilter(adminDomainSystemSetting);

      UsageStatsEntityManager usageStatsMgr = UsageStatsEntityManager.getInstance();
      usageStatsMgr.addStats(nonDomainExperimentStats, domainExperimentStats);

    }
  }

  private long getTotalEventCount() {
    long numberOfEvents = 0;

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("__Stat_Kind__");
    query.setFilter(new FilterPredicate("kind_name", FilterOperator.EQUAL, "Event"));
    Entity eventTotalStat = datastore.prepare(query).asSingleEntity();
    if (eventTotalStat != null) {
      numberOfEvents = (long) eventTotalStat.getProperty("count");
    } else {
      log.info("could not stat entity count of events");
    }
    return numberOfEvents;
  }

  private void loadAdminDomainSetting() {
    adminDomainSystemSetting = System.getProperty("com.pacoapp.adminDomain");
    if (Strings.isNullOrEmpty(adminDomainSystemSetting)) {
      adminDomainSystemSetting = "";
    }
  }

  private UsageStat computeStats(DateTime dateTime, List<ExperimentDAO> experiments, long numberOfEvents, Long numberOfParticipants) {
    UsageStat usageStats = new UsageStat(dateTime);
    usageStats.setExperimentCountTotal(experiments.size());
    usageStats.setNumberOfEvents(numberOfEvents);
    usageStats.setNumberOfParticipants(numberOfParticipants);

    int unpublishedExperimentCountTotal = 0;
    int publishedExperimentCountTotal = 0;
    int publishedExperimentPublicCountTotal = 0;
    int publishedExperimentPrivateCountTotal = 0;
    int publishedExperimentFutureCountTotal = 0;
    int publishedExperimentPastCountTotal = 0;

    int publishedExperimentPresentCountTotal = 0;
    int publishedExperimentOngoingCountTotal = 0;

    List<Integer> publishedExperimentPrivateUserCountsTotal = Lists.newArrayList();

    int publishedExperimentPublicCountNonPilotTotal = 0;
    int publishedExperimentPrivateCountNonPilot = 0;
    int publishedExperimentFutureCountNonPilot = 0;
    int publishedExperimentPastCountNonPilot = 0;
    int publishedExperimentPresentCountNonPilot = 0;
    int publishedExperimentOngoingCountNonPilot = 0;
    List<Integer> publishedExperimentPrivateUserCountsNonPilot = Lists.newArrayList();

    for (ExperimentDAO experimentDAO : experiments) {
      boolean isNonPilot = isNonPilotExperiment(experimentDAO);
      if (experimentDAO.getPublished()) {
        publishedExperimentCountTotal++;
        if (experimentDAO.getPublishedUsers() == null || experimentDAO.getPublishedUsers().isEmpty()) {
          publishedExperimentPublicCountTotal++;
          if (isNonPilot) {
            publishedExperimentPublicCountNonPilotTotal++;
          }

        } else {
          publishedExperimentPrivateCountTotal++;
          publishedExperimentPrivateUserCountsTotal.add(experimentDAO.getPublishedUsers().size());

          if (isNonPilot) {
            publishedExperimentPrivateCountNonPilot++;
            publishedExperimentPrivateUserCountsNonPilot.add(experimentDAO.getPublishedUsers().size());
          }
        }

        if (!ExperimentHelper.isAnyGroupOngoingDuration(experimentDAO)) {
          DateMidnight startDate = ActionScheduleGenerator.getEarliestStartDate(experimentDAO);
          DateTime endDate = ActionScheduleGenerator.getLastEndTime(experimentDAO);
          if (startDate != null && startDate.isAfter(dateTime)) {
            publishedExperimentFutureCountTotal++;
            if (isNonPilot) {
              publishedExperimentFutureCountNonPilot++;
            }
          } else if (endDate != null && endDate.isBefore(dateTime)) {
            publishedExperimentPastCountTotal++;
            if (isNonPilot) {
              publishedExperimentPastCountNonPilot++;
            }
          } else if (endDate != null && startDate != null && startDate.isBefore(dateTime) && endDate.isAfter(dateTime)) {
            publishedExperimentPresentCountTotal++;
            if (isNonPilot) {
              publishedExperimentPresentCountNonPilot++;
            }
          }
        } else {
          publishedExperimentOngoingCountTotal++;
          if (isNonPilot) {
            publishedExperimentOngoingCountNonPilot++;
          }
//          String modifiedDate = experimentDAO.getModifyDate();
//          if (modifiedDate != null) {
//            DateTime modifiedDateTime = com.pacoapp.paco.shared.util.TimeUtil.unformatDate(modifiedDate);
//            Days ageDays = Days.daysBetween(modifiedDateTime, nowDateTime);
//            int dayCount = ageDays.getDays();
//            lastModifiedAge.add(dayCount);
//          }
        }
      } else {
        unpublishedExperimentCountTotal++;
      }


      usageStats.setUnpublishedExperimentCountTotal(unpublishedExperimentCountTotal);
      usageStats.setPublishedExperimentCountTotal(publishedExperimentCountTotal);
      usageStats.setPublishedExperimentPublicCountTotal(publishedExperimentPublicCountTotal);
      usageStats.setPublishedExperimentPrivateCountTotal(publishedExperimentPrivateCountTotal);
      usageStats.setPublishedExperimentFutureCountTotal(publishedExperimentFutureCountTotal);
      usageStats.setPublishedExperimentPastCountTotal(publishedExperimentPastCountTotal);
      usageStats.setPublishedExperimentPresentCountTotal(publishedExperimentPresentCountTotal);
      usageStats.setPublishedExperimentOngoingCountTotal(publishedExperimentOngoingCountTotal);
      usageStats.setPublishedExperimentPrivateUserCountsTotal(publishedExperimentPrivateUserCountsTotal);

      usageStats.setPublishedExperimentPublicCountNonPilotTotal(publishedExperimentPublicCountNonPilotTotal);
      usageStats.setPublishedExperimentPrivateCountNonPilot(publishedExperimentPrivateCountNonPilot);
      usageStats.setPublishedExperimentFutureCountNonPilot(publishedExperimentFutureCountNonPilot);
      usageStats.setPublishedExperimentPastCountNonPilot(publishedExperimentPastCountNonPilot);
      usageStats.setPublishedExperimentPresentCountNonPilot(publishedExperimentPresentCountNonPilot);
      usageStats.setPublishedExperimentOngoingCountNonPilot(publishedExperimentOngoingCountNonPilot);
      usageStats.setPublishedExperimentPrivateUserCountsNonPilot(publishedExperimentPrivateUserCountsNonPilot);


//    usageStats.setDomainExperimentModifiedAges(lastModifiedAge);
    }
    return usageStats;
  }

  private boolean isNonPilotExperiment(ExperimentDAO experimentDAO) {
    if (experimentDAO.getPublished() && experimentDAO.getPublishedUsers() != null && experimentDAO.getPublishedUsers().size() >= 5) {
      return true;
    }

    //TODO add more heuristics that non-pilot experiments possess
    return false;
  }
}
