// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.paco.shared.model.Event;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class PacoEventResource extends PacoExperimentResource {
  protected Event event;

  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    long eventId = Long.valueOf((String) getRequest().getAttributes().get("eventId"));
    event = dao.getEvent(eventId);

    if (event == null || event.getExperimentId().equals(experiment.getId()) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }
  }
}
