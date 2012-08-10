// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.Feedback;
import com.google.sampling.experiential.shared.LikertInput;
import com.google.sampling.experiential.shared.ListInput;
import com.google.sampling.experiential.shared.TextInput;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ExperimentTestHelper {
  public static Experiment constructExperiment() {
    Experiment experiment = new Experiment();

    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setPublished(false);
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(), new LikertInput()));
    experiment.setSignalSchedule(null);
    experiment.setFeedbacks(Lists.newArrayList(new Feedback()));

    return experiment;
  }

  public static void createPublishedPublicExperiment() {
    createExperiment(true, false);
  }

  public static void createPublishedPrivateExperiment() {
    createExperiment(true, true);
  }

  public static void createUnpublishedExperiment() {
    createExperiment(false, false);
  }

  protected static void createExperiment(boolean published, boolean specific) {
    Experiment experiment = constructExperiment();
    experiment.setPublished(published);
    if (specific) {
      experiment.addViewer("subject@google.com");
    } else {
      experiment.setViewers(null);
    }

    Request request =
        ServerTestHelper.createJsonPostRequest("/experiments", DAOHelper.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());
  }

  public static void joinExperiment() {
    Request request = ServerTestHelper.createJsonPostRequest("/experiments/1", "");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/subject/experiments/1", response.getLocationRef().getPath());
  }

  public static void leaveExperiment() {
    Request request = ServerTestHelper.createJsonDeleteRequest("/subject/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }

  public static void destroyExperiment() {
    Request request = ServerTestHelper.createJsonDeleteRequest("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }
}
