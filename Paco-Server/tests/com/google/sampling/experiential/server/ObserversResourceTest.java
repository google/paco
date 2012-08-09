// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.sampling.experiential.shared.Experiment;

public class ObserversResourceTest {
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

  private Experiment createExperiment() {
    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(false);
    observedExperiment.setViewers(null);
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/experiments", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());

    return observedExperiment;
  }

  @Test
  public void testList() {
    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreate() {
    Experiment experiment = createExperiment();

    experiment.setId(1l);
    experiment.addObserver("test@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments");
    Response response = new PacoApplication().handle(request);
    String json = DAOHelper.toJson(experiment, Experiment.Summary.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  @Test
  public void testListAsNonObserverAfterCreate() {
    createExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testStats() {
    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testStatsAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("", response.getEntityAsText());
  }

  @Test
  public void testStatsAsNonObserverAfterCreate() {
    createExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testUpdate() {
    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(false);
    observedExperiment.setViewers(null);
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/observer/experiments/1", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testUpdateAfterCreate() {
    createExperiment();

    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(null);
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/observer/experiments/1", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testUpdateAsNonObserverAfterCreate() {
    createExperiment();

    helper.setEnvEmail("imposter@google.com");

    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(null);
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/observer/experiments/1", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testDestroy() {
    Request request = ServerTestHelper.createJsonDeleteRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testDestroyAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper.createJsonDeleteRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testDestroyAsNonObserverAfterCreate() {
    createExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = ServerTestHelper.createJsonDeleteRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
