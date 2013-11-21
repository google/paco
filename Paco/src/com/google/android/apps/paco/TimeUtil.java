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
package com.google.android.apps.paco;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class TimeUtil {

  private static DateTimeFormatter timeFormatter = ISODateTimeFormat.time();

  static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ssZ";
  private static DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);

  static final String DATETIME_NOZONE_FORMAT = "yyyy/MM/dd hh:mm:ssa";
  public static DateTimeFormatter dateTimeNoZoneFormatter = DateTimeFormat.forPattern(DATETIME_NOZONE_FORMAT);

  static final String DATE_FORMAT = "yyyy/MM/dd";
  private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(DATE_FORMAT);

  static final String DATE_WITH_ZONE_FORMAT = "yyyy/MM/ddZ";
  private static DateTimeFormatter dateZoneFormatter = DateTimeFormat.forPattern(DATE_WITH_ZONE_FORMAT);

  private TimeUtil() {
    super();
    // TODO Auto-generated constructor stub
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

  public static DateTime parseDateTime(String dateTimeStr) {
    return dateTimeFormatter.parseDateTime(dateTimeStr);
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
}
