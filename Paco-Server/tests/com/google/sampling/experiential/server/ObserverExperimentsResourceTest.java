// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.paco.shared.model.Experiment;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObserverExperimentsResourceTest extends PacoResourceTest {
  /*
   * create tests
   */
  @Test
  public void testCreate() {
    Experiment experiment = PacoTestHelper.constructExperiment();

    Request request = PacoTestHelper.post("/observer/experiments", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testCreateWhenExperimentNull() {
    Request request = PacoTestHelper.post("/observer/experiments", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
  }

  /*
   * list tests
   */
  @Test
  public void testList() {
    Request request = PacoTestHelper.get("/observer/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();
    Request request = PacoTestHelper.get("/observer/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.setPublished(true);
    experiment.addObserver("observer@google.com");

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + PacoConverter.toJson(experiment) + "]",
        response.getEntityAsText());
  }

  @Test
  public void testListAsImposterAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = PacoTestHelper.get("/observer/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }
}
