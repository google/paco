// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Experiment;
import com.google.paco.shared.model.FixedSignal;
import com.google.paco.shared.model.SharedTestHelper;
import com.google.paco.shared.model.SignalSchedule;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentResourceTest extends PacoResourceTest {
  /*
   * show tests
   */
  @Test
  public void testShow() {
    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreatePublishedPublic() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.setPublished(true);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreatePublishedPrivate() {
    PacoTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("subject@google.com");

    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.addViewer("subject@google.com");
    experiment.setPublished(true);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreateUnpublished() {
    PacoTestHelper.createUnpublishedExperiment();

    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAsImposterAfterCreatePublishedPrivate() {
    PacoTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndDestroy() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.destroyExperiment();

    Request request = PacoTestHelper.get("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  /*
   * join tests
   */
  @Test
  public void testJoin() {
    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testJoinWithSignalScheduleAfterCreatePublishedPublic() {
    PacoTestHelper.createPublishedPublicExperiment();

    SignalSchedule signalSchedule =
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule());

    Request request = PacoTestHelper.post("/experiments/1", PacoConverter.toJson(signalSchedule));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinWithSignalScheduleAfterCreatePublishedPublicWhenUneditable() {
    PacoTestHelper.createPublishedPublicExperiment(false);

    SignalSchedule signalSchedule =
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule());

    Request request = PacoTestHelper.post("/experiments/1", PacoConverter.toJson(signalSchedule));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreatePublishedPublic() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreatePublishedPrivate() {
    PacoTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("subject@google.com");

    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreateUnpublished() {
    PacoTestHelper.createUnpublishedExperiment();

    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testJoinAsImposterAfterCreatePublishedPrivate() {
    PacoTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testJoinAfterJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.post("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
  }
}
