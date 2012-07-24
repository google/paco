/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.sampling.experiential.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.shared.Event;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventMatcherTest {


  private EventMatcher eventMatcher;
  private List<Event> events;
  private List<Query> query;

  @Before
  public void setUp() throws Exception {
    eventMatcher = new EventMatcher();
    events = Lists.newArrayList();
    query = Lists.newArrayList();

    Map<String, String> what = Maps.newHashMap();
    what.put("restaurant", "CafeMoma");
    what.put("rating", "2");

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2009);
    cal.set(Calendar.MONTH, 7);
    cal.set(Calendar.DAY_OF_MONTH, 31);
    Date aug3109 = cal.getTime();
    cal.set(Calendar.MONTH, 8);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    Date sep109 = cal.getTime();
    events.add(new Event("bobevans@google.com",
        null,
        null,
        aug3109,
        "test",
        null,
        what,
        true,
        null,
        null,
        null,
        null,
        null));
  }

  @Test
  public void testMatchNull() throws Exception {
    assertNull(eventMatcher.matchingEvents(null, null));
  }

  @Test
  public void testMatchAllQueryNull() throws Exception {
    query = null;
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchAllEmptyQuery() throws Exception {
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhoQuery() throws Exception {
    query.add(new Query("who", "bobevans@google.com"));
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhoQueryFail() throws Exception {
    query.add(new Query("who", "somebodyelse@google.com"));
    assertEquals(0, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhatQuery() throws Exception {
    query.add(new Query("restaurant", "CafeMoma"));
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhatQueryFails() throws Exception {
    query.add(new Query("restaurant", "AmericanTable"));
    assertEquals(0, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhatQueryNoKey() throws Exception {
    Map<String, String> what = Maps.newHashMap();
    what.put("restaurant", "AmericanTable");
    what.put("rating", "1");
    events.add(new Event("bobevans@google.com",
        null,
        null,
        new Date(),
        "test",
        null,
        what,
        true,
        null,
        null,
        null,
        null,
        null));

    Map<String, String> nonRestaurantWhat = Maps.newHashMap();
    nonRestaurantWhat.put("weight", "10");
    events.add(new Event("bobevans@google.com",
        null,
        null,
        new Date(),
        "test",
        null,
        nonRestaurantWhat,
        true,
        null,
        null,
        null,
        null,
        null));

    query.add(new Query("restaurant", null));
    assertEquals(2, eventMatcher.matchingEvents(events, query).size());
  }

  @Test
  public void testMatchWhatTwoQueries() throws Exception {
    Map<String, String> what = Maps.newHashMap();
    what.put("restaurant", "CafeMoma");
    what.put("rating", "1");
    events.add(new Event("bobevans@google.com",
        null,
        null,
        new Date(),
        "test",
        null,
        what,
        true,
        null,
        null,
        null,
        null,
        null));

    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", "1"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
    assertEquals("CafeMoma", results.get(0).getWhatByKey("restaurant"));
    assertEquals("1", results.get(0).getWhatByKey("rating"));
  }

  @Test
  public void testMatchWhatTwoQueriesOneWithNoValue() throws Exception {
    Map<String, String> what = Maps.newHashMap();
    what.put("restaurant", "CafeMoma");
    what.put("rating", "1");
    events.add(new Event("bobevans@google.com",
        null,
        null,
        new Date(),
        "test",
        null,
        what,
        true,
        null,
        null,
        null,
        null,
        null));

    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", null));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(2, results.size());
  }

  @Test
  public void testMatchWhatTwoQueriesOneWithEmptyValue() throws Exception {
    Map<String, String> what = Maps.newHashMap();
    what.put("restaurant", "CafeMoma");
    what.put("rating", "1");
    events.add(new Event("bobevans@google.com",
        null,
        null,
        new Date(),
        "test",
        null,
        what,
        true,
        null,
        null,
        null,
        null,
        null));

    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", ""));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(2, results.size());
  }

  @Test
  public void testMatchDateRange() throws Exception {
    query.add(new Query("date_range", "20090831-20090901"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
  }

  @Test
  public void testMatchDateRangeOneDate() throws Exception {
    query.add(new Query("date_range", "20090831"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
  }

  @Test
  public void testMatchDateRangeNoMatch() throws Exception {
    query.add(new Query("date_range", "20090829-20090830"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(0, results.size());
  }
}
