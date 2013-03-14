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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventMatcherTest extends TestCase {

  
  private EventMatcher eventMatcher;
  private List<Event> events;
  private List<Query> query;
  
  Date sep109 = null;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    eventMatcher = new EventMatcher();
    events = Lists.newArrayList();
    query = Lists.newArrayList();

    HashSet<What> newHashSet = Sets.newHashSet();
    newHashSet.add(new What("restaurant", "CafeMoma"));
    newHashSet.add(new What("rating", "2"));
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2009);
    cal.set(Calendar.MONTH, 7);
    cal.set(Calendar.DAY_OF_MONTH, 31);
    Date aug3109 = cal.getTime();
    cal.set(Calendar.MONTH, 8);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    sep109 = cal.getTime();
    events.add(new Event("bobevans@google.com", 
        null, null, aug3109, "test", null, newHashSet, true, "1", "foo", 1, sep109, sep109, null, null));

  }

  public void testMatchNull() throws Exception {
    assertNull(eventMatcher.matchingEvents(null, null));
  }
  
  public void testMatchAllQueryNull() throws Exception {
    query = null;
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }
  
  public void testMatchAllEmptyQuery() throws Exception {
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());    
  }
  
  public void testMatchWhoQuery() throws Exception {
    query.add(new Query("who", "bobevans@google.com"));
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }
  
  public void testMatchWhoQueryFail() throws Exception {
    query.add(new Query("who", "somebodyelse@google.com"));
    assertEquals(0, eventMatcher.matchingEvents(events, query).size());
  }
  
  public void testMatchWhatQuery() throws Exception {
    query.add(new Query("restaurant", "CafeMoma"));
    assertEquals(1, eventMatcher.matchingEvents(events, query).size());
  }

  public void testMatchWhatQueryFails() throws Exception {
    query.add(new Query("restaurant", "AmericanTable"));
    assertEquals(0, eventMatcher.matchingEvents(events, query).size());
  }
  
  public void testMatchWhatQueryNoKey() throws Exception {
    HashSet<What> newHashSet = Sets.newHashSet();
    newHashSet.add(new What("restaurant", "AmericanTable"));
    newHashSet.add(new What("rating", "1"));
    events.add(new Event("bobevans@google.com", 
        null, null, new Date(), "test", null, newHashSet, true, "1", "foo", 1, sep109, sep109, null, null));
    
    Set<What> nonRestaurantWhat = Sets.newHashSet();
    nonRestaurantWhat.add(new What("weight", "10"));
    events.add(new Event("bobevans@google.com", 
        null, null, new Date(), "test", null, nonRestaurantWhat, true, "1", "foo", 1, sep109, sep109, null, null));
    
    query.add(new Query("restaurant", null));
    assertEquals(2, eventMatcher.matchingEvents(events, query).size());
  }
  
  public void testMatchWhatTwoQueries() throws Exception {
    HashSet<What> newHashSet = Sets.newHashSet();
    newHashSet.add(new What("restaurant", "CafeMoma"));
    newHashSet.add(new What("rating", "1"));
    events.add(new Event("bobevans@google.com", 
        null, null, new Date(), "test", null, newHashSet, true, "1", "foo", 1, sep109, sep109, null, null));
        
    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", "1"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
    assertEquals("CafeMoma", results.get(0).getWhatByKey("restaurant"));
    assertEquals("1", results.get(0).getWhatByKey("rating"));
  }
  
  public void testMatchWhatTwoQueriesOneWithNoValue() throws Exception {
    HashSet<What> newHashSet = Sets.newHashSet();
    newHashSet.add(new What("restaurant", "CafeMoma"));
    newHashSet.add(new What("rating", "1"));
    events.add(new Event("bobevans@google.com", 
        null, null, new Date(), "test", null, newHashSet, true, "1", "foo", 1, sep109, sep109, null, null));
        
    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", null));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(2, results.size());
  }
  
  public void testMatchWhatTwoQueriesOneWithEmptyValue() throws Exception {
    HashSet<What> newHashSet = Sets.newHashSet();
    newHashSet.add(new What("restaurant", "CafeMoma"));
    newHashSet.add(new What("rating", "1"));
    events.add(new Event("bobevans@google.com", 
        null, null, new Date(), "test", null, newHashSet, true, "1", "foo", 1, sep109, sep109, null, null));
        
    query.add(new Query("restaurant", "CafeMoma"));
    query.add(new Query("rating", ""));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(2, results.size());
  }

  public void testMatchDateRange() throws Exception {
    query.add(new Query("date_range", "2009/08/31-2009/09/01"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
  }

  public void testMatchDateRangeOneDate() throws Exception {
    query.add(new Query("date_range", "2009/08/31"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(1, results.size());
  }
  
  public void testMatchDateRangeNoMatch() throws Exception {
    query.add(new Query("date_range", "2009/08/29-2009/08/30"));
    List<Event> results = eventMatcher.matchingEvents(events, query);
    assertEquals(0, results.size());
  }
}
