// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoResourceTest {
  protected final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setEnvIsAdmin(false);
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail("subject@google.com");
    helper.setEnvAuthDomain("google.com");
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testClientNotLoggedIn() {
    helper.setEnvIsLoggedIn(false);

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
  }
}
