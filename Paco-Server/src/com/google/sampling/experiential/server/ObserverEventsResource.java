// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Event;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObserverEventsResource extends PacoExperimentResource {
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.hasObserver(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  @Get("json|gwt")
  public List<Event> list() {
    return dao.getEvents(experiment);
  }
}
