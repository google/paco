/*
* Copyright 2011 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.pacoapp.paco.shared.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Strings;


public class TimeUtil {

  private static DateTimeFormatter timeFormatter = ISODateTimeFormat.time();

  public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ssZ";
  public static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);

  public static final String DATE_LONG_FORMAT = "MMMM dd, yyyy";
  private static DateTimeFormatter dateLongFormatter = DateTimeFormat.forPattern(DATE_LONG_FORMAT);

  public static final String DATETIME_NOZONE_FORMAT = "yyyy/MM/dd hh:mm:ssa";
  public static DateTimeFormatter dateTimeNoZoneFormatter = DateTimeFormat.forPattern(DATETIME_NOZONE_FORMAT);

  public static final String DATETIME_NOZONE_SHORT_FORMAT = "yy/MM/dd HH:mm";
  public static DateTimeFormatter dateTimeNoZoneShortFormatter = DateTimeFormat.forPattern(DATETIME_NOZONE_SHORT_FORMAT);

  public static final String DATE_FORMAT = "yyyy/MM/dd";
  public static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(DATE_FORMAT);

  public static final String DATE_WITH_ZONE_FORMAT = "yyyy/MM/ddZ";
  private static DateTimeFormatter dateZoneFormatter = DateTimeFormat.forPattern(DATE_WITH_ZONE_FORMAT);

  public static final String DATE_TIME_WITH_NO_TZ = "yyyy/MM/dd HH:mm:ss";
  public static DateTimeFormatter dateTimeWithNoTzFormatter = DateTimeFormat.forPattern(DATE_TIME_WITH_NO_TZ);
  public static SimpleDateFormat localFormatter = new SimpleDateFormat (DATE_TIME_WITH_NO_TZ);

  public static final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("hh:mma");
  private static final Logger log = Logger.getLogger(TimeUtil.class.getName());

  private TimeUtil() {
    super();
  }

  public static String formatTime(long dateTimeMillis) {
    return new DateTime(dateTimeMillis).toString(timeFormatter);
  }

  public static String formatDateTime(long dateTimeMillis) {
    return new DateTime(dateTimeMillis).toString(dateTimeFormatter);
  }

  public static String formatDateTime(DateTime dateTime) {
    return dateTime.toString(dateTimeFormatter);
  }

  public static String formatDateLong(DateTime dateTime) {
    return dateTime.toString(dateLongFormatter);
  }

  public static String formatDateTimeShortNoZone(DateTime dateTime) {
    return dateTime.toString(dateTimeNoZoneShortFormatter);
  }

  public static DateTime parseDateTime(String dateTimeStr) {
    return dateTimeFormatter.parseDateTime(dateTimeStr);
  }

  public static long convertDateToLong(String dateTimeStr) {
    DateTime dt = dateTimeWithNoTzFormatter.parseDateTime(dateTimeStr);
    return dt.getMillis();
  }

  public static DateTime parseDateWithZone(String dateTimeStr) {
    return dateZoneFormatter.parseDateTime(dateTimeStr);
  }

  public static String formatDate(long dateTimeMillis) {
    return new DateTime(dateTimeMillis).toString(dateFormatter);
  }

  public static DateTime unformatDate(String dateStr) {
    return dateFormatter.parseDateTime(dateStr);
  }

  public static String formatDateWithZone(DateTime dateTime) {
    return dateTime.toString(dateZoneFormatter);
  }

  public static String formatDateWithZone(long dateTimeMillis) {
    return new DateTime(dateTimeMillis).toString(dateZoneFormatter);
  }

  public static DateTime unformatDateWithZone(String dateStr) {
    return dateZoneFormatter.parseDateTime(dateStr);

  }

  public static DateMidnight getMondayOfWeek(DateTime now) {
    DateMidnight mondayOfWeek = now.toDateMidnight();
    int dow = mondayOfWeek.getDayOfWeek();
    if (dow != DateTimeConstants.MONDAY) {
      mondayOfWeek = mondayOfWeek.minusDays(dow - 1);
    }
    return mondayOfWeek;
  }

  public static boolean isWeekend(int dayOfWeek) {
    return dayOfWeek == DateTimeConstants.SATURDAY ||
      dayOfWeek == DateTimeConstants.SUNDAY;
  }

  public static boolean isWeekend(DateTime dateTime) {
    return isWeekend(dateTime.getDayOfWeek());
  }

  public static DateTime skipWeekends(DateTime plusDays) {
    if (plusDays.getDayOfWeek() == DateTimeConstants.SATURDAY) {
      return plusDays.plusDays(2);
    } else if (plusDays.getDayOfWeek() == DateTimeConstants.SUNDAY) {
      return plusDays.plusDays(1);
    }
    return plusDays;
  }

  public static DateTime parseDateWithoutZone(String dateParam) {
    if (Strings.isNullOrEmpty(dateParam)) {
      return null;
    }
    try {
      return dateFormatter.parseDateTime(dateParam);
    } catch (Exception e) {
      return null;
    }
  }

  public static Date convertToUTC(Date dt, DateTimeZone clientTz) throws ParseException{
    if (dt == null) {
      return null;
    }
    long eventMillsInUTCTimeZone = clientTz.convertLocalToUTC(dt.getTime(), false);
    DateTime evenDateTimeInUTCTimeZone = new DateTime(eventMillsInUTCTimeZone);
    return evenDateTimeInUTCTimeZone.toDate();
  }

  public static DateTime convertToLocal(Date dt, String clientTz) throws ParseException{
    if (dt == null) {
      return null;
    }
    DateTimeZone dtz= DateTimeZone.forID(clientTz);
    long eventMillsInLocalTimeZone = dtz.convertUTCToLocal(dt.getTime());
    DateTime evenDateTimeInlocalTimeZone = new DateTime(eventMillsInLocalTimeZone);
    return evenDateTimeInlocalTimeZone;
  }

}
