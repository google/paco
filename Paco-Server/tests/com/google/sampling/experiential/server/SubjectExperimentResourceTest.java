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
    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testStatsAfterCreate() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testStatsAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_OK, response.getStatus());
    assertEquals("{}", response.getEntityAsText());
  }

  @Test
  public void testStatsAsImpostorAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonGetRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  /*
   * leave tests
   */
  @Test
  public void testLeave() {
    Request request = ServerTestHelper.createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreate() {
    ExperimentTestHelper.createPublishedPublicExperiment();

    Request request = ServerTestHelper.createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }

  @Test
  public void testLeaveAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    Request request = ServerTestHelper.createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  @Test
  public void testLeaveAsImpostorAfterCreateAndJoin() {
    ExperimentTestHelper.createPublishedPublicExperiment();
    ExperimentTestHelper.joinExperiment();

    helper.setEnvEmail("impostor@google.com");

    Request request = ServerTestHelper.createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
  }
}
