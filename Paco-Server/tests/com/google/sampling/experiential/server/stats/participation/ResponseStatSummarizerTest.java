package com.google.sampling.experiential.server.stats.participation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Lists;



public class ResponseStatSummarizerTest {
  private String experimentGroupName = "default";
  
//  @Test
//  public void testSummingOneResult() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", null, 1, 0, 0);
//    stats.add(responseStat);
//    ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStat(stats);  
//    
//    ResponseStatTest.assertExpectedResponseStat(responseStat, total);
//  }
//  
//  @Test
//  public void testSummingTwoResult() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    stats.add(new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0));
//    stats.add(new ResponseStat(1, experimentGroupName,  "steve", date1, 1, 0, 0));
//    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  null, null, 2, 0, 0);
//    ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStat(stats);
//    
//    ResponseStatTest.assertExpectedResponseStat(expectedStat, total);    
//  }
//
//  @Test
//  public void testSummingTwoResultsDifferingStats() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    stats.add(new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0));
//    stats.add(new ResponseStat(1, experimentGroupName,  "steve", date1, 0, 1, 1));
//    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  null, null, 1, 1, 1);
//    ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStat(stats);
//    
//    ResponseStatTest.assertExpectedResponseStat(expectedStat, total);    
//  }
//  
//  @Test
//  public void testSummingTwoResultsDifferingDates() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    stats.add(new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0));
//    stats.add(new ResponseStat(1, experimentGroupName,  "steve", date1.plusDays(1), 0, 1, 1));
//    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  null, null, 1, 1, 1);
//    ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStat(stats);
//    
//    ResponseStatTest.assertExpectedResponseStat(expectedStat, total);            
//  }
//  
//  @Test
//  public void testSummingTwoResultsDifferingExperiments() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    stats.add(new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0));
//    stats.add(new ResponseStat(2, "default", "steve", date1, 0, 1, 1));
//    try {
//      ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStat(stats);
//      
//      fail("should have thrown multiple experiment error");
//      
//    } catch (IllegalArgumentException iae) {
//      // success
//    }        
//  }
//  
  
  @Test
  public void testSummingOneResultByWho() {
    List<ResponseStat> stats = Lists.newArrayList();
    DateTime localDateWithZone = new DateTime();
    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", null, 1, 0, 0);
    stats.add(responseStat);
    List<ResponseStat> total = new ResponseStatSummarizer().totalExperimentResponseStatsByWho(stats, null);
    
    assertEquals(1, total.size());
    
    ResponseStat actualStat = total.get(0);
    ResponseStatTest.assertExpectedResponseStat(responseStat, actualStat);
    assertEquals(responseStat.who, actualStat.who);
  }
  
  @Test
  public void testSummingTwoResultByWho() {
    List<ResponseStat> stats = Lists.newArrayList();
    DateTime localDateWithZone = new DateTime();
    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0);
    ResponseStat responseStat2 = new ResponseStat(1, experimentGroupName,  "bob", date1.plusDays(1), 0, 1, 1);
    stats.add(responseStat);
    stats.add(responseStat2);
    
    List<ResponseStat> total = new ResponseStatSummarizer().totalExperimentResponseStatsByWho(stats, null);
    
    assertEquals(1, total.size());
    ResponseStat actualStat = total.get(0);
    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  "bob", null, 1,1,1);
    
    ResponseStatTest.assertExpectedResponseStat(expectedStat, actualStat);
    assertEquals(responseStat.who, actualStat.who);
  }

  @Test
  public void testSummingTwoResultTwoPeopleByWho() {
    List<ResponseStat> stats = Lists.newArrayList();
    DateTime localDateWithZone = new DateTime();
    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0);
    ResponseStat responseStat2 = new ResponseStat(1, experimentGroupName,  "steve", date1.plusDays(1), 0, 1, 1);
    stats.add(responseStat);
    stats.add(responseStat2);
    
    List<ResponseStat> total = new ResponseStatSummarizer().totalExperimentResponseStatsByWho(stats, null);
    
    assertEquals(2, total.size());
    // check p1
    ResponseStat actualStat = total.get(0);
    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  "bob", null, 1, 0, 0);
    
    ResponseStatTest.assertExpectedResponseStat(expectedStat, actualStat);
    assertEquals(responseStat.who, actualStat.who);
    
 // check p1
    ResponseStat actualStat2 = total.get(1);
    ResponseStat expectedStat2 = new ResponseStat(1, experimentGroupName,  "steve", null, 0, 1, 1);
    
    ResponseStatTest.assertExpectedResponseStat(expectedStat2, actualStat2);
    assertEquals(responseStat2.who, actualStat2.who);
  }

  
  @Test
  public void testSummingTwoResultPerTwoPeopleByWho() {
    List<ResponseStat> stats = Lists.newArrayList();
    DateTime localDateWithZone = new DateTime();
    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0);
    stats.add(responseStat);
    ResponseStat responseStatBob2 = new ResponseStat(1, experimentGroupName,  "bob", date1.plusDays(1), 1, 4, 8);
    stats.add(responseStatBob2);
    
    ResponseStat responseStat2 = new ResponseStat(1, experimentGroupName,  "steve", date1, 0, 1, 1);    
    stats.add(responseStat2);
    ResponseStat responseStatSteve2 = new ResponseStat(1, experimentGroupName,  "steve", date1.plusDays(1), 3, 0, 0);
    stats.add(responseStatSteve2);
    
    List<ResponseStat> total = new ResponseStatSummarizer().totalExperimentResponseStatsByWho(stats, null);
    
    assertEquals(2, total.size());
    // check p1
    ResponseStat actualStat = total.get(0);
    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  "bob", null, 2, 4, 8);
    
    ResponseStatTest.assertExpectedResponseStat(expectedStat, actualStat);
    assertEquals(responseStat.who, actualStat.who);
    
 // check p1
    ResponseStat actualStat2 = total.get(1);
    ResponseStat expectedStat2 = new ResponseStat(1, experimentGroupName,  "steve", null, 3, 1, 1);
    
    ResponseStatTest.assertExpectedResponseStat(expectedStat2, actualStat2);
    assertEquals(responseStat2.who, actualStat2.who);
  }

//  @Test
//  public void testSummingTwoResultPerTwoPeopleOnDate() {
//    List<ResponseStat> stats = Lists.newArrayList();
//    DateTime localDateWithZone = new DateTime();
//    DateTime date1 = LocalToUTCTimeZoneConverter.changeZoneToUTC(localDateWithZone);
//    ResponseStat responseStat = new ResponseStat(1, experimentGroupName,  "bob", date1, 1, 0, 0);
//    stats.add(responseStat);
//    
//    ResponseStat responseStat2 = new ResponseStat(1, experimentGroupName,  "steve", date1, 0, 1, 1);    
//    stats.add(responseStat2);
//    
//    ResponseStat total = new ResponseStatSummarizer().totalExperimentResponseStatsOnDate(stats);
//    
//    assertNotNull(total);
//    ResponseStat expectedStat = new ResponseStat(1, experimentGroupName,  null, date1, 1, 1, 1);    
//    ResponseStatTest.assertExpectedResponseStat(expectedStat, total);
//    assertEquals(null, total.who);
//    assertEquals(date1, total.date);
//    
//  }



}
