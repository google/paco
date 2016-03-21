package com.google.sampling.experiential.server.stats.usage;

import java.io.IOException;
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
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.scheduling.ActionScheduleGenerator;
import com.pacoapp.paco.shared.util.ExperimentHelper;

/**
 * This class is called by a scheduled Task weekly and adds a new stats row into the 
 * usage_stats table
 * 
 * 
 *
 */
public class UsageStatsCronJob {

  private static final Logger log = Logger.getLogger(UsageStatsCronJob.class.getName());
  private String adminDomainSystemSetting;

  public UsageStatsCronJob() {
  }

  public void run() throws IOException {
    log.info("writing usage stats");    
    loadAdminDomainSetting();
    
    // part 1 = event stats
    
    //TODO split out query of events to compute sub score for domain-specific count
    // currently we only show the total;
    Long numberOfEvents = getTotalEventCount(); 
    
    //part 3 participant stats
    Long totalParticipantsJoined = ExperimentAccessManager.getTotalJoinedParticipantsCount();
    
    // part 2 experiment stats
    ExperimentQueryResult experimentsQueryResults = ExperimentServiceFactory.getExperimentService().getAllExperiments(null);    
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();
    
    
    if (experimentList != null) {      
      DateTime dateTime = DateTime.now();
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
