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
package com.google.sampling.experiential.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ExperimentStats;
import com.google.sampling.experiential.shared.SignalSchedule;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Delete;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public interface ExperimentResourceProxy extends ClientProxy {
  @Post
  public void create(Experiment experiment, AsyncCallback<Void> callback);

  @Post
  public void update(Experiment newExperiment, AsyncCallback<Void> callback);

  @Post
  public void join(SignalSchedule signalSchedule, AsyncCallback<Void> callback);

  @Get
  public void index(AsyncCallback<List<Experiment>> callback);

  @Get
  public void show(AsyncCallback<Experiment> callback);

  @Get
  public void stats(AsyncCallback<ExperimentStats> callback);

  @Delete
  public void leave(AsyncCallback<Void> callback);

  @Delete
  public void destroy(AsyncCallback<Void> callback);
}
