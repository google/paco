package com.google.sampling.experiential.server.stats.participation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;


public class ResponseStatTest {
  private String experimentGroupName = "default";

  @Test
  public void testCompareToWithEqualFullyConfiguredObject() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat bobJan1 = new ResponseStat(1, experimentGroupName,  "bob", date1, 0, 0, 0);
    ResponseStat bobJan1_2 = new ResponseStat(1, experimentGroupName,  "bob", date1, 0, 0, 0);
    assertEquals(0, bobJan1.compareTo(bobJan1_2));
  }
  
  @Test
  public void testCompareToWithDateDifferingFullyConfiguredObject() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat bobJan1 = new ResponseStat(1, experimentGroupName,  "bob", date1, 0, 0, 0);
    ResponseStat bobJan2 = new ResponseStat(1, experimentGroupName,  "bob", date1.plusDays(1), 0, 0, 0);
    assertEquals(-1, bobJan1.compareTo(bobJan2));
  }

  @Test
  public void testCompareToWithDifferentWhoAndDateEqualFullyConfiguredObject() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat bobJan1 = new ResponseStat(1, experimentGroupName,  "bob", date1, 0, 0, 0);
    ResponseStat steveJan1 = new ResponseStat(1, experimentGroupName,  "steve", date1, 0, 0, 0);
    assertTrue(bobJan1.compareTo(steveJan1) < 0);
  }

  @Test
  public void testCompareToWithDifferentWhoAndDateDifferingFullyConfiguredObject() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat bobJan1 = new ResponseStat(1, experimentGroupName,  "bob", date1, 0, 0, 0);
    ResponseStat steveJan2 = new ResponseStat(1, experimentGroupName,  "steve", date1.plusDays(1), 0, 0, 0);
    assertTrue(bobJan1.compareTo(steveJan2) < 0);
  }
  
  @Test
  public void testCompareToEqualNoDate() {
    ResponseStat bob1 = new ResponseStat(1, experimentGroupName,  "bob", null, 0, 0, 0);
    ResponseStat bob2 = new ResponseStat(1, experimentGroupName,  "bob", null, 0, 0, 0);
    assertEquals(0, bob1.compareTo(bob2));
  }
  
  @Test
  public void testCompareToWhoDifferingNoDate() {
    ResponseStat bob = new ResponseStat(1, experimentGroupName,  "bob", null, 0, 0, 0);
    ResponseStat steve = new ResponseStat(1, experimentGroupName,  "steve", null, 0, 0, 0);
    assertTrue(bob.compareTo(steve) < 0);
  }

  @Test
  public void testCompareToSameDateNoWho() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat stat1 = new ResponseStat(1, experimentGroupName,  null, date1, 0, 0, 0);
    ResponseStat stat2 = new ResponseStat(1, experimentGroupName,  null, date1, 0, 0, 0);
    assertTrue(stat1.compareTo(stat2) == 0);
  }

  @Test
  public void testCompareToDateDiffereingNoWho() {
    DateTime date1 = new DateTime(2016,1,1, 0, 0);
    ResponseStat stat1 = new ResponseStat(1, experimentGroupName,  null, date1, 0, 0, 0);
    ResponseStat stat2 = new ResponseStat(1, experimentGroupName,  null, date1.plusDays(1), 0, 0, 0);
    assertTrue(stat1.compareTo(stat2) < 0);
  }
  
  @Test
  public void testCompareToNoDateNoWho() {
    ResponseStat stat1 = new ResponseStat(1, experimentGroupName,  null, null, 0, 0, 0);
    ResponseStat stat2 = new ResponseStat(1, experimentGroupName,  null, null, 0, 0, 0);
    assertTrue(stat1.compareTo(stat2) == 0);
  }
  
  @Test
  public void testCompareToNoDateNoWhoDifferingStats() {
    ResponseStat stat1 = new ResponseStat(1, experimentGroupName,  null, null, 1, 1, 1);
    ResponseStat stat2 = new ResponseStat(1, experimentGroupName,  null, null, 0, 0, 0);
    assertTrue(stat1.compareTo(stat2) == 0);
  }

  /**
   * Helper method to compare the stats in two ResponseStats
   * 
   * @param expectedStat
   * @param actualStat
   */
  public static void assertExpectedResponseStat(ResponseStat expectedStat, ResponseStat actualStat) {
    assertEquals(expectedStat.experimentId, actualStat.experimentId);
    assertEquals(expectedStat.date, actualStat.date);
    assertEquals(expectedStat.schedR, actualStat.schedR);
    assertEquals(expectedStat.missedR, actualStat.missedR);
    assertEquals(expectedStat.selfR, actualStat.selfR);
  }

}
