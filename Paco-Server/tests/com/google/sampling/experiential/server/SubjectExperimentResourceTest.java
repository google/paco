package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.paco.shared.model.Experiment;
import com.google.paco.shared.model.FixedSignal;
import com.google.paco.shared.model.SignalSchedule;

public class SubjectExperimentResourceTest extends PacoResourceTest {
  /*
   * show test
   */
  @Test
  public void testShow() {
    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testShowAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testShowAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Experiment experiment = PacoTestHelper.constructExperiment();
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.setPublished(true);

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreateAndJoinEditable() {
    PacoTestHelper.createPublishedPublicExperiment(true);
    PacoTestHelper.joinExperiment();

    Experiment experiment = PacoTestHelper.constructExperiment(true);
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.setPublished(true);

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreateAndJoinWithCustomSignalSchedule() {
    SignalSchedule signalSchedule = PacoTestHelper.constructSignalSchedule(true);
    signalSchedule.setSignal(new FixedSignal());

    PacoTestHelper.createPublishedPublicExperiment(true);
    PacoTestHelper.joinExperiment(signalSchedule);

    Experiment experiment = PacoTestHelper.constructExperiment(true);
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.setPublished(true);
    experiment.setSignalSchedule(signalSchedule);

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAfterCreateAndJoinWithUneditableCustomSignalSchedule() {
    SignalSchedule signalSchedule = PacoTestHelper.constructSignalSchedule(true);
    signalSchedule.setSignal(new FixedSignal());

    PacoTestHelper.createPublishedPublicExperiment(false);
    PacoTestHelper.joinExperiment(signalSchedule);

    Experiment experiment = PacoTestHelper.constructExperiment(false);
    experiment.setId(1l);
    experiment.setVersion(1);
    experiment.addObserver("observer@google.com");
    experiment.setPublished(true);

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals(PacoConverter.toJson(experiment, Experiment.Subject.class), response.getEntityAsText());
  }

  @Test
  public void testShowAsImpostorAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  /*
   * leave tests
   */
  @Test
  public void testLeave() {
    Request request = PacoTestHelper.delete("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.delete("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.delete("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreateAndJoinAndLeave() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    PacoTestHelper.leaveExperiment();

    Request request = PacoTestHelper.delete("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeaveAsImpostorAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = PacoTestHelper.delete("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testCacheProperties() {
    DateTime before = DateTime.now().minusMinutes(1);
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();
    DateTime after = DateTime.now().plusMinutes(1);

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    DateTime modificationDate = new DateTime(response.getEntity().getModificationDate());

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertTrue(modificationDate.isAfter(before));
    assertTrue(modificationDate.isBefore(after));
    assertEquals("1", response.getEntity().getTag().toString());
  }
}
