package com.google.android.apps.paco;

import org.joda.time.DateTime;

import com.google.android.apps.paco.TimeUtil;

import junit.framework.TestCase;

public class TimeUtilTest extends TestCase {

  public void testSkipWeekendDoesntSkipFriday() throws Exception {
    DateTime friday = new DateTime(2012, 3, 23, 0, 0, 0, 0);
    assertFalse(TimeUtil.isWeekend(friday));
    assertEquals(friday, TimeUtil.skipWeekends(friday));    
  }
  
  public void testSkipWeekendSkipsSaturday() throws Exception {
    DateTime saturday = new DateTime(2012, 3, 24, 0, 0, 0, 0);
    assertTrue(TimeUtil.isWeekend(saturday));
    DateTime monday = saturday.plusDays(2);
    assertEquals(monday, TimeUtil.skipWeekends(saturday));
  }
  
  public void testSkipWeekendSkipsSunday() throws Exception {
    DateTime sunday = new DateTime(2012, 3, 25, 0, 0, 0, 0);
    assertTrue(TimeUtil.isWeekend(sunday));
    DateTime monday = sunday.plusDays(1);
    assertEquals(monday, TimeUtil.skipWeekends(sunday));
  }

}
