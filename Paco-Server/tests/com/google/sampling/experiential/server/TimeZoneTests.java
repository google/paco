package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

public class TimeZoneTests {

  @Test
  public void testIdempotenceOfTimeZones() {
    DateTime t = new DateTime();
    DateTimeZone zone = t.getZone();
    DateTime tWithZone = new DateTime(t).withZone(zone);
    assertEquals(t, tWithZone);
  }

}
