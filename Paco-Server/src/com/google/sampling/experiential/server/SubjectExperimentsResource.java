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

import org.restlet.resource.Get;

import java.util.List;

/**
 * A resource for experiments a user has joined. According to the
 * router, this resource is available at /subject/experiments.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class SubjectExperimentsResource extends PacoResource {
  /**
   * Retrieves a list of experiments the user has joined.
   *
   * @return the list of experiments the user has joined
   */
  @Get("json|gwt")
  public List<Experiment> index() {
    return dao.getSubjectedExperiments(user);
  }
}
