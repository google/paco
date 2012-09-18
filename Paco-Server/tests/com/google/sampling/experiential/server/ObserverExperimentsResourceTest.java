// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.paco.shared.model.Experiment;

import org.joda.time.DateTime;
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

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + PacoConverter.toJson(experiment, Experiment.Viewer.class) + "]",
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

  @Test
  public void testModifiedSinceBefore() {
    DateTime before = DateTime.now().minusHours(1); // -1 hour to be safe
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.get("/observer/experiments", before);
    Response response = new PacoApplication().handle(request);

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.setPublished(true);
    experiment.addObserver("observer@google.com");

    String json = PacoConverter.toJson(experiment, Experiment.Viewer.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  @Test
  public void testModifiedSinceAfter() {
    PacoTestHelper.createPublishedPublicExperiment();
    DateTime after = DateTime.now().plusHours(1); // +1 hour to be safe

    Request request = PacoTestHelper.get("/observer/experiments", after);
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }
}
