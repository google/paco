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
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import java.util.List;

/**
 * A resource for observed experiments. According to the router, these methods
 * are accessible via /observer/experiments/.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class ObserverExperimentsResource extends PacoResource {
  /**
   * Retrieves a list of experiments the user observes.
   *
   * @return a list of observed experiments
   */
  @Get("json")
  public List<Experiment> index() {
    return dao.getObservedExperiments(user);
  }

  /**
   * Creates an observed experiment and returns a location to the newly created
   * observed experiment if successful.
   *
   * @param experiment the experiment to create
   */
  @Post("json")
  public void create(Experiment experiment) {
    if (experiment == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    Long id = dao.createExperiment(experiment, user);

    if (id == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_CREATED);
    setLocationRef("/observer/experiments/" + id);
  }
}
