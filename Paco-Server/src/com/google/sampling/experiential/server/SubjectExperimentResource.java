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
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * A resource for a specific joined experiment. According to the router, this resource is available
 * at /subject/experiments/{experimentId}.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class SubjectExperimentResource extends PacoExperimentResource {
  /*
   * Ensure the user has joined the current experiment.
   *
   * (non-Javadoc)
   *
   * @see com.google.sampling.experiential.server.PacoExperimentResource#doInit()
   */
  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if (experiment.hasSubject(user) == false) {
      throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
    }
  }

  @Get("json|gwt")
  public Experiment show() {
    SignalSchedule signalSchedule = dao.getSignalSchedule(experiment, user);

    if (signalSchedule != null) {
      experiment.setSignalSchedule(signalSchedule);
    }

    return experiment;
  }

  /**
   * Unenrolls the current user from the experiment.
   */
  @Delete("json|gwt")
  public void leave() {
    if (dao.leaveExperiment(experiment, user) == false) {
      throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
    }

    setStatus(Status.SUCCESS_NO_CONTENT);
  }
}
