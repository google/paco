package com.google.sampling.experiential.server.stats.participation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;


public class LocalToUTCTimeZoneConverterTest {

  @Test
  public void testConvertLocalToUTC_PreservingHoursMinutes() {
    DateTimeZone participantTimeZone = DateTimeZone.forID("America/New_York");
    DateTime localParticipantTime = new DateTime(participantTimeZone);
    localParticipantTime = localParticipantTime
        .withYear(2016)
        .withMonthOfYear(1)
        .withDayOfMonth(5)
        .withHourOfDay(10)
        .withMinuteOfHour(25);
    
    DateTime timeWithUTCZone = LocalToUTCTimeZoneConverter.changeZoneToUTC(localParticipantTime);
    assertNotNull(timeWithUTCZone);
    assertNotEquals(localParticipantTime.getZone(), timeWithUTCZone.getZone());
    assertEquals(localParticipantTime.getYear(), timeWithUTCZone.getYear());
    assertEquals(localParticipantTime.getMonthOfYear(), timeWithUTCZone.getMonthOfYear());
    assertEquals(localParticipantTime.getDayOfMonth(), timeWithUTCZone.getDayOfMonth());
    assertEquals(localParticipantTime.getHourOfDay(), timeWithUTCZone.getHourOfDay());
    assertEquals(localParticipantTime.getMinuteOfHour(), timeWithUTCZone.getMinuteOfHour());
    assertEquals(localParticipantTime.getSecondOfMinute(), timeWithUTCZone.getSecondOfMinute());
    
  }

}
