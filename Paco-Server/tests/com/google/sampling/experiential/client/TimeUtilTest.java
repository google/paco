package com.google.sampling.experiential.client;

import com.google.gwt.junit.client.GWTTestCase;


public class TimeUtilTest extends GWTTestCase {

  public void testTimezone() throws Exception {
    assertEquals("-0700", com.google.sampling.experiential.client.TimeUtil.getTimezone());
  }

  @Override
  public String getModuleName() {
    return "com.google.sampling.experiential.PacoEventserver";
  }

}
