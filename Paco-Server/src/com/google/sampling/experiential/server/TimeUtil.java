package com.google.sampling.experiential.server;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.sampling.experiential.shared.EventDAO;

public class TimeUtil {

  public static final Logger log = Logger.getLogger(TimeUtil.class.getName());
  public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ssZ";
  public static final String DATETIME_FORMAT_MS = "yyyy/MM/dd HH:mm:ss.SSSZ";
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
    Locale clientLocale = null;
    if (req != null) {
      String tzStr = req.getParameter("tz"); // don't urldecode this as it
                                             // always gets decoded properly..
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
      clientLocale = req.getLocale();
    }
    if (clientLocale == null) {
      clientLocale = Locale.getDefault();
    }
    Calendar calendar = Calendar.getInstance(clientLocale);
    TimeZone clientTimeZone = calendar.getTimeZone();
    DateTimeZone jodaTimeZone = DateTimeZone.forTimeZone(clientTimeZone);
    return jodaTimeZone;

  }
  
  public static DateTime parseDate(DateTimeFormatter df, String when) throws ParseException {
    return df.parseDateTime(when);
  }

  public static Date adjustTimeToTimezoneIfNecesssary(String tz, Date dateObj) {
    if (dateObj == null) {
      return null;
    }
    DateTimeZone timezone = null;
    if (tz != null) {
      timezone = DateTimeZone.forID(tz);
    }
    if (timezone != null && dateObj.getTimezoneOffset() != timezone.getOffset(dateObj.getTime())) {
      dateObj = new DateTime(dateObj).withZone(timezone).toDate();
    }
    return dateObj;
  }
  
  public static int getFractionalSeconds(Timestamp tStamp) {
    int fracSeconds = 0;
    if (tStamp.getNanos() >= 1000000) {
      fracSeconds = tStamp.getNanos() / 1000000;
    }
    return fracSeconds;
  }
  
  public static Integer getIntFromOffsetString(String timeZone) {
    String hours = timeZone.substring(0,3);
    if (hours.startsWith("+")) {
      hours = hours.substring(1);
    }

    int parseInt;
    try {
      parseInt = Integer.parseInt(hours);
    } catch (NumberFormatException e) {
      log.warning("Timezone hours are not an integer this event: " + hours);
      return 0; 
    }
    return parseInt;
  }
  
  public static void adjustTimeZone(EventDAO event) throws ParseException {
    int offsetHrs = 0;
    if (event.getTimezone() != null) {
      offsetHrs = TimeUtil.getIntFromOffsetString(event.getTimezone());
    }
    
    if (event.getScheduledTime() != null) {
      event.setScheduledTime(event.getScheduledTime().withZoneRetainFields(DateTimeZone.forOffsetHours(offsetHrs)));
    }
    if (event.getResponseTime() != null) {
      event.setResponseTime(event.getResponseTime().withZoneRetainFields(DateTimeZone.forOffsetHours(offsetHrs)));
    }
    if (event.getSortDate() != null) {
      event.setSortDate(event.getSortDate().withZoneRetainFields(DateTimeZone.forOffsetHours(offsetHrs)));
    }
  }
}
