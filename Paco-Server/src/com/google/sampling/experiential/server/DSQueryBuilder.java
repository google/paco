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
package com.google.sampling.experiential.server;

import java.util.Iterator;
import java.util.List;

import javax.jdo.Query;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.TimeUtil;

public class DSQueryBuilder {

  private static final List<String> CANNED_DATE_RANGES = Lists.newArrayList("last_week",
      "last_month");

  private EventDSQuery jdoQuery;

  private DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern(TimeUtil.DATE_FORMAT);



  public DSQueryBuilder(Query newQuery) {
    this.jdoQuery = new EventDSQuery(newQuery);
  }

  public void addFilters(List<com.google.sampling.experiential.server.Query> queryFilters,
      DateTimeZone jodaTimeZone) {
    if (queryFilters == null || queryFilters.isEmpty()) {
      return;
    }
    for (com.google.sampling.experiential.server.Query query : queryFilters) {
      String key = query.getKey();
      if (key.equals("date_range") || key.startsWith("@")) {
        compareDateRange(key, query.getValue(), jodaTimeZone);
      } else {
        if (key.equals("who") && query.getValue() != null) {
          jdoQuery.setHasWho(query.getValue());
        }
        compareMemberResultToQueryValue(query, key);
      }
    }
  }

  private boolean isCannedDateRange(String value) {
    return CANNED_DATE_RANGES.contains(value);
  }

  private boolean isKeyCannedDateRange(String key) {
    return key.startsWith("@") && isCannedDateRange(key.substring(1));
  }

  private void compareDateRange(String key, String range, DateTimeZone jodaTimeZone) {
    DateTime startDate = null;
    DateTime endDate = null;

    boolean keyCannedDateRange = isKeyCannedDateRange(key);
    boolean rangeCannedDateRange = isCannedDateRange(range);
    if (keyCannedDateRange || rangeCannedDateRange) {
      String rangeName = null;
      if (keyCannedDateRange) {
        rangeName = key.substring(1);
      } else {
        rangeName = range;
      }
      Interval interval;
      if (rangeName.equals("last_week")) {
        interval = getLastWeek(jodaTimeZone);
      } else if (rangeName.equals("last_month")) {
        interval = getLast4Weeks(jodaTimeZone);
      } else {
        throw new IllegalArgumentException("Unknown date range");
      }
      startDate = interval.getStart();
      endDate = interval.getEnd().plusDays(1);

    } else {
      Iterable<String> iterable = Splitter.on("-").split(range);
      Iterator<String> iter = iterable.iterator();
      if (!iter.hasNext()) {
        throw new IllegalArgumentException("Illformed Date Range: " + range);
      }
      String firstDate = iter.next();
      String secondDate = null;
      if (iter.hasNext()) {
        secondDate = iter.next();
      }

      startDate = newDateTimeFromString(firstDate, jodaTimeZone);
      endDate = null;
      if (secondDate != null && !secondDate.isEmpty()) {
        endDate = newDateTimeFromString(secondDate, jodaTimeZone).plusDays(1);
      } else {
        endDate = startDate.plusDays(1);
      }
    }
    jdoQuery.addFilters("when >= startDateParam", "when <= endDateParam");
    jdoQuery.declareParameters("java.util.Date startDateParam", "java.util.Date endDateParam");
    jdoQuery.addParameterObjects(startDate.toDate(), endDate.toDate());
  }

  private void compareMemberResultToQueryValue(com.google.sampling.experiential.server.Query query,
      String key) {
    String value = query.getValue();
    if (eventPropertyHasKey(key)) {
      if (value != null) {
        addTestThatKeyEquals(key, value);
      } else {
        throw new IllegalArgumentException("All Events have this property. " +
            "Specify a matching value");
      }
    } else {
      if (value != null) {
        addTestThatWhatKeyEquals(key, value);
      } else {
        addExistenceOfWhatKeyTest(key);
      }
    }

  }

  // TODO(bobevans): once appengine supports querying on properties of
  // children types make this do the equality test on the associated what value.
  // For now, we still do the EventMatcher on the results returned, but at least
  // it is a smaller set, and the database query should be more efficient than retrieving
  // all events.
  private void addTestThatWhatKeyEquals(String key, String value) {
    // TODO (bobevans): this is not exactly correct, because the value could be contained
    // at a different index, meaning that we don't actually have equality.
    // We really want a Map (not supported), but we would also like an index for the key and
    // value to ensure that those match.
   jdoQuery.addFilters("keysList.contains(\"" + key
       + "\") && valuesList.contains(\"" + value +"\")");
  }

  private void addTestThatKeyEquals(String key, String value) {
    jdoQuery.addFilters(key + " == '" + value + "'");
  }

  private void addExistenceOfWhatKeyTest(String key) {
    jdoQuery.addFilters("keysList.contains(\"" + key + "\")");
  }

  private boolean eventPropertyHasKey(String key) {
    return Event.eventProperties.contains(key);
  }

  private DateTime newDateTimeFromString(String firstDate, DateTimeZone jodaTimeZone) {
    DateTime parsedTime = jodaFormatter.withZone(jodaTimeZone).parseDateTime(firstDate);
    return parsedTime.withZone(DateTimeZone.UTC);
  }

  public EventDSQuery getQuery() {
    return jdoQuery;
  }


  private static Interval getLastWeek(DateTimeZone clientTimeZone) {
    return getWeekEnclosing(getDayLastWeek(clientTimeZone));
  }

  private static Interval getLast4Weeks(DateTimeZone clientTimeZone) {
    return getXWeeksAgo(3, clientTimeZone);
  }

  private static Interval getLast13Weeks(DateTimeZone clientTimeZone) {
    return getXWeeksAgo(12, clientTimeZone);
  }

  private static Interval getNextWeek(DateTimeZone clientTimeZone) {
    DateTime dayNextWeek = new DateTime(clientTimeZone).plusDays(7).withZone(DateTimeZone.UTC);
    return getWeekEnclosing(dayNextWeek);
  }

  private static Interval getXWeeksAgo(int weeksAgo, DateTimeZone clientTimeZone) {
    DateTime lastMondayAfter = getMondayAfterWeekEnclosing(getDayLastWeek(clientTimeZone));
    DateTime xMondaysAfterAgo = lastMondayAfter.minusWeeks(weeksAgo);
    DateTime xMondaysAgo = xMondaysAfterAgo.minusWeeks(1);
    return new Interval(xMondaysAgo, lastMondayAfter);
  }

  private static DateTime getDayLastWeek(DateTimeZone clientTimeZone) {
    return new DateTime(clientTimeZone).minusDays(7).withZone(DateTimeZone.UTC);
  }

  private static Interval getWeekEnclosing(DateTime today) {
    DateTime monday = getMondayOfWeekEnclosing(today);
    DateTime nextMonday = getMondayAfterWeekEnclosing(today);
    return new Interval(monday, nextMonday);
  }

  private static DateTime getMondayAfterWeekEnclosing(DateTime today) {
    return today.dayOfWeek().setCopy(DateTimeConstants.SUNDAY).plusDays(1);
  }

  private static DateTime getMondayOfWeekEnclosing(DateTime today) {
    return today.dayOfWeek().setCopy(DateTimeConstants.MONDAY);
  }

}
