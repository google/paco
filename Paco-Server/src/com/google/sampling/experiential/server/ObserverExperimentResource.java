/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.sampling.experiential.server;

import com.google.paco.shared.model.Experiment;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * A resource for a specific observed experiment. According to the router, these methods are
 * accessible via /observer/experiments/{experimentId}.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class ObserverExperimentResource extends PacoExperimentResource {
  /*
   * Ensure the logged-in user is an observer of the experiment they are trying to access.
   *
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.server.PacoExperimentResource#doInit()
   */
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.hasObserver(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  /**
   * Returns a single observed experiment
   *
   * @return the observed experiment
   */
  @Get("json")
  public Experiment show() {
    return experiment;
  }

  /**
   * Updated the observed experiment with the specified experiment. If the versions numbers do not
   * match an conflict is raised. Otherwise the update is forced to be on the same experiment with
   * the version number incremented.
   *
   * @param newExperiment the updated experiment
   */
  @Put("json")
  public void update(Experiment newExperiment) {
    if (getConditions().getUnmodifiedSince() == null
        || getConditions().getUnmodifiedSince().before(experiment.getModificationDate())) {
      throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED);
    }

    /*
     * if (newExperiment.getVersion() != experiment.getVersion()) { throw new
     * ResourceException(Status.CLIENT_ERROR_CONFLICT); }
     */

    if (dao.updateExperiment(experiment, newExperiment) == false) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_NO_CONTENT);
  }

  /**
   * Soft-deletes the observed experiment.
   */
  @Delete("json")
  public void destroy() {
    if (dao.deleteExperiment(experiment) == false) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}
