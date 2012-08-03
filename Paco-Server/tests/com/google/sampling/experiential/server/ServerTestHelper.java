// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.sampling.experiential.server;

import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;

/**
 * @author corycornelius@google.com (Cory Cornelius)
 *
 */
public class ServerTestHelper {
  public static Request createJsonGetRequest(String uri) {
    Request request = new Request(Method.GET, uri);

    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));

    return request;
  }

  public static Request createJsonPostRequest(String uri, String entity) {
    Request request = new Request(Method.POST, uri);

    request.setClientInfo(new ClientInfo(MediaType.APPLICATION_JSON));
    request.setEntity(entity, MediaType.APPLICATION_JSON);

    return request;
  }
}
