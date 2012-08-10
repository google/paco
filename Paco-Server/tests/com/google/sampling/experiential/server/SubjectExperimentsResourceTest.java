// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.sampling.experiential.shared.Experiment;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class SubjectExperimentsResourceTest extends PacoResourceTest {
  /*
   * list tests
   */
  @Test
  public void testList() {
    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreate() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testListAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + DAOHelper.toJson(experiment, Experiment.Summary.class) + "]",
        response.getEntityAsText());
  }

  @Test
  public void testListAsImpostorAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }
}
