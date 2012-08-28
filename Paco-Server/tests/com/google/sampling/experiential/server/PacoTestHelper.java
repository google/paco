// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.paco.shared.model.DailySchedule;
import com.google.paco.shared.model.Experiment;
import com.google.paco.shared.model.LikertInput;
import com.google.paco.shared.model.ListInput;
import com.google.paco.shared.model.RandomSignal;
import com.google.paco.shared.model.SignalSchedule;
import com.google.paco.shared.model.TextInput;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoTestHelper {
  /*
   * SignalSchedule
   */
  public static SignalSchedule constructSignalSchedule() {
    SignalSchedule signalSchedule = new SignalSchedule();

    signalSchedule.setSignal(new RandomSignal());
    signalSchedule.setSchedule(new DailySchedule());

    return signalSchedule;
  }

  /*
   * Experiment
   */
  public static Experiment constructExperiment() {
    Experiment experiment = new Experiment();

    experiment.setTitle("title");
    experiment.setDescription("description");
    experiment.setCreator("creator");
    experiment.setConsentForm("consent form");
    experiment.setPublished(false);
    experiment.setInputs(Lists.newArrayList(new TextInput(), new ListInput(), new LikertInput()));
    experiment.setSignalSchedule(constructSignalSchedule());
    experiment.setFeedback("feedback");
    experiment.addObserver("observer@google.com");

    return experiment;
  }

  public static String createPublishedPublicExperiment() {
    return createExperiment(true, false);
  }

  public static String createPublishedPrivateExperiment() {
    return createExperiment(true, true);
  }

  public static String createUnpublishedExperiment() {
    return createExperiment(false, false);
  }

  private static String createExperiment(boolean published, boolean specific) {
    Experiment experiment = constructExperiment();
    experiment.setPublished(published);
    if (specific) {
      experiment.addViewer("subject@google.com");
    } else {
      experiment.setViewers(null);
    }

    Request request = post("/observer/experiments", PacoConverter.toJson(experiment));
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_CREATED, response.getStatus());
    assertEquals("/observer/experiments/1", response.getLocationRef().getPath());

    return response.getLocationRef().getPath();
  }

  public static void destroyExperiment() {
    Request request = delete("/observer/experiments/1");
    Response response = new PacoApplication().handle(request);

    assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
  }


  /*
   * Json HTTP methods
   */
  public static Request get(String uri) {
    Request request = new Request(Method.GET, "http://localhost" + uri);

    request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));

    return request;
  }

  public static Request post(String uri, String entity) {
    Request request = new Request(Method.POST, "http://localhost" + uri);

    request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));
    request.setEntity(entity, MediaType.APPLICATION_JSON);

    return request;
  }

  public static Request put(String uri, String entity) {
    Request request = new Request(Method.PUT, "http://localhost" + uri);

    request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));
    request.setEntity(entity, MediaType.APPLICATION_JSON);

    return request;
  }

  public static Request delete(String uri) {
    Request request = new Request(Method.DELETE, "http://localhost" + uri);

    request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());
    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));

    return request;
  }
}
