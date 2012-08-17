// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.sampling.experiential.shared.Event;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObserverEventResource extends PacoEventResource {
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.hasObserver(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  @Get("json|gwt")
  public Event show() {
    return event;
  }
}
