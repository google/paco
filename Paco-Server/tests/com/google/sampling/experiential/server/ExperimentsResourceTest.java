// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.FixedSignal;
import com.google.sampling.experiential.shared.SharedTestHelper;
import com.google.sampling.experiential.shared.SignalSchedule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Response;
import org.restlet.Request;
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
  public void testIndex() {
    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testCreate() {
    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(null);
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/experiments", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testCreateNull() {
    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(null);

    Request request = ServerTestHelper.createJsonPostRequest("/experiments", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void testIndexAfterCreate() {
    testCreate();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());

    Experiment experiment = DAOTest.constructExperiment(1l);
    String json = DAOHelper.toJson(experiment, Experiment.Views.Summary.class);

    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  private void createWithViewers() {
    Experiment observedExperiment = DAOTest.constructObservedExperiment();
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(Lists.newArrayList("test@google.com"));
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest(
        "/experiments", DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testIndexAfterCreateWithViewers() {
    createWithViewers();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());

    Experiment experiment = DAOTest.constructExperiment(1l);
    String json = DAOHelper.toJson(experiment, Experiment.Views.Summary.class);

    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  @Test
  public void testIndexAsNonViewerAfterCreateWithViewers() {
    createWithViewers();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testShow() {
    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreate() {
    testCreate();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);
    assertEquals(Status.SUCCESS_OK, response.getStatus());

    Experiment experiment = DAOTest.constructExperiment(1l);
    String json = DAOHelper.toJson(experiment, Experiment.Views.Subject.class);

    assertEquals(json, response.getEntityAsText());
  }

  @Test
  public void testShowAsImposterAfterCreate() {
    testCreate();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());

    Experiment experiment = DAOTest.constructExperiment(1l);
    String json = DAOHelper.toJson(experiment, Experiment.Views.Subject.class);

    assertEquals(json, response.getEntityAsText());
  }

  @Test
  public void testJoin() {
    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testJoinAfterCreate() {
    testCreate();

    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinWithSignalScheduleAfterCreate() {
    testCreate();

    SignalSchedule signalSchedule =
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule());

    Request request =
        ServerTestHelper.createJsonPostRequest("/experiments/1", DAOHelper.toJson(signalSchedule));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }
}
