// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.sampling.experiential.shared.DailySchedule;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.FixedSignal;
import com.google.sampling.experiential.shared.SharedTestHelper;
import com.google.sampling.experiential.shared.SignalSchedule;

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
    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreatePublishedPublic() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(DAOHelper.toJson(experiment, Experiment.Viewer.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    Experiment experiment = ExperimentTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(DAOHelper.toJson(experiment, Experiment.Viewer.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreateUnpublished() {
    ExperimentTestHelper.createUnpublishedExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAsImposterAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("imposter@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndDestroy() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.destroyExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  /*
   * join tests
   */
  @Test
  public void testJoin() {
    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testJoinWithSignalScheduleAfterCreatePublishedPublic() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    SignalSchedule signalSchedule =
        SharedTestHelper.createSignalSchedule(new FixedSignal(), new DailySchedule());

    Request request =
        ServerTestHelper.createJsonPostRequest("/experiments/1", DAOHelper.toJson(signalSchedule));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreatePublishedPublic() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  @Test
  public void testJoinAfterCreateUnpublished() {
    ExperimentTestHelper.createUnpublishedExperiment();

    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testJoinAsImposterAfterCreatePublishedPrivate() {
    ExperimentTestHelper.createPublishedPrivateExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
