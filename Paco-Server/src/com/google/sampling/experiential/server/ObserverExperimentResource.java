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

import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ExperimentStats;

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
public class ObserverExperimentResource extends PacoResource {
  private Experiment experiment = null;

  @Override
  protected void doInit() throws ResourceException {
    String experimentId = (String) getRequest().getAttributes().get("experimentId");

    experiment = dao.getExperiment(experimentId);

    if (experiment == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }

    /*
    if (user.isObserverOf(experiment) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
    */
  }

  @Get("gwt|json")
  public ExperimentStats stats() {
    return null;
  }

  @Post("gwt|json")
  public void update(Experiment experiment) {
    dao.updateExperiment(this.experiment, experiment);
  }

  @Delete("gwt|json")
  public void destroy() {
    dao.deleteExperiment(experiment);
  }
}
