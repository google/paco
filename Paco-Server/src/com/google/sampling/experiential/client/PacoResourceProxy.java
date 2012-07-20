/*
* Copyright 2012 Google Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.sampling.experiential.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.sampling.experiential.shared.Experiment;
import com.google.sampling.experiential.shared.ExperimentStats;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Delete;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;
import org.restlet.client.resource.Put;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public interface PacoResourceProxy extends ClientProxy {
  // Generic
  @Get
  public void list(AsyncCallback<List<Experiment>> callback);

  @Get
  public void stats(AsyncCallback<ExperimentStats> callback);


  // Observer
  @Post
  public void create(Experiment experiment, AsyncCallback<Void> callback);

  @Put
  public void update(Experiment experiment, AsyncCallback<Void> callback);

  @Delete
  public void destroy(AsyncCallback<Experiment> callback);


  // Subject
  @Post
  public void join(AsyncCallback<Experiment> callback);

  @Delete
  public void leave(AsyncCallback<Experiment> callback);
}
