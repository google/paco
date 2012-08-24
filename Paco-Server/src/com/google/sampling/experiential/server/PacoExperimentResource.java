// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import com.google.paco.shared.model.Experiment;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public abstract class PacoExperimentResource extends PacoResource {
  protected Experiment experiment;

  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    long experimentId = Long.valueOf((String) getRequest().getAttributes().get("experimentId"));
    experiment = dao.getExperiment(experimentId);

    if (experiment == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }
  }
}
