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

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * The main entry point into the paco application. Here we establish restful routes.
 *
 * @author corycornelius@google.com (Cory Cornelius)
 */
public class PacoApplication extends Application {
  @Override
  public Restlet createInboundRoot() {
    Router router = new Router(getContext());

    router.attach("/observer/experiments", ObserverExperimentsResource.class);
    router.attach("/observer/experiments/{experimentId}", ObserverExperimentResource.class);

    return router;
  }
}
