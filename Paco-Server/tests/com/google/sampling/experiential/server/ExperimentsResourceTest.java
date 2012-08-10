// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import com.google.sampling.experiential.shared.Experiment;

import org.junit.Test;
import org.restlet.Response;
import org.restlet.Request;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentsResourceTest extends PacoResourceTest {
  /*
   * create tests
   */
  @Test
  public void testCreate() {
    Experiment experiment = ExperimentTestHelper.constructExperiment();

    Request request =
        ServerTestHelper.createJsonPostRequest("/experiments", DAOHelper.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testCreateWhenExperimentNull() {
    Request request = ServerTestHelper.createJsonPostRequest("/experiments", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
  }

  /*
   * index tests
   */
  @Test
  public void testIndex() {
    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testIndexAfterCreatePublishedPublic() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    String json = DAOHelper.toJson(experiment, Experiment.Summary.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  @Test
  public void testIndexAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    String json = DAOHelper.toJson(experiment, Experiment.Summary.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[" + json + "]", response.getEntityAsText());
  }

  @Test
  public void testIndexAfterCreateUnpublished() {
    ExperimentTestHelper.createUnpublishedExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    String json = DAOHelper.toJson(experiment, Experiment.Summary.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testIndexAsImposterAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    String json = DAOHelper.toJson(experiment, Experiment.Summary.class);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }

  @Test
  public void testIndexAfterCreateAndDestroy() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.destroyExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("[]", response.getEntityAsText());
  }
}
