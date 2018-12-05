package com.google.sampling.experiential.server.stats.participation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;


public class ParticipationStatsServiceTest {

  private String experimentGroupName = "default";
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }


  @Test
  public void testGetExperimentTotalsForEachParticipant() {
    DateTime date = new DateTime();
    long experimentId = 1l;

    ParticipationStatsService participationStatsService = new ParticipationStatsService();
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);


    ResponseStat expectedResponseStat = new ResponseStat(experimentId, experimentGroupName,  "bob", null, 1, 0, 0);

    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getTotalByParticipant(experimentId);
    ResponseStat actual = experimentTotalsForEachParticipant.get(0);

    assertEquals(1, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStat, actual);
  }

  @Test
  public void testGetExperimentTotalsForEachParticipant_OnePersonMultipleDays() {
    DateTime date = new DateTime();
    long experimentId = 1l;

    ParticipationStatsService participationStatsService = new ParticipationStatsService();
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(2));
    participationStatsService.updateSelfResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(3));

    ResponseStat expectedResponseStat = new ResponseStat(experimentId, experimentGroupName,  "bob", null, 3, 1, 1);

    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getTotalByParticipant(experimentId);
    ResponseStat actual = experimentTotalsForEachParticipant.get(0);

    assertEquals(1, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStat, actual);
  }

  @Test
  public void testGetExperimentTotalsForEachParticipant_TwoPeopleMultipleDays() {
    DateTime date = new DateTime();
    long experimentId = 1l;

    ParticipationStatsService participationStatsService = new ParticipationStatsService();
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(2));
    participationStatsService.updateSelfResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(3));

    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(3));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(5));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(2));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(10));
    participationStatsService.updateSelfResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateSelfResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(3));

    ResponseStat expectedResponseStatBob = new ResponseStat(experimentId, experimentGroupName,  "bob", null, 3, 1, 1);
    ResponseStat expectedResponseStatSteve = new ResponseStat(experimentId, experimentGroupName,  "steve", null, 5, 3, 2);


    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getTotalByParticipant(experimentId);
    ResponseStat actualBob = experimentTotalsForEachParticipant.get(0);
    ResponseStat actualSteve = experimentTotalsForEachParticipant.get(1);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBob, actualBob);
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatSteve, actualSteve);
  }

  @Test
  public void testGetExperimentTotalsForEachParticipantOnDay() {
    DateTime date = new DateTime();
    DateTime dateOfInterest = date;
    long experimentId = 1l;

    ParticipationStatsService participationStatsService = new ParticipationStatsService();

    // add response stats
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));

    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(1));

    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getTotalByParticipantOnDate(experimentId, dateOfInterest);

    ResponseStat expectedResponseStatBob = new ResponseStat(experimentId, experimentGroupName,  "bob", date, 1, 0, 0);
    ResponseStat expectedResponseStatSteve = new ResponseStat(experimentId, experimentGroupName,  "steve", date, 0, 1, 0);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBob, experimentTotalsForEachParticipant.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatSteve, experimentTotalsForEachParticipant.get(1));


    List<ResponseStat> experimentTotalsForEachParticipant2 = participationStatsService.getTotalByParticipantOnDate(experimentId, dateOfInterest.plusDays(1));

    ResponseStat expectedResponseStatBob2 = new ResponseStat(experimentId, experimentGroupName,  "bob", date.plusDays(1), 1, 0, 0);
    ResponseStat expectedResponseStatSteve2 = new ResponseStat(experimentId, experimentGroupName,  "steve", date.plusDays(1), 0, 1, 0);

    assertEquals(2, experimentTotalsForEachParticipant2.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBob2, experimentTotalsForEachParticipant2.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatSteve2, experimentTotalsForEachParticipant2.get(1));

  }

  @Test
  public void testGetDailyTotalsForParticipant() {
    DateTime date = new DateTime();
    long experimentId = 1l;
    String who = "bob";


    ParticipationStatsService participationStatsService = new ParticipationStatsService();

    // add response stats
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));

    participationStatsService.updateMissedResponseCountForWho(experimentId, "group2", "bob", date);
    participationStatsService.updateSelfResponseCountForWho(experimentId, "group2", "bob", date.plusDays(1));

    // add some extraneous data
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date);


    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getDailyTotalsForParticipant(experimentId, who);

    ResponseStat expectedResponseStatBobFirstDay = new ResponseStat(experimentId, experimentGroupName,  "bob", date, 1, 1, 0);
    ResponseStat expectedResponseStatBobSecondDay = new ResponseStat(experimentId, experimentGroupName,  "bob", date.plusDays(1), 1, 0, 1);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobFirstDay, experimentTotalsForEachParticipant.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobSecondDay, experimentTotalsForEachParticipant.get(1));
  }


  @Test
  public void testGetExperimentTotalsForEachDayForParticipant_MultipleExperimentGroups() {
    DateTime date = new DateTime();
    long experimentId = 1l;
    String who = "bob";


    ParticipationStatsService participationStatsService = new ParticipationStatsService();

    // add response stats
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    // this is the important part - it is a different experimentGroup
    participationStatsService.updateScheduledResponseCountForWho(experimentId, "group2", "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, "group2", "bob", date.plusDays(1));

    // add some extraneous data
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date);


    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getDailyTotalsForParticipant(experimentId, who);
    ResponseStat expectedResponseStatBobFirstDay = new ResponseStat(experimentId, null,  "bob", date, 2, 0, 0);
    ResponseStat expectedResponseStatBobSecondDay = new ResponseStat(experimentId, null,  "bob", date.plusDays(1), 1, 0, 0);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobFirstDay, experimentTotalsForEachParticipant.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobSecondDay, experimentTotalsForEachParticipant.get(1));
  }

  @Test
  public void testGetExperimentTotalsForEachParticipantForOneGroup() {
    DateTime date = new DateTime();
    String experimentGroupName2 = "group2";
    long experimentId = 1l;

    ParticipationStatsService participationStatsService = new ParticipationStatsService();

    // add response stats for experimentGroupName and bob on two days
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));

    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName2, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName2, "bob", date.plusDays(1));

    // add response stats for experimentGroupName and steve on two days
    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName, "steve", date);
    participationStatsService.updateSelfResponseCountForWho(experimentId, experimentGroupName, "steve", date.plusDays(1));

    participationStatsService.updateMissedResponseCountForWho(experimentId, experimentGroupName2, "steve", date.plusDays(1));
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName2, "steve", date.plusDays(1));


    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getTotalByParticipantForGroup(experimentId, experimentGroupName);

    ResponseStat expectedResponseStatBob = new ResponseStat(experimentId, experimentGroupName,  "bob", null, 2, 1, 0);

    ResponseStat expectedResponseStatSteve = new ResponseStat(experimentId, experimentGroupName,  "steve", null, 0, 1, 1);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBob, experimentTotalsForEachParticipant.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatSteve, experimentTotalsForEachParticipant.get(1));


  }


  /**
   * retrieve a list of stats, one per day for a participant for a particular experiment group
   */
  @Test
  public void testGetExperimentTotalForEachDayForOneParticipantForOneGroup() {
    long experimentId = 1l;
    DateTime date = new DateTime();


    ParticipationStatsService participationStatsService = new ParticipationStatsService();

    // add response stats
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date);
    participationStatsService.updateScheduledResponseCountForWho(experimentId, "group2", "bob", date);

    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "bob", date.plusDays(1));
    participationStatsService.updateMissedResponseCountForWho(experimentId, "group2", "bob", date.plusDays(1));

    // add some extraneous data
    participationStatsService.updateScheduledResponseCountForWho(experimentId, experimentGroupName, "steve", date);


    List<ResponseStat> experimentTotalsForEachParticipant = participationStatsService.getDailyTotalsForParticipantForGroup(experimentId,
        experimentGroupName, "bob");

    ResponseStat expectedResponseStatBobGroup1FirstDay = new ResponseStat(experimentId, experimentGroupName,  "bob", date, 2, 0, 0);
    ResponseStat expectedResponseStatBobSecondDay = new ResponseStat(experimentId, "group2",  "bob", date.plusDays(1), 1, 0, 0);

    assertEquals(2, experimentTotalsForEachParticipant.size());
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobGroup1FirstDay, experimentTotalsForEachParticipant.get(0));
    ResponseStatTest.assertExpectedResponseStat(expectedResponseStatBobSecondDay, experimentTotalsForEachParticipant.get(1));
  }


}
