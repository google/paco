// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.paco.shared.model.Experiment;

public class ObserverExperimentResourceTest extends PacoResourceTest {
  /*
   * show tests
   */
  @Test
  public void testShow() {
    Request request = PacoTestHelper.get("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreate() {
    PacoTestHelper.createUnpublishedExperiment();
    Request request = PacoTestHelper.get("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Observer.class),
        response.getEntityAsText());
  }


  @Test
  public void testShowAfterCreateWithVersion() {
    Experiment experiment = PacoTestHelper.constructExperiment();

    experiment.setVersion(10);
    experiment.setId(100l);

    Request request =
        PacoTestHelper.post("/observer/experiments", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());

    request = PacoTestHelper.get("/observer/experiments/1");
    response = new PacoApplication().handle(request);

    Experiment experiment2 = PacoTestHelper.constructExperiment();
    experiment2.setId(1l);
    experiment2.setVersion(1);
    experiment2.addObserver("observer@google.com");

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment2, Experiment.Observer.class),
        response.getEntityAsText());
  }

  @Test
  public void testShowAsImposterAfterCreate() {
    PacoTestHelper.createUnpublishedExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.get("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndDestroy() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.destroyExperiment();

    Request request = PacoTestHelper.get("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  /*
   * update tests
   */
  @Test
  public void testUpdate() {
    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setPublished(false);
    experiment.setViewers(null);
    experiment.setSignalSchedule(null);

    Request request =
        PacoTestHelper.put("/observer/experiments/1", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testUpdateWithVersionEqualAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setVersion(1);

    Request request =
        PacoTestHelper.put("/observer/experiments/1", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testUpdateWithVersionGreaterAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setVersion(2);

    Request request =
        PacoTestHelper.put("/observer/experiments/1", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_CONFLICT, response.getStatus());
  }

  @Test
  public void testUpdateWithVersionLessAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setVersion(0);

    Request request =
        PacoTestHelper.put("/observer/experiments/1", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_CONFLICT, response.getStatus());
  }

  @Test
  public void testUpdateAsImpostorAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    helper.setEnvEmail("imposter@google.com");

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setVersion(1);

    Request request =
        PacoTestHelper.post("/observer/experiments/1", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  /*
   * destroy tests
   */
  @Test
  public void testDestroy() {
    Request request = PacoTestHelper.delete("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testDestroyAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.delete("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testDestroyAsImpostorAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = PacoTestHelper.delete("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
