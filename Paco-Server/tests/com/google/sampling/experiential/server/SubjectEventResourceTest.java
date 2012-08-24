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
public class SubjectEventResourceTest extends PacoResourceTest {
  /*
   * show tests
   */
  @Test
  public void testShow() {
    Request request = PacoTestHelper.get("/subject/experiments/1/events/2");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.get("/subject/experiments/1/events/2");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndJoinAndAdd() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.addEvent();

    Request request = PacoTestHelper.get("/subject/experiments/1/events/2");
    Response response = new PacoApplication().handle(request);

    Event event = PacoTestHelper.constructEvent();

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(DAOHelper.toJson(event), response.getEntityAsText());
  }

  @Test
  public void testShowAsImpostorAfterCreateAndJoinAndAdd() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.addEvent();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.get("/subject/experiments/1/events/2");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAsJoinedImpostorAfterCreateAndJoinAndAdd() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.addEvent();

    helper.setEnvEmail("impostor@google.com");
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.get("/subject/experiments/1/events/2");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
