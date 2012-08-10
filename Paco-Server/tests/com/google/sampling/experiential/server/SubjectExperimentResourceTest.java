package com.google.sampling.experiential.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

public class SubjectExperimentResourceTest extends PacoResourceTest {
  /*
   * stats test
   */
  @Test
  public void testStats() {
    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testStatsAfterCreate() {
    PacoTestHelper.createPublishedPublicExperiment();

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testStatsAfterCreateAndJoin() {
    PacoTestHelper.createPublishedPublicExperiment();
    PacoTestHelper.joinExperiment();

    Request request = PacoTestHelper.get("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("{}", response.getEntityAsText());
  }

  @Test
  public void testStatsAsImpostorAfterCreateAndJoin() {
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
}
