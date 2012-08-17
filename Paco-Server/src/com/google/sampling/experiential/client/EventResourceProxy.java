// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.sampling.experiential.shared.Event;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;

import java.util.List;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public interface EventResourceProxy extends ClientProxy {
  @Get("json|gwt")
  public void show(AsyncCallback<Event> callback);

  @Get("json|gwt")
  public void list(AsyncCallback<List<Event>> callback);

  @Get("json|gwt")
  public void summary(AsyncCallback<List<Event>> callback);

  @Post("json|gwt")
  public void add(Event event, AsyncCallback<Void> callback);
}
