// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.paco.shared.model.Event;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SubjectEventsResourceTest extends PacoResourceTest {
  /*
   * add tests
   */
  @Test
  public void testAdd() {
    Request request = PacoTestHelper.post("/subject/experiments/1/events", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testAddAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.post("/subject/experiments/1/events", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testAddAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Event event = PacoTestHelper.constructEvent();

    Request request = PacoTestHelper.post("/subject/experiments/1/events", PacoConverter.toJson(event));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiment/1/events/2", response.getLocationRef().getPath());
  }

  @Test
  public void testAddWithNullAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.post("/subject/experiments/1/events", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void testAddAsImpostorAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Event event = PacoTestHelper.constructEvent();

    Request request = PacoTestHelper.post("/subject/experiments/1/events", PacoConverter.toJson(event));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  /*
   * list tests
   */
  @Test
  public void testList() {
    Request request = PacoTestHelper.get("/subject/experiments/1/events");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testListAfterCreateAndJoinAndAdd() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.addEvent();

    Request request = PacoTestHelper.get("/subject/experiments/1/events");
    Response response = new PacoApplication().handle(request);

    Event event = PacoTestHelper.constructEvent();

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + PacoConverter.toJson(event) + "]", response.getEntityAsText());
  }

  @Test
  public void testListAsImpostorAfterCreateAndJoinAndAdd() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.addEvent();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.get("/subject/experiments/1/events");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
