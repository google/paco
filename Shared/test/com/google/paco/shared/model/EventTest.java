// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.paco.shared.model;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.google.paco.shared.model.Event;

import org.junit.Test;

import java.util.Date;
import java.util.Map;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class EventTest {
  @Test
  public void testIsMissedSignal() {
    Event response = new Event();
    response.setResponseTime(null);
    assertTrue(response.isMissedSignal());
  }

  @Test
  public void testIstMissedSignalWhenNot() {
    Event response = new Event();
    response.setResponseTime(new Date());

    assertFalse(response.isMissedSignal());
  }

  @Test
  public void testResponseTime() {
    Event response = new Event();
    response.setSignalTime(new Date(3));
    response.setResponseTime(new Date(10));

    assertEquals(response.responseTime(), 7l);
  }

  @Test
  public void testResponseTimeWhenSignalTimeAndResponseTimeAreNull() {
    Event response = new Event();
    response.setSignalTime(null);
    response.setResponseTime(null);

    assertEquals(response.responseTime(), -1l);
  }

  @Test
  public void testResponseTimeWhenSignalTimeIsNull() {
    Event response = new Event();
    response.setSignalTime(null);
    response.setResponseTime(new Date(3));

    assertEquals(response.responseTime(), -1l);
  }

  @Test
  public void testResponseTimeWhenResponseTimeIsNull() {
    Event response = new Event();
    response.setSignalTime(new Date(3));
    response.setResponseTime(null);

    assertEquals(response.responseTime(), -1l);
  }

  @Test
  public void testOutputString() {
    Map<String, String> outputs = Maps.newLinkedHashMap();
    outputs.put("a", "b");
    outputs.put("c", "d");
    outputs.put("e", "f");

    Event response = new Event();
    response.setOutputs(outputs);

    assertEquals(response.getOutputsString(), "{a=b, c=d, e=f}");
  }

  @Test
  public void testOutputStringWhenOutputsAreNull() {
    Event response = new Event();
    response.setOutputs(null);

    assertEquals(response.getOutputsString(), "{}");
  }

  @Test
  public void testOutputStringWhenOutputsAreEmpty() {
    Map<String, String> outputs = Maps.newHashMap();

    Event response = new Event();
    response.setOutputs(outputs);

    assertEquals(response.getOutputsString(), "{}");
  }

  @Test
  public void testGetOutputByString() {
    Event response = new Event();

    assertEquals(response.getOutputByKey("test"), null);
  }

  @Test
  public void testGetOutputByStringWhenSet() {
    Event response = new Event();
    response.setOutputByKey("test", "value");

    assertEquals(response.getOutputByKey("test"), "value");
  }

  @Test
  public void testGetOutputByStringWhenNull() {
    Event response = new Event();
    response.setOutputs(null);

    assertEquals(response.getOutputByKey("test"), null);
  }
}
