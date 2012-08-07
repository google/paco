package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Event;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.ObservedExperiment;
import com.google.sampling.experiential.shared.TextInput;

public class SubjectsResourceTest {
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

  private ObservedExperiment createExperiment() {
    ObservedExperiment observedExperiment = DAOTest
        .constructObservedExperiment();
    observedExperiment.setDeleted(false);
    observedExperiment.setPublished(true);
    observedExperiment.setViewers(Lists.newArrayList("test@google.com"));
    observedExperiment.setSignalSchedule(null);

    Request request = ServerTestHelper.createJsonPostRequest("/experiments",
        DAOHelper.toJson(observedExperiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());

    return observedExperiment;
  }

  private Experiment joinExperiment() {
    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1",
        "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());

    Experiment experiment = new Experiment();

    experiment.setId(1l);
    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setDeleted(false);
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(),
        new LikertInput()));
    experiment.setSignalSchedule(null);
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    return experiment;
  }

  private void leaveExperiment() {
    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  private Event constructEvent() {
    Event event = new Event();

    event.setExperimentVersion(1l);
    event.setSignalTime(new Date(3));
    event.setResponseTime(new Date(13));
    event.setOutputByKey("test", "value");

    return event;
  }

  @Test
  public void testList() {
    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreateAndJoin() {
    createExperiment();
    Experiment experiment = joinExperiment();

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + DAOHelper.toJson(experiment) + "]",
        response.getEntityAsText());
  }

  @Test
  public void testListAsNonSubjectAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testStats() {
    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testStatsAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testStatsAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("{}", response.getEntityAsText());
  }

  @Test
  public void testStatsAsNonSubjectAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper
        .createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testUpdate() {
    Request request = ServerTestHelper.createJsonPostRequest(
        "/subject/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testUpdateAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper.createJsonPostRequest(
        "/subject/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testUpdateAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    Event event = constructEvent();

    Request request = ServerTestHelper.createJsonPostRequest(
        "/subject/experiments/1", DAOHelper.toJson(event));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testUpdateAsNonSubjectAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Event event = constructEvent();

    Request request = ServerTestHelper.createJsonPostRequest(
        "/subject/experiments/1", DAOHelper.toJson(event));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeave() {
    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreate() {
    createExperiment();

    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreateAndJoinAndLeave() {
    createExperiment();
    joinExperiment();
    leaveExperiment();

    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeaveAsNonSubjectAfterCreateAndJoin() {
    createExperiment();
    joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper
        .createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
