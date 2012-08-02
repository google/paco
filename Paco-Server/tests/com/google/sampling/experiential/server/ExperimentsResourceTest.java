// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ObservedExperiment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Response;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentsResourceTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setEnvIsAdmin(false);
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail("test@google.com");
    helper.setEnvAuthDomain("google.com");
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testGet() {
    Request request = new Request(Method.GET, "/experiments");
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));

    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testPost() {
    ObservedExperiment experiment = DAOTest.constructObservedExperiment();
    experiment.setPublished(true);
    experiment.setViewers(null);

    Request request = new Request(Method.POST, "/experiments");
    request.setEntity(DAOHelper.observedExperimentToJson(experiment), MediaType.APPLICATION_JSON);

    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().toString());
  }

  @Test
  public void testGetAfterPost() {
    testPost();

    Request request = new Request(Method.GET, "/experiments");
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));

    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());

    Experiment experiment = DAOTest.constructExperiment();

    assertEquals("[" + DAOHelper.experimentToJson(experiment) + "]", response.getEntityAsText());
  }
}
