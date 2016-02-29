package com.google.sampling.experiential.server.stats.participation;

import java.util.List;

import org.joda.time.DateTime;

/**
 * This class is a frontend to all the stats requests about participation and the
 * stats updates for answering those requests.
 * 
 * We have this because we might change the backend system for computing these stats. 
 * We probably will.
 * 
 */
public class ParticipationStatsService {
  

  /**
   * Increment the scheduled responses count for an individual in an experiment on a particular date.
   * @param experimentId - the experiment for which this was a response
   * @param experimentGroupName - the group for which this was a signal
   * @param who - the participant
   * @param date - the date the event is for (either response time or scheduled time)
   */
  public void updateScheduledResponseCountForWho(Long experimentId, String experimentGroupName, String who, DateTime date) {
    new ResponseStatEntityManager().updateScheduledResponseCountForWho(experimentId, experimentGroupName, who, date);
  }

  /**
   * Increment the missed responses count for an individual in an experiment on a particular date.
   * @param experimentId - the experiment for which this was a response
   * @param experimentGroupName - the group for which this was a signal
   * @param who - the participant
   * @param date - the date the event is for (either response time or scheduled time)
   */
  public void updateMissedResponseCountForWho(Long experimentId, String experimentGroupName, String who, DateTime date) {
    new ResponseStatEntityManager().updateMissedResponseCountForWho(experimentId, experimentGroupName, who, date);
  }
  
  /**
   * Increment the self responses count for an individual in an experiment on a particular date.
   * @param experimentId - the experiment for which this was a response
   * @param experimentGroupName - the group for which this was a signal
   * @param who - the participant
   * @param date - the date the event is for (either response time or scheduled time)
   */
  public void updateSelfResponseCountForWho(Long experimentId, String experimentGroupName, String who, DateTime date) {
    new ResponseStatEntityManager().updateSelfResponseCountForWho(experimentId, experimentGroupName, who, date);
  }


  /**
   * All participants total
   * 
   * returns List of ResponseStats, one for each participant, containing their overall responseRate 
   * 
   * @param experimentId
   * @return
   */
  public List<ResponseStat> getTotalByParticipant(Long experimentId) {
    List<ResponseStat> responseStatsByParticipant = new ResponseStatEntityManager().getResponseStatsForExperiment(experimentId);
    return new ResponseStatSummarizer().totalExperimentResponseStatsByWho(responseStatsByParticipant, null);
  }
  
  /**
   * All participants total
   * 
   * Returns total stat for each participant for experimentGroup
   * 
   * @param experimentId
   * @param experimentGroupName
   * @return
   */
  public List<ResponseStat> getTotalByParticipantForGroup(long experimentId, String experimentGroupName) {
    List<ResponseStat> responseStatsByParticipant = new ResponseStatEntityManager().getResponseStatsForExperimentGroup(experimentId, experimentGroupName);
    return new ResponseStatSummarizer().totalExperimentResponseStatsByWho(responseStatsByParticipant, experimentGroupName, null);
  }


  
  /**
   * 
   * All participants stats for date
   * 
   * Returns List of ResponseStats, one for each participant, with their responseRate for the given date.
   * 
   * @param experimentId
   * @param date
   * @return
   */
  public List<ResponseStat> getTotalByParticipantOnDate(Long experimentId, DateTime date) {
    List<ResponseStat> stat = new ResponseStatEntityManager().getResponseStatsForExperimentOnDate(experimentId, date);
    return new ResponseStatSummarizer().totalExperimentResponseStatsByWho(stat, date);
  }
  
  /**
   * All participants stats for date
   * 
   * Returns List of ResponseStats, one for each participant, with their responseRate for the given date, for a given group.
   * 
   * @param experimentId
   * @param experimentGroupName
   * @param date
   * @return
   */
  public List<ResponseStat> getTotalByParticipantOnDateForGroup(Long experimentId, String experimentGroupName, DateTime date) {
    List<ResponseStat> responseStatsByParticipant = new ResponseStatEntityManager().getResponseStatsForExperimentGroupOnDate(experimentId, 
        experimentGroupName, date);
    return responseStatsByParticipant;
  }
  
  
  
  /**
   * 
   * participant detail stats
   * 
   * Returns List of ResponseStats, one for each day, for given participant.
   * 
   * @param experimentId
   * @param participant
   * @return
   */
  public List<ResponseStat> getDailyTotalsForParticipant(Long experimentId, String participant) {
    List<ResponseStat> responseStatsForWho = new ResponseStatEntityManager().getResponseStatsForParticipant(experimentId, participant);
    return new ResponseStatSummarizer().dailyTotalsByWho(responseStatsForWho);
  }

/**
 * participant detail stats
 * 
 * Returns a list of ResponseStats for each day, for one group, for one participant
 * @param experimentId
 * @param experimentGroupName
 * @param who
 * @return
 */
  public List<ResponseStat> getDailyTotalsForParticipantForGroup(long experimentId, String experimentGroupName, String who) {
    return new ResponseStatEntityManager().getResponseStatsForParticipantForGroup(experimentId, experimentGroupName, who);
  }


}
