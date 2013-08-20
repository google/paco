package com.google.sampling.experiential.server;


import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TimeZoneTests  extends TestCase {

  public void testIdempotenceOfTimeZones() {
    DateTime t = new DateTime();
    DateTimeZone zone = t.getZone();
    DateTime tWithZone = new DateTime(t).withZone(zone);
    assertEquals(t, tWithZone);
  }

}
