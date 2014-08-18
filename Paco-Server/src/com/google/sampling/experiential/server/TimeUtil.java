package com.google.sampling.experiential.server;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimeUtil {

  public static final Logger log = Logger.getLogger(TimeUtil.class.getName());
  public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ssZ";
  public static final String DATE_FORMAT = "yyyy/MM/dd";

  public static DateMidnight getDateMidnightForDateString(String dateStr) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_FORMAT);
    return new DateMidnight(formatter.parseDateTime(dateStr));
  }

  public static DateTime getNowInUserTimezone(DateTimeZone dateTimeZone) {
    DateTime datetime = new DateTime();
    return TimeUtil.adjustToTimezone(dateTimeZone, datetime);
  }

  public static DateTime getNowInUserTimezone(String dateTimeZoneId) {
    DateTimeZone dateTimeZone = DateTimeZone.forID(dateTimeZoneId);
    DateTime datetime = new DateTime();
    return TimeUtil.adjustToTimezone(dateTimeZone, datetime);
  }

  private static DateTime adjustToTimezone(DateTimeZone dateTimeZone, DateTime datetime) {
    if (dateTimeZone != null) {
      return datetime.withZone(dateTimeZone);
    }
    return datetime;
  }

  public static DateTime adjustToTimezone(String tz, DateTime datetime) {
    if (tz != null) {
      DateTimeZone timeZone = DateTimeZone.forID(tz);
      if (timeZone != null) {
        return datetime.withZone(timeZone);
      }
    }
    return datetime;
  }

  public static DateTimeZone getTimeZoneForClient(HttpServletRequest req) {
    String tzStr = req.getParameter("tz"); // don't urldecode this as it always gets decoded properly..
    if (tzStr != null && !tzStr.isEmpty()) {
      DateTimeZone jodaTimeZone = null;
      try {
        jodaTimeZone = DateTimeZone.forID(tzStr);
        if (jodaTimeZone != null) {
          return jodaTimeZone;
        }
      } catch (IllegalArgumentException e) {
        log.severe("Could not parse timezone: " + tzStr);
      }
    }

    Locale clientLocale = req.getLocale();
    Calendar calendar = Calendar.getInstance(clientLocale);
    TimeZone clientTimeZone = calendar.getTimeZone();
    DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(clientTimeZone);
    return jodaTimeZone;

  }

}
