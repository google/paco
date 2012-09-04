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
import com.google.paco.shared.model.SignalSchedule;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * A resource for a specific viewable experiment. According to the router, this resource is
 * available at /experiments/{experimentId}.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class ExperimentResource extends PacoExperimentResource {
  /*
   * Ensure the experiment is published and the user can view it.
   *
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.server.PacoExperimentResource#doInit()
   */
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.isPublished() == false || experiment.hasViewer(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  /**
   * Retrieves the viewable experiment.
   *
   * @return the viewable experiment
   */
  @Get("json")
  public Experiment show() {
    return experiment;
  }

  /**
   * Enrolls the current user into the experiment with an (optionally) specified signal-schedule.
   *
   * @param signalSchedule a (optional) customized signal-schedule
   */
  @Put("json")
  public void join(SignalSchedule signalSchedule) {
    if (dao.joinExperiment(experiment, user, signalSchedule) == false) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
    }

    setStatus(Status.SUCCESS_CREATED);
    setLocationRef("/subject/experiments/" + experiment.getId());
  }
}
