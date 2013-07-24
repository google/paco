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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.shared.TimeUtil;

/**
 * Converts string queries into method calls and tests on an Event
 * 
 * @author Bob Evans
 * 
 */
public class EventMatcher {

  public List<Event> matchingEvents(List<Event> events, List<Query> queries) {
    if (queries == null || events == null || queries.isEmpty() || events.isEmpty()) {
      return events;
    }
    List<Event> keepers = Lists.newArrayList();
    for (Event event : events) {
      boolean matches = true;
      for (Query query : queries) {
        if (!matches(query, event)) {
          matches = false;
        }
      }
      if (matches) {
        keepers.add(event);
      }
    }
    return keepers;
  }

  private boolean matches(Query query, Event event) {
    String key = query.getKey();
    if (key.equals("date_range")) {
      return compareDateRange(query.getValue(), event);
    } else {
      return compareMemberResultToQueryValue(query, event, key);
    }
  }

  private boolean compareMemberResultToQueryValue(Query query, Event event, String key) {
    String value = query.getValue();
    
    if (event.getWhatMap().containsKey(key)) {
      if (value == null || value.length() == 0) {
        return true;
      }
      return value.equals(event.getWhatMap().get(key));
    } else {
      String upcasedKey = key.substring(0, 1).toUpperCase() + key.substring(1);      
      try {
        Method keyMethod = Event.class.getDeclaredMethod("get" + upcasedKey);
        if (keyMethod != null && (value == null || value.isEmpty())) {
          return true;
        }
        // Class<?> type = keyMethod.getReturnType();
        Object result = keyMethod.invoke(event, new Object[] {});
        return value.equals(result);
      } catch (SecurityException e) {
        return false;
      } catch (NoSuchMethodException e) {
        return false;
      } catch (IllegalArgumentException e) {
        return false;
      } catch (IllegalAccessException e) {
        return false;
      } catch (InvocationTargetException e) {
        return false;
      }
    }
  }

  private boolean compareDateRange(String range, Event event) {
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
    DateMidnight startDate = newDateMidnightFromString(firstDate);
    DateMidnight endDate = null;
    if (secondDate != null && !secondDate.isEmpty()) {
      endDate = newDateMidnightFromString(secondDate);
    } else {
      endDate = startDate.plusDays(1);
    }
    Interval r2 = new Interval(startDate, endDate);
    Date eventDate = event.getWhen();
    if (eventDate == null) {
      return false;
    }
    DateMidnight eventDateMidnight = new DateMidnight(eventDate.getTime());
    return r2.contains(eventDateMidnight);
  }

  private DateMidnight newDateMidnightFromString(String firstDate) {
    SimpleDateFormat df = new SimpleDateFormat(TimeUtil.DATE_FORMAT);
    try {
      return new DateMidnight(df.parse(firstDate).getTime());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Cannot parse date: " + firstDate + 
          ". Format is " + TimeUtil.DATE_FORMAT);
    }
  }

}
