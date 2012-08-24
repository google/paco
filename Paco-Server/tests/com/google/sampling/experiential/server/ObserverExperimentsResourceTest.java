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
    assertEquals("[" + DAOHelper.toJson(experiment, Experiment.Summary.class) + "]",
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
