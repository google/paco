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
  private static DateTimeFormatter dateTimeFormatter =
      DateTimeFormat.forPattern("yyyyMMdd:HH:mm:ssZ");

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

  public static DateMidnight getMondayOfWeek(DateTime now) {
    DateMidnight mondayOfWeek = now.toDateMidnight();
    int dow = mondayOfWeek.getDayOfWeek();
    if (dow != DateTimeConstants.MONDAY) {
      mondayOfWeek = mondayOfWeek.minusDays(dow - 1);
    }
    return mondayOfWeek;
  }



}
