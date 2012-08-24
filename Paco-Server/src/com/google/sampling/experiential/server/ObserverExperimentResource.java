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
 *
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ObserverExperimentResource extends PacoExperimentResource {
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.hasObserver(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  @Get("json|gwt")
  public Experiment show() {
    return experiment;
  }

  @Put("json|gwt")
  public void update(Experiment newExperiment) {
    if (newExperiment.getVersion() != experiment.getVersion()) {
      throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
    }

    newExperiment.setId(experiment.getId());
    newExperiment.setVersion(experiment.getVersion() + 1);

    if (dao.updateExperiment(experiment, newExperiment) == false) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_NO_CONTENT);
  }

  @Delete("json|gwt")
  public void destroy() {
    experiment.setDeleted(true);

    if (dao.deleteExperiment(experiment) == false) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}
