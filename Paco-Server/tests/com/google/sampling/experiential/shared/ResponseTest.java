// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.shared;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.Date;
import java.util.Map;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ResponseTest {
  @Test
  public void testIsMissedSignal() {
    Response response = new Response();
    response.setResponseTime(null);
    assertTrue(response.isMissedSignal());
  }

  @Test
  public void testIstMissedSignalWhenNot() {
    Response response = new Response();
    response.setResponseTime(new Date());

    assertFalse(response.isMissedSignal());
  }

  @Test
  public void testResponseTime() {
    Response response = new Response();
    response.setSignalTime(new Date(3));
    response.setResponseTime(new Date(10));

    assertEquals(response.responseTime(), 7l);
  }

  @Test
  public void testResponseTimeWhenSignalTimeAndResponseTimeAreNull() {
    Response response = new Response();
    response.setSignalTime(null);
    response.setResponseTime(null);

    assertEquals(response.responseTime(), -1l);
  }

  @Test
  public void testResponseTimeWhenSignalTimeIsNull() {
    Response response = new Response();
    response.setSignalTime(null);
    response.setResponseTime(new Date(3));

    assertEquals(response.responseTime(), -1l);
  }

  @Test
  public void testResponseTimeWhenResponseTimeIsNull() {
    Response response = new Response();
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

    Response response = new Response();
    response.setOutputs(outputs);

    assertEquals(response.getOutputsString(), "{a=b, c=d, e=f}");
  }

  @Test
  public void testOutputStringWhenOutputsAreNull() {
    Response response = new Response();
    response.setOutputs(null);

    assertEquals(response.getOutputsString(), "");
  }


  @Test
  public void testOutputStringWhenOutputsAreEmpty() {
    Map<String, String> outputs = Maps.newHashMap();

    Response response = new Response();
    response.setOutputs(outputs);

    assertEquals(response.getOutputsString(), "{}");
  }
}
