package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.sampling.experiential.shared.TimeUtil;

public class TimeZoneTests {

  @Test
  public void testIdempotenceOfTimeZones() {
    DateTime t = new DateTime();
    DateTimeZone zone = t.getZone();
    DateTime tWithZone = new DateTime(t).withZone(zone);
    assertEquals(t, tWithZone);
  }

  @Test
  public void testDateTimeEventQueryParsing() throws Exception {
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(TimeUtil.DATETIME_FORMAT_EVENT_QUERY);
    DateTime parsedTime = dateTimeFormatter.parseDateTime("2016-04-21T13:00:00-08:00");
  }
}
