package com.google.sampling.experiential.server.stats.participation;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class LocalToUTCTimeZoneConverter {

  public static DateTime changeZoneToUTC(DateTime localParticipantTime) {
    return localParticipantTime.withZoneRetainFields(DateTimeZone.UTC);
  }

}
