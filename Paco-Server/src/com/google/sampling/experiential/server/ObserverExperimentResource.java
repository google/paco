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

import com.google.sampling.experiential.shared.ExperimentStats;
import com.google.sampling.experiential.shared.ObservedExperiment;

import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
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

  @Get("gwt|json")
  public ExperimentStats stats() {
    setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);

    return null;
  }

  @Post("gwt|json")
  public void update(ObservedExperiment experiment) {
    if (dao.updateExperiment(experiment, this.experiment)) {
      setStatus(Status.SUCCESS_NO_CONTENT);
    } else {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
  }

  @Delete("gwt|json")
  public void destroy() {
    if (dao.deleteExperiment(experiment)) {
      setStatus(Status.SUCCESS_NO_CONTENT);
    } else {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
  }
}
